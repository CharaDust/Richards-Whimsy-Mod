# 传送门（Portal）配方 · 人工核对版

> 来源：玩家在游戏内逐条核对（2026-11-20）。**这是目前唯一经过人工验证的工作台**。
> 二进制表位置：专家 `0xE4B91C`、普通 `0xE4DBFC`。

## 关于「等级」

传送门界面按**固定顺序**列出项目，等级由位置决定（每级一块）。
**唯一例外**：专家模式下的 `Teleport Here`，在传送门未满 6 级时会显示成**不可合成的按钮**，
但它仍排在列表靠前的位置 —— 这是特例，不代表整体顺序失效。

## 普通模式

| # | 项目 | 配方 | 耗时 |
| --- | --- | --- | --- |
| 1 | Workbench | 1 Dirt | 10s |
| 2 | Blockhead | 可变配方 | — |
| 3 | Double-Time | 内购，非配方 | — |
| 4 | Time Crystal | 内购，非配方 | — |
| 5 | Teleport Here | 功能 | — |
| 6 | Copper Coins ×100 | 1 Gold Coin | 1s |
| 7 | Gold Coin | 100 Copper Coins | 1s |
| 8 | Gold Coins ×100 | 1 Platinum Coin | 1s |
| 9 | Platinum Coin | 100 Gold Coins | 1s |
| 10 | Trade Portal | 1 Portal + 1 Gold Coin + 50 Time Crystals | 5s |
| 11 | Ownership Sign | 1 Sign + 3 Gold Ingots | 10s |
| 12 | Amethyst Upgrade | 1 Amethyst + 20 Time Crystals | 10s |
| 13 | Amethyst Pickaxe | 5 Amethyst + 20 Time Crystals | 10s |
| 14 | Sapphire Upgrade | 1 Sapphire + 40 Time Crystals | 10s |

> 后续等级按同样的规律：Sapphire/Emerald/Ruby/Diamond 各自「Upgrade + Pickaxe」。

## 专家模式

| # | 项目 | 配方 | 耗时 |
| --- | --- | --- | --- |
| 1 | Workbench | 1 Dirt + 5 Clay + 10 Sticks | 10s |
| 2 | Blockhead | 可变配方 | — |
| 3 | **Portal** | 10 Stone + 5 Iron Ingots + 120 Time Crystals | 180s |
| 4 | Double-Time | 内购，非配方 | — |
| 5 | Time Crystal | 内购，非配方 | — |
| 6 | Teleport Here | 功能 | — |
| 7 | Copper Coins ×100 | 1 Gold Coin | 1s |
| 8 | Gold Coin | 100 Copper Coins | 1s |
| 9 | Gold Coins ×100 | 1 Platinum Coin | 1s |
| 10 | Platinum Coin | 100 Gold Coins | 1s |
| 11 | Ownership Sign | 1 Sign + 3 Gold Ingots | 10s |
| 12 | Amethyst Upgrade | 10 Amethyst + 20 Time Crystals | 10s |
| 13 | Amethyst Pickaxe | 10 Amethyst + 20 Time Crystals | 10s |
| 14 | Sapphire Upgrade | 10 Sapphire + 40 Time Crystals | 10s |
| … | Emerald / Ruby 同理 | | |
| — | Diamond Upgrade | 10 Diamonds + 100 Time Crystals | 10s |
| — | Diamond Pickaxe | （待补） | |
| — | Golden Bed | 10 Gold Blocks + 10 Fur + 100 Time Crystals | 10s |
| 末位 | **Trade Portal** | （待补，位于钻石传送门之后） | |

## 与二进制的差异（已由玩家确认）

1. **Trade Portal 耗时**：普通 **5s**、专家 **10s** —— 二进制一致 ✅
2. **Teleport Here**：确实消耗时间水晶，但数量**取决于与传送门的距离**；二进制里写死的 `200` 只是占位/基准值，不是固定消耗。
3. **专家 Golden Bed**：位于**钻石镐之后、Trade Portal 之前**，已确认。
4. **传送门最高 Level 6**（钻石升级），没有 Level 7。
5. 玩家列表之外的项目，**传送门里不存在**。

二进制侧对应（专家表 `0xE4B91C`）末尾三条：

```
Diamond Pickaxe  <- Diamond×10 + Time Crystal×100   10s
Golden Bed       <- Gold Block×10 + Fur×10 + TC×100 10s
Trade Portal     <- Portal×1 + Gold Coin×100 + TC×50 10s
```

## 由此确认的物品 id

| id | 名称 |
| --- | --- |
| 9 | Double-Time |
| 11 | Time Crystal |
| 19 | Blockhead |
| 72 | Gold Ingot |
| 75 / 76 / 86 / 87 / 88 | Ruby / Emerald / Sapphire / Amethyst / Diamond |
| 94–98 | Amethyst / Sapphire / Emerald / Ruby / Diamond Pickaxe |
| 121 | Fur |
| 132 | UPGRADE（升级标记） |
| 134 | Portal |
| 162 | Teleport Here |
| 163 | Sign |
| 166 / 167 / 260 | Copper Coin / Gold Coin / Platinum Coin |
| 169 | Soft Bed |
| 170 | Golden Bed |
| 210 | Trade Portal |
| 301 | Ownership Sign |
| 1045 | Gold Block |
| 1050 | Workbench |
| 1074 | Portal Chest |
| 1050 | Workbench |
