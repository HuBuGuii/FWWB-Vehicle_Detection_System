#!/usr/bin/env python
"""
基于 PyTorch 和 OpenCV 的 YOLO 图像检测脚本
"""

import argparse
import cv2
import torch
import sys
import  numpy

def detect_image(model, input_file, output_file):
    # 读取输入图像
    image = cv2.imread(input_file)
    if image is None:
        print("Error: 无法打开图像文件", input_file)
        sys.exit(1)
    # 调用模型对图像进行检测
    results = model(image)
    # 使用模型渲染检测结果，返回含检测框的图像列表，取第一个
    rendered_image = results.render()[0]
    # 保存处理后的图像
    cv2.imwrite(output_file, rendered_image)
    print("检测完成，输出图像保存至：", output_file)

def main():
    parser = argparse.ArgumentParser(description="YOLO 图像检测")
    parser.add_argument('--input', required=True, help="输入图片文件路径")
    parser.add_argument('--output', required=True, help="输出图片文件路径")
    parser.add_argument('--model', default='model.pt', help="YOLO 模型 .pt 文件路径")
    args = parser.parse_args()

    print("加载 YOLO 模型中...")
    try:
        # 加载自定义模型，使用 torch.hub 加载 ultralytics/yolov5 仓库中的 custom 模型
        model = torch.hub.load('ultralytics/yolov5', 'custom', path=args.model, force_reload=True)
    except Exception as e:
        print("模型加载失败：", e)
        sys.exit(1)
    print("模型加载成功。")

    detect_image(model, args.input, args.output)

if __name__ == "__main__":
    main()