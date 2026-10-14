package com.rcdmake.whimsy.item.ClassWhimsyIdea.potion;

/**
 * WI 药水的<b>着色常量</b>：取各自效果的粒子颜色，做到瓶色与粒子一致。
 *
 * <p>这些值同时被 {@code ItemModelGenerator.ITEM_TINT_PREFIXES} 使用（那边按物品 id 前缀查表），
 * 两边必须一致 —— 改色时两处一起改。
 *
 * <h2>为什么单开一个类</h2>
 * <p>瘟疫与放射两个条目类都要拿这些颜色，放在任何一边都会让另一边的依赖方向变得奇怪。
 * 收到共享常量类里，两个条目类各自单向依赖它。
 */
public final class WI_PotionFamily {

	/** 瘟疫药水（生物毒性）：墨绿 {@code 0x007951}。 */
	public static final int PLAGUE_TINT = 0x007951;

	/** 放射药水（电离辐射）：琥珀 {@code 0xF3A923}。 */
	public static final int RADIATION_TINT = 0xF3A923;

	private WI_PotionFamily() {
	}
}
