package com.rcdmake.whimsy.block.ClassRandomThings;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 超级滑滑冰（{@code rdt_super_lubricent_ice}）—— 由旧项目 {@code RDT_SuperLubricentIce} 迁来。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassRandomThings/RDT_SuperLubricentIce.md}）：
 * <ul>
 *   <li>父级：蓝冰；除「表面阻力系数」外，属性表逐行都是「父级」</li>
 *   <li>表面阻力系数一行带星号：wiki 自己注明「此数值不准确，需要使用其他方法实现无阻力」</li>
 *   <li>地图颜色「父级*」（待定，该方块为蓝绿色）、渲染：常规、改写方法/事件/方块状态：无</li>
 *   <li>获取方式：无</li>
 * </ul>
 *
 * <h2>「滑行而不改变速度」在 26.2 怎么实现（本轮重做）</h2>
 * 水平衰减的公式是（{@code LivingEntity.travelInAir} 尾部，逐字节读过）：
 * <pre>{@code
 * f3    = onGround() ? computeModifiedFriction(方块摩擦, FRICTION_MODIFIER属性) : 1.0f
 * horiz = f3 * computeModifiedFriction(0.91f, AIR_DRAG_MODIFIER属性)     // 属性默认 1.0 → ×0.91
 * 水平速度 ×= horiz
 * }</pre>
 * 要「不改变速度」需要 {@code f3 = 1/0.91 = 1.0989}（旧代码那个数就是这么来的），
 * 但 26.2 的 {@code computeModifiedFriction(a,b) = clamp(1-(1-a)*b, 0, 1)} <b>把结果封顶在 1.0</b>，
 * 于是属性这条路到顶也就是 {@code friction(1.0F)}、每 Tick 仍掉 9%。
 * 所以本类：
 * <ol>
 *   <li>属性写 {@code friction(1.0F)} —— 原版允许的最滑档位（缺省即「无阻力」的封顶值）；</li>
 *   <li>真正的「零衰减」由 {@code com.rcdmake.whimsy.mixin.ClassRandomThings.RDT_SuperLubricentIceFrictionMixin}
 *       在那唯一的调用点补上那 9%（该 Mixin 里写了完整推导与风险说明）。</li>
 * </ol>
 *
 * <h2>1.20.4 (Yarn) → 26.2 官方名对照</h2>
 * <table border="1">
 *   <tr><th>Yarn（旧）</th><th>26.2 官方名</th><th>说明</th></tr>
 *   <tr><td>{@code FabricBlockSettings.copyOf(Blocks.BLUE_ICE)}</td>
 *       <td>{@code BlockBehaviour.Properties.ofFullCopy(Blocks.BLUE_ICE)}</td><td>「父级 = 蓝冰」</td></tr>
 *   <tr><td>{@code slipperiness(1.0989f)}</td><td>{@code friction(F)}</td>
 *       <td>地面摩擦。旧值 1.0989 在 26.2 会被 clamp 成 1.0（理由见上），故改为显式的 1.0F</td></tr>
 * </table>
 *
 * <p>地图颜色：wiki 写「父级*（待定，该方块为蓝绿色）」，本阶段沿用蓝冰的地图色，
 * 待你给出确切值再改（贴图本身已是蓝绿色）。
 */
public class RDT_SuperLubricentIce extends Block {

	/**
	 * 表面阻力系数：26.2 允许的最大值（= 原版「无阻力」封顶）。
	 * <p>原版蓝冰是 0.989、普通方块约 0.6、冰 0.98；1.0 已经把「每 Tick 衰减」压到最小
	 * （0.91），剩下的那一点由配套的 Mixin 抹平。
	 */
	private static final float FRICTION = 1.0F;

	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block RDT_SUPER_LUBRICENT_ICE;

	public RDT_SuperLubricentIce(BlockBehaviour.Properties properties) {
		super(properties);
	}

	/** 超级滑滑冰的属性：复制蓝冰 + 表面阻力系数取 26.2 的封顶值。 */
	public static BlockBehaviour.Properties superLubricentIceProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.BLUE_ICE)
				.friction(FRICTION);
	}

	public static void OnInit() {
		RDT_SUPER_LUBRICENT_ICE = ModRegister.registerBlockWithItem("rdt_super_lubricent_ice",
				new RDT_SuperLubricentIce(superLubricentIceProperties()
						.setId(ModRegister.blockKey("rdt_super_lubricent_ice"))));
	}
}
