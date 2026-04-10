"""
喉部健康音频检测脚本（CLI）
================================

用途说明（满足小程序 + 本地调试两种场景）：

1. 由 Java / SpringBoot 后端通过 ProcessBuilder 同步调用：
   - 命令示例：
       python -u voice/detect_audio.py --audio <本地音频绝对路径> --outdir <图谱输出目录>
   - 标准输出（stdout）**只保证最后一段是 JSON**，前面可以有中文日志。
   - 最后一段 JSON 始终只有一条，结构形如：
  成功：
    {
      "success": true,
      "stage": "inference",
      "audioPath": "C:/.../xxx.MP3",
      "predicted_class": 0,
      "confidence": 0.87,
      "prob0": 0.87,
      "prob1": 0.13,
      "benign": true,
      "malignant": false,
         "rule": "predicted_class=0 && confidence>0.5 => 良性(0)，否则为恶性(1)",
         "zhResult": "良性嗓音",
         "zhExplanation": "模型认为该声音为“良性嗓音”，置信度约为 87.00%。",
         "usingModel": "voice_best_model.pth",
      "method": "voice_best_model.pth",
      "device": "cuda",
      "mfcc_file": "xxx_mfcc.png",
      "mel_file": "xxx_mel.png"
    }

       失败（脚本本身不抛异常，用 success=false 表达）：
    {
      "success": false,
      "stage": "load_audio",
      "audioPath": "C:/.../xxx.MP3",
      "errorType": "RuntimeError",
      "errorMessage": "... 具体报错 ...",
         "hint": "可能原因：1) 未安装 ffmpeg 2) 文件损坏 3) 格式不支持",
         "stackTrace": "traceback ...",
         "msg": "[load_audio] RuntimeError: ... 具体报错 ..."
    }

2. 由开发者在本地命令行直接运行，用于测试模型和环境：
   - 在项目根目录执行：
       python voice/detect_audio.py --audio "voice/test_2_82/声带良性/xxx.MP3"
   - 或快速在内置测试集上跑一遍（不会输出 JSON，只打印中文日志）：
       python voice/detect_audio.py --demo
       python voice/detect_audio.py --demo --limit 10

脚本内部统一使用 `voice_inference.py` 中的 `load_model`、`extract_mfcc_features`
加载模型和提取特征，同时使用 `mp3toMFCC.py` 中的 `generate_mfcc_and_mel` 生成
供前端展示的 MFCC 图谱和 Mel 频谱图。
"""

from __future__ import annotations

import argparse
import json
import os
import sys
import traceback
from pathlib import Path
from typing import Any, Dict, Tuple

import matplotlib

# 无界面后端，适配服务器环境
matplotlib.use("Agg")

import numpy as np  # noqa: E402
import torch  # noqa: E402
import torch.nn.functional as F  # noqa: E402

PROJECT_ROOT = Path(__file__).resolve().parent.parent
VOICE_DIR = Path(__file__).resolve().parent


def _ensure_imports() -> None:
    """
    将项目根目录加入 sys.path，方便复用已有模块。
    """
    if str(PROJECT_ROOT) not in sys.path:
        sys.path.insert(0, str(PROJECT_ROOT))


_ensure_imports()

try:
    # 复用已有的模型加载与特征提取逻辑
    from voice_inference import load_model, extract_mfcc_features  # type: ignore
except Exception:  # pragma: no cover - 仅在极端场景下触发
    load_model = None  # type: ignore
    extract_mfcc_features = None  # type: ignore

try:
    # 使用 mp3toMFCC 生成 MFCC 图谱与 Mel 频谱图
    from mp3toMFCC import generate_mfcc_and_mel  # type: ignore
except Exception:  # pragma: no cover
    generate_mfcc_and_mel = None  # type: ignore


def _load_model_and_device(model_path: Path) -> Tuple[torch.nn.Module, str]:
    """
    加载模型并返回 (model, device_str)。
    """
    if load_model is None:
        raise RuntimeError(
            "无法导入 voice_inference.load_model，请确认 voice_inference.py 是否存在且无语法错误"
        )
    print(f"[INFO] 正在加载模型文件: {model_path}")
    model = load_model(str(model_path))
    device = str(next(model.parameters()).device)
    print(f"[INFO] 模型加载完成，当前设备: {device}")
    return model, device


def _generate_spectrograms(audio_path: Path, outdir: Path) -> Tuple[str, str]:
    """
    调用 mp3toMFCC.generate_mfcc_and_mel 生成 MFCC 与 Mel 图像文件。

    返回 (mfcc_file_name, mel_file_name)，仅文件名部分，方便后端按 URL 目录拼接。
    生成失败时不影响主流程，返回空字符串。
    """
    if generate_mfcc_and_mel is None:
        print("[WARN] 无法导入 mp3toMFCC.generate_mfcc_and_mel，本次不生成图谱文件。")
        return "", ""

    try:
        outdir.mkdir(parents=True, exist_ok=True)
        mfcc_file, mel_file = generate_mfcc_and_mel(str(audio_path), str(outdir))
        print(f"[INFO] 已生成 MFCC 图谱: {mfcc_file}，Mel 频谱图: {mel_file}")
        return mfcc_file, mel_file
    except Exception as exc:  # pragma: no cover - 图谱生成失败不影响主流程
        print(f"[WARN] 生成 MFCC/Mel 图谱失败: {exc}")
        return "", ""


def detect(audio_path: Path, outdir: Path) -> Dict[str, Any]:
    """
    核心检测逻辑：

    1. 解析音频路径与输出目录
    2. 加载 voice_best_model.pth 模型
    3. 使用 voice_inference.extract_mfcc_features 提取 MFCC 特征张量
    4. 使用 DenseNet 模型进行二分类推理
    5. 调用 mp3toMFCC 生成 MFCC 图谱与 Mel 频谱图
    6. 返回统一 JSON 结构
    """
    stage = "resolve_paths"

    # ---------- 1. 路径解析 ----------
    audio_path = audio_path.expanduser().resolve()
    outdir = outdir.expanduser().resolve()

    if not audio_path.exists():
        raise FileNotFoundError(f"音频文件不存在: {audio_path}")

    # 模型路径优先：voice 目录下的 voice_best_model.pth
    model_path = VOICE_DIR / "voice_best_model.pth"
    if not model_path.exists():
        alt = PROJECT_ROOT / "voice_best_model.pth"
        if alt.exists():
            model_path = alt
        else:
            raise FileNotFoundError(
                f"未找到模型文件 voice_best_model.pth，尝试路径: {model_path} / {alt}"
            )

    # ---------- 2. 加载模型 ----------
    stage = "load_model"
    model, device_str = _load_model_and_device(model_path)

    # ---------- 3. 特征提取 ----------
    stage = "preprocess"
    if extract_mfcc_features is None:
        raise RuntimeError(
            "无法导入 voice_inference.extract_mfcc_features，请确认 voice_inference.py 是否存在且无语法错误"
        )

    print(f"[INFO] 正在从音频中提取 MFCC 特征: {audio_path}")
    mfcc_tensor = extract_mfcc_features(str(audio_path))  # [1, 1, H, W]

    # ---------- 4. 推理 ----------
    stage = "inference"
    mfcc_tensor = mfcc_tensor.to(next(model.parameters()).device)

    with torch.no_grad():
        outputs = model(mfcc_tensor)
        probs = F.softmax(outputs, dim=1).cpu().numpy()[0]

    if probs.shape[0] < 2:
        raise RuntimeError(f"模型输出类别数异常: {probs.shape[0]}，期望至少为 2 类")

    predicted_class = int(np.argmax(probs))
    confidence = float(probs[predicted_class])
    prob0 = float(probs[0])
    prob1 = float(probs[1]) if probs.shape[0] > 1 else float("nan")

    # 判定规则：class=0 且 confidence>0.5 => 良性(0)，否则恶性(1)
    is_benign = predicted_class == 0 and confidence > 0.5
    malignant = not is_benign

    zh_result = "良性嗓音" if is_benign else "疑似恶性嗓音"
    zh_explanation = (
        f"模型认为该声音为“{zh_result}”，置信度约为 {confidence * 100:.2f}%。"
    )

    # ---------- 5. 生成 MFCC / Mel 图谱 ----------
    stage = "postprocess"
    mfcc_file, mel_file = _generate_spectrograms(audio_path, outdir)

    # ---------- 6. 组装结果 JSON ----------
    result: Dict[str, Any] = {
        "success": True,
        "stage": "inference",
        "audioPath": str(audio_path),
        "predicted_class": predicted_class,
        "confidence": confidence,
        "prob0": prob0,
        "prob1": prob1,
        "benign": is_benign,
        "malignant": malignant,
        "rule": "predicted_class=0 && confidence>0.5 => 良性(0)，否则为恶性(1)",
        "zhResult": zh_result,
        "zhExplanation": zh_explanation,
        "method": model_path.name,
        "usingModel": model_path.name,
        "device": device_str,
        "mfcc_file": mfcc_file,
        "mel_file": mel_file,
    }

    print("[INFO] 检测完成：", zh_result, f"(置信度 {confidence * 100:.2f}%)")
    return result


def make_error_payload(
    stage: str,
    audio_path: Path | None,
    exc: Exception,
) -> Dict[str, Any]:
    """
    构造统一的错误 JSON，兼容后端 / 小程序 展示需求。
    """
    etype = exc.__class__.__name__
    msg = str(exc) or repr(exc)
    tb = traceback.format_exc()

    hint = ""
    if stage in ("load_audio", "preprocess"):
        hint = "可能原因：1) 未安装 ffmpeg 2) 文件损坏 3) 格式不支持"
    elif stage == "load_model":
        hint = "可能原因：1) 模型文件缺失 2) 模型版本与代码不兼容 3) 设备(CPU/GPU)环境异常"

    return {
        "success": False,
        "stage": stage,
        "audioPath": str(audio_path) if audio_path else None,
        "errorType": etype,
        "errorMessage": msg,
        "hint": hint,
        "stackTrace": tb,
        "msg": f"[{stage}] {etype}: {msg}",
    }


def demo_on_test_dataset(limit: int | None = 5) -> int:
    """
    本地命令行快速测试：

    - 自动扫描 voice/test_2_82 目录下的 mp3/wav/m4a/flac 音频文件
    - 依次调用 detect(...) 并在控制台输出简要中文结果

    用法示例（在项目根目录执行）：
        python voice/detect_audio.py --demo
        python voice/detect_audio.py --demo --limit 10
    """
    base_dir = VOICE_DIR / "test_2_82"
    if not base_dir.exists():
        print(f"[DEMO] 未找到测试数据目录：{base_dir}")
        return 1

    exts = (".mp3", ".MP3", ".wav", ".WAV", ".m4a", ".M4A", ".flac", ".FLAC")
    audio_files = [p for p in base_dir.rglob("*") if p.suffix in exts]

    if not audio_files:
        print(f"[DEMO] 测试目录下未找到音频文件：{base_dir}")
        return 1

    audio_files.sort()
    if limit is not None and limit > 0:
        audio_files = audio_files[:limit]

    print(f"[DEMO] 在 {base_dir} 共找到 {len(audio_files)} 个测试音频，开始逐个检测...")

    ok_count = 0
    fail_count = 0

    for idx, audio_path in enumerate(audio_files, start=1):
        outdir = audio_path.parent
        print(f"\n[DEMO] ({idx}/{len(audio_files)}) 文件: {audio_path}")
        try:
            result = detect(audio_path, outdir)
            print(
                f"[OK]  预测类别={result['predicted_class']}, "
                f"置信度={result['confidence']:.4f}, "
                f"良性={result['benign']}, "
                f"MFCC图={result['mfcc_file'] or '-'}, "
                f"Mel图={result['mel_file'] or '-'}"
            )
            ok_count += 1
        except Exception as exc:
            payload = make_error_payload("demo", audio_path, exc)
            print(f"[FAIL] {payload['msg']}")
            fail_count += 1

    print(f"\n[DEMO] 完成，本次共成功 {ok_count} 条，失败 {fail_count} 条。")
    # 对于本地测试，失败依然返回 0，避免脚本异常退出影响体验
    return 0


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="喉部健康音频检测脚本（DenseNet 二分类）")
    parser.add_argument(
        "--audio",
        required=False,
        help="待检测的本地音频文件绝对路径（mp3/wav/m4a/flac 等）。正常由后端传入；本地调试可不填，使用 --demo。",
    )
    parser.add_argument(
        "--outdir",
        required=False,
        help="图谱输出目录（可选，默认与音频文件同目录）",
    )
    parser.add_argument(
        "--demo",
        action="store_true",
        help="在内置测试集 voice/test_2_82 上本地测试脚本（命令行调试用，后端不会使用此参数）。",
    )
    parser.add_argument(
        "--limit",
        type=int,
        default=5,
        help="demo 模式下最多检测的样本数（默认 5 个，传 0 或负数表示不限制）。",
    )

    args = parser.parse_args(argv)

    # 如果用户直接运行脚本（无任何参数），自动进入 demo 模式，方便本地测试；
    # 后端调用时始终会显式传入 --audio，不会走到这个分支。
    if not args.audio and not args.demo and (argv is None):
        args.demo = True

    # 本地调试模式：在内置测试集上跑通整个流程
    if args.demo:
        lim = None if args.limit is not None and args.limit <= 0 else args.limit
        return demo_on_test_dataset(limit=lim)

    # 正常模式：必须提供 --audio（供后端调用）
    if not args.audio:
        payload = make_error_payload("resolve_paths", None, ValueError("缺少参数 --audio"))
        print(json.dumps(payload, ensure_ascii=False))
        return 0

    audio_path = Path(args.audio)
    outdir = Path(args.outdir) if args.outdir else audio_path.parent

    # 顶层兜底：无论如何都只输出一条 JSON，并尽量保持退出码为 0（业务失败用 success=false 表示）
    try:
        result = detect(audio_path, outdir)
        print(json.dumps(result, ensure_ascii=False))
        return 0
    except Exception as exc:  # pragma: no cover - 运行期错误路径
        # 粗略猜测错误阶段，便于前端展示
        stage = "java_exception"
        msg = str(exc)
        msg_lower = msg.lower()
        if "no such file" in msg_lower or "not found" in msg_lower or isinstance(
            exc, FileNotFoundError
        ):
            stage = "resolve_paths"
        elif "audio" in msg_lower or "librosa" in msg_lower:
            stage = "load_audio"
        elif "model" in msg_lower:
            stage = "load_model"
        elif "mfcc" in msg_lower or "spectrogram" in msg_lower:
            stage = "preprocess"

        payload = make_error_payload(stage, audio_path, exc)
        print(json.dumps(payload, ensure_ascii=False))
        return 0


if __name__ == "__main__":  # pragma: no cover
    sys.exit(main())

