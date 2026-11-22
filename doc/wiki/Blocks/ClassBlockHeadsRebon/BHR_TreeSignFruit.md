# 果木告示牌（方块）

> 类：`BHR_TreeSignFruit`　注册名：`bhr_fruit_sign`（立牌）+ `bhr_fruit_wall_sign`（墙牌）　模块：BHR
> 状态：**2026-11 新增**，照搬原版橡木对应方块。
> 父级：`Blocks.OAK_SIGN` / `Blocks.OAK_WALL_SIGN`，属性分别 `ofFullCopy` 自这两块。

## 基本

- 中文名：果木告示牌 / 果木墙牌　英文名：Fruit Wood Sign / Fruit Wood Wall Sign
- 创造模式物品栏：方块愚者（`bhr_general`）→ 树木组，排在果木活板门之后、松木树叶之前
- 外观：牌面贴图 `bhr/fruit_sign`；写满字后按原版规则显示荧光墨水色
- **一块物品、两个方块**：物品只有「果木告示牌」一件（`SignItem`，堆叠 16），
  放下时按朝向自动变成立牌或墙牌；墙牌**没有**对应物品，也不进创造栏。

## 构造（属性表）

| 项 | 值 |
| --- | --- |
| 父级 | 橡木告示牌 / 橡木墙牌（`Blocks.OAK_SIGN` / `OAK_WALL_SIGN`） |
| 硬度 / 爆炸抗性 | 父级（1 / 1，徒手可破坏） |
| 音效 | 父级（木质） |
| 方块状态 | 立牌：`ROTATION`（0–15）+ `WATERLOGGED`；墙牌：`FACING` + `WATERLOGGED` |
| 改写方法 | 两个子类都覆写 `newBlockEntity`、`getTicker`；墙牌属性额外 `overrideDescription` |
| 方块实体 | `BHR_FRUIT_SIGN_ENTITY`（自有 `BlockEntityType<SignBlockEntity>`） |

- 结构：`BHR_TreeSignFruit` 下内嵌两个静态子类 `Standing`（继承 `StandingSignBlock`）与
  `Wall`（继承 `WallSignBlock`），构造器额外参数都是 `WoodType.OAK`。
- 可燃性：两块各登记一次 `FlammableBlockRegistry.getDefaultInstance().add(…, 5, 20);`

## 三个必须记住的坑（2026-11 实测）

1. **方块实体类型必须自己的**。`SignBlock#newBlockEntity` 默认造的是
   `BlockEntityType.SIGN` 的实体，而 `SIGN` 的 `validBlocks` 里没有我们的方块 ——
   存档读取时方块实体会被丢弃，**牌上的字就丢了**。所以两个子类都覆写了 `newBlockEntity`，
   用 26.2 提供的 `SignBlockEntity(BlockEntityType<? extends SignBlockEntity>, BlockPos, BlockState)`
   构造器传入 `BHR_FRUIT_SIGN_ENTITY`。
2. **`getTicker` 也要覆写**。默认 ticker 认的是 `SIGN` 类型，不覆写则编辑锁
   `playerWhoMayEdit` 不会被清理，玩家下线后牌子长时间「被别人编辑中」。
3. **客户端渲染器要补**。`BlockEntityRenderers.register(BHR_FRUIT_SIGN_ENTITY, StandingSignRenderer::new)`
   写在客户端入口 `RichardsWhimsyModClient` —— 原版只把 `StandingSignRenderer` 注册给
   `BlockEntityTypes.SIGN`，不补这一段则**牌面在、字看不见**。

### 墙牌的「翻译键」坑

26.2 原版 `Blocks.OAK_WALL_SIGN` 的属性是用 `wallVariant(OAK_SIGN, true)` 从**立牌**派生的，
`ofFullCopy` 会把「翻译键」一并复制过来，实测 `getDescriptionId()` 变成 `block.minecraft.oak_sign`
（即墙牌显示成「橡木告示牌」）。所以 `fruitWallSignProperties()` 里追加了：

```
.overrideDescription("block.richards-whimsy-mod.bhr_fruit_wall_sign")
```

把它钉回自己的语言键（对应 lang 里的「果木墙牌」）。

## 标签

| 标签 | 落地文件 | 追加内容 |
| --- | --- | --- |
| `#minecraft:standing_signs`（方块） | `data/minecraft/tags/block/standing_signs.json` | `bhr_fruit_sign` |
| `#minecraft:wall_signs`（方块） | `data/minecraft/tags/block/wall_signs.json` | `bhr_fruit_wall_sign` |
| `#minecraft:signs`（物品） | `data/minecraft/tags/item/signs.json` | `bhr_fruit_sign` |

- 三份都用 `"replace": false` **追加**；物品侧只登记立牌（墙牌无物品）。
- 「斧可挖掘」经原版 `mineable/axe.json` 里的 `#minecraft:standing_signs` /
  `#minecraft:wall_signs` 标签引用**传递命中**。

## 资源

| 类型 | 文件 |
| --- | --- |
| 贴图 | `textures/block/bhr/fruit_sign.png`；物品图 `textures/item/bhr/fruit_sign.png` |
| 模型 | `models/block/bhr_fruit_sign_rot_{0,1,2,3}.json` + `bhr_fruit_wall_sign.json`（5 个）；物品模型 `models/item/bhr_fruit_sign.json` |
| blockstate | `blockstates/bhr_fruit_sign.json`（16 个 variants，四个模型轮转）、`blockstates/bhr_fruit_wall_sign.json`（4 个，手写） |
| 物品模型定义 | `items/bhr_fruit_sign.json` → **`item/bhr_fruit_sign`**（例外，2D 图） |

## 登记点

1. ✅ 主类 `RichardsWhimsyMod.onInitialize()` → `BHR_TreeSignFruit.OnInit();`（含方块实体类型与 `SignItem`）
2. ✗ `datagen/BlockStateGenerator` —— 多状态，两个 blockstate 均手写（照抄 `oak_sign` / `oak_wall_sign`）
3. ✅ `ItemModelGenerator.ITEM_MODEL_OVERRIDES`：`bhr_fruit_sign → item/bhr_fruit_sign`
4. ✅ `datagen/ClassBlockHeadsRebon/BHR_LangEntries`（`block.` 三条含墙牌 + `item.` 一条）
5. ✅ `item/BHR_CreativeTabs` 的「树木」组（只放立牌物品）
6. ✅ 资源（贴图 + 5 个方块模型 + 物品图 + 2 个 blockstate + items）
7. ◐ 标签已落地（见上）；配方 / 战利品表暂缓

## 同家族

[果木木板](BHR_TreePlanksFruit.md)　[果木台阶](BHR_TreeSlabFruit.md)　[果木楼梯](BHR_TreeStairsFruit.md)
[果木栅栏](BHR_TreeFenceFruit.md)　[果木栅栏门](BHR_TreeFenceGateFruit.md)　[果木门](BHR_TreeDoorFruit.md)
[果木活板门](BHR_TreeTrapdoorFruit.md)
