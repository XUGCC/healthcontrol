import os
os.environ['HF_ENDPOINT']='https://hf-mirror.com'
import torch
from PIL import Image
from torchvision import transforms
from PyQt6.uic import loadUi
#from PyQt6.Qt import *
from PyQt6.Qt6 import *
import sys
import numpy as np
import cv2
from PyQt6.QtCore import *
from PyQt6.QtWidgets import *
from PyQt6.QtGui import *
from PyQt6 import QtCore, QtGui, QtWidgets
import timm
def detect_soft_NBI(filepath_img):
        device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
        data_transform = transforms.Compose(
            [transforms.Resize([224, 224]),
             transforms.ToTensor(),
             transforms.Normalize([0.5, 0.5, 0.5], [0.5, 0.5, 0.5])])
        img = Image.open(filepath_img)
        print(f"图片加载成功，尺寸：{img.size}，模式：{img.mode}")  # 确认尺寸和通道
        img = data_transform(img)
        img=img.to(device)
        try:
            model = timm.create_model("vit_base_r50_s16_224.orig_in21k", pretrained=False, num_classes=6)
            model.load_state_dict(torch.load('./larynscope/net.pth', map_location=device))
            print("模型加载成功")  # 现在此语句在无异常时可达
        except Exception as e:
            print(f"模型加载失败: {str(e)}")
            return None  # 或抛出异常供上层处理
        model=model.to(device)
        img = torch.unsqueeze(img, dim=0)
        output=model(img)
        value,predicted=torch.max(output,dim=1)
        return predicted.item()


def detect_soft_normal(filepath_img):
        device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

        data_transform = transforms.Compose(
            [transforms.Resize([224, 224]),
             transforms.ToTensor(),
             transforms.Normalize([0.5, 0.5, 0.5], [0.5, 0.5, 0.5])])
        img = Image.open(filepath_img)

        img = data_transform(img)
        img=img.to(device)

        model = timm.create_model("vit_base_r50_s16_224.orig_in21k", pretrained=False, num_classes=6)
        model.load_state_dict(torch.load('./larynscope/net.pth', map_location=device))
        model=model.to(device)
        img = torch.unsqueeze(img, dim=0)
        output=model(img)
        value,predicted=torch.max(output,dim=1)
        return predicted.item()