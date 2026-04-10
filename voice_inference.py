"""
音频分类模型推理脚本
用于加载voice_best_model.pth并进行音频分类
"""

import sys
import io
import os

# 设置控制台编码为UTF-8，解决Windows乱码问题
if sys.platform == 'win32':
    try:
        # 方法1: 设置环境变量
        os.environ['PYTHONIOENCODING'] = 'utf-8'
        
        # 方法2: 重新包装stdout和stderr
        if hasattr(sys.stdout, 'buffer'):
            sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
        if hasattr(sys.stderr, 'buffer'):
            sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8', errors='replace')
        
        # 方法3: 尝试设置控制台代码页为UTF-8
        try:
            import subprocess
            subprocess.run(['chcp', '65001'], shell=True, capture_output=True)
        except:
            pass
    except Exception as e:
        # 如果设置失败，继续执行（不影响程序运行）
        pass

import torch
import torch.nn as nn
import torch.nn.functional as F
import librosa
import numpy as np
from sklearn import preprocessing
import os
from os.path import join

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

# DenseNet模型架构定义
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


class DenseNet(nn.Module):
    def __init__(self, growth_rate=32, block_config=(6, 12, 48, 32), num_init_features=64,
                 bn_size=4, drop_rate=0, num_classes=1000):
        super(DenseNet, self).__init__()
        
        # 初始卷积层
        self.features = nn.Sequential(
            nn.Conv2d(1, num_init_features, kernel_size=7, stride=2, padding=3, bias=False),
            nn.BatchNorm2d(num_init_features),
            nn.ReLU(inplace=True),
            nn.MaxPool2d(kernel_size=3, stride=2, padding=1)
        )
        
        num_features = num_init_features
        
        # DenseBlock和Transition层
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
        
        # 最终批归一化
        self.bn = nn.BatchNorm2d(num_features)
        self.relu = nn.ReLU(inplace=True)
        
        # 分类器
        self.classifier = nn.Linear(num_features, num_classes)
        
    def forward(self, x):
        x = self.features(x)
        x = self.layer1(x)
        x = self.transition1(x)
        x = self.layer2(x)
        x = self.transition2(x)
        x = self.layer3(x)
        x = self.transition3(x)
        x = self.layer4(x)
        x = self.bn(x)
        x = self.relu(x)
        x = F.adaptive_avg_pool2d(x, (1, 1))
        x = torch.flatten(x, 1)
        x = self.classifier(x)
        return x


def extract_mfcc_features(audio_path, n_mfcc=13, target_height=224, target_width=224):
    """
    从音频文件提取MFCC特征并转换为图像格式
    使用插值调整到目标尺寸，确保模型可以处理
    """
    # 加载音频（添加错误处理）
    try:
        audio, sr = librosa.load(audio_path, sr=22050)
    except Exception as e:
        error_msg = f"无法加载音频文件 {audio_path}: {e}\n"
        error_msg += "可能的原因：\n"
        error_msg += "1. 缺少音频解码库（ffmpeg）- 请运行: conda install -c conda-forge ffmpeg\n"
        error_msg += "2. 音频文件损坏或不完整\n"
        error_msg += "3. 音频格式不支持\n"
        raise RuntimeError(error_msg)
    
    # 提取MFCC特征
    mfcc = librosa.feature.mfcc(y=audio, sr=sr, n_mfcc=n_mfcc)
    
    # 标准化（处理可能的数值问题）
    try:
        mfcc = preprocessing.scale(mfcc, axis=1)
    except (ValueError, RuntimeWarning):
        # 如果标准化失败，使用简单的归一化
        mfcc = (mfcc - np.mean(mfcc, axis=1, keepdims=True)) / (np.std(mfcc, axis=1, keepdims=True) + 1e-8)
    
    # 处理NaN和Inf值
    mfcc = np.nan_to_num(mfcc, nan=0.0, posinf=0.0, neginf=0.0)
    
    # 转换为tensor格式 [batch, channels, height, width]
    # 先转换为tensor以便使用插值
    mfcc_tensor = torch.FloatTensor(mfcc).unsqueeze(0).unsqueeze(0)  # [1, 1, n_mfcc, time_frames]
    
    # 使用双线性插值调整到目标尺寸
    # 这确保输入尺寸足够大，避免池化后尺寸变为0
    mfcc_tensor = F.interpolate(
        mfcc_tensor, 
        size=(target_height, target_width), 
        mode='bilinear', 
        align_corners=False
    )
    
    return mfcc_tensor


def load_model(model_path, num_classes=None):
    """
    加载训练好的模型
    """
    checkpoint = torch.load(model_path, map_location=device)
    
    # 尝试从checkpoint中获取类别数
    if num_classes is None:
        # 查找分类器层的权重
        if isinstance(checkpoint, dict):
            state_dict = checkpoint.get('state_dict', checkpoint)
            classifier_keys = [k for k in state_dict.keys() if 'classifier' in k and 'weight' in k]
            if classifier_keys:
                num_classes = state_dict[classifier_keys[0]].shape[0]
                print(f"从模型推断类别数: {num_classes}")
            else:
                num_classes = 1000  # 默认值
                print(f"无法推断类别数，使用默认值: {num_classes}")
        else:
            num_classes = 1000
    
    # 创建模型
    model = DenseNet(num_classes=num_classes)
    
    # 加载权重
    if isinstance(checkpoint, dict):
        if 'state_dict' in checkpoint:
            state_dict = checkpoint['state_dict']
        elif 'model_state_dict' in checkpoint:
            state_dict = checkpoint['model_state_dict']
        else:
            state_dict = checkpoint
    else:
        state_dict = checkpoint
    
    # 处理键名不匹配的情况（移除'module.'前缀）
    new_state_dict = {}
    for k, v in state_dict.items():
        name = k[7:] if k.startswith('module.') else k
        new_state_dict[name] = v
    
    model.load_state_dict(new_state_dict, strict=False)
    model.to(device)
    model.eval()
    
    return model


def predict_audio(model, audio_path, class_names=None):
    """
    对音频文件进行预测
    """
    # 提取特征
    print(f"正在处理音频文件: {audio_path}")
    mfcc_features = extract_mfcc_features(audio_path)
    mfcc_features = mfcc_features.to(device)
    
    # 推理
    with torch.no_grad():
        outputs = model(mfcc_features)
        probabilities = F.softmax(outputs, dim=1)
        confidence, predicted = torch.max(probabilities, 1)
    
    predicted_class = predicted.item()
    confidence_score = confidence.item()
    
    # 获取top-5预测
    top5_probs, top5_indices = torch.topk(probabilities, min(5, probabilities.size(1)))
    
    print(f"\n预测结果:")
    print(f"最可能的类别: {predicted_class} (置信度: {confidence_score:.4f})")
    
    if class_names:
        print(f"类别名称: {class_names[predicted_class] if predicted_class < len(class_names) else '未知'}")
    
    print(f"\nTop-5 预测:")
    for i, (prob, idx) in enumerate(zip(top5_probs[0], top5_indices[0])):
        class_name = class_names[idx.item()] if class_names and idx.item() < len(class_names) else f"类别{idx.item()}"
        print(f"  {i+1}. {class_name}: {prob.item():.4f}")
    
    return predicted_class, confidence_score, top5_probs, top5_indices


if __name__ == "__main__":
    # 配置
    # 重要：使用“脚本所在目录”的绝对路径，避免在不同工作目录运行时报找不到文件
    base_dir = os.path.dirname(os.path.abspath(__file__))

    # 模型文件优先尝试：
    # 1) 与本脚本同级（例如你把 voice_inference.py 放在项目根目录，模型也在根目录）
    # 2) base_dir/voice/voice_best_model.pth（模型在 voice 目录时）
    model_path = os.path.join(base_dir, 'voice_best_model.pth')
    if not os.path.exists(model_path):
        alt_model_path = os.path.join(base_dir, 'voice', 'voice_best_model.pth')
        if os.path.exists(alt_model_path):
            model_path = alt_model_path

    # 默认测试音频（同样用绝对路径）
    audio_file = join(base_dir, 'voice', 'test_2_82', '声带良性', '杨俊香2022-10-8.MP3')  # 修改为你的音频文件路径
    
    # 类别名称（如果有的话，请根据实际情况修改）
    # class_names = ['类别1', '类别2', ...]  # 替换为实际的类别名称列表
    class_names = None  # 如果没有类别名称，设置为None
    
    # 加载模型
    print("正在加载模型...")
    try:
        model = load_model(model_path)
        print("模型加载成功！\n")
    except Exception as e:
        print(f"模型加载失败: {e}")
        print("请检查模型文件路径和模型架构是否匹配")
        exit(1)
    
    # 检查音频文件是否存在
    if not os.path.exists(audio_file):
        print(f"错误: 找不到音频文件 {audio_file}")
        print("请修改audio_file变量为正确的音频文件路径")
        exit(1)
    
    # 进行预测
    try:
        predicted_class, confidence, top5_probs, top5_indices = predict_audio(
            model, audio_file, class_names
        )
        print(f"\n预测完成！")
    except Exception as e:
        print(f"预测过程中出错: {e}")
        import traceback
        traceback.print_exc()
