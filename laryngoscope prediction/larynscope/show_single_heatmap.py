#Display the heat map with abslote path
from PyQt6.QtGui import QImage, QPixmap
from PyQt6.uic.properties import QtGui
from torchvision.transforms import Compose, CenterCrop, ToTensor, Resize, Normalize, \
    RandomAutocontrast, RandomEqualize  # , InterpolationMode
from torchvision.transforms import RandomResizedCrop, Resize, RandomAffine, RandomRotation, RandomHorizontalFlip, \
    RandomVerticalFlip,RandomAdjustSharpness,ColorJitter
import torch.utils.data as data
from os import listdir
from os.path import exists, join
from PIL import Image
import torch
from torchvision.transforms import InterpolationMode
import traceback as trace
import cv2
import numpy as np
from .utils_heatmap import GradCAM, show_cam_on_image
from torch.utils.data import DataLoader
from . import model_repAlexnet


device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
def input_transform():
        image_size= 224
        return Compose([ToTensor(),
                        Resize((image_size, image_size),
                        interpolation=InterpolationMode.BICUBIC),
                        Normalize([0.4208, 0.3874, 0.3595], [0.0543, 0.0493, 0.0429])])

def load_img(filepath, data_transform,class_name):
    model = model_repAlexnet.RepAlexNet(num_blocks=[1, 1, 1, 1], num_classes=6)
    model = model_repAlexnet.repvgg_model_convert(model)
    model.load_state_dict(torch.load("./store/repalexnet_heatmap_n100.pth", map_location=device))
    print("model load complete!")
    target_layers = [model.stage5]

    img = Image.open(filepath).convert('RGB')
    img=img.resize((224,224))
    img = np.array(img, dtype=np.uint8)
    img_tensor = data_transform(img)

    input_tensor = torch.unsqueeze(img_tensor, dim=0)
    cam = GradCAM(model=model, target_layers=target_layers, use_cuda=False)
    target_category =class_name # label
    grayscale_cam = cam(input_tensor=input_tensor, target_category=target_category)

    grayscale_cam = grayscale_cam[0, :]
    visualization = show_cam_on_image(img.astype(dtype=np.float32) / 255.,
                                      grayscale_cam,
                                      use_rgb=True)

    image_bgr = cv2.cvtColor(visualization, cv2.COLOR_RGB2BGR)
    #cv2.imwrite('temp_image.jpg', image_bgr)
    return image_bgr

def run(Img_Path,Class_Name):
    transform =input_transform()
    img=load_img(Img_Path, transform, Class_Name)

    return img