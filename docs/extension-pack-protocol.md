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
  "2": {"versionName": "2.0.0", "url": "https://example.com/ext_v2.0.0.zip"}
}
```

key 为 `versionCode`，系统比较后显示更新按钮。

## 加载机制（Hook）

1. **i18n**：`i18n/` 下的 JSON 被读入 `I18n.setOverride()`，优先于内置翻译
2. **Substances**：`substances/<lang>/` 下的 JSON 被 SubstanceRepository 的扩展路径加载，与内置物质合并

## 创建扩展包

1. 创建上述目录结构 + `manifest.json`
2. 将 **目录内所有文件** 打包为 `.zip`（不要包含外层文件夹）
3. 在应用设置 → Extension pack → 导入 中选择该 ZIP
