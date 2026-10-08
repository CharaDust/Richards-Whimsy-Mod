package com.rcdmake.whimsy.block.ClassWhimsyIdea;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 包装木材（{@code packed_planks} → {@code wi_packed_planks}）—— 由模板类 {@code ModBlocks} 迁入 WI 模块。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassWhimsyIdea/WI_PackedPlanks.md}）：
 * <ul>
 *   <li>父级：橡木木板（属性表逐行都是「父级」）</li>
 *   <li>渲染：常规；改写方法：无；事件：无；方块状态：无（单状态）</li>
 *   <li>获取方式：留待「迁移后统一设定」（本阶段不产出战利品表 / 挖掘标签）</li>
 *   <li>创造模式物品栏：<b>WI 方块页的图标就是本方块</b>（见 {@code WI_General}）</li>
 * </ul>
 *
 * <h2>迁移要点</h2>
 * <ul>
 *   <li><b>注册名补模块前缀</b>：{@code packed_planks} → {@code wi_packed_planks}。</li>
 *   <li>「父级 = 橡木木板」= 26.2 的 {@code BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)}
 *       （旧 Yarn 写法 {@code FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)}）。</li>
 * </ul>
 */
public class WI_PackedPlanks extends Block {

	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block WI_PACKED_PLANKS;

	public WI_PackedPlanks(BlockBehaviour.Properties properties) {
		super(properties);
	}

	/** 包装木材的属性：完整复制橡木木板（含其木质可燃性等）。 */
	public static BlockBehaviour.Properties packedPlanksProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS);
	}

	public static void OnInit() {
		WI_PACKED_PLANKS = ModRegister.registerBlockWithItem("wi_packed_planks",
				new WI_PackedPlanks(packedPlanksProperties()
						.setId(ModRegister.blockKey("wi_packed_planks"))));
	}
}
