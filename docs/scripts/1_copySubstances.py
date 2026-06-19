import json
import os
import sys
from pathlib import Path

# 需要提取的字段列表
FIELDS_TO_EXTRACT = [
    "tolerance",
    "addictionPotential",
    "toxicities",
    "summary",
    "effectsSummary",
    "dosageRemark",
    "generalRisks",
    "longtermRisks",
    "saferUse"
]

def process_files(lang_key: str, source_dir: str = "root", target_base: str = ".") -> None:
    """
    从 source_dir 中读取所有 .json 文件（排除 _categories.json），
    提取指定字段并添加 localizedName，输出到 target_base/lang_key/ 下。

    Args:
        lang_key: 语言键，用于创建子目录
        source_dir: 源目录，默认为 "root"
        target_base: 目标根目录，默认为当前目录
    """
    source_path = Path(source_dir)
    target_dir = Path(target_base) / lang_key
    target_dir.mkdir(parents=True, exist_ok=True)

    if not source_path.exists() or not source_path.is_dir():
        print(f"错误：源目录 '{source_dir}' 不存在或不是目录。")
        sys.exit(1)

    json_files = [f for f in source_path.glob("*.json") if f.name != "_categories.json"]
    if not json_files:
        print(f"警告：在 '{source_dir}' 中没有找到需要处理的 JSON 文件（已排除 _categories.json）。")
        return

    for json_file in json_files:
        try:
            with open(json_file, 'r', encoding='utf-8') as f:
                data = json.load(f)
        except (json.JSONDecodeError, IOError) as e:
            print(f"跳过文件 '{json_file}'：读取或解析失败 - {e}")
            continue

        if not isinstance(data, dict):
            print(f"跳过文件 '{json_file}'：内容不是 JSON 对象。")
            continue

        # 提取指定字段（只提取存在的字段）
        extracted = {}
        for field in FIELDS_TO_EXTRACT:
            if field in data:
                extracted[field] = data[field]

        # 添加 localizedName，值复制自原 name 字段
        original_name = data.get("name")
        if original_name is not None:
            extracted["localizedName"] = original_name
        else:
            print(f"警告：文件 '{json_file}' 中没有 'name' 字段，localizedName 将不会被添加。")

        # 输出到目标目录
        output_file = target_dir / json_file.name
        try:
            with open(output_file, 'w', encoding='utf-8') as f:
                json.dump(extracted, f, indent=2, ensure_ascii=False)
            print(f"已生成: {output_file}")
        except IOError as e:
            print(f"写入文件失败 '{output_file}'：{e}")

def main():
    # 从命令行参数或用户输入获取 langKey
    if len(sys.argv) > 1:
        lang_key = sys.argv[1]
    else:
        lang_key = input("请输入 langKey（如 zh-CN, en 等）: ").strip()
        if not lang_key:
            print("错误：langKey 不能为空。")
            sys.exit(1)

    process_files(lang_key)

if __name__ == "__main__":
    main()