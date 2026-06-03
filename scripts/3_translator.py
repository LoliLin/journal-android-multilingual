import json
import time
import requests
import sys
import argparse
from pathlib import Path

# ========== 默认配置 ==========
DEFAULT_MODEL = "deepseek-chat"
T_DEFAULT_DELAY = 0.1          # 请求间隔秒数
DEFAULT_MAX_RETRIES = 3      # 单个文本重试次数
DEFAULT_RETRY_DELAY = 2      # 重试等待秒数

def translate_text(text: str, api_key: str, target_lang: str, model: str = DEFAULT_MODEL) -> str:
    """调用 DeepSeek API 翻译单个文本，返回目标语言结果。"""
    if not text or not text.strip():
        return ""

    system_prompt = (
        f"你是一个专业的翻译助手。请将用户提供的文本逐字逐句翻译成{target_lang}。"
        "不要添加任何额外的解释、警告、评论或拒绝翻译。"
        "如果文本包含专业术语（包括药物名称、化学物质等），请采用公认的译名。"
        "只输出翻译结果，不要输出任何其他内容。"
    )

    messages = [
        {"role": "system", "content": system_prompt},
        {"role": "user",   "content": f"请将以下文本翻译成{target_lang}：\n{text}"}
    ]

    payload = {
        "model": model,
        "messages": messages,
        "temperature": 0.3,
        "max_tokens": 2048,
        "stream": False
    }

    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json"
    }

    for attempt in range(DEFAULT_MAX_RETRIES):
        try:
            resp = requests.post("https://api.deepseek.com/v1/chat/completions",
                                 json=payload, headers=headers, timeout=60)
            if resp.status_code == 200:
                result = resp.json()
                translated = result["choices"][0]["message"]["content"].strip()
                # 简单检测是否包含拒绝特征
                if any(skip in translated.lower() for skip in ["sorry", "无法翻译", "拒绝"]):
                    if attempt < DEFAULT_MAX_RETRIES - 1:
                        time.sleep(DEFAULT_RETRY_DELAY)
                        continue
                return translated
            else:
                print(f"API 错误 {resp.status_code}: {resp.text}")
                if attempt < DEFAULT_MAX_RETRIES - 1:
                    time.sleep(DEFAULT_RETRY_DELAY)
                else:
                    return f"[翻译失败] {text}"
        except Exception as e:
            print(f"请求异常: {e}")
            if attempt < DEFAULT_MAX_RETRIES - 1:
                time.sleep(DEFAULT_RETRY_DELAY)
            else:
                return f"[翻译失败] {text}"

    return f"[翻译失败] {text}"

def main():
    parser = argparse.ArgumentParser(description="使用 DeepSeek API 翻译 JSON 常量文件")
    parser.add_argument("--api-key", required=True, help="DeepSeek API Key")
    parser.add_argument("--input", required=True, help="输入 JSON 文件路径（例如 zh_cn_constants.json）")
    parser.add_argument("--target-lang", default="简体中文", help="目标语言，如 简体中文、繁体中文、日本語、Deutsch 等（默认：简体中文）")
    parser.add_argument("--output", help="输出文件路径（可选，默认在输入文件名后加 _translated）")
    parser.add_argument("--delay", type=float, default=0.1, help="每次请求间隔秒数（默认 0.1）")
    args = parser.parse_args()

    input_path = Path(args.input)
    if not input_path.exists():
        print(f"错误：文件 {input_path} 不存在！")
        sys.exit(1)

    with open(input_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    if not isinstance(data, dict):
        print("错误：JSON 文件根元素不是对象。")
        sys.exit(1)

    # 确定输出文件路径
    if args.output:
        output_path = Path(args.output)
    else:
        output_path = input_path.parent / f"{input_path.stem}_translated{input_path.suffix}"

    total = len(data)
    translated_data = {}
    print(f"开始翻译，共 {total} 个项目，目标语言：{args.target_lang}")

    # 临时备份原 API Key 和延迟设置（全局变量修改）
    T_DEFAULT_DELAY = args.delay

    for idx, (key, value) in enumerate(data.items(), 1):
        original_text = value  # 键和值相同
        print(f"[{idx}/{total}] 正在翻译: {original_text[:50]}...")

        translated = translate_text(original_text, args.api_key, args.target_lang)
        translated_data[key] = translated
        print(f"> {translated}")

        time.sleep(T_DEFAULT_DELAY)

    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(translated_data, f, indent=2, ensure_ascii=False)

    print(f"\n翻译完成！结果已保存至 {output_path}")

if __name__ == "__main__":
    main()