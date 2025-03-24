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

PROVINCE_ABBR = [
    '京', '沪', '津', '渝', '冀', '晋', '辽', '吉', '黑',
    '苏', '浙', '皖', '闽', '赣', '鲁', '豫', '鄂', '湘',
    '粤', '琼', '川', '贵', '云', '陕', '甘', '青', '台',
    '蒙', '桂', '宁', '新', '藏', '港', '澳'
]

class LicensePlateProcessor:
    def __init__(self, args):
        self.args = args
        # 加载YOLO模型
        self.yolo_model = YOLO(args.model)
        # 配置 PaddleOCR（用于车牌识别）
        self.paddle_ocr = PaddleOCR(
            use_angle_cls=True,
            lang="ch",
            rec_algorithm="SVTR_LCNet",
            det_db_score_mode="fast"
        )
        # 配置输出目录、结果文件
        self.setup_output()

        # 这里指定中文字体路径（需要保证相应字体存在）
        self.font_path = "C:/Windows/Fonts/simhei.ttf"
        if not os.path.exists(self.font_path):
            raise FileNotFoundError(f"Chinese font not found: {self.font_path}")
        self.font = ImageFont.truetype(self.font_path, 20)

    def setup_output(self):
        """
        根据 --project 和 --name 参数以及时间戳创建输出目录，
        并初始化 JSON/TXT 结果文件保存路径。
        """
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        self.output_dir = Path(self.args.project) / f"{self.args.name}_{timestamp}"
        self.output_dir.mkdir(parents=True, exist_ok=True)
        self.txt_path = self.output_dir / f"results_{timestamp}.txt"
        self.json_path = self.output_dir / f"detections_{timestamp}.json"

    def is_valid_plate(self, text):
        """验证车牌号是否符合中文车牌格式"""
        clean_text = text.replace("·", "").replace(" ", "")
        pattern = r"^([" + "".join(PROVINCE_ABBR) + r"])[A-HJ-NP-Z][0-9A-HJ-NP-Z]{5,6}$"
        return re.match(pattern, clean_text) and clean_text[:1] in PROVINCE_ABBR

    def process_frame(self, frame, frame_count=0):
        """
        检测单帧图像，对检测到的目标进行车牌识别，
        并调用保存结果函数写入 JSON/TXT 文件，同时在图像上绘制标注。
        """
        results = self.yolo_model(frame, conf=self.args.conf, imgsz=640)
        for result in results:
            # result.boxes 提供所有目标检测的边界框、类别和置信度信息
            for box, cls, conf in zip(result.boxes.xyxy,
                                      result.boxes.cls,
                                      result.boxes.conf):
                # 解析坐标
                x1, y1, x2, y2 = map(int, box.tolist())
                x1 = max(0, x1)
                y1 = max(0, y1)
                x2 = min(frame.shape[1], x2)
                y2 = min(frame.shape[0], y2)

                # 车牌识别
                vehicle_roi = frame[y1:y2, x1:x2]
                best_plate = self.process_roi(vehicle_roi)

                # 保存检测结果（直接用 YOLO 结果中的类别名称，不再调用 getTypeNameByClassId）
                self.save_results(frame_count, result.names[int(cls)],
                                  conf.item(), (x1, y1, x2, y2), best_plate)

                # 绘制检测标注
                frame = self.draw_annotations(frame, (x1, y1, x2, y2),
                                              result.names[int(cls)], best_plate)
        return frame

    def process_roi(self, roi):
        """处理车牌区域，利用 OCR 识别车牌"""
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
        """
        将检测记录以 JSON（及 TXT 可选）格式写入结果文件，
        记录格式示例：

        {
          "timestamp": "2025-03-21 19:02:15.903",
          "frame": 0,
          "type": "car",
          "confidence": 0.809,
          "coordinates": [5, 144, 1073, 893],
          "license_plate": "京NQ4163"
        }
        """
        timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]
        record = {
            "timestamp": timestamp,
            "frame": frame_id,
            "type": vehicle_type,
            "confidence": round(conf, 3),
            "coordinates": list(map(int, coords)),
            "license_plate": license_plate
        }

        # 保存TXT结果（如果启用了 --txt）
        if self.args.txt:
            with open(self.txt_path, "a", encoding="utf-8") as f:
                f.write(f"{json.dumps(record, ensure_ascii=False)}\n")

        # 保存JSON结果（如果启用了 --json）
        if self.args.json:
            with open(self.json_path, "a", encoding="utf-8") as f:
                f.write(f"{json.dumps(record, ensure_ascii=False, indent=2)},\n")

    def draw_annotations(self, frame, coords, vehicle_type, license_plate):
        """
        在图像上绘制边框和结果标注（使用 PIL 绘图支持中文显示）
        """
        x1, y1, x2, y2 = coords
        pil_img = Image.fromarray(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
        draw = ImageDraw.Draw(pil_img)
        draw.rectangle([x1, y1, x2, y2], outline=(0, 255, 0), width=2)
        label = f"{vehicle_type}: {license_plate}"
        text_y = y1 - 25 if y1 > 30 else y1 + 5
        draw.text((x1, text_y), label, font=self.font, fill=(0, 255, 0))
        return cv2.cvtColor(np.array(pil_img), cv2.COLOR_RGB2BGR)

    def process_images_folder(self):
        """处理输入目录下所有图像文件，检测后保存输出图像，并删除原始输入文件"""
        image_extensions = {'.png', '.jpg', '.jpeg', '.bmp'}
        input_folder = Path(self.args.source)
        files = list(input_folder.glob("*"))
        if not files:
            print("未发现输入图像文件于目录：", input_folder)
            return

        frame_count = 0
        for file_path in files:
            if file_path.suffix.lower() in image_extensions:
                print("处理图像文件：", file_path)
                frame = cv2.imread(str(file_path))
                if frame is None:
                    print("读取图像失败：", file_path)
                    continue
                processed_frame = self.process_frame(frame, frame_count)
                # 输出文件名与原始文件名保持一致，也可根据需要增加前缀
                output_path = self.output_dir / file_path.name
                cv2.imwrite(str(output_path), processed_frame)
                print("保存检测结果图像：", output_path)
                frame_count += 1
                # 删除源文件
                try:
                    file_path.unlink()
                    print("删除输入图像文件：", file_path)
                except Exception as e:
                    print("删除文件失败：", file_path, e)

    def process_videos_folder(self):
        """处理输入目录下所有视频文件，逐帧检测、生成输出视频，并删除原始视频文件"""
        video_extensions = {'.mp4', '.avi', '.mov', '.mkv'}
        input_folder = Path(self.args.source)
        files = list(input_folder.glob("*"))
        if not files:
            print("未发现输入视频文件于目录：", input_folder)
            return

        for file_path in files:
            if file_path.suffix.lower() in video_extensions:
                print("处理视频文件：", file_path)
                cap = cv2.VideoCapture(str(file_path))
                fps = cap.get(cv2.CAP_PROP_FPS)
                width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
                height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
                output_video_path = self.output_dir / file_path.name
                fourcc = cv2.VideoWriter_fourcc(*'mp4v')
                writer = cv2.VideoWriter(str(output_video_path), fourcc, fps, (width, height))
                frame_count = 0
                while cap.isOpened():
                    ret, frame = cap.read()
                    if not ret:
                        break
                    processed_frame = self.process_frame(frame, frame_count)
                    writer.write(processed_frame)
                    frame_count += 1
                cap.release()
                writer.release()
                print("保存检测结果视频：", output_video_path)
                # 删除原始视频文件
                try:
                    file_path.unlink()
                    print("删除输入视频文件：", file_path)
                except Exception as e:
                    print("删除文件失败：", file_path, e)

    def process_source(self):
        """
        根据输入源类型分情况处理：
          - 若 --source 为目录，则遍历目录内文件：
              • 如果指定 --save-video 为 True，则视为视频处理；
              • 否则作为图像处理。
          - 如果为单个文件，则根据文件后缀判断。
        """
        if os.path.isdir(self.args.source):
            if self.args.save_video:
                self.process_videos_folder()
            else:
                self.process_images_folder()
        else:
            # 若为单个文件，则按后缀判断处理类型
            lower_source = self.args.source.lower()
            if lower_source.endswith(('.png', '.jpg', '.jpeg', '.bmp')):
                frame = cv2.imread(self.args.source)
                processed_frame = self.process_frame(frame)
                output_path = self.output_dir / "output.jpg"
                cv2.imwrite(str(output_path), processed_frame)
                print("保存检测结果图像：", output_path)
            else:
                cap = cv2.VideoCapture(self.args.source)
                fps = cap.get(cv2.CAP_PROP_FPS)
                width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
                height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
                output_video_path = self.output_dir / "output.mp4"
                fourcc = cv2.VideoWriter_fourcc(*'mp4v')
                writer = cv2.VideoWriter(str(output_video_path), fourcc, fps, (width, height))
                frame_count = 0
                while cap.isOpened():
                    ret, frame = cap.read()
                    if not ret:
                        break
                    processed_frame = self.process_frame(frame, frame_count)
                    writer.write(processed_frame)
                    frame_count += 1
                cap.release()
                writer.release()
                print("保存检测结果视频：", output_video_path)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="车辆及车牌检测")
    parser.add_argument("--model", type=str, default="yolo11n.pt",
                        help="YOLO模型权重文件路径")
    parser.add_argument("--source", type=str, required=True,
                        help="输入源（单个文件或文件夹路径）")
    parser.add_argument("--project", type=str, required=True,
                        help="检测结果输出父目录")
    parser.add_argument("--name", type=str, required=True,
                        help="检测结果文件夹名称前缀（如 imageExp 或 videoExp）")
    parser.add_argument("--conf", type=float, default=0.7,
                        help="YOLO置信度阈值")
    parser.add_argument("--txt", action="store_true",
                        help="启用TXT结果输出")
    parser.add_argument("--json", action="store_true",
                        help="启用JSON结果输出")
    parser.add_argument("--save-video", action="store_true",
                        help="保存视频检测结果（视频检测时使用）")
    parser.add_argument("--show", action="store_true",
                        help="实时显示检测过程")
    args = parser.parse_args()

    processor = LicensePlateProcessor(args)
    processor.process_source()