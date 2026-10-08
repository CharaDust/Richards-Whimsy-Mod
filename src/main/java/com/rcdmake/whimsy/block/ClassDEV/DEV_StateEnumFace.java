package com.rcdmake.whimsy.block.ClassDEV;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * 开发_朝向方块（{@code dev_block_state_face}）—— 由旧项目 {@code DEV_StateEnumFace} 迁来。
 *
 * <p><b>多方块状态</b>：朝向属性 {@code facing}（6 个方向）。
 * blockstate 由人工手写，本类不在 {@code blockStateModels()} 里声明。
 *
 * <p>注意：本类**没有**覆写放置逻辑（旧代码也没有），所以默认恒为
 * {@code NORTH}；需要「放置时按玩家朝向」的话得自己加
 * {@code getStateForPlacement}。
 *
 * <p>改名对照：{@code Properties.FACING} → {@code BlockStateProperties.FACING}；
 * {@code AbstractBlock.Settings} → {@code BlockBehaviour.Properties}。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code dev_block_state_face}　<b>模块</b> DEV　<b>父级</b> 石头</li>
 *   <li><b>启用的可选层</b>：层 3 状态属性（{@code FACING}）、层 8 状态模式（{@code createBlockStateDefinition}）
 *       —— <b>没有</b> {@code getStateForPlacement}，默认恒为 NORTH（与旧代码一致）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 6 家族、层 7 codec、层 9 交互与判定、层 10 形状与外观、
 *       层 11 预留区、层 C 客户端、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：停在第 2 级「覆写本类方法」—— 想让它「放置时按玩家朝向」就再加一条
 *       {@code getStateForPlacement}（那是层 8 的第二个覆写点）</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☒ <b>不在</b> {@code blockStateModels()} 里声明
 *       （6 个方向，blockstate 手写）　③☒ 物品模型无例外　④☑ 中英 {@code DEV_LangEntries}
 *       　⑤☑ {@code DEV_CreativeTabs}　⑥☑ 贴图 / 模型　⑦☐ 配方与战利品表（wiki：获取方式「无」）</li>
 * </ul>
 */
public class DEV_StateEnumFace extends Block {

	/** 方块本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Block DEV_BLOCK_STATE_FACE;

	/**
	 * 状态属性字段（层 3）：朝向，直接取原版共享常量。
	 * <p>序列化名是 {@code facing}（原版 {@code BlockStateProperties.FACING} 的名字），手写的 blockstate 用的就是它。
	 */
	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

	/** 朝向方块的属性（层 4）：wiki「构造」表逐行都是「父级」，故完整复制石头。 */
	public static BlockBehaviour.Properties stateEnumFaceProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE);
	}

	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：装配 + 给状态初值。
	 * <p>属性来自上面的 {@link #stateEnumFaceProperties()}；{@code registerDefaultState} 只能写在
	 * {@code super(...)} 之后。
	 */
	public DEV_StateEnumFace(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState()
				.setValue(FACING, Direction.NORTH));
	}

	/** 状态模式（层 8）：声明本方块有 {@code facing} 这一个状态维度。 */
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	/** 注册（层 12）。 */
	public static void OnInit() {
		DEV_BLOCK_STATE_FACE = ModRegister.registerBlockWithItem("dev_block_state_face",
				new DEV_StateEnumFace(stateEnumFaceProperties()
						.setId(ModRegister.blockKey("dev_block_state_face"))));
	}
}
