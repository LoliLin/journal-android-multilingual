import json
import os
import re
import sys
from pathlib import Path

def sanitize_filename(name: str) -> str:
    """将字符串中的非法文件名字符替换为下划线"""
    return re.sub(r'[\\/*?:"<>|]', '_', name)

def split_json(input_file: str, output_dir: str = "root") -> None:
    """
    读取JSON文件，提取categories和substances并分别输出。

    Args:
        input_file: 输入的JSON文件路径
        output_dir: 输出根目录，默认为"root"
    """
    # 读取原始JSON
    try:
        with open(input_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
    except FileNotFoundError:
        print(f"错误：文件 '{input_file}' 不存在。")
        sys.exit(1)
    except json.JSONDecodeError as e:
        print(f"错误：JSON解析失败 - {e}")
        sys.exit(1)

    # 创建输出目录
    os.makedirs(output_dir, exist_ok=True)

    # 1. 提取 categories 并输出到 _categories.json
    if "categories" in data:
        categories_output = os.path.join(output_dir, "_categories.json")
        with open(categories_output, 'w', encoding='utf-8') as f:
            json.dump(data["categories"], f, indent=2, ensure_ascii=False)
        print(f"已输出 categories -> {categories_output}")
    else:
        print("警告：输入JSON中未找到 'categories' 字段，跳过输出。")

    # 2. 提取 substances 数组，按 name 输出
    if "substances" in data and isinstance(data["substances"], list):
        substances = data["substances"]
        for idx, obj in enumerate(substances):
            if not isinstance(obj, dict):
                print(f"警告：substances[{idx}] 不是对象，跳过。")
                continue
            name = obj.get("name")
            if not name:
                print(f"警告：substances[{idx}] 缺少 'name' 字段，跳过。")
                continue
            safe_name = sanitize_filename(str(name))
            file_path = os.path.join(output_dir, f"{safe_name}.json")
            with open(file_path, 'w', encoding='utf-8') as f:
                json.dump(obj, f, indent=2, ensure_ascii=False)
            print(f"已输出 substance '{name}' -> {file_path}")
    else:
        print("警告：输入JSON中未找到 'substances' 字段或其不是数组，跳过输出。")

if __name__ == "__main__":
    # 用法：python script.py input.json
    if len(sys.argv) < 2:
        print("用法: python split_json.py <input_json_file>")
        sys.exit(1)
    input_path = sys.argv[1]
    split_json(input_path)