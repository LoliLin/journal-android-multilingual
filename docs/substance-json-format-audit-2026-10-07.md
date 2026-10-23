# Substance JSON ???????????2026-10-07?


2026-10-13 ????? DailyMed ?????? Promethazine?Quetiapine?Mirtazapine?Trazodone ?????????????????????????????Quetiapine ????low toxicity??????????Trazodone ????? route ??????????????????????????????????????????????????? 905 ?? 902?Promethazine ??? metabolism ????


2026-10-13 ?????? DailyMed ???? Gabapentin?Pregabalin?Hydroxyzine?Amitriptyline ???????????????????? gabapentinoid ? Hydroxyzine/Amitriptyline ???????????Hydroxyzine ? Amitriptyline ?????????? route ???/??????????????????????????? 902 ?? 898?


2026-10-13 ??????? DailyMed ?????? Citalopram?Propranolol?Scopolamine ? route?????????????? Diphenhydramine ???? OTC ???? metabolism ???????????????????/???????????????? 898 ?? 895?


2026-10-13：补录 Doxylamine、Methylphenidate、Dextromethorphan、Venlafaxine 标签安全信息。此前复点候选记录为 892；Dextromethorphan 的 metabolism 仍未解决。

2026-10-13 Morphine：补入单方口服缓释胶囊标签支持的呼吸抑制、意外误服及酒精/中枢抑制剂风险；metabolism 仍未核实，保留候选。

2026-10-13 继续增补 Lorazepam、Methadone、Hydrocodone：分别记录口服片相互作用、口服溶液及单方缓释片安全要点。Hydrocodone metabolism 仍待核实。当前候选记录数经清单重算为 884。

## ???????

?????????????????root ? 982 ???????? `_categories.json`???? CSV ? 982 ???????????????? 3170 ? JSON ???root 983?en_us 292?zh_cn 958?zh_tw 937???? JSON ???????root ? 982 ? `name` ???????982 ????? HTTP(S) `url`??????? 61 ???????????? 226 ???????????????????????????????????????????????????????????

| ??? | ?? |
|---|---:|
| root ???? | 982 |
| ?? JSON ???? | 982 / 982 |
| ???? | 0 |
| `name` ????????? | 982 / 982 |
| ?? HTTP(S) `url` | 982 / 982 |
| `categories` ???? | 61 |
| ?????????? | 226 |
| CSV ??? / ???? | 982 / 0 |

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

“缺失/空”合并统计缺失键、空列表/对象和空字符串；逐条 CSV 保留各状态。`roas` 只按是否存在途径记录计数，剂量阈值、时长范围和释药形式另行统计；一条 route/form 记录不等同于有剂量或时长数据。

| 字段 | 有数据的记录 | 缺失/空 |
|---|---:|---:|
| `commonNames` | 472 | 510 |
| `categories` | 921 | 61 |
| `tolerance` | 240 | 742 |
| `crossTolerances` | 226 | 756 |
| `addictionPotential` | 260 | 722 |
| `toxicities` | 286 | 696 |
| `interactions` | 249 | 733 |
| `roas` | 321 | 661 |
| `roas[].dose` thresholds | 293 | 689 |
| `roas[].duration` ranges | 292 | 690 |
| `roas[].releaseForm` | 13 | 969 |
| `summary` | 914 | 68 |
| `effectsSummary` | 137 | 845 |
| `dosageRemark` | 174 | 808 |
| `generalRisks` | 198 | 784 |
| `longtermRisks` | 130 | 852 |
| `saferUse` | 211 | 771 |
| `metabolism` | 653 | 329 |
| `metabolismSources` | 206 | 776 |
| `oralReleaseForms` | 13 | 969 |

## 分类缺口候选（59 条）

以下记录的 `categories` 键缺失或为空，是结构覆盖待核验项；未凭空补分类。其他字段状态可在逐条 CSV 中筛选。

| 物质 | 文件 | 状态 | 同时缺少的重点字段 |
|---|---|---|---|
| 4-BMC | `app/src/main/assets/substances/root/4-BMC.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| 4-CA | `app/src/main/assets/substances/root/4-CA.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Amanita citrina | `app/src/main/assets/substances/root/Amanita citrina.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Amanita muscaria | `app/src/main/assets/substances/root/Amanita muscaria.json` | missing | roas,toxicities,interactions,saferUse |
| Amanita pantherina | `app/src/main/assets/substances/root/Amanita pantherina.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Argyreia nervosa | `app/src/main/assets/substances/root/Argyreia nervosa.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Banisteriopsis caapi | `app/src/main/assets/substances/root/Banisteriopsis caapi.json` | missing | roas,toxicities,interactions,saferUse |
| Blue Lotus | `app/src/main/assets/substances/root/Blue Lotus.json` | empty | toxicities,interactions,saferUse,metabolism |
| Cathinone | `app/src/main/assets/substances/root/Cathinone.json` | missing | roas,toxicities,interactions,saferUse |
| Coca | `app/src/main/assets/substances/root/Coca.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Cocoa | `app/src/main/assets/substances/root/Cocoa.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Datura (botany) | `app/src/main/assets/substances/root/Datura (botany).json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Echinopsis lageniformis | `app/src/main/assets/substances/root/Echinopsis lageniformis.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Echinopsis pachanoi | `app/src/main/assets/substances/root/Echinopsis pachanoi.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Echinopsis peruviana | `app/src/main/assets/substances/root/Echinopsis peruviana.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Flumazenil | `app/src/main/assets/substances/root/Flumazenil.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Hyoscyamus niger (botany) | `app/src/main/assets/substances/root/Hyoscyamus niger (botany).json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| IHCH-7113 | `app/src/main/assets/substances/root/IHCH-7113.json` | missing | roas,toxicities,interactions,saferUse,metabolism |

| Lophophora diffusa | `app/src/main/assets/substances/root/Lophophora diffusa.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Lophophora fricii | `app/src/main/assets/substances/root/Lophophora fricii.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Magnesium threonate | `app/src/main/assets/substances/root/Magnesium threonate.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Magnesium | `app/src/main/assets/substances/root/Magnesium.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Mandragora officinarum (botany) | `app/src/main/assets/substances/root/Mandragora officinarum (botany).json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Mandragora | `app/src/main/assets/substances/root/Mandragora.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| MDNEB | `app/src/main/assets/substances/root/MDNEB.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| MDNEP | `app/src/main/assets/substances/root/MDNEP.json` | missing | roas,toxicities,interactions,saferUse,metabolism |

| MDNMP | `app/src/main/assets/substances/root/MDNMP.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Mimosa tenuiflora | `app/src/main/assets/substances/root/Mimosa tenuiflora.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| MMDA | `app/src/main/assets/substances/root/MMDA.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Morning glory | `app/src/main/assets/substances/root/Morning glory.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| N-(2C)-fentanyl | `app/src/main/assets/substances/root/N-(2C)-fentanyl.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |
| N-Methylcyclazodone | `app/src/main/assets/substances/root/N-Methylcyclazodone.json` | missing | roas,toxicities,interactions,summary,saferUse,metabolism |

| Nitromethaqualone | `app/src/main/assets/substances/root/Nitromethaqualone.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Oxymorphazone | `app/src/main/assets/substances/root/Oxymorphazone.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Peganum harmala | `app/src/main/assets/substances/root/Peganum harmala.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Phalaris aquatica | `app/src/main/assets/substances/root/Phalaris aquatica.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Piper nigrum (botany) | `app/src/main/assets/substances/root/Piper nigrum (botany).json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Poppers | `app/src/main/assets/substances/root/Poppers.json` | empty | roas,toxicities,interactions,metabolism |
| Psilocybe cubensis | `app/src/main/assets/substances/root/Psilocybe cubensis.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Psilocybe cyanescens | `app/src/main/assets/substances/root/Psilocybe cyanescens.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Psilocybe mexicana | `app/src/main/assets/substances/root/Psilocybe mexicana.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Psilocybe subaeruginosa | `app/src/main/assets/substances/root/Psilocybe subaeruginosa.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Psychotria viridis | `app/src/main/assets/substances/root/Psychotria viridis.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| RGPU-95 | `app/src/main/assets/substances/root/RGPU-95.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Semax | `app/src/main/assets/substances/root/Semax.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Serotonin | `app/src/main/assets/substances/root/Serotonin.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| SR-17018 | `app/src/main/assets/substances/root/SR-17018.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Tabernanthe iboga (botany) | `app/src/main/assets/substances/root/Tabernanthe iboga (botany).json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Tetrahydrocannabihexol | `app/src/main/assets/substances/root/Tetrahydrocannabihexol.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Tetrahydrocannabinol | `app/src/main/assets/substances/root/Tetrahydrocannabinol.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Tetrahydrocannabiphorol | `app/src/main/assets/substances/root/Tetrahydrocannabiphorol.json` | missing | roas,toxicities,interactions,saferUse |
| Tetrahydrocannabutol | `app/src/main/assets/substances/root/Tetrahydrocannabutol.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Thujone | `app/src/main/assets/substances/root/Thujone.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Tobacco | `app/src/main/assets/substances/root/Tobacco.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Tryptamine | `app/src/main/assets/substances/root/Tryptamine.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Yohimbine | `app/src/main/assets/substances/root/Yohimbine.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Δ-10-Tetrahydrocannabinol | `app/src/main/assets/substances/root/Δ-10-Tetrahydrocannabinol.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Δ-11-Tetrahydrocannabinol | `app/src/main/assets/substances/root/Δ-11-Tetrahydrocannabinol.json` | missing | roas,toxicities,interactions,saferUse,metabolism |
| Δ-8-Tetrahydrocannabinol | `app/src/main/assets/substances/root/Δ-8-Tetrahydrocannabinol.json` | missing | roas,toxicities,interactions,saferUse |

## 多语言覆盖

| 目录 | 覆盖物质数 | 缺少覆盖文件 | 无非空 `localizedName` |
|---|---:|---:|---:|
| `en_us/` | 292 | 690 | 690 |
| `zh_cn/` | 958 | 24 | 206 |
| `zh_tw/` | 937 | 45 | 206 |

简体缺 24 个、繁体缺 45 个覆盖文件；现有覆盖文件中，简体有 182 个、繁体有 161 个未提供非空 `localizedName`。两种语言各有 206 条显示名回退到英文 `name`。当前合并后有 `summary` 的记录数：`en_us` 914、`zh_cn` 916、`zh_tw` 916。

## 文档与台账差异

- `docs/substances-translation-protocol.md` 称 root 必须包含 `localizedName`；当前 982 条 root 记录都没有该字段。实际值放在语言覆盖层，应用也会回退到 `name`，因此这是文档与数据布局不一致。
- 227 条 root 记录在 `tolerance` 中含 `halfToleranceInHours` 和/或 `zeroToleranceInHours`；Kotlin `Tolerance` 只解析 `full`、`half`、`zero`，这些数值目前会被忽略。逐条 CSV 标出了未映射键。
- `docs/substance-catalog-expansion.md` 标注 2026-09-12、记录 1063 条；当前 main 的 root 为 982 条，文档快照与现状计数不一致。
- `docs/substance-metabolism-sources.json` 的 `checkedOn` 为 `2026-09-08`，含 148 个 reviewed、455 个 notYetVerified 名称；root 当前有 592 条 metabolism、144 条来源。台账中 7 个 reviewed、21 个 notYetVerified 名称已不在 root；root 另有 433 条通用“尚未核实”文本、15 条多成分材料说明。合并或改名可能造成台账漂移，需先按别名核对。

## 审查边界与建议

- 这是结构与字段覆盖清单，不是安全性或医学事实判断；缺少剂量、相互作用字段只说明当前未记录。
- `roas`、`toxicities`、`interactions` 等可选字段应按条目查证后再补，不能因缺失而推断或填造。
- 建议顺序：先核实分类候选；再统一 `localizedName` 文档口径与耐受小时字段是否纳入模型；随后按来源台账核对过期名称。
- 摘要工作包括将 689 条已核验英文摘要迁入 root，以及新增 5 条有来源的摘要与简中翻译；释药形式工作新增 18 条有标签依据的口服 route/form 记录。随后又人工核对 26 条 FreeODwiki 记录，按证据逐条更新 route、剂量/时长边界、风险、相互作用与减害字段；23 条记录实际录入了来源剂量分档，并为 N-Ethylhexedrone、NEP 另记录来源明确标记的抽吸 route。Loperamide 因来源警告未转录非医疗剂量数据；3-CMC 仅记录来源支持的途径和风险，未转录其用户报告剂量分档，因为同页报告已确认死亡并强烈建议避免高剂量及鼻吸。2-MMC 的 FreeODwiki 页面标为未经审核且警告可能有危险错误；保留低置信度提示，因剂量分档重叠而不转录其数值剂量/时长。1B-LSD 对照口服剂量/时长表修正轻微剂量下界，记录代谢研究并明确人体药代局限；1cP-AL-LAD 核对口服剂量/时长，修正轻微剂量下界并保留 20 µg 阈值差异；将 AL-LAD 前药关系标为未证实假设，并记录毒性、长期效应和代谢资料缺口；1cP-LSD 核对口服阈值、剂量和时长，修正 lightMin；按来源仅记录人体血清孵育形成 LSD 的观察，不外推整体药代动力学，并保留毒性章节存根警告；2-FA 核对口服剂量/时长、阈值和路线警告，修正轻微剂量下界，并仅记录无剂量/时长数据的鼻吸和吸入路线；2-FEA 核对鼻吸剂量/时长表，修正 lightMin 并将阈值留在 dosageRemark；页面未提供代谢资料，因此保留代谢缺口；2-FMA 核对口服/鼻吸剂量与时长表，补充效应、风险和代谢证据缺口；来源关于雾化分解风险的机制仅作为警告记录并标注尚未验证；3-FA 核对口服剂量/时长表，修正 lightMin 并把 10 mg 阈值留在说明字段；将来源的药理描述限定为临床前证据；3-FEA 对照口服/鼻吸剂量表修正两路线 lightMin，保留鼻吸中等/强烈分档重叠和人体代谢、毒性证据不足；3-MMC 核对口服、鼻吸和静脉剂量/时长并修正前两路线 lightMin；将阈值记录于说明，并遵从来源的剂量表与毒性章节警告；4-FA 核对口服阈值、剂量和时长，记录急性心血管风险报告并保留因果不确定性；长期毒性与代谢仍为研究缺口；4-FMA 核对口服剂量/时长和阈值，修正 lightMin 并保留来源剂量区间空档；将 4-FA 类比与 4-FMA 的直接报告分开，未把 CYP450 推测写成已确立代谢机制；3-Cl-PCP 补充来源明确的相互作用级别、风险和减害字段，长期泌尿风险保留为来源未经证实的类似物类比。2C-E 核对两路线剂量/时长、数据缺口、效应概述和减害信息，并修正口服轻微剂量下界；2C-T-2 记录毒理与长期健康数据缺口、来源剂量阈值和两路线分档，并披露严重剂量分级与模型边界重叠；2C-T-7 对照两路线剂量/时长表修正轻微剂量下界，说明未建模的剂量阈值，并保留鼻吸死亡与联用警告。另依据 PsychonautWiki 为 2C-H 补入口服 route（页面剂量与时长均标未知）、风险说明和减害提示；为 25B-NBOH 补入舌下 route、主观效应概述、严重伤害风险警告、减害和证据限制说明。2C-EF 则依据 FreeODwiki 归档补入口服剂量/时长表、耐受、联用、风险、减害等字段，并保留来源的用户报告免责声明与未知剂量边界。逐条来源与证据说明见 attribution 台账。

## 逐条表格

[完整 CSV（982 条逐记录状态）](substance-incomplete-records-2026-10-07.csv) 列出每个字段状态、简繁覆盖、未映射耐受字段和重点待核验字段。
