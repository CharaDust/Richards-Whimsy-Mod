# 制作台等级（组件）
{`richards-whimsy-mod:workbench_level`}
## 特点简介
携带「这台制作台是第几级」的组件，范围 **1~29**。它一件顶四件事：挖取保等级、放置还原、
同级堆叠 / 异级不堆叠、**物品图标与描述栏都跟着等级走**。

## 构造
```
components（compound）
	richards-whimsy-mod:workbench_level（int）表示等级，取值 1~29
```
- 载荷在 JSON / NBT / 指令里都是**裸整数**（`Codec.INT`），不是 `{"level": 9}`。
  Java 侧是 `record Level(int value)`，只是为了能实现 `TooltipProvider`；
  codec 用 `Codec.INT.xmap(...)` 把 record 与整数对起来（网络侧同理，`VAR_INT.map(...)`）。
- 归属物件：`bhr_workbench` 的方块物品（见 [[BHR_WorkBench]]）。
- **1 级靠原型组件供给**：方块物品的属性里写了
  `Item.Properties#component(组件, new Level(1))`，所以新建出来的物品堆读到的就是 1 级。
- 读取侧统一 `null → 1`，越界值夹到 1~29（手改存档 / 指令写坏数据时不会在放置时抛异常）。

## 改写方法
- 写入：`BHR_WorkBenchLevel.set(stack, level)`。**等级为 1 时什么都不做**
  （原型已给 1 级，不碰补丁才等价于「新堆」）。
  ⚠️ 不能写成 `stack.remove(组件)`：原型里有该组件时，「移除」会在补丁里留下「显式没有」的记号，
  物品堆与新堆不再相同 → 堆叠分裂（26.2 实测 `isSameItemSameComponents` = false）。
- 读取：`BHR_WorkBenchLevel.levelOf(stack)`（`stack` 为 `null` 时返回 1）。
- **图标**：`items/bhr_workbench.json` 用
  `minecraft:select` + `property: "minecraft:component"` + `component: richards-whimsy-mod:workbench_level`，
  29 个 case 的 `when` 直接写整数（因为本组件的持久化 codec 是 `Codec.INT`）。
  这套判定**没有客户端代码**，纯资源 + 组件 codec。
- **描述栏**：`Level` 实现 `TooltipProvider`，注册时经 Fabric 的
  `ItemComponentTooltipProviderRegistry.addAfter(DataComponents.LORE, 组件)` 挂在 lore 之后，
  输出灰色斜体的「等级：N」（语言键 `tooltip.richards-whimsy-mod.workbench_level`）。

## 备注
- **已实现**（2026-10）。注册在 `BuiltInRegistries.DATA_COMPONENT_TYPE`，由
  `BHR_WorkBench.OnInit()` 在方块 / 方块物品注册**之前**调用（物品属性要用到它）。
- 客户端实测（26.2）：`items/bhr_workbench.json` 烘焙成
  `SelectItemModel`，其 property 打印为
  `ComponentContents[componentType=richards-whimsy-mod:workbench_level]` —— 即
  `minecraft:component` 正确绑到了本组件，且没有模型加载报错。
- 服务端实测：`createStack(1)` 与 `new ItemStack(物品)` 逐组件相同（可堆叠）；
  5 级与 6 级不同堆；7 级方块挖下来的掉落物读回 7 级；
  tooltip 行是 `tooltip.richards-whimsy-mod.workbench_level` + 参数（1 / 5）。
