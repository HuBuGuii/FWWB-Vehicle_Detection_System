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

PROVINCE_ABBR = [
    '京', '沪', '津', '渝', '冀', '晋', '辽', '吉', '黑',
    '苏', '浙', '皖', '闽', '赣', '鲁', '豫', '鄂', '湘',
    '粤', '琼', '川', '贵', '云', '陕', '甘', '青', '台',
    '蒙', '桂', '宁', '新', '藏', '港', '澳'
]

class LicensePlateProcessor:
    def __init__(self, args):
        self.args = args
        # 加载 YOLO 模型
        self.yolo_model = YOLO(args.model)
        # 配置 PaddleOCR（用于车牌识别）
        self.paddle_ocr = PaddleOCR(
            use_angle_cls=True,
            lang="ch",
            rec_algorithm="SVTR_LCNet",
            det_db_score_mode="fast"
        )
        # 注意：不再自动拼接时间戳，直接使用传入的 project 与 name 生成输出目录
        self.setup_output()
        # 指定中文字体路径，注意确认该路径下有相应字体
        self.font_path = "C:/Windows/Fonts/simhei.ttf"
        if not os.path.exists(self.font_path):
            raise FileNotFoundError(f"Chinese font not found: {self.font_path}")
        self.font = ImageFont.truetype(self.font_path, 20)

    def setup_output(self):
        """
        直接使用传入的 --project 与 --name 构建输出目录，
        使其与 Java 端创建的文件夹保持一致。
        """
        self.output_dir = Path(self.args.project) / self.args.name
        self.output_dir.mkdir(parents=True, exist_ok=True)
        self.txt_path = self.output_dir / "results.txt"
        self.json_path = self.output_dir / "detections.json"

    def is_valid_plate(self, text):
        clean_text = text.replace("·", "").replace(" ", "")
        pattern = r"^([" + "".join(PROVINCE_ABBR) + r"])[A-HJ-NP-Z][0-9A-HJ-NP-Z]{5,6}$"
        return re.match(pattern, clean_text) and clean_text[:1] in PROVINCE_ABBR

    def process_frame(self, frame, frame_count=0):
        results = self.yolo_model(frame, conf=self.args.conf, imgsz=640)
        for result in results:
            for box, cls, conf in zip(result.boxes.xyxy,
                                      result.boxes.cls,
                                      result.boxes.conf):
                # 解析目标检测框坐标
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

    def process_source(self):
        """
        如果输入源是目录，则进行批量处理；
        否则（典型为摄像头设备数字，例如 "0"），进入实时采集模式，
        实时处理每一帧，保存为 jpg 文件（供 Java 获取）及视频文件（如果启用了 --save-video）。
        """
        # 针对实时检测，认为 args.source 为摄像头编号，尽量转换为 int
        try:
            camera_index = int(self.args.source)
        except ValueError:
            camera_index = self.args.source

        cap = cv2.VideoCapture(camera_index)
        fps = cap.get(cv2.CAP_PROP_FPS)
        if fps <= 0:
            fps = 25  # 默认帧率
        width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
        height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))

        # 若启用了视频保存，则创建 VideoWriter（输出文件为 output.mp4）
        writer = None
        if self.args.save_video:
            self.output_video_path = self.output_dir / "output.mp4"
            fourcc = cv2.VideoWriter_fourcc(*'mp4v')
            writer = cv2.VideoWriter(str(self.output_video_path), fourcc, fps, (width, height))

        frame_count = 0
        stop_signal_path = self.output_dir / "stop.txt"
        while cap.isOpened():
            ret, frame = cap.read()
            if not ret:
                break
            processed_frame = self.process_frame(frame, frame_count)
            # 每次覆盖最新图像，供 Java 端读取
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
            print("视频保存完成，保存路径：", self.output_video_path)
        else:
            print("检测结束！")

    def process_images_folder(self):
        # 图像批量处理（非实时模式）可另行实现
        pass

    def process_videos_folder(self):
        # 视频批量处理（非实时模式）可另行实现
        pass

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="车辆及车牌检测")
    parser.add_argument("--model", type=str, default="yolo11n.pt",
                        help="YOLO模型权重文件路径")
    parser.add_argument("--source", type=str, required=True,
                        help="输入源（单个文件或文件夹路径，或摄像头设备编号）")
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
                        help="保存视频检测结果（实时模式下有效）")
    parser.add_argument("--show", action="store_true",
                        help="实时显示检测过程")
    args = parser.parse_args()

    processor = LicensePlateProcessor(args)
    processor.process_source()