from __future__ import annotations

import argparse
import json
import os
import sys
from dataclasses import asdict, dataclass
from datetime import datetime
from pathlib import Path
from tkinter import Tk, filedialog

import cv2
import numpy as np
import torch
from PIL import Image
from torchvision.transforms import Compose, Normalize, Resize, ToTensor
from torchvision.transforms.functional import InterpolationMode

from larynscope.model_repAlexnet import RepAlexNet, repvgg_model_convert
from larynscope.utils_heatmap import GradCAM, show_cam_on_image


ROOT = Path(__file__).resolve().parent
IMAGE_SUFFIXES = {".jpg", ".jpeg", ".png", ".bmp", ".tif", ".tiff"}
DISPLAY_LABELS = {
    0: "健康",
    1: "喉鳞状细胞癌",
    2: "不典型增生",
    3: "炎症角化",
    4: "不典型增生",
    5: "不典型增生",
}


@dataclass
class PredictionRecord:
    image: str
    predicted_class_id: int
    predicted_label: str
    confidence: float
    probabilities: list[float]
    heatmap_path: str


def first_existing_path(*candidates: Path) -> Path:
    for candidate in candidates:
        if candidate.exists():
            return candidate
    return candidates[0]


DEFAULT_MODEL_PATH = first_existing_path(
    ROOT / "store" / "repalexnet_heatmap_n100.pth",
    ROOT / "store" / "store" / "repalexnet_heatmap_n100.pth",
)


def configure_console_encoding() -> None:
    if os.name == "nt":
        try:
            import ctypes

            ctypes.windll.kernel32.SetConsoleOutputCP(65001)
            ctypes.windll.kernel32.SetConsoleCP(65001)
        except Exception:
            pass

    for stream_name in ("stdout", "stderr"):
        stream = getattr(sys, stream_name, None)
        if stream is not None and hasattr(stream, "reconfigure"):
            try:
                stream.reconfigure(encoding="utf-8", errors="replace")
            except Exception:
                pass


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="使用 RepAlexNet 喉镜模型进行预测，并生成 Grad-CAM 热力图。"
    )
    parser.add_argument(
        "--input",
        type=Path,
        default=None,
        help="输入图片文件或图片目录。不传时会弹出图片选择框。",
    )
    parser.add_argument(
        "--select-dir",
        action="store_true",
        help="没有传 --input 时，弹出文件夹选择框而不是图片选择框。",
    )
    parser.add_argument(
        "--model-path",
        type=Path,
        default=DEFAULT_MODEL_PATH,
        help="RepAlexNet 权重文件路径。",
    )
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=None,
        help="本次运行的输出目录。默认自动创建 outputs/larynscope_run_时间戳。",
    )
    parser.add_argument(
        "--device",
        default="auto",
        choices=("auto", "cpu", "cuda"),
        help="推理设备。auto 会优先使用 CUDA。",
    )
    parser.add_argument(
        "--recursive",
        action="store_true",
        help="当输入为目录时，递归扫描子目录中的图片。",
    )
    return parser.parse_args()


def resolve_device(device_name: str) -> torch.device:
    if device_name == "cpu":
        return torch.device("cpu")
    if device_name == "cuda":
        if not torch.cuda.is_available():
            raise RuntimeError("当前环境不可用 CUDA，请改用 --device cpu 或 --device auto。")
        return torch.device("cuda")
    return torch.device("cuda" if torch.cuda.is_available() else "cpu")


def build_transform() -> Compose:
    return Compose(
        [
            Resize((224, 224), interpolation=InterpolationMode.BICUBIC),
            ToTensor(),
            Normalize([0.4208, 0.3874, 0.3595], [0.0543, 0.0493, 0.0429]),
        ]
    )


def load_checkpoint(model_path: Path, device: torch.device) -> dict[str, torch.Tensor]:
    checkpoint = torch.load(model_path, map_location=device)
    if isinstance(checkpoint, dict) and "state_dict" in checkpoint:
        checkpoint = checkpoint["state_dict"]
    if not isinstance(checkpoint, dict):
        raise TypeError(f"不支持的权重格式: {type(checkpoint)!r}")

    normalized_state_dict: dict[str, torch.Tensor] = {}
    for key, value in checkpoint.items():
        if key.startswith("module."):
            key = key[len("module.") :]
        normalized_state_dict[key] = value
    return normalized_state_dict


def relative_display(path: Path) -> str:
    try:
        return str(path.resolve().relative_to(ROOT))
    except ValueError:
        return str(path.resolve())


def select_files_via_dialog(select_dir: bool, recursive: bool) -> tuple[list[Path], Path | None]:
    try:
        root = Tk()
        root.withdraw()
        root.attributes("-topmost", True)
        root.update()

        if select_dir:
            selected_dir = filedialog.askdirectory(title="选择包含喉镜图片的文件夹")
            root.destroy()
            if not selected_dir:
                raise RuntimeError("没有选择任何文件夹，已取消。")
            input_path = Path(selected_dir)
            return collect_images_from_path(input_path, recursive=recursive), input_path

        selected_files = filedialog.askopenfilenames(
            title="选择喉镜图片",
            filetypes=[
                ("Image Files", "*.jpg *.jpeg *.png *.bmp *.tif *.tiff"),
                ("All Files", "*.*"),
            ],
        )
        root.destroy()
    except Exception as exc:
        raise RuntimeError(
            "无法打开选择窗口。请检查图形界面环境，或改用 --input 指定图片路径。"
        ) from exc

    if not selected_files:
        raise RuntimeError("没有选择任何图片，已取消。")

    images = []
    for item in selected_files:
        path = Path(item)
        if path.suffix.lower() in IMAGE_SUFFIXES:
            images.append(path)

    if not images:
        raise FileNotFoundError("选择的文件里没有可处理的图片。")
    return sorted(images), None


def collect_images_from_path(input_path: Path, recursive: bool) -> list[Path]:
    if input_path.is_file():
        if input_path.suffix.lower() not in IMAGE_SUFFIXES:
            raise ValueError(f"不是支持的图片格式: {input_path}")
        return [input_path]

    if not input_path.exists():
        raise FileNotFoundError(f"输入路径不存在: {input_path}")
    if not input_path.is_dir():
        raise ValueError(f"输入路径既不是文件也不是目录: {input_path}")

    iterator = input_path.rglob("*") if recursive else input_path.glob("*")
    images = sorted(
        path for path in iterator if path.is_file() and path.suffix.lower() in IMAGE_SUFFIXES
    )
    if not images:
        raise FileNotFoundError(f"在 {input_path} 中没有找到可处理的图片。")
    return images


def resolve_input_images(args: argparse.Namespace) -> tuple[list[Path], Path | None]:
    if args.input is not None:
        input_path = args.input.resolve()
        images = collect_images_from_path(input_path=input_path, recursive=args.recursive)
        return images, input_path if input_path.is_dir() else None
    return select_files_via_dialog(select_dir=args.select_dir, recursive=args.recursive)


def build_output_paths(output_dir: Path | None) -> tuple[Path, Path, Path, Path]:
    if output_dir is None:
        run_name = f"larynscope_run_{datetime.now().strftime('%Y%m%d_%H%M%S')}"
        run_root = ROOT / "outputs" / run_name
    else:
        run_root = output_dir.resolve()

    heatmap_dir = run_root / "heatmaps"
    report_path = run_root / "predictions.json"
    summary_path = run_root / "predictions_summary.txt"
    return run_root, heatmap_dir, report_path, summary_path


def build_heatmap_path(
    image_path: Path,
    heatmap_dir: Path,
    source_root: Path | None,
    index: int,
) -> Path:
    if source_root is not None:
        relative_path = image_path.relative_to(source_root)
        return heatmap_dir / relative_path
    return heatmap_dir / f"{index:03d}_{image_path.name}"


def save_image(image: np.ndarray, output_path: Path) -> None:
    output_path.parent.mkdir(parents=True, exist_ok=True)
    suffix = output_path.suffix or ".jpg"
    success, encoded = cv2.imencode(suffix, image)
    if not success:
        raise RuntimeError(f"图像编码失败: {output_path}")
    encoded.tofile(str(output_path))


class RepAlexNetPredictor:
    def __init__(self, model_path: Path, device_name: str = "auto") -> None:
        self.device = resolve_device(device_name)
        self.transform = build_transform()

        model = RepAlexNet(num_blocks=[1, 1, 1, 1], num_classes=6)
        model = repvgg_model_convert(model)
        state_dict = load_checkpoint(model_path, self.device)
        model.load_state_dict(state_dict)
        self.model = model.to(self.device).eval()
        self.cam = GradCAM(
            model=self.model,
            target_layers=[self.model.stage5],
            use_cuda=self.device.type == "cuda",
        )

    def predict(self, image_path: Path, heatmap_path: Path) -> PredictionRecord:
        image = Image.open(image_path).convert("RGB")
        image_for_overlay = image.resize((224, 224))
        input_tensor = self.transform(image).unsqueeze(0).to(self.device)

        with torch.no_grad():
            logits = self.model(input_tensor)
            probabilities = torch.softmax(logits, dim=1)[0].cpu().numpy()

        predicted_class_id = int(np.argmax(probabilities))
        confidence = float(probabilities[predicted_class_id])
        grayscale_cam = self.cam(
            input_tensor=input_tensor.clone().requires_grad_(True),
            target_category=predicted_class_id,
        )[0]

        overlay = show_cam_on_image(
            np.asarray(image_for_overlay, dtype=np.float32) / 255.0,
            grayscale_cam,
            use_rgb=True,
        )
        heatmap_bgr = cv2.cvtColor(overlay, cv2.COLOR_RGB2BGR)
        save_image(heatmap_bgr, heatmap_path)

        return PredictionRecord(
            image=relative_display(image_path),
            predicted_class_id=predicted_class_id,
            predicted_label=DISPLAY_LABELS.get(
                predicted_class_id, f"未命名类别_{predicted_class_id}"
            ),
            confidence=confidence,
            probabilities=[float(value) for value in probabilities],
            heatmap_path=relative_display(heatmap_path),
        )


def save_report(report_path: Path, records: list[PredictionRecord], model_path: Path) -> None:
    report_path.parent.mkdir(parents=True, exist_ok=True)
    payload = {
        "model_path": relative_display(model_path),
        "records": [asdict(record) for record in records],
    }
    report_path.write_text(
        json.dumps(payload, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )


def save_summary(summary_path: Path, records: list[PredictionRecord], model_path: Path) -> None:
    lines = [
        "喉镜预测结果汇总",
        f"模型: {relative_display(model_path)}",
        "",
    ]
    for index, record in enumerate(records, start=1):
        lines.extend(
            [
                f"[{index}] {Path(record.image).name}",
                f"  预测结果: {record.predicted_label} (class {record.predicted_class_id})",
                f"  置信度  : {record.confidence:.4f}",
                f"  热力图  : {record.heatmap_path}",
                "",
            ]
        )

    summary_path.parent.mkdir(parents=True, exist_ok=True)
    summary_path.write_text("\n".join(lines), encoding="utf-8")


def print_header(
    predictor: RepAlexNetPredictor,
    image_count: int,
    model_path: Path,
    run_root: Path,
    heatmap_dir: Path,
    report_path: Path,
    summary_path: Path,
) -> None:
    print("=" * 72)
    print("喉镜预测开始")
    print(f"设备      : {predictor.device}")
    print(f"模型      : {relative_display(model_path)}")
    print(f"图片数量  : {image_count}")
    print(f"输出目录  : {relative_display(run_root)}")
    print(f"热力图目录: {relative_display(heatmap_dir)}")
    print(f"JSON 结果 : {relative_display(report_path)}")
    print(f"文字汇总  : {relative_display(summary_path)}")
    print("=" * 72)


def print_record(index: int, total: int, record: PredictionRecord) -> None:
    print(f"[{index}/{total}] {Path(record.image).name}")
    print(f"  预测结果: {record.predicted_label} (class {record.predicted_class_id})")
    print(f"  置信度  : {record.confidence:.4f}")
    print(f"  热力图  : {record.heatmap_path}")


def main() -> None:
    configure_console_encoding()
    args = parse_args()

    model_path = args.model_path.resolve()
    if not model_path.exists():
        raise FileNotFoundError(f"模型权重不存在: {model_path}")

    images, source_root = resolve_input_images(args)
    run_root, heatmap_dir, report_path, summary_path = build_output_paths(args.output_dir)

    predictor = RepAlexNetPredictor(model_path=model_path, device_name=args.device)
    print_header(
        predictor=predictor,
        image_count=len(images),
        model_path=model_path,
        run_root=run_root,
        heatmap_dir=heatmap_dir,
        report_path=report_path,
        summary_path=summary_path,
    )

    records: list[PredictionRecord] = []
    total = len(images)
    for index, image_path in enumerate(images, start=1):
        heatmap_path = build_heatmap_path(
            image_path=image_path,
            heatmap_dir=heatmap_dir,
            source_root=source_root,
            index=index,
        )
        record = predictor.predict(image_path=image_path, heatmap_path=heatmap_path)
        records.append(record)
        print_record(index=index, total=total, record=record)

    save_report(report_path=report_path, records=records, model_path=model_path)
    save_summary(summary_path=summary_path, records=records, model_path=model_path)

    print("-" * 72)
    print("处理完成")
    print(f"热力图目录: {relative_display(heatmap_dir)}")
    print(f"JSON 结果 : {relative_display(report_path)}")
    print(f"文字汇总 : {relative_display(summary_path)}")


if __name__ == "__main__":
    main()
