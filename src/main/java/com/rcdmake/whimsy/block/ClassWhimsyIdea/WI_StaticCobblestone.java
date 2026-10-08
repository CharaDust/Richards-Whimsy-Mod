package com.rcdmake.whimsy.block.ClassWhimsyIdea;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 固石（{@code static_cobblestone} → {@code wi_static_cobblestone}）—— 由模板类 {@code ModBlocks} 迁入 WI 模块。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassWhimsyIdea/WI_StaticCobblestone.md}）：
 * <ul>
 *   <li>父级：石头（属性表逐行都是「父级」）</li>
 *   <li>渲染：常规；改写方法：无；事件：无；方块状态：无（单状态）</li>
 *   <li>获取方式：留待「迁移后统一设定」（本阶段不产出战利品表 / 挖掘标签）</li>
 * </ul>
 *
 * <h2>迁移要点</h2>
 * <ul>
 *   <li><b>注册名补模块前缀</b>：{@code static_cobblestone} → {@code wi_static_cobblestone}
 *       （wiki 备注：命名参照同模块的 {@code wi_static_liquid_water}）。</li>
 *   <li>「父级 = 石头」= 26.2 的 {@code BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)}
 *       （旧 Yarn 写法 {@code FabricBlockSettings.copyOf(Blocks.STONE)}）。</li>
 *   <li>方块物品的注册留在本类内（项目约定），不另建物品类。</li>
 *   <li>迁入后需同步 WI 创造模式物品栏的包含列表（已在本阶段一并更新）。</li>
 * </ul>
 *
 * <p>本类没有事件、没有状态、没有改写方法，因此除了属性复制与注册之外没有别的代码 —— 这是刻意的：
 * 模板遗留方块的价值就是「跟石头一模一样」。
 */
public class WI_StaticCobblestone extends Block {

	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block WI_STATIC_COBBLESTONE;

	public WI_StaticCobblestone(BlockBehaviour.Properties properties) {
		super(properties);
	}

	/** 固石的属性：完整复制石头。 */
	public static BlockBehaviour.Properties staticCobblestoneProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE);
	}

	public static void OnInit() {
		WI_STATIC_COBBLESTONE = ModRegister.registerBlockWithItem("wi_static_cobblestone",
				new WI_StaticCobblestone(staticCobblestoneProperties()
						.setId(ModRegister.blockKey("wi_static_cobblestone"))));
	}
}
