import json
import os
import shutil
import sys
from pathlib import Path

def replace_strings_in_json(obj, mapping):
    """
    递归遍历JSON对象，将所有字符串值根据映射进行替换。
    返回新的对象（不修改原对象）。
    """
    if isinstance(obj, dict):
        new_dict = {}
        for k, v in obj.items():
            # 键名不替换，只替换值
            new_dict[k] = replace_strings_in_json(v, mapping)
        return new_dict
    elif isinstance(obj, list):
        return [replace_strings_in_json(item, mapping) for item in obj]
    elif isinstance(obj, str):
        # 如果字符串在映射中，返回映射后的值；否则返回原字符串
        return mapping.get(obj, obj)
    else:
        # 数字、布尔、null 保持不变
        return obj

def process_lang(lang_key, output_suffix="_replaced", in_place=True):
    """
    根据常量映射替换语言目录中的字符串。
    
    Args:
        lang_key: 语言键（如 zh-CN）
        output_suffix: 输出目录后缀，默认为 "_replaced"
        in_place: 是否原地修改（True则覆盖原文件，False则输出到新目录）
    """
    constants_file = Path(f"./{lang_key}_constants.json")
    if not constants_file.exists():
        print(f"错误：常量文件 '{constants_file}' 不存在。")
        sys.exit(1)

    # 读取映射
    try:
        with open(constants_file, 'r', encoding='utf-8') as f:
            mapping = json.load(f)
        if not isinstance(mapping, dict):
            print("错误：常量文件内容不是 JSON 对象。")
            sys.exit(1)
        print(f"已加载映射，共 {len(mapping)} 个条目。")
    except (json.JSONDecodeError, IOError) as e:
        print(f"读取常量文件失败：{e}")
        sys.exit(1)

    source_dir = Path(f"./{lang_key}")
    if not source_dir.exists() or not source_dir.is_dir():
        print(f"错误：源目录 './{lang_key}' 不存在或不是目录。")
        sys.exit(1)

    # 确定输出目录
    if in_place:
        output_dir = source_dir
        print(f"警告：将原地修改文件（不可恢复）！")
    else:
        output_dir = Path(f"./{lang_key}{output_suffix}")
        if output_dir.exists():
            # 可选：清空已有目录，或询问用户
            shutil.rmtree(output_dir)
        output_dir.mkdir(parents=True, exist_ok=True)
        print(f"输出目录：{output_dir}")

    # 递归查找所有 JSON 文件
    json_files = list(source_dir.rglob("*.json"))
    if not json_files:
        print(f"警告：在 './{lang_key}' 中没有找到任何 JSON 文件。")
        return

    replaced_count = 0
    for json_file in json_files:
        try:
            with open(json_file, 'r', encoding='utf-8') as f:
                data = json.load(f)

            # 替换字符串
            new_data = replace_strings_in_json(data, mapping)

            # 确定输出路径（保持相对路径结构）
            rel_path = json_file.relative_to(source_dir)
            if in_place:
                out_file = json_file
            else:
                out_file = output_dir / rel_path
                out_file.parent.mkdir(parents=True, exist_ok=True)

            with open(out_file, 'w', encoding='utf-8') as f:
                json.dump(new_data, f, indent=2, ensure_ascii=False)
            replaced_count += 1
            print(f"已处理：{json_file} -> {out_file}")
        except (json.JSONDecodeError, IOError) as e:
            print(f"跳过文件 {json_file}：{e}")

    print(f"\n完成！共处理 {replaced_count} 个文件。")

def main():
    if len(sys.argv) > 1:
        lang_key = sys.argv[1]
    else:
        lang_key = input("请输入语言键（例如 zh-CN, en）：").strip()
        if not lang_key:
            print("错误：语言键不能为空。")
            sys.exit(1)

    # 可选：添加 --in-place 参数支持原地修改
    in_place = "--in-place" in sys.argv or "-i" in sys.argv
    process_lang(lang_key, in_place=in_place)

if __name__ == "__main__":
    main()