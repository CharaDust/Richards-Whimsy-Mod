package com.rcdmake.whimsy.block.ClassDEV;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * 开发_枚举方块（{@code dev_block_state_enum}）—— 由旧项目 {@code DEV_StateEnum} 迁来。
 *
 * <p><b>多方块状态</b>：枚举属性 {@code dev_enum}（3 种取值）。
 * blockstate 由人工手写，本类不在 {@code blockStateModels()} 里声明。
 *
 * <p>改名对照（Yarn → 26.2 官方名）：
 * {@code EnumProperty.of(name, class)} → {@code EnumProperty.create(name, class)}；
 * {@code StringIdentifiable.asString()} → {@code StringRepresentable.getSerializedName()}。
 *
 * <p>枚举的每个取值由 {@code getSerializedName()} 提供序列化名（写进 blockstate 的
 * variants 里用的就是它），所以那三个字符串 {@code dev_in/dev_out/dev_def} 不能改。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code dev_block_state_enum}　<b>模块</b> DEV　<b>父级</b> 石头</li>
 *   <li><b>启用的可选层</b>：层 3 状态属性（{@code DEV_ENUM} + 嵌套枚举 {@code DEVEnum}）、
 *       层 8 状态模式（{@code createBlockStateDefinition}）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 6 家族、层 7 codec、层 9 交互与判定、层 10 形状与外观、
 *       层 11 预留区、层 C 客户端、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：停在第 2 级「覆写本类方法」</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☒ <b>不在</b> {@code blockStateModels()} 里声明
 *       （3 种取值，blockstate 手写）　③☒ 物品模型无例外　④☑ 中英 {@code DEV_LangEntries}
 *       　⑤☑ {@code DEV_CreativeTabs}　⑥☑ 贴图 / 模型　⑦☐ 配方与战利品表（wiki：获取方式「无」）</li>
 * </ul>
 */
public class DEV_StateEnum extends Block {

	/** 枚举取值：序列化名对应 blockstate variants 里的 property 值。 */
	public enum DEVEnum implements StringRepresentable {
		DEVIN("dev_in"),
		DEVOUT("dev_out"),
		DEVDEF("dev_def");

		private final String id;

		DEVEnum(String id) {
			this.id = id;
		}

		@Override
		public String getSerializedName() {
			return id;
		}
	}

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Block DEV_BLOCK_STATE_ENUM;

	// ── 层 3 ｜ 状态属性字段 ──
	/** 状态属性字段（层 3）：枚举。 */
	public static final EnumProperty<DEVEnum> DEV_ENUM = EnumProperty.create("dev_enum", DEVEnum.class);

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/** 枚举的属性（层 4）：wiki「构造」表逐行都是「父级」，故完整复制石头。 */
	public static BlockBehaviour.Properties stateEnumProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：装配 + 给状态初值。
	 * <p>属性来自上面的 {@link #stateEnumProperties()}；{@code registerDefaultState} 只能写在
	 * {@code super(...)} 之后。
	 */
	public DEV_StateEnum(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(DEV_ENUM, DEVEnum.DEVDEF));
	}

	/** 状态模式（层 8）：声明本方块有 {@code dev_enum} 这一个状态维度。 */
	@Override
	// ── 层 8 ｜ 状态模式与生命周期 ──
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(DEV_ENUM);
	}

	// ── 层 12 ｜ 初始化 ──
	/** 注册（层 12）。 */
	public static void OnInit() {
		DEV_BLOCK_STATE_ENUM = ModRegister.registerBlockWithItem("dev_block_state_enum",
				new DEV_StateEnum(stateEnumProperties()
						.setId(ModRegister.blockKey("dev_block_state_enum"))));
	}
}
