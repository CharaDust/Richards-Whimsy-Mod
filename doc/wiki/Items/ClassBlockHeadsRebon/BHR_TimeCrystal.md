# 时间水晶（物品）
{`bhr_time_crystal`}
## 特点简介
制作台加速用的耗材。每一个水晶可以跳过 **10 秒**制作时间；最大堆叠 **99**（不是原版 64）。
放在制作台界面右下角的「时间水晶放置处」使用，退出界面时会立即退回背包（背包满则留在原地）。

## 构造
| 条目 | 值 |
| --- | --- |
| 最大堆叠 | **99**（`Item.Properties#stacksTo(99)`） |
| 单跳时间 | 10 秒（`BHR_TimeCrystal.SECONDS_PER_CRYSTAL`） |
| 组件 | 无 |

## 获取方式
- 待定（属于「获取方式」阶段）。目前从方块愚者物品栏可以直接取到。

## 备注
- ⚠️ **堆叠上限必须在注册时套到 `ModRegister` 递进来的那份 `Item.Properties` 上**：
  它内部已经调用过 `setId(key)`，自己另 `new Item.Properties()` 会丢掉物品 id。
  实测踩过：早期版本另起了一份属性，游戏里水晶最大堆叠仍然是 64。
- 贴图 `textures/item/bhr/time_crystal.png`；模型 `models/item/bhr_time_crystal.json`（`item/generated`）；
  `items/bhr_time_crystal.json` 由 datagen 生成。
- 界面里的用法（放置 → 加速 / 退出返还）随制作台界面的第二阶段一起实现。
