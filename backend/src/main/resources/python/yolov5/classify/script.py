import os
import shutil

# 设置路径
root_dir = 'D:\Work\Competition\ServiceOutsourcingCompetition 2025\\test\yolo_startup\datasets\A10'
images_dir = os.path.join(root_dir, 'images')
labels_dir = os.path.join(root_dir, 'labels')

# 定义子文件夹
subfolders = ['train', 'val']
new_images_dir = os.path.join(root_dir, 'new_images')  # 新的图片存储位置
new_labels_dir = os.path.join(root_dir, 'new_labels')  # 新的标签存储位置
class_subfolders = ['class_1', 'class_2']  # 根据实际类别定义

# 创建新的文件夹结构
def create_new_dirs():
    for subfolder in subfolders:
        # 创建新的 train 和 val 目录
        new_subfolder_images = os.path.join(new_images_dir, subfolder)
        new_subfolder_labels = os.path.join(new_labels_dir, subfolder)
        
        if not os.path.exists(new_subfolder_images):
            os.makedirs(new_subfolder_images)
        if not os.path.exists(new_subfolder_labels):
            os.makedirs(new_subfolder_labels)
        
        # 为每个类别创建新的子目录
        for class_name in class_subfolders:
            class_image_folder = os.path.join(new_subfolder_images, class_name)
            if not os.path.exists(class_image_folder):
                os.makedirs(class_image_folder)

            class_label_folder = os.path.join(new_subfolder_labels, class_name)
            if not os.path.exists(class_label_folder):
                os.makedirs(class_label_folder)

# 复制图片和标签到新的文件夹
def copy_images_and_labels():
    for subfolder in subfolders:
        # 获取原图片和标签的路径
        image_subfolder = os.path.join(images_dir, subfolder)
        label_subfolder = os.path.join(labels_dir, subfolder)

        # 遍历标签文件夹中的文件
        for label_file in os.listdir(label_subfolder):
            if label_file.endswith('.txt'):
                label_path = os.path.join(label_subfolder, label_file)
                with open(label_path, 'r') as f:
                    lines = f.readlines()
                    for line in lines:
                        img_name, class_id = line.strip().split()
                        img_path = os.path.join(image_subfolder, img_name)
                        
                        # 新的图片类别文件夹
                        new_class_folder = os.path.join(new_images_dir, subfolder, class_subfolders[int(class_id)])
                        new_label_folder = os.path.join(new_labels_dir, subfolder, class_subfolders[int(class_id)])

                        # 复制图片到新的文件夹
                        shutil.copy(img_path, os.path.join(new_class_folder, img_name))
                        
                        # 复制标签到新的文件夹
                        new_label_path = os.path.join(new_label_folder, label_file)
                        shutil.copy(label_path, new_label_path)

                        print(f"Copied {img_name} to {new_class_folder}")
                        print(f"Copied {label_file} to {new_label_folder}")

if __name__ == '__main__':
    create_new_dirs()  # 创建新的目录
    copy_images_and_labels()  # 复制图片和标签
