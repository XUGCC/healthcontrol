"""
音频病变识别测试脚本
对应论文: The Initial Screening of Laryngeal Tumors via Voice Acoustic Analysis
         Based on Siamese Network Under Small Samples
模型: voice_best_model.pth（孪生 DenseNet + 肿瘤/性别双分类头）
实现与论文 codes（模型/codes：myModels/DenseNet.py, myDatasets.py, mp3_to_mfcc.py）一致：
  - 输入: MFCC 图像 512×512，n_mfcc=50（keyword _50），Resize+ToTensor，无 Normalize
  - 骨干: DenseNet201 结构，avgpool(7, stride=1)
  - 输出: 肿瘤/非肿瘤、女性/男性

使用方法:
  1. 命令行: python audio_lesion_test.py <音频路径>
  2. 文件选择: python audio_lesion_test.py --gui
  3. Web 上传: python audio_lesion_test.py --web  (需 pip install gradio)
"""

import sys
import io
import os
import argparse

# 设置控制台编码为UTF-8，解决Windows乱码问题
if sys.platform == 'win32':
    try:
        os.environ['PYTHONIOENCODING'] = 'utf-8'
        if hasattr(sys.stdout, 'buffer'):
            sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
        if hasattr(sys.stderr, 'buffer'):
            sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8', errors='replace')
        try:
            import subprocess
            subprocess.run(['chcp', '65001'], shell=True, capture_output=True)
        except Exception:
            pass
    except Exception:
        pass

import torch
import torch.nn as nn
import torch.nn.functional as F
import librosa
import librosa.display
import numpy as np
from sklearn import preprocessing
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt


# ============ 模型架构定义 ============
# 与 voice_best_model.pth 的 Siamese 网络结构匹配

class DenseLayer(nn.Module):
    def __init__(self, num_input_features, growth_rate, bn_size=4, drop_rate=0):
        super(DenseLayer, self).__init__()
        self.dense_layer = nn.Sequential(
            nn.BatchNorm2d(num_input_features),
            nn.ReLU(inplace=True),
            nn.Conv2d(num_input_features, bn_size * growth_rate, kernel_size=1, stride=1, bias=False),
            nn.BatchNorm2d(bn_size * growth_rate),
            nn.ReLU(inplace=True),
            nn.Conv2d(bn_size * growth_rate, growth_rate, kernel_size=3, stride=1, padding=1, bias=False),
        )
        self.drop_rate = drop_rate

    def forward(self, x):
        new_features = self.dense_layer(x)
        if self.drop_rate > 0:
            new_features = F.dropout(new_features, p=self.drop_rate, training=self.training)
        return torch.cat([x, new_features], 1)


class DenseBlock(nn.Module):
    def __init__(self, num_layers, num_input_features, bn_size, growth_rate, drop_rate):
        super(DenseBlock, self).__init__()
        self.layers = nn.ModuleList()
        for i in range(num_layers):
            layer = DenseLayer(num_input_features + i * growth_rate, growth_rate, bn_size, drop_rate)
            self.layers.append(layer)

    def forward(self, x):
        for layer in self.layers:
            x = layer(x)
        return x


class Transition(nn.Module):
    def __init__(self, num_input_features, num_output_features):
        super(Transition, self).__init__()
        self.transition_layer = nn.Sequential(
            nn.BatchNorm2d(num_input_features),
            nn.ReLU(inplace=True),
            nn.Conv2d(num_input_features, num_output_features, kernel_size=1, stride=1, bias=False),
            nn.AvgPool2d(kernel_size=2, stride=2)
        )

    def forward(self, x):
        return self.transition_layer(x)


class SiameseMultiTaskNet(nn.Module):
    """
    与论文代码 myModels/DenseNet.py 一致：孪生共享 DenseNet + 双分类头。
    - classifier1: 肿瘤 vs 非肿瘤 (num_classes=2)
    - classifier2: 性别 (2)
    输入须为 512×512 以得到 10×10 池化后 192000 维（与 checkpoint 一致）。
    """
    def __init__(self, img_size=512, growth_rate=32, block_config=(6, 12, 48, 32), num_init_features=64,
                 bn_size=4, drop_rate=0, in_channels=3):
        super(SiameseMultiTaskNet, self).__init__()
        self.img_size = img_size
        self.conv1 = nn.Sequential(
            nn.Conv2d(in_channels, num_init_features, kernel_size=7, stride=2, padding=3, bias=False),
            nn.BatchNorm2d(num_init_features),
            nn.ReLU(inplace=True),
            nn.MaxPool2d(kernel_size=3, stride=2, padding=1)
        )
        num_features = num_init_features
        self.layer1 = DenseBlock(block_config[0], num_features, bn_size, growth_rate, drop_rate)
        num_features = num_features + block_config[0] * growth_rate
        self.transition1 = Transition(num_features, num_features // 2)
        num_features = num_features // 2
        self.layer2 = DenseBlock(block_config[1], num_features, bn_size, growth_rate, drop_rate)
        num_features = num_features + block_config[1] * growth_rate
        self.transition2 = Transition(num_features, num_features // 2)
        num_features = num_features // 2
        self.layer3 = DenseBlock(block_config[2], num_features, bn_size, growth_rate, drop_rate)
        num_features = num_features + block_config[2] * growth_rate
        self.transition3 = Transition(num_features, num_features // 2)
        num_features = num_features // 2
        self.layer4 = DenseBlock(block_config[3], num_features, bn_size, growth_rate, drop_rate)
        num_features = num_features + block_config[3] * growth_rate
        self.avgpool = nn.AvgPool2d(7, stride=1)
        self.classifier1 = nn.Linear(num_features * 10 * 10, 2)
        self.classifier2 = nn.Linear(num_features * 10 * 10, 2)

    def forward(self, x):
        x = self.conv1(x)
        x = self.layer1(x)
        x = self.transition1(x)
        x = self.layer2(x)
        x = self.transition2(x)
        x = self.layer3(x)
        x = self.transition3(x)
        x = self.layer4(x)
        x = self.avgpool(x)
        x = torch.flatten(x, 1)
        out1 = self.classifier1(x)
        out2 = self.classifier2(x)
        return out1, out2


# ============ MFCC 特征提取 (与论文 codes 一致) ============
# 论文：myDatasets 使用 keyword _50 即 n_mfcc=50 的 MFCC 图；mp3_to_mfcc.draw_mfcc + preprocessing.scale
# 预处理：Resize(image_size, image_size) + ToTensor()，无 Normalize，取值 [0,1]

IMAGE_SIZE = 512
DEFAULT_N_MFCC = 50

def audio_to_mfcc(audio_path, n_mfcc=50, target_height=512, target_width=512, sr=22050):
    """
    与论文 codes 一致：draw_mfcc (librosa.feature.mfcc + preprocessing.scale) → specshow → RGB，
    Resize 到 image_size，仅 ToTensor [0,1]，不做 (x-0.5)/0.5。
    """
    try:
        audio, sr = librosa.load(audio_path, sr=sr)
    except Exception as e:
        raise RuntimeError(
            f"无法加载音频文件 {audio_path}: {e}\n"
            "可能原因：缺少 ffmpeg、文件损坏或格式不支持"
        )
    mfcc = librosa.feature.mfcc(y=audio, sr=sr, n_mfcc=n_mfcc)
    try:
        mfcc = preprocessing.scale(mfcc, axis=1)
    except (ValueError, RuntimeWarning):
        mfcc = (mfcc - np.mean(mfcc, axis=1, keepdims=True)) / (np.std(mfcc, axis=1, keepdims=True) + 1e-8)
    mfcc = np.nan_to_num(mfcc, nan=0.0, posinf=0.0, neginf=0.0)
    fig = plt.figure(figsize=(512 / 100, 512 / 100), dpi=100)
    plt.axis('off')
    librosa.display.specshow(mfcc, sr=sr, x_axis='time')
    plt.tight_layout(pad=0)
    fig.canvas.draw()
    img = np.frombuffer(fig.canvas.tostring_rgb(), dtype=np.uint8)
    img = img.reshape(fig.canvas.get_width_height()[::-1] + (3,))
    plt.close(fig)
    img = img[:, :, :3].copy()
    img_tensor = torch.from_numpy(img).permute(2, 0, 1).float() / 255.0
    img_tensor = F.interpolate(
        img_tensor.unsqueeze(0), size=(target_height, target_width),
        mode='bilinear', align_corners=False
    )
    return img_tensor


# ============ 模型加载与推理 ============
# 温度缩放：软化极端 logits，使输出概率更合理、不直接呈 0/1，便于反映模型不确定性
TEMPERATURE = 2.5

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

def load_siamese_model(model_path, img_size=512):
    checkpoint = torch.load(model_path, map_location=device)
    state_dict = checkpoint.get('state_dict', checkpoint.get('model_state_dict', checkpoint))
    new_state_dict = {}
    for k, v in state_dict.items():
        name = k[7:] if k.startswith('module.') else k
        new_state_dict[name] = v
    model = SiameseMultiTaskNet(img_size=img_size)
    model.load_state_dict(new_state_dict, strict=True)
    model.to(device)
    model.eval()
    return model


def predict_lesion(model, audio_path, temperature=None, n_mfcc=50):
    """
    对单个音频进行病变与性别分类。
    使用温度缩放得到概率，避免输出被压成 0/1，以反映模型实际置信度。
    返回:
        tumor_result: (预测类别, 置信度, 肿瘤概率, 非肿瘤概率)
        gender_result: (预测类别, 置信度, 女性概率, 男性概率)
        final_result: 综合描述字符串
    """
    if temperature is None:
        temperature = TEMPERATURE
    mfcc = audio_to_mfcc(audio_path, n_mfcc=n_mfcc).to(device)
    with torch.no_grad():
        logits1, logits2 = model(mfcc)
        probs1 = F.softmax(logits1 / temperature, dim=1)
        probs2 = F.softmax(logits2 / temperature, dim=1)
    # 任务1: 肿瘤 vs 非肿瘤 (0: 非肿瘤/声带良性, 1: 肿瘤/喉肿物)
    tumor_probs = probs1[0].cpu().numpy()
    tumor_pred = int(torch.argmax(probs1, dim=1).item())
    tumor_conf = float(tumor_probs[tumor_pred])
    tumor_labels = ['非肿瘤', '肿瘤']
    tumor_result = (tumor_pred, tumor_conf, float(tumor_probs[0]), float(tumor_probs[1]))
    # 任务2: 女性 vs 男性 (0: 女性, 1: 男性)
    gender_probs = probs2[0].cpu().numpy()
    gender_pred = int(torch.argmax(probs2, dim=1).item())
    gender_conf = float(gender_probs[gender_pred])
    gender_labels = ['女性', '男性']
    gender_result = (gender_pred, gender_conf, float(gender_probs[0]), float(gender_probs[1]))
    final_result = f"{tumor_labels[tumor_pred]} / {gender_labels[gender_pred]}"
    return tumor_result, gender_result, final_result, tumor_labels, gender_labels


def run_inference(model_path, audio_path, temperature=None, n_mfcc=50):
    model = load_siamese_model(model_path)
    tumor_res, gender_res, final, tumor_labels, gender_labels = predict_lesion(
        model, audio_path, temperature=temperature, n_mfcc=n_mfcc
    )
    return tumor_res, gender_res, final, tumor_labels, gender_labels


def print_results(audio_path, tumor_res, gender_res, final, tumor_labels, gender_labels):
    print("=" * 55)
    print("音频病变识别结果（以下为模型预测概率，非直接 0/1 判定）")
    print("=" * 55)
    print(f"音频文件: {audio_path}\n")
    print("[任务1] 肿瘤 vs 非肿瘤（模型输出概率）")
    print(f"  非肿瘤: {tumor_res[2]:.4f}  肿瘤: {tumor_res[3]:.4f}")
    print(f"  预测类别: {tumor_labels[tumor_res[0]]}（取概率较高者，对应置信度: {tumor_res[1]:.4f}）\n")
    print("[任务2] 女性 vs 男性（模型输出概率）")
    print(f"  女性: {gender_res[2]:.4f}  男性: {gender_res[3]:.4f}")
    print(f"  预测类别: {gender_labels[gender_res[0]]}（取概率较高者，对应置信度: {gender_res[1]:.4f}）\n")
    print("[综合] 最终结果（由上述概率推导）")
    print(f"  {final}")
    print("=" * 55)


# ============ 主入口 ============

def main():
    parser = argparse.ArgumentParser(description="音频病变识别 - 肿瘤/非肿瘤、女性/男性分类")
    parser.add_argument("audio", nargs="?", help="音频文件路径 (支持 mp3/wav/flac 等)")
    parser.add_argument("-m", "--model", default="voice_best_model.pth", help="模型文件路径")
    parser.add_argument("--gui", action="store_true", help="打开文件选择对话框选择音频")
    parser.add_argument("--web", action="store_true", help="启动 Web 上传界面 (需要 pip install gradio)")
    parser.add_argument("-t", "--temperature", type=float, default=TEMPERATURE,
                        help="softmax 温度 (默认 %.1f)，越大概率越平滑、越不易出现 0/1" % TEMPERATURE)
    parser.add_argument("--n-mfcc", type=int, default=DEFAULT_N_MFCC,
                        help="MFCC 阶数 (默认 50，与论文 codes keyword _50 一致)")
    args = parser.parse_args()
    model_path = args.model
    audio_path = args.audio
    if args.web:
        try:
            import gradio as gr
        except ImportError:
            print("请安装 gradio: pip install gradio")
            return
        if not os.path.exists(model_path):
            print(f"错误: 找不到模型文件 {model_path}")
            return
        print("正在加载模型...")
        model = load_siamese_model(model_path)
        temp = getattr(args, 'temperature', TEMPERATURE)
        n_mfcc = getattr(args, 'n_mfcc', DEFAULT_N_MFCC)
        def predict(audio_file):
            if audio_file is None:
                return "请上传音频文件", "", ""
            tumor_res, gender_res, final, tumor_labels, gender_labels = predict_lesion(
                model, audio_file, temperature=temp, n_mfcc=n_mfcc
            )
            t = "**模型输出概率**（非 0/1 判定）\n非肿瘤: %.2f  肿瘤: %.2f\n预测: **%s**（置信度: %.2f）" % (
                tumor_res[2], tumor_res[3], tumor_labels[tumor_res[0]], tumor_res[1])
            g = "**模型输出概率**（非 0/1 判定）\n女性: %.2f  男性: %.2f\n预测: **%s**（置信度: %.2f）" % (
                gender_res[2], gender_res[3], gender_labels[gender_res[0]], gender_res[1])
            return final, t, g
        with gr.Blocks(title="音频病变识别") as demo:
            gr.Markdown("# 音频病变识别 - 孪生网络多任务模型")
            gr.Markdown("上传音频文件，识别 **肿瘤/非肿瘤** 和 **女性/男性** 两个分类任务")
            with gr.Row():
                audio_in = gr.Audio(type="filepath", label="上传音频 (mp3/wav/flac)")
                with gr.Column():
                    result_out = gr.Textbox(label="最终结果", interactive=False)
                    tumor_out = gr.Markdown(label="肿瘤 vs 非肿瘤")
                    gender_out = gr.Markdown(label="女性 vs 男性")
            btn = gr.Button("识别")
            btn.click(predict, inputs=audio_in, outputs=[result_out, tumor_out, gender_out])
        demo.launch()
        return
    if args.gui or not audio_path:
        try:
            import tkinter as tk
            from tkinter import filedialog
            root = tk.Tk()
            root.withdraw()
            root.attributes('-topmost', True)
            audio_path = filedialog.askopenfilename(
                title="选择音频文件",
                filetypes=[("音频文件", "*.mp3 *.wav *.flac *.m4a"), ("所有文件", "*.*")]
            )
            root.destroy()
            if not audio_path:
                print("未选择文件，已取消")
                return
        except ImportError:
            print("请通过命令行指定音频路径: python audio_lesion_test.py <音频路径>")
            return
    if not os.path.exists(model_path):
        print(f"错误: 找不到模型文件 {model_path}")
        return
    if not os.path.exists(audio_path):
        print(f"错误: 找不到音频文件 {audio_path}")
        return
    temp = getattr(args, 'temperature', TEMPERATURE)
    n_mfcc = getattr(args, 'n_mfcc', DEFAULT_N_MFCC)
    print(f"使用设备: {device}  温度: {temp}  n_mfcc: {n_mfcc}")
    print("正在加载模型...")
    try:
        tumor_res, gender_res, final, tumor_labels, gender_labels = run_inference(
            model_path, audio_path, temperature=temp, n_mfcc=n_mfcc
        )
        print_results(audio_path, tumor_res, gender_res, final, tumor_labels, gender_labels)
    except Exception as e:
        print(f"推理出错: {e}")
        import traceback
        traceback.print_exc()


if __name__ == "__main__":
    main()
