# DeepSeek 智能体文档索引 / DeepSeek Agent Docs

> 本目录用于存放由 **DeepSeek（deepseek-flash）** 智能体生成的说明文档、勘察报告与描述性配置文件。
> This folder holds explanatory documents, survey reports and descriptive config files produced by the **DeepSeek** agent.

## 1. 目录约定 / Folder conventions

| 路径 / Path | 用途 / Purpose |
| --- | --- |
| `doc/agent/deepseek/` | DeepSeek 智能体产出的一切文档与描述文件 / everything the DeepSeek agent writes |
| `doc/agent/<其他品牌>/` | 其他品牌智能体各自独立的文档空间（预留）/ reserved for other agent brands |
| `doc/` 其他子目录 | 人类可读的项目正式文档 / human-facing project docs |

约束 / Rules:

1. 智能体**不修改**其他品牌智能体的目录，也不修改 `doc/` 下的人类文档（除非用户明确要求）。
   Agents never edit another brand's folder, nor human docs under `doc/` unless explicitly asked.
2. 本项目后续会引入其他品牌的 AI，因此本目录内的文档必须**自包含**：任何结论都要写清依据（文件路径 + 行号、命令、外部链接），不能依赖"我上次说过"。
   Because other AI brands will join later, every doc here must be **self-contained**: state evidence (file path + line, command, external link) for each conclusion.
3. 结论若来自外部资料，需给出可点击的来源链接，并区分「已核实 / 未核实（UNVERIFIED）」。
   External facts need clickable sources, marked verified vs UNVERIFIED.
4. 面向用户的描述文件（如 `fabric.mod.json` 文案、README 段落草稿）也放在本目录，由人类确认后再落地到正式位置。
   Descriptive files aimed at users (e.g. proposed `fabric.mod.json` text) also live here until a human approves moving them.

## 2. 文档清单 / Document index

| 编号 | 文件 | 内容 |
| --- | --- | --- |
| 00 | [README.md](./README.md) | 本索引与协作约定 |
| 01 | [01-环境勘察报告.md](./01-环境勘察报告.md) | 项目结构、工具链、源码/资源清单、API 依赖面、当前开发环境缺口 |
| 02 | [02-升级路线图-1.20.4-to-26.2.md](./02-升级路线图-1.20.4-to-26.2.md) | 从 1.20.4 迁移到 26.2 的版本事实、破坏性变更清单、分阶段路线与验收标准 |
| 03 | [03-重制方案可行性评价.md](./03-重制方案可行性评价.md) | 对「放弃阶梯迁移、直接重制所有功能」的可行性评价（依据 `doc/wiki/` 与 `block/ClassRandomThings`、`block/ClassWhimsyIdea`） |
| 04 | [04-文档缺口清单.md](./04-文档缺口清单.md) | 已写 wiki 文档（24 份方块 + 3 份方块实体）相对「重制所需规格」的欠缺清单：模板级欠缺、逐条欠缺、文档与代码不一致 |
| 05 | [05-文档补全任务列表.md](./05-文档补全任务列表.md) | 待用户决定的文档补全/优化任务列表：6 项决策、12 项任务、缺失章节清单（尚未执行） |
| 06 | [06-注册名对照表.md](./06-注册名对照表.md) | 全模块注册名对照（文档页 ↔ Java 类 ↔ 现名 ↔ 目标名）、15 条改名决定、豁免清单 |
| 07 | [07-文档补全执行报告.md](./07-文档补全执行报告.md) | T1–T11 执行结果、新建 33 份后继页、需复核的 3 处、顺带发现的事实 |
| 08 | [08-IDEA设置指南-26.2骨架.md](./08-IDEA设置指南-26.2骨架.md) | **给人看的操作步骤**：IDEA 里配 JDK 25 / Gradle JVM / `genSources` / 旧代码排除 / 运行配置 |
| 09 | [09-26.2骨架交付报告.md](./09-26.2骨架交付报告.md) | **26.2 骨架交付与可行性结论**：改动清单、实测 API 结论、8 个已修的坑、下一轮重制流程 |
| 10 | [10-方块类范式V1统一计划.md](./10-方块类范式V1统一计划.md) | **方块类范式 V1 的统一（归一）计划**：不变量、按模块的改动量排序（阶段 0 公共小件 → WI → RDT → DEV）、每阶段的改动清单/风险/游戏内测试清单 |

## 3. 当前状态速览 / Status at a glance

> **2026-10-07 更新（见 09 号文档 §0）：旧代码已全部迁入 `src/main/javaref/`，
> 骨架退场，DEV 分支首批内容落地。**
> `DEV_BlocksOther`（原 `DEV_Blocks`）与 `DEV_ItemsOther`（原 `DEVItems`）已按
> `doc/agent/humanidea/开发约定.md` 重制完成，注册核心收口到 `com.rcdmake.whimsy.ModRegister`。
> `gradlew build` 与 `gradlew runDatagen` 均通过，产出 2 张战利品表。

- 项目现版本：`richards-whimsy-mod` **0.3-26.2**，Fabric，Minecraft **26.2**，Loader **0.19.5**，
  Fabric API **0.161.0+26.2**，Loom **1.18.2**（新 plugin id `net.fabricmc.fabric-loom`），
  Gradle **9.7.1**，Java **25**（**无 mappings 行** —— 26.1 起游戏去混淆、Yarn 停更）。
- 旧代码状态：**已全部迁入 `src/main/javaref/`**（87 个 1.20.4 时代文件），
  `build.gradle` 也已切为迁移后版本（那 10 行 `exclude` 已删除）。`src/main/java` 只剩新代码。
- 环境状态：**JDK 25（IDEA 自带 JBR 25.0.4）+ Gradle 9.7.1 + 约 0.4 GB 依赖缓存均已就位，
  可离线构建。无需再安装任何东西。**
- 新代码规模：8 个类（注册核心 + 物品栏 + 3 个入口 + 2 个 DEV 分支类 + 1 个 datagen provider），
  作为后续逐条重制的模板。
- **当前源码结构（`src/main/java/com/rcdmake/whimsy/`）**：
  `ModRegister`（注册核心）、`ModCreativeTabs`（物品栏汇总入口）、三个入口类，
  `block/ClassDEV/DEV_BlocksOther`、`item/ClassDEV/DEV_ItemsOther`，
  `item/{BHR,DEV,RDT,WI,LGC}_CreativeTabs`（各模块物品栏，除 DEV 外暂空），
  `skeleton/datagen/ModBlockLootProvider`（datagen 入口引用；`skeleton/` 其余文件待删）。
- 物品栏 id 已按 wiki 整改：`whimsy_group_dev` → **`dev_general`**。

### 3.1 历史状态（2026-10-06 骨架落地前的勘察，部分已过时）

- 规模：90 个 Java 文件 / 约 4,598 行；81 处 `Registry.register`（29 方块、37 物品、5 物品栏、4 状态效果、2 方块实体、1 容器界面、1 配方类型 + 1 配方序列化器）。
- 目标：升级到 **Minecraft 26.2**（"Chaos Cubed"，新的年份版本号体系）。
- 环境缺口（**已解决**）：当时本机无独立 JDK、`%USERPROFILE%\.gradle` 不存在、
  PowerShell 子进程无对外 HTTPS 能力。现在 JDK 25 用 IDEA 自带 JBR 解决，
  Gradle 发行版与依赖缓存已落地，构建已实测跑通（任务由主智能体经宿主 Gradle 执行器完成）。
