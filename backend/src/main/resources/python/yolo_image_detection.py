#!/usr/bin/env python
"""
基于 PyTorch 和 OpenCV 的 YOLO 图像检测脚本，无需 torch.hub
"""

import argparse
import cv2
import torch
import sys
import os

def load_yolov5_model(model_path):
    """
    加载本地 YOLOv5 模型
    1. 添加 yolov5 文件夹到 sys.path，
    2. 使用 YOLOv5 内部的 attempt_load 方法加载模型
    """
    # 当前脚本所在目录
    current_dir = os.path.dirname(os.path.abspath(__file__))
    # 假设 YOLOv5 克隆目录为当前目录下的 'yolov5'
    yolov5_path = os.path.join(current_dir, 'yolov5')
    if not os.path.exists(yolov5_path):
        raise FileNotFoundError("找不到 yolov5 目录，请确认已将 YOLOv5 仓库克隆到该目录下。")
    if yolov5_path not in sys.path:
        sys.path.insert(0, yolov5_path)

    # 从 yolov5 模块中导入模型加载方法
    from models.experimental import attempt_load
    # 设置设备
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    # 加载模型
    model = attempt_load(model_path, map_location=device)
    return model

def detect_image(model, input_file, output_file):
    """
    执行图像检测，并将检测结果保存至 output_file。
    """
    # 读取输入图像
    image = cv2.imread(input_file)
    if image is None:
        print("Error: 无法打开图像文件", input_file)
        sys.exit(1)

    # 模型前向推理时，YOLOv5 通常要求传入 RGB 格式的图像
    image_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

    # 调用模型检测（注意：这里会自动进行预处理，具体可参考 YOLOv5 的源码）
    results = model(image_rgb)

    # 使用模型方法渲染检测结果，返回包含绘制检测框图像的列表，此处取第一个图像
    rendered_image = results.render()[0]

    # 渲染结果一般为 RGB 格式，转换回 BGR 保存为 JPEG 格式
    rendered_image_bgr = cv2.cvtColor(rendered_image, cv2.COLOR_RGB2BGR)

    # 保存处理后的图像
    cv2.imwrite(output_file, rendered_image_bgr)
    print("检测完成，输出图像保存至：", output_file)

def main():
    parser = argparse.ArgumentParser(description="YOLO 图像检测，无需 torch.hub")
    parser.add_argument('--input', required=True, help="输入图片文件路径")
    parser.add_argument('--output', required=True, help="输出图片文件路径")
    parser.add_argument('--model', default='model.pt', help="YOLO 模型 .pt 文件路径")
    args = parser.parse_args()

    print("加载 YOLO 模型中...")
    try:
        model = load_yolov5_model(args.model)
    except Exception as e:
        print("模型加载失败：", e)
        sys.exit(1)
    print("模型加载成功。")

    detect_image(model, args.input, args.output)

if __name__ == "__main__":
    main()