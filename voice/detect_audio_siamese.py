"""
Siamese 多任务音频检测脚本
========================

- 供后端 `AudioDetectController` 通过 ProcessBuilder 调用：
    python -u voice/detect_audio_siamese.py --audio <音频绝对路径> --outdir <图谱输出目录>
- 输出 JSON 结构与原 `detect_audio.py` 基本兼容，并新增语音性别相关字段。
"""

from __future__ import annotations

import argparse
import json
import sys
import traceback
from pathlib import Path
from typing import Any, Dict, Tuple, List, Optional

import matplotlib

# 无界面后端，适配服务器环境
matplotlib.use("Agg")

import torch  # noqa: E402

PROJECT_ROOT = Path(__file__).resolve().parent.parent
VOICE_DIR = Path(__file__).resolve().parent


def _ensure_imports() -> None:
    if str(PROJECT_ROOT) not in sys.path:
        sys.path.insert(0, str(PROJECT_ROOT))


_ensure_imports()

try:
    # 论文模型推理脚本
    from audio_lesion_test import (  # type: ignore
        run_inference,
        TEMPERATURE as DEFAULT_TEMPERATURE,
        DEFAULT_N_MFCC,
        device as siamese_device,
    )
except Exception:  # pragma: no cover
    run_inference = None  # type: ignore
    DEFAULT_TEMPERATURE = 2.5
    DEFAULT_N_MFCC = 50
    siamese_device = torch.device("cpu")

try:
    # 生成 MFCC / Mel 图像，供前端展示
    from mp3toMFCC import generate_mfcc_and_mel  # type: ignore
except Exception:  # pragma: no cover
    generate_mfcc_and_mel = None  # type: ignore


def _generate_spectrograms(audio_path: Path, outdir: Path) -> Tuple[str, str, str]:
    if generate_mfcc_and_mel is None:
        return "", "", ""
    try:
        outdir.mkdir(parents=True, exist_ok=True)
        mfcc_file, mel_file, waveform_file = generate_mfcc_and_mel(
            str(audio_path), str(outdir)
        )
        return mfcc_file, mel_file, waveform_file
    except Exception:
        return "", ""


def detect(audio_path: Path, outdir: Path) -> Dict[str, Any]:
    """使用 Siamese 模型完成肿瘤/非肿瘤 + 性别检测，并返回统一 JSON 结果。"""
    global run_inference, DEFAULT_TEMPERATURE, DEFAULT_N_MFCC, siamese_device

    # 兜底导入（避免因模块顺序问题导致 ImportError）
    if run_inference is None:  # pragma: no cover
        from audio_lesion_test import (  # type: ignore
            run_inference as _run,
            TEMPERATURE as _TEMP,
            DEFAULT_N_MFCC as _N,
            device as _dev,
        )

        run_inference = _run  # type: ignore
        DEFAULT_TEMPERATURE = _TEMP
        DEFAULT_N_MFCC = _N
        siamese_device = _dev

    audio_path = audio_path.expanduser().resolve()
    outdir = outdir.expanduser().resolve()
    if not audio_path.exists():
        raise FileNotFoundError(f"音频文件不存在: {audio_path}")

    # 模型路径：优先 voice 目录
    model_path = VOICE_DIR / "voice_best_model.pth"
    if not model_path.exists():
        alt = PROJECT_ROOT / "voice_best_model.pth"
        if alt.exists():
            model_path = alt
        else:
            raise FileNotFoundError(
                f"未找到模型文件 voice_best_model.pth，尝试路径: {model_path} / {alt}"
            )

    # 调用论文同款推理函数
    tumor_res, gender_res, final_text, tumor_labels, gender_labels = run_inference(  # type: ignore[arg-type]
        str(model_path),
        str(audio_path),
        temperature=DEFAULT_TEMPERATURE,
        n_mfcc=DEFAULT_N_MFCC,
    )

    # tumor_res: (pred, conf, p_non_tumor, p_tumor)
    t_pred, t_conf, p_non_tumor, p_tumor = tumor_res
    # gender_res: (pred, conf, p_female, p_male)
    g_pred, g_conf, p_female, p_male = gender_res

    t_pred = int(t_pred)
    g_pred = int(g_pred)
    t_conf = float(t_conf)
    p_non_tumor = float(p_non_tumor)
    p_tumor = float(p_tumor)
    g_conf = float(g_conf)
    p_female = float(p_female)
    p_male = float(p_male)

    tumor_label = tumor_labels[t_pred] if 0 <= t_pred < len(tumor_labels) else ""
    gender_label = gender_labels[g_pred] if 0 <= g_pred < len(gender_labels) else ""

    # 兼容老逻辑：1=恶性(肿瘤)，0=良性(非肿瘤)
    is_malignant = t_pred == 1
    is_benign = not is_malignant
    predicted_class = 1 if is_malignant else 0
    confidence = p_tumor if is_malignant else p_non_tumor

    zh_result = "良性嗓音（非肿瘤）" if is_benign else "疑似恶性嗓音（喉肿物）"
    zh_explanation = (
        f"模型认为该声音为“{zh_result}”，肿瘤概率约为 {p_tumor * 100:.2f}%，"
        f"非肿瘤概率约为 {p_non_tumor * 100:.2f}%。"
    )
    if gender_label:
        zh_explanation += (
            f" 同时模型判断该声音更接近“{gender_label}”声线，置信度约为 {g_conf * 100:.2f}%。"
        )

    mfcc_file, mel_file, waveform_file = _generate_spectrograms(audio_path, outdir)

    return {
        "success": True,
        "stage": "inference",
        "audioPath": str(audio_path),
        # 兼容旧字段
        "predicted_class": predicted_class,
        "confidence": confidence,
        "prob0": p_non_tumor,
        "prob1": p_tumor,
        "benign": is_benign,
        "malignant": is_malignant,
        "rule": "根据 Siamese 模型输出的“非肿瘤/肿瘤”概率，取概率更大者作为结果：0=良性(非肿瘤)，1=恶性(肿瘤)",
        "zhResult": zh_result,
        "zhExplanation": zh_explanation,
        "method": model_path.name,
        "usingModel": model_path.name,
        "device": str(siamese_device),
        "mfcc_file": mfcc_file,
        "mel_file": mel_file,
        "waveform_file": waveform_file,
        # 新增：语音性别
        "voiceGender": gender_label,          # 女性 / 男性
        "voiceGenderCode": g_pred,            # 0=女性, 1=男性
        "voiceGenderConfidence": g_conf,      # 对应预测类别的概率
        "voiceGenderProbFemale": p_female,
        "voiceGenderProbMale": p_male,
        "finalText": final_text,
        "tumorLabel": tumor_label,
    }


def make_error_payload(stage: str, audio_path: Optional[Path], exc: Exception) -> Dict[str, Any]:
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


def main(argv: Optional[List[str]] = None) -> int:
    parser = argparse.ArgumentParser(description="喉部健康音频检测脚本（Siamese 多任务）")
    parser.add_argument("--audio", required=False, help="待检测的本地音频文件绝对路径")
    parser.add_argument("--outdir", required=False, help="图谱输出目录（默认与音频同目录）")
    args = parser.parse_args(argv)

    if not args.audio:
        payload = make_error_payload("resolve_paths", None, ValueError("缺少参数 --audio"))
        print(json.dumps(payload, ensure_ascii=False))
        return 0

    audio_path = Path(args.audio)
    outdir = Path(args.outdir) if args.outdir else audio_path.parent

    try:
        result = detect(audio_path, outdir)
        print(json.dumps(result, ensure_ascii=False))
        return 0
    except Exception as exc:  # pragma: no cover
        stage = "java_exception"
        msg = str(exc).lower()
        if "no such file" in msg or "not found" in msg or isinstance(exc, FileNotFoundError):
            stage = "resolve_paths"
        elif "audio" in msg:
            stage = "load_audio"
        elif "model" in msg:
            stage = "load_model"
        elif "mfcc" in msg or "spectrogram" in msg:
            stage = "preprocess"
        payload = make_error_payload(stage, audio_path, exc)
        print(json.dumps(payload, ensure_ascii=False))
        return 0


if __name__ == "__main__":  # pragma: no cover
    sys.exit(main())




   
# 复用 Siamese 多任务音频病变 + 性别模型推理脚本
try:
    from audio_lesion_test import (  # type: ignore
        run_inference,
        TEMPERATURE as DEFAULT_TEMPERATURE,
        DEFAULT_N_MFCC,
        device as siamese_device,
    )
except Exception:  # pragma: no cover - 仅在极端场景下触发
    run_inference = None  # type: ignore
    DEFAULT_TEMPERATURE = 2.5
    DEFAULT_N_MFCC = 50
    siamese_device = torch.device("cpu")

try:
    # 使用 mp3toMFCC 生成 MFCC 图谱与 Mel 频谱图
    from mp3toMFCC import generate_mfcc_and_mel  # type: ignore
except Exception:  # pragma: no cover
    generate_mfcc_and_mel = None  # type: ignore


def _generate_spectrograms(audio_path: Path, outdir: Path) -> Tuple[str, str, str]:
    
    if generate_mfcc_and_mel is None:
        print("[WARN] 无法导入 mp3toMFCC.generate_mfcc_and_mel，本次不生成图谱文件。")
        return "", "", ""

    try:
        outdir.mkdir(parents=True, exist_ok=True)
        mfcc_file, mel_file, waveform_file = generate_mfcc_and_mel(
            str(audio_path), str(outdir)
        )
        print(
            f"[INFO] 已生成波形图: {waveform_file}，MFCC 图谱: {mfcc_file}，Mel 频谱图: {mel_file}"
        )
        return mfcc_file, mel_file, waveform_file
    except Exception as exc:  # pragma: no cover - 图谱生成失败不影响主流程
        print(f"[WARN] 生成 MFCC/Mel 图谱失败: {exc}")
        return "", ""


def detect(audio_path: Path, outdir: Path) -> Dict[str, Any]:
    
    global run_inference, DEFAULT_TEMPERATURE, DEFAULT_N_MFCC, siamese_device

    if run_inference is None:
        try:  # type: ignore
            from audio_lesion_test import (  # type: ignore
                run_inference as _run_inference,
                TEMPERATURE as _TEMP,
                DEFAULT_N_MFCC as _N_MFCC,
                device as _device,
            )

            run_inference = _run_inference  # type: ignore
            DEFAULT_TEMPERATURE = _TEMP
            DEFAULT_N_MFCC = _N_MFCC
            siamese_device = _device
        except Exception as exc:  # pragma: no cover - 极端兜底
            raise RuntimeError(
                "无法导入 audio_lesion_test.run_inference，请确认 voice/audio_lesion_test.py 是否存在且无语法错误"
            ) from exc

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

    # ---------- 2. 使用 Siamese 多任务模型推理 ----------
    stage = "inference"

    print(f"[INFO] 使用 Siamese 多任务模型进行推理: {model_path}")
    tumor_res, gender_res, final_text, tumor_labels, gender_labels = run_inference(  # type: ignore[arg-type]
        str(model_path),
        str(audio_path),
        temperature=DEFAULT_TEMPERATURE,
        n_mfcc=DEFAULT_N_MFCC,
    )

    # tumor_res: (tumor_pred, tumor_conf, prob_non_tumor, prob_tumor)
    tumor_pred, tumor_conf, prob_non_tumor, prob_tumor = tumor_res
    # gender_res: (gender_pred, gender_conf, prob_female, prob_male)
    gender_pred, gender_conf, prob_female, prob_male = gender_res

    tumor_pred = int(tumor_pred)
    gender_pred = int(gender_pred)
    tumor_conf = float(tumor_conf)
    prob_non_tumor = float(prob_non_tumor)
    prob_tumor = float(prob_tumor)
    gender_conf = float(gender_conf)
    prob_female = float(prob_female)
    prob_male = float(prob_male)

    tumor_label = tumor_labels[tumor_pred] if 0 <= tumor_pred < len(tumor_labels) else ""
    gender_label = (
        gender_labels[gender_pred] if 0 <= gender_pred < len(gender_labels) else ""
    )

    # 兼容旧字段语义：
    # - PrimaryScreenResult / malignant: true 表示“恶性(1) / 肿瘤”
    # - benign: false 表示“良性(0) / 非肿瘤”
    is_malignant = tumor_pred == 1  # 0: 非肿瘤(良性), 1: 肿瘤(恶性)
    is_benign = not is_malignant

    zh_result = "良性嗓音（非肿瘤）" if is_benign else "疑似恶性嗓音（喉肿物）"
    zh_explanation = (
        f"模型认为该声音为“{zh_result}”，肿瘤概率约为 {prob_tumor * 100:.2f}%，"
        f"非肿瘤概率约为 {prob_non_tumor * 100:.2f}%。"
    )
    gender_explanation = ""
    if gender_label:
        gender_explanation = (
            f"同时模型判断该声音更接近“{gender_label}”声线，置信度约为 {gender_conf * 100:.2f}%。"
        )

    device_str = str(siamese_device)

    # ---------- 3. 生成 MFCC / Mel 图谱 ----------
    stage = "postprocess"
    mfcc_file, mel_file, waveform_file = _generate_spectrograms(audio_path, outdir)

    # ---------- 4. 组装结果 JSON ----------
    # 为兼容旧前端，这里仍然保留 predicted_class / confidence / prob0 / prob1 / benign / malignant 字段：
    # - predicted_class: 0=良性(非肿瘤)，1=恶性(肿瘤)
    # - confidence: 取当前预测类别对应的概率
    predicted_class = 1 if is_malignant else 0
    confidence = prob_tumor if is_malignant else prob_non_tumor

    result: Dict[str, Any] = {
        "success": True,
        "stage": "inference",
        "audioPath": str(audio_path),
        "predicted_class": predicted_class,
        "confidence": confidence,
        "prob0": prob_non_tumor,  # 0 => 非肿瘤 / 良性
        "prob1": prob_tumor,  # 1 => 肿瘤 / 恶性
        "benign": is_benign,
        "malignant": is_malignant,
        "rule": "根据 Siamese 模型输出的“非肿瘤/肿瘤”概率，取概率更大者作为结果：0=良性(非肿瘤)，1=恶性(肿瘤)",
        "zhResult": zh_result,
        "zhExplanation": zh_explanation + (" " + gender_explanation if gender_explanation else ""),
        "method": model_path.name,
        "usingModel": model_path.name,
        "device": device_str,
        "mfcc_file": mfcc_file,
        "mel_file": mel_file,
        "waveform_file": waveform_file,
        # 新增：性别与详细概率信息
        "voiceGender": gender_label,  # 预测性别：女性/男性
        "voiceGenderCode": gender_pred,  # 0=女性，1=男性
        "voiceGenderConfidence": gender_conf,  # 预测性别对应的概率
        "voiceGenderProbFemale": prob_female,
        "voiceGenderProbMale": prob_male,
        # 便于前端/后端调试展示的综合描述
        "finalText": final_text,
        "tumorLabel": tumor_label,
    }

    print(
        "[INFO] 检测完成：",
        zh_result,
        f"(肿瘤概率 {prob_tumor * 100:.2f}% , 非肿瘤概率 {prob_non_tumor * 100:.2f}%)",
    )
    if gender_label:
        print(
            "[INFO] 语音性别预测：",
            gender_label,
            f"(置信度 {gender_conf * 100:.2f}%)",
        )
    return result


def make_error_payload(
    stage: str,
    audio_path: Optional[Path],
    exc: Exception,
) -> Dict[str, Any]:
    
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


def demo_on_test_dataset(limit: Optional[int] = 5) -> int:
    
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
                f"性别={result.get('voiceGender', '-')}, "
                f"MFCC图={result['mfcc_file'] or '-'}, "
                f"Mel图={result['mel_file'] or '-'}"
            )
            ok_count += 1
        except Exception as exc:
            payload = make_error_payload("demo", audio_path, exc)
            print(f"[FAIL] {payload['msg']}")
            fail_count += 1

    print(f"\n[DEMO] 完成，本次共成功 {ok_count} 条，失败 {fail_count} 条。")
    return 0


def main(argv: Optional[List[str]] = None) -> int:
    parser = argparse.ArgumentParser(
        description="喉部健康音频检测脚本（Siamese 多任务：肿瘤/非肿瘤 + 性别）"
    )
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

    # 如果用户直接运行脚本（无任何参数），自动进入 demo 模式，方便本地测试
    if not args.audio and not args.demo and (argv is None):
        args.demo = True

    # 本地调试模式
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

