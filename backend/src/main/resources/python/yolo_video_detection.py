#!/usr/bin/env python
"""
基于 PyTorch 和 OpenCV 的 YOLO 视频检测脚本
"""

import argparse
import cv2
import torch
import sys

def detect_video(model, input_file, output_file):
    cap = cv2.VideoCapture(input_file)
    if not cap.isOpened():
        print("Error: 无法打开视频文件", input_file)
        sys.exit(1)
    fps = cap.get(cv2.CAP_PROP_FPS)
    width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
    fourcc = cv2.VideoWriter_fourcc(*'mp4v')
    out = cv2.VideoWriter(output_file, fourcc, fps, (width, height))
    frame_count = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    current_frame = 0

    print(f"处理视频，共 {frame_count} 帧...")
    while True:
        ret, frame = cap.read()
        if not ret:
            break
        results = model(frame)
        rendered_frame = results.render()[0]
        out.write(rendered_frame)
        current_frame += 1
        if current_frame % 10 == 0:
            print(f"已处理 {current_frame}/{frame_count} 帧")
    cap.release()
    out.release()
    print("检测完成，输出文件保存至：", output_file)

def main():
    parser = argparse.ArgumentParser(description="YOLO 视频检测")
    parser.add_argument('--input', required=True, help="输入视频文件路径")
    parser.add_argument('--output', required=True, help="输出视频文件路径")
    parser.add_argument('--model', default='model.pt', help="YOLO 模型 .pt 文件路径")
    args = parser.parse_args()

    print("加载 YOLO 模型中...")
    try:
        model = torch.hub.load('ultralytics/yolov5', 'custom', path=args.model, force_reload=True)
    except Exception as e:
        print("模型加载失败：", e)
        sys.exit(1)
    print("模型加载成功。")
    detect_video(model, args.input, args.output)

if __name__ == "__main__":
    main()