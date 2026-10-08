# 宝石祝福（组件）
{`richards-whimsy-mod:gem_blessing`}
## 特点简介
一种负责控制工具对附魔的额外修饰的组件

## 构造
```
components（compound）
	richards-whimsy-mod:gem_blessing（compound）表示内含的宝石祝福
		Amethyst（boolean）表示是否具有紫锂辉祝福
		Sapphire（boolean）表示是否具有蓝宝石祝福
		Emerald（boolean）表示是否具有祖母绿祝福
		Ruby（boolean）表示是否具有红刚玉祝福
		Diamond（boolean）表示是否具有宝石钻石祝福
```
- 对于各个名词的解释：
	- 紫锂辉祝福：使得工具的效率魔咒视为等级+1的效果
	- 蓝宝石祝福：使得工具的击退与冲击魔咒视为等级+1的效果
	- 祖母绿祝福：使得工具的耐久魔咒视为等级+1的效果
	- 红刚玉祝福：使得工具的锋利与力量魔咒视为等级+2的效果
	- 宝石钻石祝福：使得工具的时运与抢夺魔咒视为等级+2的效果

## 改写方法
见“构造”说明部分的描述

## 备注
- **整个功能尚未实现**，目前仍在计划中
- 现阶段仅在 [[BHR_GemBlessing_1]] 中测试了紫锂辉祝福的注入，其余四项祝福待后续完善
- 组件字段名 `Amethyst` 保持原名（致敬《The Blockheads》），仅显示名改为「紫锂辉」