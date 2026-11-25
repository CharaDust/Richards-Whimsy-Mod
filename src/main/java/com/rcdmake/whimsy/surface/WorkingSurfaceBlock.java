package com.rcdmake.whimsy.surface;

import com.rcdmake.whimsy.component.ClassBlockHeadsRebon.BHR_WorkBenchLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作面（Working Surface）方块基类 —— 制作台 / 木工台 / 简易篝火共用。
 *
 * <p>它只做「三台完全一样」的那些事：等级状态、放置时取等级、掉落带等级、右键开界面、
 * 造带等级的物品堆。差异全部来自 {@link WorkingSurfaceSpec}；子类只补自己独有的部分
 * （制作台的 29 级音效与模型、篝火的燃料与外观）。
 *
 * <p>等级落在共用属性 {@link #LEVEL}（1~29）上：等级同时决定模型变体与掉落物组件，
 * 两者都发生在「引擎按方块状态查表」的路径上，所以不能放在方块实体里。
 * 每台的实际上限由 {@link WorkingSurfaceSpec#levelMax()} 夹取（制作台 29 / 木工台 3 / 篝火 2）。
 */
public abstract class WorkingSurfaceBlock extends Block implements EntityBlock {

	/** 等级属性（所有工作面共用一套取值范围 1~29）。 */
	public static final IntegerProperty LEVEL =
			IntegerProperty.create("level", WorkingSurfaceSpec.LEVEL_MIN, WorkingSurfaceSpec.LEVEL_ABS_MAX);

	private final WorkingSurfaceSpec spec;

	protected WorkingSurfaceBlock(BlockBehaviour.Properties properties, WorkingSurfaceSpec spec) {
		super(properties);
		this.spec = spec;
		this.registerDefaultState(this.defaultBlockState().setValue(LEVEL, WorkingSurfaceSpec.LEVEL_MIN));
	}

	/** 规格（注册名 / 等级上限 / 燃料 / 底图）。 */
	public WorkingSurfaceSpec spec() {
		return this.spec;
	}

	/** 本机的注册名。 */
	public String surfaceId() {
		return this.spec.id();
	}

	/** 界面标题（<b>一律带等级</b>）。 */
	public Component titleFor(int level) {
		return this.spec.titleFor(level);
	}

	/** 把通用等级夹到本机的上限。 */
	public int clampLevel(int level) {
		return Math.clamp(level, WorkingSurfaceSpec.LEVEL_MIN, this.spec.levelMax());
	}

	/** 读物品堆上的等级组件并夹到本机上限。 */
	public int levelOf(ItemInstance stack) {
		return this.clampLevel(BHR_WorkBenchLevel.levelOf(stack));
	}

	/** 造一个指定等级的本方块物品堆（掉落 / 选取方块共用）。 */
	public ItemStack createStack(int level) {
		ItemStack stack = new ItemStack(this.asItem());
		return BHR_WorkBenchLevel.set(stack, this.clampLevel(level));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LEVEL);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(LEVEL, this.levelOf(context.getItemInHand()));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new WorkingSurfaceEntity(pos, state);
	}

	/** 右键打开界面：菜单类型按 spec.id 取，标题带当前等级。 */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hitResult) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (player instanceof ServerPlayer serverPlayer) {
			WorkingSurfaceEntity entity = level.getBlockEntity(pos) instanceof WorkingSurfaceEntity surface
					? surface
					: null;
			serverPlayer.openMenu(new SimpleMenuProvider(
					(containerId, inventory, ignored) -> WorkingSurfaceMenu.create(this.spec, containerId, inventory,
							ContainerLevelAccess.create(level, pos), entity),
					this.titleFor(state.getValue(LEVEL))));
			// 原版配方投影：只有规则表里登记过的工作面才会真的投影出东西（其余是空列表）
			SurfaceNetwork.sendSurface(serverPlayer, this.surfaceId());
		}
		return InteractionResult.SUCCESS;
	}

	/** 掉落：一件带当前等级的方块物品（不用战利品表 —— 等级来自状态，表里写不进去）。 */
	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
		List<ItemStack> drops = new ArrayList<>(1);
		drops.add(this.createStack(state.getValue(LEVEL)));
		return drops;
	}

	/** 创造模式中键取到的是同等级的那一件。 */
	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return this.createStack(state.getValue(LEVEL));
	}
}
