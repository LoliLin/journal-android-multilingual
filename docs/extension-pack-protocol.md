# Extension Pack 协议

Extension Pack 是一个 `.zip` 文件，用于扩展 PsychonautWiki Journal 的功能。

[Get Template](https://github.com/LoliLin/journal-android-multilingual-ext_template)

## 目录结构

```
extension_pack.zip
├── manifest.json
├── icon.png                       (可选，展示用)
├── substances/
│   ├── zh_cn/                     (可选，格式同 assets/substances/)
│   │   └── MyNewDrug.json
│   └── en_us/
│       └── MyNewDrug.json
└── i18n/
    └── zh_cn.json                 (可选，格式同 assets/lang/)
```

目录结构与 `app/src/main/assets/` 下的格式**完全一致**，加载时会与原内容透明合并。

## manifest.json 格式

```json
{
  "registerName": "my_extension",
  "titleTranslateable": "ext_my_title",
  "descriptionTranslateable": "ext_my_desc",
  "officalLink": "https://example.com/my-extension",
  "updateJsonLink": "https://example.com/update.json",
  "versionName": "1.0.0",
  "versionCode": 1
}
```

### 字段说明

| 字段 | 类型 | 必需 | 说明 |
|------|------|:----:|------|
| `registerName` | string | ✅ | 唯一标识符 |
| `icon` | string | ❌ | icon的相对路径 |
| `titleTranslateable` | string | ✅ | 标题 i18n key |
| `descriptionTranslateable` | string | ✅ | 描述 i18n key |
| `officalLink` | string | ✅ | 官网链接 |
| `updateJsonLink` | string | ✅ | 更新查询 URL |
| `versionName` | string | ✅ | 版本名称 |
| `versionCode` | int | ✅ | 版本号 |

## 更新机制

`updateJsonLink` 指向的 JSON：

```json
{
  "2": {"versionName": "2.0.0", "url": "https://example.com/ext_v2.0.0.zip", "sha256": "<hex sha256 of the zip>"}
}
```

key 为 `versionCode`，系统比较后显示更新按钮。

### 安全要求（v2 协议）

- `sha256` 字段**必填**：应用下载 zip 后计算 SHA-256 并与该值比对，不一致则拒绝安装。缺少 `sha256` 的更新条目会被忽略（视为无更新）。
- `url` 与 `updateJsonLink` 必须为 `https`，否则更新检查/下载直接失败。
- 建议所有扩展包作者在发布流程中生成校验和，例如：
  ```bash
  cd 扩展包目录 && zip -r ../ext.zip . && sha256sum ../ext.zip
  ```

## 安装与更新行为

- 导入或更新**降级安装会被拒绝**：目标包的 `versionCode` 必须大于已安装版本。
- 更新流程：下载 → SHA-256 校验 → 备份现有版本 → 解压（拒绝路径穿越与超大包，上限 100MB / 2000 个文件）→ 校验 `manifest.json` → 替换。任何一步失败都会回滚到备份版本。
- 重新导入同名包会先删除旧目录再解压，避免残留文件与覆盖层合并。

## 加载机制（Hook）

1. **i18n**：`i18n/` 下的 JSON 被读入 `I18n.setOverride()`，优先于内置翻译
2. **Substances**：`substances/<lang>/` 下的 JSON 被 SubstanceRepository 的扩展路径加载，与内置物质合并

## 创建扩展包

1. 创建上述目录结构 + `manifest.json`
2. 将 **目录内所有文件** 打包为 `.zip`（不要包含外层文件夹）
3. 在应用设置 → Extension pack → 导入 中选择该 ZIP
