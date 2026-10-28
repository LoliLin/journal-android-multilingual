# 口服缓释剂型记录

新增记录的确认页与已有记录的编辑页提供「剂型」选择。只有目录里配置了口服剂型的物质才显示该选择；
已有剂型标记的记录，即使目录配置以后变化，仍可查看和清除这个标记。

## 支持的物质与说明书来源

首批 13 个品种，核对日期 2026-09-08：

| 物质 | 说明书来源（DailyMed） |
|---|---|
| Alprazolam（阿普唑仑） | https://dailymed.nlm.nih.gov/dailymed/lookup.cfm?setid=5d839a8b-0f45-42bf-acc1-2bd28f948e53 |
| Bupropion（安非他酮） | https://dailymed.nlm.nih.gov/dailymed/lookup.cfm?setid=439a7e93-f578-434f-943f-4b93c35450f5 |
| Carbamazepine（卡马西平） | https://dailymed.nlm.nih.gov/dailymed/drugInfo.cfm?setid=1fc170d7-db1a-486d-8c10-faa6e3da3b1e |
| Clonidine（可乐定） | https://dailymed.nlm.nih.gov/dailymed/drugInfo.cfm?setid=03a53175-d301-4001-b28b-78ecd763c4ae |
| Guanfacine（胍法辛） | https://dailymed.nlm.nih.gov/dailymed/lookup.cfm?setid=07a701e2-39b7-4bb1-a7c7-d9baed4ab18a |
| Lithium（锂） | https://dailymed.nlm.nih.gov/dailymed/drugInfo.cfm?setid=f7f5b69a-c2a1-4586-a189-1475d41387c0 |
| Methylphenidate（哌甲酯） | https://dailymed.nlm.nih.gov/dailymed/lookup.cfm?setid=1a88218c-5b18-4220-8f56-526de1a276cd |
| Paliperidone（帕利哌酮） | https://dailymed.nlm.nih.gov/dailymed/drugInfo.cfm?setid=463db841-297f-7692-e063-6394a90a8c14 |
| Paroxetine（帕罗西汀） | https://dailymed.nlm.nih.gov/dailymed/downloadpdffile.cfm?setId=a73cf8ee-f99b-4972-8939-0d394a527134 |
| Quetiapine（喹硫平） | https://dailymed.nlm.nih.gov/dailymed/lookup.cfm?setid=5e4a84b1-fb8c-44d0-8213-51e43d71db69 |
| Valproic acid（丙戊酸相关制剂） | https://dailymed.nlm.nih.gov/dailymed/drugInfo.cfm?setid=93d6ddb5-7143-4917-baf7-31cbf8a90e8d |
| Venlafaxine（文拉法辛） | https://dailymed.nlm.nih.gov/dailymed/drugInfo.cfm?setid=db1d16b3-d1a2-4f41-adff-a87c17689aa0 |
| Zolpidem（唑吡坦） | https://dailymed.nlm.nih.gov/dailymed/lookup.cfm?setid=0c64bc71-2e7f-4b15-a8e1-acb8de04395f |

可选值为：

- **未指定**：没有记录释放方式，旧记录保持这个状态。
- **普通 / 速释**：`IMMEDIATE_RELEASE`。
- **缓释 / 控释**：`EXTENDED_RELEASE`，作为 SR、ER、XR、CR 等标示的概括性记录。

帕利哌酮的口服选项只有**缓释 / 控释**（其余 12 个品种同时提供普通 / 速释）。
剂型依据实际包装记录；仅有肠溶包衣不代表缓释。此选项不换算剂量，也不保证不同品牌、盐型和释放系统
可按相同毫克数互换。

## 保存与回溯

- Room 数据库从 v7 自动迁移到 v8（`AutoMigration(from = 7, to = 8)`），为 `Ingestion` 添加可空
  `releaseForm` 列；旧数据不被推断为普通片，保持「未指定」。
- 日记记录行显示已指定的剂型，编辑时保留选择。
- 最近使用的快捷剂量包含剂型，因此相同物质、相同剂量的普通与缓释记录不会合并为同一个快捷项；
  再次使用快捷项会带入原剂型。
- 导出 / 导入使用可选 `releaseForm` 字段（`JournalExport.kt`）；旧备份省略该字段时按「未指定」读取。
- 设置中的日记备份按类型序列化 DataStore 偏好并包含当前头像。导入只覆盖本版本已识别且类型匹配的偏好键；
  未出现、未知或类型不匹配的键保留本机值，因此不含偏好字段的旧备份也不改动本机设置。
- 头像先写入临时文件，再逐个原子替换。Room、DataStore 和文件系统没有共同事务；极端 I/O 故障仍可能造成部分导入。
- 当前目录有 13 个口服释药形式记录：12 个同时列普通/速释与缓释/控释，帕利哌酮仅列口服缓释片。本次（2026-10-07）已逐条核对这 13 条记录，将 route 与释药形式写进 `roas`；对原来只有一份口服数据的 7 条保留为未分型数据，并单独增加有标签依据的缓释口服行。
- `Roa` 现可按途径和释药形式检索。未选择剂型时优先使用旧的未分型数据；没有未分型数据时用显式速释数据。速释可回退到旧的未分型数据；缓释只使用明确标为缓释的行，不回退到速释数据。
- 目前这些新分型行只记录已核实的口服途径和释药形式，尚无逐剂型剂量分级或作用时间曲线。官方说明书中的适应症剂量不能直接映射成 app 的 light/common/strong/heavy 等级；缓释行不会借用未分型剂量或时长。字段缺失只代表尚无可比证据，不代表不存在。

## 本次 ROA 释药形式数据（2026-10-07）

以下字段只记录产品标签明确支持的口服途径与释药类型；没有把治疗适应症剂量映射到 `lightMin` / `commonMin` 等非医疗剂量等级。

| 物质 | 本次写入 | 标签来源与核对点 |
|---|---|---|
| Alprazolam | `root/Alprazolam.json#roas`：口服缓释 | [DailyMed alprazolam extended-release](https://dailymed.nlm.nih.gov/dailymed/drugInfo.cfm?setid=259387c1-e374-4bc2-9510-5f7f2c60d1a5)：缓释片，口服给药 |
| Bupropion | `root/Bupropion.json#roas`：口服缓释 | [DailyMed bupropion XL](https://dailymed.nlm.nih.gov/dailymed/drugInfo.cfm?setid=df959e1b-3f6d-42cf-8521-fe03b5a4ceb8)；[bupropion SR](https://dailymed.nlm.nih.gov/dailymed/drugInfo.cfm?setid=8069c521-c49a-4b6d-baa8-4b0be897d585) |
| Carbamazepine | `root/Carbamazepine.json#roas`：口服速释、缓释 | [常规片](https://dailymed.nlm.nih.gov/dailymed/drugInfo.cfm?setid=25cca2b1-9020-4d8a-bc22-b8063e57f9e1)；[缓释片](https://dailymed.nlm.nih.gov/dailymed/lookup.cfm?setid=c91a5a1b-5481-445b-86e8-f10109ac3937) 与[缓释胶囊](https://dailymed.nlm.nih.gov/dailymed/drugInfo.cfm?setid=fbca2152-0022-47a5-a1ea-58950b45bcbf) |
| Clonidine | `root/Clonidine.json#roas`：口服缓释 | [DailyMed clonidine extended-release](https://dailymed.nlm.nih.gov/dailymed/drugInfo.cfm?setid=99485427-a120-4fc7-bb61-25857d1900ec)：缓释片，口服给药 |
| Guanfacine | `root/Guanfacine.json#roas`：口服速释、缓释 | [Tenex 速释片](https://dailymed.nlm.nih.gov/dailymed/drugInfo.cfm?setid=886e050c-dd22-4f35-ac3b-243f091125c3)；[Intuniv 缓释片](https://dailymed.nlm.nih.gov/dailymed/drugInfo.cfm?setid=301f01a7-2b0a-cfba-9c3d-a1dd831785a3) |
| Lithium | `root/Lithium.json#roas`：口服缓释 | [DailyMed lithium carbonate extended-release](https://dailymed.nlm.nih.gov/dailymed/drugInfo.cfm?setid=7f3aa4f5-908a-44e7-8113-b64f91dc86e7)：缓释片，口服给药 |
| Methylphenidate | `root/Methylphenidate.json#roas`：口服缓释 | [Metadate ER](https://dailymed.nlm.nih.gov/dailymed/fda/fdaDrugXsl.cfm?setid=739bbd64-d9e1-4771-967b-a2cd08f4eaf5&type=display)：缓释片，口服给药；其标签同时区分常规片剂 |
| Paliperidone | `root/Paliperidone.json#roas`：口服缓释 | [DailyMed paliperidone extended-release](https://dailymed.nlm.nih.gov/dailymed/drugInfo.cfm?setid=7b269778-803b-4ce2-a8c0-636fb131d16a)：口服缓释片。长效注射剂不计入口服形式 |
| Paroxetine | `root/Paroxetine.json#roas`：口服速释、控释 | [常规片](https://dailymed.nlm.nih.gov/dailymed/lookup.cfm?setid=662223ec-3b9b-4a85-9680-789522363a7b)；[Paxil CR 控释片](https://dailymed.nlm.nih.gov/dailymed/drugInfo.cfm?setid=483bd97f-c4d0-4e23-aaa8-6334f4471e0c)。本目录将 CR 归入 `EXTENDED_RELEASE` |
| Quetiapine | `root/Quetiapine.json#roas`：口服缓释 | [Seroquel XR](https://dailymed.nlm.nih.gov/dailymed/drugInfo.cfm?setid=51927f2e-3d56-4644-a6a8-40e0b9a5b579)：缓释片 |
| Valproic acid | `root/Valproic acid.json#roas`：口服常规、缓释 | [Valproic acid capsules](https://dailymed.nlm.nih.gov/dailymed/drugInfo.cfm?setid=7d4df6ad-3d53-4664-9200-749487bf3689)；[divalproex sodium extended-release](https://dailymed.nlm.nih.gov/dailymed/lookup.cfm?setid=dc9e4871-fb3d-4c59-81e5-2ab6e8be9b7c)，标签注明 divalproex sodium 与 valproic acid 的等效关系 |
| Venlafaxine | `root/Venlafaxine.json#roas`：口服速释、缓释 | [常规片剂](https://dailymed.nlm.nih.gov/dailymed/lookup.cfm?setid=20319949-ccf9-477a-a6e3-53502772deb9)；[缓释胶囊](https://dailymed.nlm.nih.gov/dailymed/lookup.cfm?setid=7f224c88-9425-9ef0-01bc-dcf3bdafbd89) |
| Zolpidem | `root/Zolpidem.json#roas`：口服缓释 | [Ambien CR](https://dailymed.nlm.nih.gov/dailymed/drugInfo.cfm?setid=40f9a7b1-b57b-4e70-b709-e81705e784a7)：双层片分别即时释放与较慢释放 |

本轮从 DailyMed 搜索结果核对了标签标题和索引摘要；该环境打开 DailyMed 正文时连接被关闭，因此保留直接标签链接供复核。字段只代表已核实的途径/产品释药类型，不代表剂量或时长完整。

## 目录格式

在对应 `root/<Name>.json` 添加结构字段（语言覆盖层不需要复制，选项名由 UI 语言文件翻译）：

```json
{
  "roas": [
    { "name": "oral", "releaseForm": "IMMEDIATE_RELEASE" },
    { "name": "oral", "releaseForm": "EXTENDED_RELEASE" }
  ],
  "oralReleaseForms": ["IMMEDIATE_RELEASE", "EXTENDED_RELEASE"]
}
```

`roas[].releaseForm` is optional for legacy entries. A form-specific dose or duration may be added inside that ROA only when a source supports it; a route/form row without those keys records availability, not missing effect or zero dose. Unclassified legacy oral rows remain usable for an unspecified form and as an immediate-release fallback. Extended-release lookup requires an explicitly marked row and never falls back to legacy or immediate-release data.

未配置时默认为空列表。解析器忽略无法识别的剂型值，保持宽容加载；该配置仅用于口服路线。

## 校验

- 目录：13 个品种的 `root/*.json` 都带 `oralReleaseForms`，取值只出现 `IMMEDIATE_RELEASE` 与
  `EXTENDED_RELEASE`，解析器与 `ReleaseForm.fromName` 一一对应。
- 语言键：`release_form_title` / `release_form_unspecified` / `release_form_immediate` /
  `release_form_extended` / `release_form_hint` / `release_form_no_reference` 在
  `assets/lang/{en_us,zh_cn,zh_tw}.json` 中各 6 条齐全，无 `missing_key`。
- JVM 测试：`gradlew.bat testDebugUnitTest` 全绿（47 个用例、0 失败，2026-09-12 实测），覆盖目录字段解析
  与导出 / 导入的剂型往返。

> 记录：原实现另在 Android API 28 模拟器上验证过 v7→v8 迁移（旧记录 `releaseForm` 为 `NULL`，
> 剂量与单位保留），以及新增缓释记录、改回普通剂型、同剂量两种剂型的快捷记录区分与恢复。
