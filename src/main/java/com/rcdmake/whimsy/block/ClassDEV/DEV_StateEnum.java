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

	/** 方块状态属性：枚举。 */
	public static final EnumProperty<DEVEnum> DEV_ENUM = EnumProperty.create("dev_enum", DEVEnum.class);

	public static Block DEV_BLOCK_STATE_ENUM;

	public DEV_StateEnum(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(DEV_ENUM, DEVEnum.DEVDEF));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(DEV_ENUM);
	}

	public static void OnInit() {
		DEV_BLOCK_STATE_ENUM = ModRegister.registerBlockWithItem("dev_block_state_enum",
				new DEV_StateEnum(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
						.setId(ModRegister.blockKey("dev_block_state_enum"))));
	}
}
