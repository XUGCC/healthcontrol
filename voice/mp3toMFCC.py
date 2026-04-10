import torch
from torch.utils.data import DataLoader
import os
from os.path import join
from os import listdir
from pypinyin import pinyin, Style
from pydub import AudioSegment
import librosa
import librosa.display
from sklearn import preprocessing
import numpy as np
from matplotlib import pyplot as plt
from scipy.io import wavfile
import wave
import json

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

dest = os.path.abspath(os.path.dirname(__file__))


def draw_wav(audio, sr, wav_img_file):
    tmax, tmin = int(audio.shape[0] / sr), 0
    t = np.linspace(tmin, tmax, (tmax - tmin) * sr)
    plt.clf()
    plt.figure(dpi=200, figsize=(12, 6))
    # plt.figure()
    plt.plot(t, audio[tmin * sr:tmax * sr])
    plt.xlim(t[0], t[-1])
    plt.xlabel('times/s', fontsize=20)
    plt.ylabel('Amplitude', fontsize=20)
    plt.grid()
    plt.savefig(wav_img_file)
    plt.close()


def draw_mfcc(audio, sr, mfcc_img_file, coeff_mfcc):
    mfcc = librosa.feature.mfcc(y=audio, sr=sr, n_mfcc=coeff_mfcc)
    mfcc = preprocessing.scale(mfcc, axis=1)
    plt.clf()
    plt.figure(figsize=(512/100, 512/100))
    librosa.display.specshow(mfcc, sr=sr, x_axis='time')
    # plt.colorbar(format='%+2.0f dB')
    # plt.title('Mel Spectrogram')
    plt.axis('off')
    plt.tight_layout()
    plt.savefig(mfcc_img_file)
    plt.close()


def draw_mel(audio, sr, mel_img_file):
    """
    生成 Mel 频谱图图像（512x512），用于前端展示或模型可视化。
    """
    S = librosa.feature.melspectrogram(y=audio, sr=sr, n_fft=2048, hop_length=512, n_mels=128)
    S_dB = librosa.power_to_db(S, ref=np.max)

    plt.clf()
    plt.figure(figsize=(512 / 100, 512 / 100))
    librosa.display.specshow(S_dB, sr=sr, x_axis='time', y_axis='mel')
    plt.axis('off')
    plt.tight_layout()
    plt.savefig(mel_img_file)
    plt.close()


def write_mfcc(path, sr, audio):
    # wav to mfcc
    base_path = os.path.splitext(path)[0]  # 获取不带扩展名的路径
    coeff_mfcc = np.arange(5, 135, 5)
    for coeff in coeff_mfcc:
        mfcc_img_file_coeff = f"{base_path}_mfcc_{coeff}.png"
        draw_mfcc(audio, sr, mfcc_img_file_coeff, int(coeff))

    # with open(join(dest, dataset+'.csv'), 'a') as epoch_log:
    #     epoch_log.write(f'{name_class},{pat},{audio.shape[0]},{sr},{audio.shape[0]/sr}\n')


def generate_mfcc_and_mel(audio_path, out_dir=None, n_mfcc=130):
    """
    供推理脚本复用的工具函数：
    - 加载指定音频
    - 在给定输出目录下生成一张波形图、一张 MFCC 图和一张 Mel 频谱图
    - 返回图像文件名（不含路径），方便后端按 URL 前缀拼接

    :param audio_path: 音频文件绝对路径
    :param out_dir: 输出目录（默认音频所在目录）
    :param n_mfcc: 生成 MFCC 时使用的系数数量（默认 130）
    :return: (mfcc_file_name, mel_file_name, waveform_file_name)
    """
    if out_dir is None:
        out_dir = os.path.dirname(audio_path)
    out_dir = os.path.abspath(out_dir)
    os.makedirs(out_dir, exist_ok=True)

    # 加载音频
    audio, sr = librosa.load(audio_path)

    base_name = os.path.splitext(os.path.basename(audio_path))[0]
    mfcc_img_path = os.path.join(out_dir, f"{base_name}_mfcc.png")
    mel_img_path = os.path.join(out_dir, f"{base_name}_mel.png")
    waveform_img_path = os.path.join(out_dir, f"{base_name}_waveform.png")

    # 生成波形图 + 单张 MFCC 和 Mel 图
    draw_wav(audio, sr, waveform_img_path)
    draw_mfcc(audio, sr, mfcc_img_path, int(n_mfcc))
    draw_mel(audio, sr, mel_img_path)

    # 仅返回文件名部分，便于后端根据 AudioUrl 目录拼接 URL
    return (
        os.path.basename(mfcc_img_path),
        os.path.basename(mel_img_path),
        os.path.basename(waveform_img_path),
    )


if __name__ == "__main__":
    # 请在这里指定你的音频文件名（支持mp3, wav, flac等格式）
    filename = 'your_audio_file.mp3'  # 修改为你的音频文件名
    
    if not filename:
        print("错误：请在第63行指定音频文件名！")
        exit(1)
    
    file_path = join(dest, filename)
    
    if not os.path.exists(file_path):
        print(f"错误：找不到文件 {file_path}")
        print(f"请确保音频文件位于：{dest}")
        exit(1)
    
    print(f"正在处理音频文件: {file_path}")
    
    # audio - 音频数据， sr - 采样频率， dct_type - 默认使用DCT离散余弦变换，值为2
    print("正在加载音频...")
    audio, sr = librosa.load(file_path)
    print(f"音频加载成功！采样率: {sr} Hz, 时长: {len(audio)/sr:.2f} 秒")
    
    # 生成输出文件名
    base_name = os.path.splitext(filename)[0]
    wav_img_file = join(dest, f"{base_name}_waveform.png")
    wav_output_file = join(dest, f"{base_name}_output.wav")
    
    print("正在生成波形图...")
    draw_wav(audio, sr, wav_img_file)
    print(f"波形图已保存: {wav_img_file}")
    
    print("正在保存WAV文件...")
    wavfile.write(wav_output_file, sr, audio)
    print(f"WAV文件已保存: {wav_output_file}")
    
    print("正在生成MFCC特征图...")
    write_mfcc(file_path, sr, audio)
    print("MFCC特征图生成完成！")
    print(f"所有文件已保存到: {dest}")