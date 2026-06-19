import json
import os
import sys
from pathlib import Path

def collect_strings_from_json(obj, strings_set):
    """
    递归遍历JSON对象，提取所有字符串值并添加到集合中。
    """
    if isinstance(obj, dict):
        for value in obj.values():
            collect_strings_from_json(value, strings_set)
    elif isinstance(obj, list):
        for item in obj:
            collect_strings_from_json(item, strings_set)
    elif isinstance(obj, str):
        strings_set.add(obj)
    # 其他类型（数字、布尔、None）忽略

def process_lang(lang_key):
    """
    处理指定语言目录，提取所有字符串并生成常量文件。
    """
    source_dir = Path(f"./{lang_key}")
    if not source_dir.exists() or not source_dir.is_dir():
        print(f"错误：目录 './{lang_key}' 不存在或不是目录。")
        sys.exit(1)

    all_strings = set()

    # 递归查找所有 .json 文件
    json_files = list(source_dir.rglob("*.json"))
    if not json_files:
        print(f"警告：在 './{lang_key}' 中没有找到任何 JSON 文件。")
        return

    for json_file in json_files:
        try:
            with open(json_file, 'r', encoding='utf-8') as f:
                data = json.load(f)
            collect_strings_from_json(data, all_strings)
            print(f"已处理：{json_file}")
        except (json.JSONDecodeError, IOError) as e:
            print(f"跳过文件 {json_file}：{e}")

    # 生成常量映射：每个字符串作为 key 和 value
    constants = {s: s for s in all_strings}

    output_file = Path(f"./{lang_key}_constants.json")
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(constants, f, indent=2, ensure_ascii=False)

    print(f"\n完成！共提取 {len(all_strings)} 个唯一字符串。")
    print(f"输出文件：{output_file}")

def main():
    if len(sys.argv) > 1:
        lang_key = sys.argv[1]
    else:
        lang_key = input("请输入语言键（例如 zh-CN, en）：").strip()
        if not lang_key:
            print("错误：语言键不能为空。")
            sys.exit(1)

    process_lang(lang_key)

if __name__ == "__main__":
    main()