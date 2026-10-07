# Substance JSON 格式与不完备记录审查（2026-10-07）

## 审查范围与结论

以更新后的 `main`（`186386f3`）为准，逐条检查 `app/src/main/assets/substances/root/*.json`，并对照应用解析器、加载合并代码和格式文档。没有修改物质记录，也没有核查外部医学事实。CSV 按文件名排序，覆盖全部记录。缺字段表示“当前未记录”，不代表信息不存在、物质安全，或字段必然适用。

| 检查项 | 结果 |
|---|---:|
| root 物质记录 | 982 |
| 严格 JSON 解析成功 | 982 / 982 |
| 解析错误 | 0 |
| `name` 非空且与文件名一致 | 982 / 982 |
| 非空 HTTP(S) `url` | 982 / 982 |
| `categories` 缺失或空 | 62 |
| 含未映射耐受小时字段 | 227 |
| 未知顶层字段 | 0（无） |

## 当前 JSON 设定

| 层/字段 | 格式与行为 |
|---|---|
| 上游导出 | `substances_pipeline.py split` 接受含 `categories` 与 `substances` 的集合，再拆成单物质文件。 |
| `root/` | 每个 `<name>.json` 是一个物质对象；`_categories.json` 是分类数组。`name` 是稳定记录键，英文描述和结构数据在基础层。 |
| 语言覆盖层 | `en_us/`、`zh_cn/`、`zh_tw/` 只放覆盖字段；加载顺序为 `root → 当前语言 → 扩展包`。对象递归合并，数组整体替换。 |
| 身份字段 | `name` 为非空字符串；`localizedName` 可空，显示名回退到 `name`；`commonNames` 为别名列表，`url` 为来源链接。 |
| 审核标记 | `isApproved` 表示目录资料审核状态，不代表监管批准。本次 `true` 281 条、`false` 701 条。 |
| 结构字段 | `tolerance` 使用 `{full, half, zero}` 文本；`interactions` 是 `{dangerous, unsafe, uncertain}` 字符串列表；`roas` 是途径对象列表，可含 dose、duration、bioavailability；另有 `categories`、`crossTolerances`、`oralReleaseForms`。 |
| 内容字段 | 可选文本/列表：`addictionPotential`、`toxicities`、`summary`、`effectsSummary`、`dosageRemark`、`generalRisks`、`longtermRisks`、`saferUse`。 |
| 代谢字段 | `metabolism` 是可翻译文本；`metabolismSources` 是来源 URL 列表。缺来源不能解释成没有代谢。 |
| 解析容错 | 解析器只强制 `name` 非空；多数其他字段允许缺失并落到 `null`、空列表或默认值。解析成功不等于内容完整。 |

## root 字段覆盖统计

“缺失/空”合并统计缺失键、空列表/对象和空字符串；逐条 CSV 保留各状态。

| 字段 | 有数据的记录 | 缺失/空 |
|---|---:|---:|
| `commonNames` | 466 | 516 |
| `categories` | 920 | 62 |
| `tolerance` | 235 | 747 |
| `crossTolerances` | 221 | 761 |
| `addictionPotential` | 253 | 729 |
| `toxicities` | 232 | 750 |
| `interactions` | 218 | 764 |
| `roas` | 293 | 689 |
| `summary` | 219 | 763 |
| `effectsSummary` | 58 | 924 |
| `dosageRemark` | 38 | 944 |
| `generalRisks` | 57 | 925 |
| `longtermRisks` | 52 | 930 |
| `saferUse` | 57 | 925 |
| `metabolism` | 590 | 392 |
| `metabolismSources` | 141 | 841 |
| `oralReleaseForms` | 13 | 969 |

其中 `interactions` 有 218 个对象，部分对象缺少分组键：3-Cl-PCP（缺 unsafe,uncertain）、Changa（缺 unsafe,uncertain）、Grayanotoxin（缺 uncertain）、Kava（缺 unsafe）、Mushrooms（缺 dangerous）、Tizanidine（缺 unsafe）。

## 分类缺口候选（62 条）

以下记录的 `categories` 键缺失或为空，是结构覆盖待核验项；未凭空补分类。其他字段状态可在逐条 CSV 中筛选。

| 物质 | 文件 | 状态 | 同时缺少的重点字段 |
|---|---|---|---|
| 4-BMC | `app/src/main/assets/substances/root/4-BMC.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| 4-CA | `app/src/main/assets/substances/root/4-CA.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Amanita citrina | `app/src/main/assets/substances/root/Amanita citrina.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Amanita muscaria | `app/src/main/assets/substances/root/Amanita muscaria.json` | missing | roas,toxicities,interactions,summary,saferUse |
| Amanita pantherina | `app/src/main/assets/substances/root/Amanita pantherina.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Argyreia nervosa | `app/src/main/assets/substances/root/Argyreia nervosa.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Banisteriopsis caapi | `app/src/main/assets/substances/root/Banisteriopsis caapi.json` | missing | roas,toxicities,interactions,summary,saferUse |
| Blue Lotus | `app/src/main/assets/substances/root/Blue Lotus.json` | empty | toxicities,interactions,summary,saferUse,metabolism |
| Cathinone | `app/src/main/assets/substances/root/Cathinone.json` | missing | roas,toxicities,interactions,summary,saferUse |
| Coca | `app/src/main/assets/substances/root/Coca.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Cocoa | `app/src/main/assets/substances/root/Cocoa.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Datura (botany) | `app/src/main/assets/substances/root/Datura (botany).json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Echinopsis lageniformis | `app/src/main/assets/substances/root/Echinopsis lageniformis.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Echinopsis pachanoi | `app/src/main/assets/substances/root/Echinopsis pachanoi.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Echinopsis peruviana | `app/src/main/assets/substances/root/Echinopsis peruviana.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Flumazenil | `app/src/main/assets/substances/root/Flumazenil.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Hyoscyamus niger (botany) | `app/src/main/assets/substances/root/Hyoscyamus niger (botany).json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| IHCH-7113 | `app/src/main/assets/substances/root/IHCH-7113.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Loperamide | `app/src/main/assets/substances/root/Loperamide.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Lophophora diffusa | `app/src/main/assets/substances/root/Lophophora diffusa.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Lophophora fricii | `app/src/main/assets/substances/root/Lophophora fricii.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Magnesium threonate | `app/src/main/assets/substances/root/Magnesium threonate.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Magnesium | `app/src/main/assets/substances/root/Magnesium.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Mandragora officinarum (botany) | `app/src/main/assets/substances/root/Mandragora officinarum (botany).json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Mandragora | `app/src/main/assets/substances/root/Mandragora.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| MDNEB | `app/src/main/assets/substances/root/MDNEB.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| MDNEP | `app/src/main/assets/substances/root/MDNEP.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| MDNMB | `app/src/main/assets/substances/root/MDNMB.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| MDNMP | `app/src/main/assets/substances/root/MDNMP.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Mimosa tenuiflora | `app/src/main/assets/substances/root/Mimosa tenuiflora.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| MMDA | `app/src/main/assets/substances/root/MMDA.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Morning glory | `app/src/main/assets/substances/root/Morning glory.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| N-(2C)-fentanyl | `app/src/main/assets/substances/root/N-(2C)-fentanyl.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| N-Methylcyclazodone | `app/src/main/assets/substances/root/N-Methylcyclazodone.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| N-Methylhexedrone | `app/src/main/assets/substances/root/N-Methylhexedrone.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Nitromethaqualone | `app/src/main/assets/substances/root/Nitromethaqualone.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Oxymorphazone | `app/src/main/assets/substances/root/Oxymorphazone.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Peganum harmala | `app/src/main/assets/substances/root/Peganum harmala.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Phalaris aquatica | `app/src/main/assets/substances/root/Phalaris aquatica.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Piper nigrum (botany) | `app/src/main/assets/substances/root/Piper nigrum (botany).json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Poppers | `app/src/main/assets/substances/root/Poppers.json` | empty | roas,toxicities,interactions,summary,metabolism |
| Psilocybe cubensis | `app/src/main/assets/substances/root/Psilocybe cubensis.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Psilocybe cyanescens | `app/src/main/assets/substances/root/Psilocybe cyanescens.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Psilocybe mexicana | `app/src/main/assets/substances/root/Psilocybe mexicana.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Psilocybe subaeruginosa | `app/src/main/assets/substances/root/Psilocybe subaeruginosa.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Psychotria viridis | `app/src/main/assets/substances/root/Psychotria viridis.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| RGPU-95 | `app/src/main/assets/substances/root/RGPU-95.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Semax | `app/src/main/assets/substances/root/Semax.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Serotonin | `app/src/main/assets/substances/root/Serotonin.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| SR-17018 | `app/src/main/assets/substances/root/SR-17018.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Tabernanthe iboga (botany) | `app/src/main/assets/substances/root/Tabernanthe iboga (botany).json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Tetrahydrocannabihexol | `app/src/main/assets/substances/root/Tetrahydrocannabihexol.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Tetrahydrocannabinol | `app/src/main/assets/substances/root/Tetrahydrocannabinol.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Tetrahydrocannabiphorol | `app/src/main/assets/substances/root/Tetrahydrocannabiphorol.json` | missing | roas,toxicities,interactions,summary,saferUse |
| Tetrahydrocannabutol | `app/src/main/assets/substances/root/Tetrahydrocannabutol.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Thujone | `app/src/main/assets/substances/root/Thujone.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Tobacco | `app/src/main/assets/substances/root/Tobacco.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Tryptamine | `app/src/main/assets/substances/root/Tryptamine.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Yohimbine | `app/src/main/assets/substances/root/Yohimbine.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Δ-10-Tetrahydrocannabinol | `app/src/main/assets/substances/root/Δ-10-Tetrahydrocannabinol.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Δ-11-Tetrahydrocannabinol | `app/src/main/assets/substances/root/Δ-11-Tetrahydrocannabinol.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| Δ-8-Tetrahydrocannabinol | `app/src/main/assets/substances/root/Δ-8-Tetrahydrocannabinol.json` | missing | roas,toxicities,interactions,summary,saferUse |

## 多语言覆盖

| 目录 | 覆盖物质数 | 缺少覆盖文件 | 无非空 `localizedName` |
|---|---:|---:|---:|
| `en_us/` | 292 | 690 | 690 |
| `zh_cn/` | 937 | 45 | 206 |
| `zh_tw/` | 937 | 45 | 206 |

简体与繁体各缺 45 个覆盖文件；各有 161 个现有覆盖文件未提供非空 `localizedName`，共 206 条显示名回退到英文 `name`。这可能是没有可靠中文译名，不自动判为错误。合并后有 `summary` 的记录数：`en_us` 219、`zh_cn` 357、`zh_tw` 328。三个语言目录都没有 `_categories.json`，分类文案回退到 root。

## 文档与台账差异

- `docs/substances-translation-protocol.md` 称 root 必须包含 `localizedName`；当前 982 条 root 记录都没有该字段。实际值放在语言覆盖层，应用也会回退到 `name`，因此这是文档与数据布局不一致。
- 227 条 root 记录在 `tolerance` 中含 `halfToleranceInHours` 和/或 `zeroToleranceInHours`；Kotlin `Tolerance` 只解析 `full`、`half`、`zero`，这些数值目前会被忽略。逐条 CSV 标出了未映射键。
- `docs/substance-catalog-expansion.md` 标注 2026-09-12、记录 1063 条；当前 main 的 root 为 982 条，文档快照与现状计数不一致。
- `docs/substance-metabolism-sources.json` 的 `checkedOn` 为 `2026-09-08`，含 148 个 reviewed、455 个 notYetVerified 名称；root 当前有 590 条 metabolism、141 条来源。台账中 7 个 reviewed、21 个 notYetVerified 名称已不在 root；root 另有 433 条通用“尚未核实”文本、15 条多成分材料说明。合并或改名可能造成台账漂移，需先按别名核对。

## 审查边界与建议

- 这是结构与字段覆盖清单，不是安全性或医学事实判断；缺少剂量、相互作用字段只说明当前未记录。
- `roas`、`toxicities`、`interactions` 等可选字段应按条目查证后再补，不能因缺失而推断或填造。
- 建议顺序：先核实分类候选；再统一 `localizedName` 文档口径与耐受小时字段是否纳入模型；随后按来源台账核对过期名称。
- 本次没有改物质数据、解析器或现有协议文档。

## 逐条表格

[完整 CSV（982 条逐记录状态）](substance-incomplete-records-2026-10-07.csv) 列出每个字段状态、简繁覆盖、未映射耐受字段和重点待核验字段。
