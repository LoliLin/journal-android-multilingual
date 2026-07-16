import json
from pathlib import Path

# 硬编码的变体 → 标准名称映射表
FIX_MAP = {
    "psychedelics": "psychedelic",
    "stimulants": "stimulant",
    "Stimulants": "stimulant",
    "entactogens": "entactogen",
    "opioids": "opioid",
    "dissociatives": "dissociative",
    "Dissociatives": "dissociative",
    "dissociative|dissociatives": "dissociative",
    "benzodiazepines": "benzodiazepine",
    "Benzodiazepines": "benzodiazepine",
    "cannabinoids": "cannabinoid",
    "nootropic|nootropics": "nootropic",
    "Deliriants": "deliriant",
    "Barbiturates": "barbiturate",
    "Antipsychotics": "antipsychotic",
    "trycyclic antidepressants": "antidepressant",
}

def fix_cross_tolerances(file_path):
    try:
        data = json.loads(file_path.read_text(encoding="utf-8"))
    except (json.JSONDecodeError, IOError) as e:
        print(f"跳过 {file_path.name}: {e}")
        return

    modified = False

    def process_entry(entry):
        nonlocal modified
        if isinstance(entry, dict) and "crossTolerances" in entry:
            old = entry["crossTolerances"]
            if isinstance(old, list):
                # 替换每个元素
                new_list = []
                for item in old:
                    item_str = str(item) if not isinstance(item, str) else item
                    replaced = FIX_MAP.get(item_str, item_str)
                    new_list.append(replaced)
                # 去重并保持顺序
                seen = set()
                deduped = []
                for val in new_list:
                    if val not in seen:
                        seen.add(val)
                        deduped.append(val)
                if deduped != old:
                    entry["crossTolerances"] = deduped
                    modified = True

    if isinstance(data, dict):
        process_entry(data)
    elif isinstance(data, list):
        for entry in data:
            process_entry(entry)

    if modified:
        file_path.write_text(json.dumps(data, indent=2, ensure_ascii=False), encoding="utf-8")
        print(f"已修复: {file_path.name}")

def main(root_dir="root"):
    root = Path(root_dir)
    if not root.is_dir():
        print(f"目录不存在: {root_dir}")
        return
    for json_file in root.glob("*.json"):
        fix_cross_tolerances(json_file)

if __name__ == "__main__":
    main(root_dir="en_us")
    main(root_dir="zh_cn")
    main(root_dir="zh_tw")
    main(root_dir="root")