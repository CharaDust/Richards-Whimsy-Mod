# 加固平台（方块）

> 类：`BHR_ReinforcedPlatform`　注册名：`bhr_reinforced_platform`　模块：BHR
> 状态：**2026-12 新增**
> 父级：`Blocks.OAK_PLANKS`（`BlockBehaviour.Properties.ofFullCopy`），`strength(2.0F, 6.0F)`

## 基本

- 中文名：加固平台　英文名：Reinforced Platform
- 创造模式物品栏：方块愚者（`bhr_general`）→ 紧接木工台之后
- Blockheads 的 Reinforced Platform：木工台的产物，普通整方块（不是薄板、不走台阶逻辑）
- 外观：六面同一张贴图 `bhr/reinforced_platform`

## 构造（属性表）

| 项 | 值 |
| --- | --- |
| 父级 | 橡木木板（`Blocks.OAK_PLANKS`） |
| 硬度 / 爆炸抗性 | 2.0 / 6.0（木 + 铁，比木板硬） |
| 音效 | 父级（木质） |
| 渲染 | 常规整方块 |
| 方块状态 | 无（单状态） |
| 改写方法 | 无 |
| 事件 | 无 |

## 获取方式

### 战利品表

#### 采集

- 挖掘加固平台的合适工具：待统一设定（当前继承木板的徒手可挖）
- 加固平台被破坏后掉落：自身 ×1

### 其它来源

- **木工台 L1**：木板×1 + 铁锭×1 → **5 个**，20 秒；专家模式木板×5 + 铁锭×1 → 5 个。

## 资源

| 类型 | 文件 |
| --- | --- |
| 贴图 | `textures/block/bhr/reinforced_platform.png` |
| 模型 | `models/block/bhr_reinforced_platform.json`（`cube_all`） |
| blockstate | `blockstates/bhr_reinforced_platform.json`（**datagen 生成**） |
| 物品模型定义 | `items/bhr_reinforced_platform.json`（datagen 生成） |

## 登记点

1. ✅ 主类（在 `BHR_WoodworkBench.OnInit()` 之后）
2. ✅ `datagen/BlockStateGenerator.collectAll()` —— 单状态，blockstate 自动产出
3. ☐ `ItemModelGenerator.ITEM_MODEL_OVERRIDES` —— 不需要
4. ✅ `BHR_LangEntries`（中英）
5. ✅ `BHR_CreativeTabs`
6. ✅ 资源
7. ✅ 木工台配方（普通 + 专家各一条）
