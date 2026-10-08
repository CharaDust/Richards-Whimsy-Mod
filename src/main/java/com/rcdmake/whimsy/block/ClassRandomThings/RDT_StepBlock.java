package com.rcdmake.whimsy.block.ClassRandomThings;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * 垫脚方块（{@code rdt_step_block}）—— 由旧项目 {@code RDT_StepBlock} 迁来。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassRandomThings/RDT_StepBlock.md}）：
 * <ul>
 *   <li>父级：无（全新属性）；硬度/爆炸抗性「默认」</li>
 *   <li>地图颜色 PALE_YELLOW、声音组 SCAFFOLDING、亮度 0</li>
 *   <li>概念性固体 ✗、光线阻挡 ✗、表面生成生物 ✗、活塞推动行为<b>破坏</b>、其余「默认」</li>
 *   <li>渲染：依据纹理像素点存在性透明（镂空）；改写方法：环境光遮蔽为 0、返回透明方块状态</li>
 *   <li>事件：① 被放置时 → 添加一个 200 Tick 的计划刻 ② 计划刻激活时 → 破坏方块</li>
 *   <li>方块状态：无（单状态）；获取方式：无</li>
 * </ul>
 *
 * <h2>1.20.4 (Yarn) → 26.2 官方名对照（逐条核验过）</h2>
 * <table border="1">
 *   <caption>本方块涉及的改名 / 迁移</caption>
 *   <tr><th>Yarn（旧）</th><th>26.2 官方名</th><th>说明</th></tr>
 *   <tr><td>{@code MapColor.PALE_YELLOW}</td><td>{@code MapColor.SAND}</td>
 *       <td>26.2 的地图色常量里没有 PALE_YELLOW；原版脚手架（同样是淡黄、同样 SCAFFOLDING 音效）用的是
 *           {@code MapColor.SAND}，故取它</td></tr>
 *   <tr><td>{@code BlockSoundGroup.SCAFFOLDING}</td><td>{@code SoundType.SCAFFOLDING}</td><td>声音组</td></tr>
 *   <tr><td>{@code pistonBehavior(PistonBehavior.DESTROY)}</td>
 *       <td>{@code pushReaction(PushReaction.DESTROY)}</td><td>活塞一推就碎</td></tr>
 *   <tr><td>{@code allowsSpawning(Blocks::never)}</td>
 *       <td>{@code isValidSpawn((s, l, p, t) -&gt; false)}</td><td>表面生成生物 ✗</td></tr>
 *   <tr><td>{@code solidBlock(Blocks::never)}</td><td>{@code forceSolidOff()}</td><td>概念性固体 ✗</td></tr>
 *   <tr><td>{@code nonOpaque()}</td><td>{@code noOcclusion()}</td><td>光线阻挡 ✗</td></tr>
 *   <tr><td>{@code onPlaced(...)}</td><td>{@code setPlacedBy(...)}</td>
 *       <td>五个参数与顺序完全一致，只换名字</td></tr>
 *   <tr><td>{@code world.scheduleBlockTick(pos, this, 200)}</td>
 *       <td>{@code level.scheduleTick(pos, this, 200)}</td>
 *       <td>计划刻（26.2 里这个方法在 {@code ScheduledTickAccess} 接口上，Level 实现了它）</td></tr>
 *   <tr><td>{@code scheduledTick(state, serverWorld, pos, random)}</td>
 *       <td>{@code tick(state, serverLevel, pos, random)}</td>
 *       <td>覆写点改名；参数类型是 {@code ServerLevel} + {@code RandomSource}</td></tr>
 *   <tr><td>{@code world.breakBlock(pos, false)}</td><td>{@code level.destroyBlock(pos, false)}</td>
 *       <td>到点自毁，不掉落</td></tr>
 * </table>
 *
 * <h2>与 1.20.4 代码的一处刻意差异（按 wiki）</h2>
 * 旧代码写了 {@code strength(0.5f)}（硬度与爆炸抗性都成 0.5），而 wiki 的「硬度 / 爆炸抗性」两行都是
 * <b>「默认」</b>，所以本阶段不设 {@code strength}（硬度走引擎默认值）。
 * 如果你要的是旧代码那个 0.5，改回 {@code .strength(0.5F)} 即可。
 */
public class RDT_StepBlock extends Block {

	/** 计划刻延迟（Tick）。wiki 写「200Tick」。 */
	private static final int BREAK_DELAY_TICKS = 200;

	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block RDT_STEP_BLOCK;

	public RDT_StepBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	/** 垫脚方块的属性（wiki「构造」表逐行落地）。 */
	public static BlockBehaviour.Properties stepBlockProperties() {
		// 父级「无」→ 全新属性；硬度/爆炸抗性按 wiki 保持引擎默认
		return BlockBehaviour.Properties.of()
				// 地图颜色 PALE_YELLOW
				.mapColor(MapColor.SAND)
				// 声音组 SCAFFOLDING
				.sound(SoundType.SCAFFOLDING)
				// 表面生成生物：否
				.isValidSpawn((state, level, pos, entityType) -> false)
				// 活塞推动行为：破坏
				.pushReaction(PushReaction.DESTROY)
				// 概念性固体：否（旧 solidBlock(Blocks::never)）
				.forceSolidOff()
				// 光线阻挡：否
				.noOcclusion();
	}

	/**
	 * 被放置时挂一个 200 Tick 的计划刻（旧 {@code onPlaced}）。
	 * <p>只在服务端排刻 —— 客户端排刻没有任何意义（旧代码同样是 {@code !world.isClient}）。
	 */
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (!level.isClientSide()) {
			level.scheduleTick(pos, this, BREAK_DELAY_TICKS);
		}
	}

	/**
	 * 计划刻到点 → 自毁（旧 {@code scheduledTick}）。
	 * <p>26.2 的覆写名是 {@code tick}；参数里的世界是 {@code ServerLevel}
	 * （计划刻只会在服务端跑），所以直接用它来破坏方块、无需再判 {@code isClientSide}。
	 */
	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		level.destroyBlock(pos, false);
		super.tick(state, level, pos, random);
	}

	/** 环境光遮蔽为 0：旧 {@code getAmbientOcclusionLightLevel} 返回 1.0。 */
	@Override
	protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		return 1.0F;
	}

	/** 返回透明方块状态：旧 {@code isTransparent} 恒为 true。 */
	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		return true;
	}

	public static void OnInit() {
		RDT_STEP_BLOCK = ModRegister.registerBlockWithItem("rdt_step_block",
				new RDT_StepBlock(stepBlockProperties().setId(ModRegister.blockKey("rdt_step_block"))));
	}
}
