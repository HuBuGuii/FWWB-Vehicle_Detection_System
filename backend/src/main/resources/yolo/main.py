#!/usr/bin/env python
# -*- coding: utf-8 -*-

from ultralytics import YOLO
from paddleocr import PaddleOCR
import cv2
import numpy as np
import re
import json
import os
import argparse
from datetime import datetime
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import time

# 省份简称列表
PROVINCE_ABBR = [
    '京', '沪', '津', '渝', '冀', '晋', '辽', '吉', '黑',
    '苏', '浙', '皖', '闽', '赣', '鲁', '豫', '鄂', '湘',
    '粤', '琼', '川', '贵', '云', '陕', '甘', '青', '台',
    '蒙', '桂', '宁', '新', '藏', '港', '澳'
]

class LicensePlateProcessor:
    def __init__(self, args):
        self.args = args
        self.yolo_model = YOLO(args.model)
        self.paddle_ocr = PaddleOCR(
            use_angle_cls=True,
            lang="ch",
            rec_algorithm="SVTR_LCNet",
            det_db_score_mode="fast"
        )
        self.setup_output()
        self.font_path = "C:/Windows/Fonts/simhei.ttf"
        if not os.path.exists(self.font_path):
            raise FileNotFoundError(f"Chinese font not found: {self.font_path}")
        self.font = ImageFont.truetype(self.font_path, 20)

    def get_unique_dir(self, base_path, base_name):
        """生成唯一的目录名称，格式为basename、basename_1、basename_2等"""
        counter = 0
        while True:
            dir_name = f"{base_name}_{counter}" if counter > 0 else base_name
            dir_path = base_path / dir_name
            if not dir_path.exists():
                return dir_path
            counter += 1

    def setup_output(self):
        """构建唯一输出目录，并设置结果文件路径"""
        base_path = Path(self.args.project)
        base_name = self.args.name
        self.output_dir = self.get_unique_dir(base_path, base_name)
        self.output_dir.mkdir(parents=True, exist_ok=True)
        self.txt_path = self.output_dir / "results.txt"
        # 使用输出目录名称作为JSON文件名的一部分
        self.json_path = self.output_dir / f"detections_{self.output_dir.name}.json"

    def is_valid_plate(self, text):
        clean_text = text.replace("·", "").replace(" ", "")
        pattern = r"^([" + "".join(PROVINCE_ABBR) + r"])[A-HJ-NP-Z][0-9A-HJ-NP-Z]{5,6}$"
        return re.match(pattern, clean_text) and clean_text[:1] in PROVINCE_ABBR

    def process_frame(self, frame, frame_count=0):
        results = self.yolo_model(frame, conf=self.args.conf, imgsz=640)
        for result in results:
            for box, cls, conf in zip(result.boxes.xyxy, result.boxes.cls, result.boxes.conf):
                # 解析检测框坐标
                x1, y1, x2, y2 = map(int, box.tolist())
                x1 = max(0, x1)
                y1 = max(0, y1)
                x2 = min(frame.shape[1], x2)
                y2 = min(frame.shape[0], y2)
                # 对检测区域进行 OCR 识别车牌
                vehicle_roi = frame[y1:y2, x1:x2]
                best_plate = self.process_roi(vehicle_roi)
                self.save_results(frame_count, result.names[int(cls)],
                                  conf.item(), (x1, y1, x2, y2), best_plate)
                frame = self.draw_annotations(frame, (x1, y1, x2, y2),
                                              result.names[int(cls)], best_plate)
        return frame

    def process_roi(self, roi):
        if roi.size == 0:
            return "None"
        ocr_result = self.paddle_ocr.ocr(roi, cls=True)
        if ocr_result and ocr_result[0]:
            for line in ocr_result[0]:
                text = line[1][0]
                if self.is_valid_plate(text):
                    return text.replace("·", "")
        return "None"

    def save_results(self, frame_id, vehicle_type, conf, coords, license_plate):
        timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]
        record = {
            "timestamp": timestamp,
            "frame": frame_id,
            "type": vehicle_type,
            "confidence": round(conf, 3),
            "coordinates": list(map(int, coords)),
            "license_plate": license_plate
        }
        if self.args.txt:
            with open(self.txt_path, "a", encoding="utf-8") as f:
                f.write(f"{json.dumps(record, ensure_ascii=False)}\n")
        if self.args.json:
            with open(self.json_path, "a", encoding="utf-8") as f:
                f.write(f"{json.dumps(record, ensure_ascii=False, indent=2)},\n")

    def draw_annotations(self, frame, coords, vehicle_type, license_plate):
        x1, y1, x2, y2 = coords
        pil_img = Image.fromarray(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
        draw = ImageDraw.Draw(pil_img)
        draw.rectangle([x1, y1, x2, y2], outline=(0, 255, 0), width=2)
        label = f"{vehicle_type}: {license_plate}"
        text_y = y1 - 25 if y1 > 30 else y1 + 5
        draw.text((x1, text_y), label, font=self.font, fill=(0, 255, 0))
        return cv2.cvtColor(np.array(pil_img), cv2.COLOR_RGB2BGR)

    def process_video_file(self, video_file):
        """
        处理单个视频文件
        """
        cap = cv2.VideoCapture(video_file)
        if not cap.isOpened():
            print(f"无法打开视频文件: {video_file}")
            return
        fps = cap.get(cv2.CAP_PROP_FPS)
        if fps <= 0:
            fps = 25  # 默认帧率
        width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
        height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
        writer = None
        base_name = os.path.basename(video_file)
        name_without_ext = os.path.splitext(base_name)[0]
        if self.args.save_video:
            out_video_path = self.output_dir / f"{name_without_ext}_output.mp4"
            fourcc = cv2.VideoWriter_fourcc(*'mp4v')
            writer = cv2.VideoWriter(str(out_video_path), fourcc, fps, (width, height))
        frame_count = 0
        while cap.isOpened():
            ret, frame = cap.read()
            if not ret:
                break
            processed_frame = self.process_frame(frame, frame_count)
            # 保存最新的检测结果帧
            cv2.imwrite(str(self.output_dir / f"{name_without_ext}_latest.jpg"), processed_frame)
            if writer is not None:
                writer.write(processed_frame)
            frame_count += 1
        cap.release()
        if writer is not None:
            writer.release()
            print("视频保存完成，保存路径：", out_video_path)
        else:
            print("处理结束：" + video_file)

    def process_video_capture(self, camera_index):
        """
        实时视频流/摄像头检测
        """
        cap = cv2.VideoCapture(camera_index)
        if not cap.isOpened():
            print(f"无法打开摄像头或视频流：{camera_index}")
            return
        fps = cap.get(cv2.CAP_PROP_FPS)
        if fps <= 0:
            fps = 25  # 默认帧率
        width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
        height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
        writer = None
        if self.args.save_video:
            out_video_path = self.output_dir / "output.mp4"
            fourcc = cv2.VideoWriter_fourcc(*'mp4v')
            writer = cv2.VideoWriter(str(out_video_path), fourcc, fps, (width, height))
        frame_count = 0
        stop_signal_path = self.output_dir / "stop.txt"
        while cap.isOpened():
            ret, frame = cap.read()
            if not ret:
                break
            processed_frame = self.process_frame(frame, frame_count)
            cv2.imwrite(str(self.output_dir / "latest.jpg"), processed_frame)
            if writer is not None:
                writer.write(processed_frame)
            frame_count += 1
            if stop_signal_path.exists():
                break
            time.sleep(0.01)  # 短暂休眠降低 CPU 占用
        cap.release()
        if writer is not None:
            writer.release()
            print("视频保存完成，保存路径：", out_video_path)
        else:
            print("检测结束！")

    def process_images_folder(self):
        """
        批量处理图像文件（非实时模式）
        """
        image_extensions = ('.jpg', '.jpeg', '.png')
        image_files = [
            os.path.join(self.args.source, f)
            for f in os.listdir(self.args.source)
            if f.lower().endswith(image_extensions)
        ]
        if not image_files:
            print(f"目录 {self.args.source} 中没有找到图片文件")
            return
        for image_file in image_files:
            print(f"正在处理图片文件： {image_file}")
            image = cv2.imread(image_file)
            if image is None:
                print(f"无法读取图片: {image_file}")
                continue
            processed_image = self.process_frame(image)
            # 保存处理后的图片，以 _output.jpg 结尾
            base_name = os.path.basename(image_file)
            name_without_ext = os.path.splitext(base_name)[0]
            cv2.imwrite(str(self.output_dir / f"{name_without_ext}_output.jpg"), processed_image)
        print("所有图片处理完成。")

    def process_source(self):
        """
        根据 --source 和 --mode 参数判断：
          - 如果 mode 为 video，则：
              * 如果传入的是目录，则批量遍历视频文件（特定扩展名）；
              * 如果传入的是单个视频文件，则直接处理；
              * 否则尝试将其作为摄像头设备（实时流）处理。
          - 如果 mode 为 image，则：
              * 如果传入的是目录，则批量遍历目录内的图片文件；
              * 如果传入的是单个图片文件，则直接处理该图像。
        """
        if self.args.mode == "video":
            if os.path.isdir(self.args.source):
                video_extensions = ('.mp4', '.avi', '.mov', '.mkv')
                video_files = [
                    os.path.join(self.args.source, f)
                    for f in os.listdir(self.args.source)
                    if f.lower().endswith(video_extensions)
                ]
                if not video_files:
                    print(f"目录 {self.args.source} 中没有找到视频文件")
                    return
                for video_file in video_files:
                    print(f"正在处理视频文件： {video_file}")
                    self.process_video_file(video_file)
                print("所有视频处理完成。")
            elif os.path.isfile(self.args.source):
                print(f"正在处理单个视频文件： {self.args.source}")
                self.process_video_file(self.args.source)
            else:
                try:
                    camera_index = int(self.args.source)
                    print(f"检测到摄像头设备编号： {camera_index}")
                    self.process_video_capture(camera_index)
                except ValueError:
                    if os.path.exists(self.args.source):
                        print(f"检测到视频文件： {self.args.source}")
                        self.process_video_file(self.args.source)
                    else:
                        print(f"无法识别输入源：{self.args.source}")
        elif self.args.mode == "image":
            if os.path.isdir(self.args.source):
                self.process_images_folder()
            elif os.path.isfile(self.args.source):
                print(f"正在处理单个图片文件： {self.args.source}")
                image = cv2.imread(self.args.source)
                if image is None:
                    print(f"无法读取图片文件：{self.args.source}")
                    return
                processed_image = self.process_frame(image)
                base_name = os.path.basename(self.args.source)
                name_without_ext = os.path.splitext(base_name)[0]
                cv2.imwrite(str(self.output_dir / f"{name_without_ext}_output.jpg"), processed_image)
                print("图像处理完成。")
            else:
                print(f"无法识别输入源：{self.args.source}")
        else:
            print(f"未知的模式: {self.args.mode}")

    def process_videos_folder(self):
        # 可根据需要实现非实时视频处理的其他逻辑
        pass

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="车辆及车牌检测")
    parser.add_argument("--model", type=str, default="yolo11n.pt",
                        help="YOLO模型权重文件路径")
    parser.add_argument("--source", type=str, required=True,
                        help="输入源（单个文件、文件夹路径，或摄像头设备编号）")
    parser.add_argument("--project", type=str, required=True,
                        help="检测结果输出父目录")
    parser.add_argument("--name", type=str, required=True,
                        help="检测结果文件夹名称前缀（例如 imageExp 或 videoExp）")
    parser.add_argument("--conf", type=float, default=0.7,
                        help="YOLO置信度阈值")
    parser.add_argument("--txt", action="store_true",
                        help="启用TXT结果输出")
    parser.add_argument("--json", action="store_true",
                        help="启用JSON结果输出")
    parser.add_argument("--save-video", action="store_true",
                        help="保存视频检测结果")
    parser.add_argument("--show", action="store_true",
                        help="实时显示检测过程")
    # 新增一个参数用于区分检测模式：video 或 image
    parser.add_argument("--mode", type=str, default="video",
                        help="检测模式：video 或 image")
    args = parser.parse_args()

    processor = LicensePlateProcessor(args)
    processor.process_source()