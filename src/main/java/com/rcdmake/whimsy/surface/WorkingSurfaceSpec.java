package com.rcdmake.whimsy.surface;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.Supplier;

/**
 * 一台<b>工作面（Working Surface）</b>的规格。
 *
 * <p>The Blockheads 的 wiki 把 Workbench / Campfire / Woodwork Bench 这类"能拿材料换产物的方块"
 * 统称为 Working Surface。本项目照此把它们的公共逻辑收进 {@code surface} 包，
 * 每一台只在 {@code WorkingSurfaceSpec} 里声明差异：
 *
 * <table border="1">
 *   <caption>规格字段</caption>
 *   <tr><th>字段</th><th>含义</th><th>制作台</th><th>木工台</th><th>简易篝火</th></tr>
 *   <tr><td>{@code id}</td><td>注册名 / 配方分组键 / 菜单注册名</td><td>bhr_workbench</td><td>bhr_woodwork_bench</td><td>bhr_campfire</td></tr>
 *   <tr><td>{@code guiTexture}</td><td>界面底图</td><td>workbench</td><td>workbench</td><td>fuel_workbench</td></tr>
 *   <tr><td>{@code levelMax}</td><td>等级上限（下限恒为 1）</td><td>29</td><td>3</td><td>2</td></tr>
 *   <tr><td>{@code fuelSlots}</td><td>燃料档位数，0 = 这台不烧燃料</td><td>0</td><td>0</td><td>3</td></tr>
 *   <tr><td>{@code panelShift}</td><td>需求格 / 水晶 / 结果 / 倍率 的整体 x 偏移</td><td>0</td><td>0</td><td>18</td></tr>
 * </table>
 *
 * <p><b>等级为什么是「方块状态」而不是方块实体字段</b>：等级同时决定贴图（模型按变体选）与
 * 掉落物上的组件，两者都发生在「引擎按方块状态查表」的路径上。所以等级落在共用属性
 * {@link WorkingSurfaceBlock#LEVEL}（1~29）上，每台用 {@link #levelMax()} 自己夹取。
 *
 * @param id            注册名，同时是配方目录名（{@code bhr_recipe/<id>/}）与菜单类型注册名
 * @param guiTexture    界面底图的资源路径（相对 {@code assets/richards-whimsy-mod/}）
 * @param levelMax      等级上限（含）；下限恒为 {@link #LEVEL_MIN}
 * @param fuelSlots     燃料档位数；0 表示这台不烧燃料（界面不画燃料列、进度不会因缺燃料暂停）
 * @param fuelUnitTicks 一个「燃料单位」等于多少 tick（{@code fuelSlots = 0} 时无意义）
 * @param fuelMaxTicks  燃料槽的上限 tick（{@code fuelSlots = 0} 时无意义）
 * @param panelShift    需求九宫格 / 水晶 / 结果 / 倍率的整体 x 偏移（有燃料列的底图要右移）
 * @param fuelTable     燃料档位表的<b>懒构建</b>入口（{@code null} = 无燃料）。
 *                      必须懒构建：燃料表里可能引用在方块之后才注册的物品（如松果）。
 */
public record WorkingSurfaceSpec(
		String id,
		String guiTexture,
		int levelMax,
		int fuelSlots,
		int fuelUnitTicks,
		int fuelMaxTicks,
		int panelShift,
		Supplier<List<FuelOption>> fuelTable) {

	/** 等级下限（所有工作面共用）。 */
	public static final int LEVEL_MIN = 1;

	/** 等级上限的物理值（方块状态的取值范围；每台再用 {@link #levelMax()} 夹一次）。 */
	public static final int LEVEL_ABS_MAX = 29;

	/** 这台工作面是否烧燃料。 */
	public boolean hasFuel() {
		return this.fuelSlots > 0;
	}

	/** 界面底图的完整 {@link Identifier}。 */
	public Identifier texture() {
		return ModRegister.id(this.guiTexture);
	}

	/** 界面标题的语言键。 */
	public String titleKey() {
		return "container." + "richards-whimsy-mod." + this.id;
	}

	/**
	 * 界面标题 —— <b>一律带等级</b>。
	 *
	 * <p>三台统一（篝火原先漏了等级参数，本次合并顺手补上）。
	 */
	public Component titleFor(int level) {
		return Component.translatable(this.titleKey(), level);
	}

	/** 燃料档位表（懒构建；无燃料的机器返回空表）。 */
	public List<FuelOption> fuels() {
		return this.fuelTable == null ? List.of() : this.fuelTable.get();
	}

	/** 第 {@code index} 档燃料；越界或没有燃料返回 {@code null}。 */
	public FuelOption fuel(int index) {
		List<FuelOption> list = this.fuels();
		return index >= 0 && index < list.size() ? list.get(index) : null;
	}

	/** 一档燃料折算多少 tick。 */
	public int ticksPerUnit() {
		return this.fuelUnitTicks;
	}
}
