# Substance 多语言翻译协议（草案）

本协议用于拆分 `substances` 的结构与文本，以便多语言翻译时只覆盖需要翻译的字段。

> 工具与操作步骤见 [`substances-pipeline.md`](substances-pipeline.md)：
> 单文件入口 `docs/scripts/substances_pipeline.py` 负责拆分、翻译、回填与校对，
> 跑 `python docs/scripts/substances_pipeline.py guide` 可打印完整流程。

## 目录结构

```
app/src/main/assets/substances/
├── root/
│   ├── _categories.json
│   ├── Pregabalin.json
│   └── ...
└── <current_Lang>/
    ├── _categories.json
    ├── Pregabalin.json
    └── ...
```

- `root/`：基础数据层（结构字段 + 英文文本），作为默认内容来源。
- `<current_Lang>/`：当前语言覆盖层，只放需要翻译的文本字段。

## 加载与覆盖规则

应用会按照下列顺序读取并合并 JSON（后者覆盖前者）：

1. `root`
2. `<current_Lang>`

合并规则：
- **对象字段**会递归合并（例如 `tolerance`、`interactions`）。
- **数组字段**（如 `roas`）会整体覆盖。
- 文件名（如 `Pregabalin.json`）是合并键：同名文件会被后者覆盖并合并。

## 地区化医疗信息约定

语言覆盖层同时代表本项目默认的**文案地区**，但不应被误认为使用者的实际居住地：

| 覆盖层 | 默认面向地区 | 医药信息引用原则 |
|---|---|---|
| `root` | 跨地区公共资料（英语） | 药理、临床研究及其局限；不把某国的批准剂量当作普遍适用剂量 |
| `en_us` | 美国 | 美国 FDA / Drugs@FDA；不能用其他国家批准的说明书代替美国标签 |
| `zh_cn` | 中国大陆 | 国家药监局批准的药品说明书、国家医保局等当地资料 |
| `zh_tw` | 台湾 | 卫福部食药署（TFDA）的药品许可证和核准仿单；不能把中国大陆说明书直接繁体化并作为台湾规范 |

- `root` 记录可以跨地区使用的研究事实，同时注明证据来自何处；临床试验地点不等于销售授权地区。
- `summary` / `dosageRemark` / `generalRisks` 等地区敏感的文字放入对应语言覆盖层；药理作用等跨地区通用事实可以继承 `root`。
- 未核实当地核准信息时，明确写明「本条目尚未核实」，不要断言「未获批准」，也不要杜撰当地处方剂量、禁忌症或法定管制等级。
- 每个地区的处方剂量必须标明其**所属地区与说明书性质**，不能用临床试验的给药剂量替代处方。语言选择不构成所在地、上市状态或处方权限的证明。
- `isApproved` 在本项目只表示「条目已经人工审核」，**不是**监管机构批准状态，不能用它表达国家/地区药证。

## Substance JSON 拆分规则

### root（基础层）
- 必须包含：
  - `name`
  - `localizedName`（默认与 `name` 相同）
  - `url`
  - 结构字段（如 `tolerance`、`interactions`、`roas` 等）
- `interactions` 的每个字符串必须是**分类键**（`_categories.json` 里的名字，如 `stimulant`、`ssri`、`benzodiazepine`）或**目录里的规范物质名**（如 `Tramadol`、`Dextromethorphan`）：
  应用在 `InteractionChecker` 里对物质名按**精确相等**匹配、对分类按 `interactionName.contains(categoryKey, ignoreCase = true)` 匹配，
  写法不对（复数、大小写、简称）就匹配不上。三个例外必须原样保留，应用会用代码展开它们：
  `Substituted amphetamines`、`Serotonin releasers`（展开成具体物质）、`Tricyclic antidepressants`（有意清空，避免误匹配 `depressant`）。
  用 `substances_pipeline.py fix-interactions` 批量规范化（规则与残留清单见 [substances-pipeline.md](substances-pipeline.md)）。
- 可包含英文文本字段，作为默认展示内容。
- 可选结构字段 `oralReleaseForms` 留在 `root`，选项通过 UI 语言文件翻译，参见 `extended-release-formulations.md`。

### <current_Lang>（文本覆盖层）
- 仅放需要翻译的文本字段，例如：
  - `summary`
  - `effectsSummary`
  - `metabolism`（代谢与排泄说明；来源 URL 数组 `metabolismSources` 留在 `root`）
  - `dosageRemark`
  - `generalRisks`
  - `longtermRisks`
  - `saferUse`（数组）
  - 以及其它需要翻译的字符串字段

### 示例

**root/Pregabalin.json**（结构层，含关键字段）
```json
{
  "name": "Pregabalin",
  "localizedName": "Pregabalin",
  "commonNames": [
    "Pregabalin",
    "Lyrica",
    "Nervalin"
  ],
  "url": "https://psychonautwiki.org/wiki/Pregabalin",
  "isApproved": true,
  "tolerance": {
    "full": "within several months of continuous use",
    "zero": "7 - 14 days"
  },
  "crossTolerances": [],
  "toxicities": [
    "low toxicity"
  ],
  "categories": [
    "depressant",
    "habit-forming",
    "common"
  ],
  "interactions": {
    "dangerous": [],
    "unsafe": [],
    "uncertain": [
      "Oxycodone",
      "ssri",
      "MDMA"
    ]
  },
  "roas": [
    {
      "name": "oral",
      "dose": {
        "units": "mg",
        "lightMin": 50,
        "commonMin": 225,
        "strongMin": 600,
        "heavyMin": 900
      },
      "duration": {
        "onset": {
          "min": 30,
          "max": 45,
          "units": "minutes"
        },
        "comeup": {
          "min": 1,
          "max": 2,
          "units": "hours"
        },
        "peak": {
          "min": 4,
          "max": 6,
          "units": "hours"
        },
        "offset": {
          "min": 4,
          "max": 8,
          "units": "hours"
        },
        "total": {
          "min": 9,
          "max": 17,
          "units": "hours"
        },
        "afterglow": {
          "min": 4,
          "max": 10,
          "units": "hours"
        }
      },
      "bioavailability": {
        "max": 90
      }
    },
    {
      "name": "rectal",
      "dose": {
        "units": "mg",
        "lightMin": 40,
        "commonMin": 200,
        "strongMin": 450,
        "heavyMin": 600
      },
      "duration": {
        "onset": {
          "min": 15,
          "max": 30,
          "units": "minutes"
        },
        "comeup": {
          "min": 30,
          "max": 120,
          "units": "minutes"
        },
        "peak": {
          "min": 2,
          "max": 3,
          "units": "hours"
        },
        "offset": {
          "min": 3,
          "max": 5,
          "units": "hours"
        },
        "total": {
          "min": 5,
          "max": 8,
          "units": "hours"
        },
        "afterglow": {
          "min": 6,
          "max": 12,
          "units": "hours"
        }
      }
    }
  ]
}
```

**<current_Lang>/Pregabalin.json**（文本层，仅覆盖 summary）
```json
{
  "summary": "Pregabalin (Lyrica) is a GABA derivative that is used to treat neuropathic pain and seizures, as well as anxiety."
}
```
