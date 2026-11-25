package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 加固平台（{@code bhr_reinforced_platform}）—— 木工台的产物之一（Blockheads 的 Reinforced Platform）。
 *
 * <h2>规格</h2>
 * <ul>
 *   <li>普通整方块（用户口径：「按照普通方块添加就是」），六面同一张贴图；</li>
 *   <li>属性复制原版木板（爆炸抗性 / 音效跟着走），硬度稍高（木 + 铁）；</li>
 *   <li>获取：木工台 L1 —— 木板 ×1 + 铁锭 ×1 → 5 个（专家模式：木板 ×5 + 铁锭 ×1 → 5 个）。</li>
 * </ul>
 *
 * <p>登记点：主类方块区、{@code BlockStateGenerator.collectAll()}（单状态，blockstate 自动生成）、
 * 手写 {@code models/block/bhr_reinforced_platform.json}（cube_all）、中英 lang、创造栏（树木组）。
 */
public class BHR_ReinforcedPlatform extends Block {

	/** 注册路径。 */
	public static final String REGISTRY_NAME = "bhr_reinforced_platform";

	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block BHR_REINFORCED_PLATFORM;

	/** 属性：复制原版木板，硬度略高（木 + 铁）。 */
	public static BlockBehaviour.Properties platformProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
				.strength(2.0F, 6.0F);
	}

	public BHR_ReinforcedPlatform(BlockBehaviour.Properties properties) {
		super(properties);
	}

	/** 注册方块 + 方块物品。 */
	public static void OnInit() {
		BHR_REINFORCED_PLATFORM = ModRegister.registerBlockWithItem(REGISTRY_NAME,
				new BHR_ReinforcedPlatform(platformProperties().setId(ModRegister.blockKey(REGISTRY_NAME))));
	}
}
