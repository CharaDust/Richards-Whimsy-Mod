package com.rcdmake.whimsy.block.ClassWhimsyIdea;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 糖玻璃（{@code rdt_sugar_glass} → {@code wi_glasssugar}）—— 由 {@code RDT_GlassSugar} 迁入 WI 模块。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassWhimsyIdea/WI_GlassSugar.md}）：
 * <ul>
 *   <li>父级：玻璃；属性表逐行「父级」，只有碰撞标了「父级（已改写）」</li>
 *   <li>渲染：依据纹理像素点存在性透明（镂空）；改写方法：环境光遮蔽 0、返回透明状态、同类相连的面不可见</li>
 *   <li>事件：① 实体落在方块上 → 破坏方块（<b>无视踩踏效果的实体不触发</b>，如鸡、幻翼）
 *       ② 弹射物命中 → 破坏方块（<b>弹射物可在该位置修改方块且允许破坏方块</b> 才触发）</li>
 *   <li>方块状态：无（单状态）；获取方式：无</li>
 * </ul>
 *
 * <h2>1.20.4 (Yarn) → 26.2 官方名对照（逐条核验过）</h2>
 * <table border="1">
 *   <caption>本方块涉及的改名 / 移除</caption>
 *   <tr><th>Yarn（旧）</th><th>26.2 官方名</th><th>说明</th></tr>
 *   <tr><td>{@code FabricBlockSettings.copyOf(Blocks.GLASS)}</td><td>{@code Properties.ofFullCopy(Blocks.GLASS)}</td><td>「父级 = 玻璃」</td></tr>
 *   <tr><td>{@code onLandedUpon(world, state, pos, entity, float)}</td>
 *       <td>{@code fallOn(level, state, pos, entity, double)}</td><td>落在方块上（第五个参数由 float 变 double）</td></tr>
 *   <tr><td>{@code entity.bypassesSteppingEffects()}</td><td>{@code entity.isSteppingCarefully()}</td>
 *       <td>「无视踩踏效果」——原版海龟蛋的踩踏判定用的就是它</td></tr>
 *   <tr><td>{@code onProjectileHit(world, state, hit, projectile)}</td><td>{@code onProjectileHit(level, state, hit, projectile)}</td>
 *       <td>签名不变，但内部判定改收 {@code ServerLevel}</td></tr>
 *   <tr><td>{@code projectile.canModifyAt(world, pos)}</td><td>{@code projectile.mayInteract(serverLevel, pos)}</td><td>可否在该位置改方块</td></tr>
 *   <tr><td>{@code projectile.canBreakBlocks(world)}</td><td>{@code projectile.mayBreak(serverLevel)}</td><td>可否破坏方块</td></tr>
 *   <tr><td>{@code isSideInvisible(state, from, dir)}</td><td>{@code skipRendering(state, neighbor, dir)}</td><td>同类相连面不可见</td></tr>
 *   <tr><td>{@code getAmbientOcclusionLightLevel} / {@code isTransparent}</td>
 *       <td>{@code getShadeBrightness} / {@code propagatesSkylightDown}</td><td>同其它 WI 方块</td></tr>
 *   <tr><td>{@code BlockRenderLayerMap.putBlock(..., RenderLayer.getCutout())}</td><td><b>已删除</b></td>
 *       <td>26.1 起镂空由贴图自动判定（本方块贴图只有 Alpha 全 0 / 全 255 两档 → 镂空）</td></tr>
 * </table>
 *
 * <h2>一个小差异（有意保留旧行为）</h2>
 * 1.20.4 在被踩碎的那条路径上调用的是 {@code super.onSteppedOn(...)}（26.2 的 {@code stepOn}），
 * 而不是 {@code fallOn} 的默认实现 —— 于是「落在这块玻璃上」<b>不会</b>产生摔落伤害。
 * 本类照搬这一行为（{@code super.stepOn(...)}），因为玻璃碎了再吃一份摔落伤害显然不是设计意图。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code wi_glasssugar}　<b>模块</b> WI（由 {@code RDT_GlasSugar} 迁入）　<b>父级</b> 玻璃</li>
 *   <li><b>启用的可选层</b>：层 9 交互与判定（{@code fallOn} 踩碎 / {@code onProjectileHit} 击碎）、
 *       层 10 形状与外观（{@code getShadeBrightness} / {@code propagatesSkylightDown} / {@code skipRendering}）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 3 状态属性（单状态）、层 6 家族、层 7 codec、层 8 生命周期、
 *       层 11 预留区、层 C 客户端（镂空由贴图自动判定，已无渲染层注册）</li>
 *   <li><b>能力阶梯</b>：停在第 2 级「覆写本类方法」</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☑ {@code BlockStateGenerator.collectAll()}（单状态）
 *       　③☒ 物品模型无例外　④☑ 中英 {@code WI_LangEntries}　⑤☑ {@code WI_CreativeTabs} 方块页
 *       　⑥☑ 贴图 / 模型　⑦☐ 配方与战利品表（wiki 写「获取方式：无」）</li>
 * </ul>
 */
public class WI_GlassSugar extends Block {

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block WI_GLASSSUGAR;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/** 糖玻璃的属性：完整复制玻璃。 */
	public static BlockBehaviour.Properties glassSugarProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：只做装配，没有别的逻辑。
	 * <p>属性来自上面的 {@link #glassSugarProperties()} —— 它是本构造器实参的来源。
	 */
	public WI_GlassSugar(BlockBehaviour.Properties properties) {
		super(properties);
	}

	/**
	 * 实体落到方块上 → 踩碎（旧 {@code onLandedUpon}）。
	 * <p>无视踩踏效果的实体（鸡、幻翼等）不会踩碎它，与原版海龟蛋的判定一致。
	 */
	@Override
	// ── 层 9 ｜ 交互与判定 ──
	public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
		if (!level.isClientSide() && !entity.isSteppingCarefully()) {
			level.destroyBlock(pos, false);
		}
		super.stepOn(level, pos, state, entity);
	}

	/**
	 * 弹射物命中 → 击碎（旧 {@code onProjectileHit}）。
	 * <p>两个判定都要过：弹射物允许在该位置改方块（{@code mayInteract}）且允许破坏方块（{@code mayBreak}）。
	 * 26.2 这两个方法都收 {@code ServerLevel}，所以先用 {@code instanceof} 取得服务端世界。
	 */
	@Override
	protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
		BlockPos pos = hit.getBlockPos();
		if (level instanceof ServerLevel serverLevel
				&& projectile.mayInteract(serverLevel, pos)
				&& projectile.mayBreak(serverLevel)) {
			level.destroyBlock(pos, false, projectile);
		}
	}

	/** 环境光遮蔽为 0：旧 {@code getAmbientOcclusionLightLevel} 返回 1.0。 */
	@Override
	// ── 层 10 ｜ 形状与外观 ──
	protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		return 1.0F;
	}

	/** 返回透明方块状态：旧 {@code isTransparent} 恒为 true。 */
	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		return true;
	}

	/** 两个相同的糖玻璃相连时，贴合的那一面不渲染（旧 {@code isSideInvisible}）。 */
	@Override
	protected boolean skipRendering(BlockState state, BlockState neighbor, Direction direction) {
		if (neighbor.getBlock() == this) {
			return true;
		}
		return super.skipRendering(state, neighbor, direction);
	}

	// ── 层 12 ｜ 初始化 ──
	public static void OnInit() {
		WI_GLASSSUGAR = ModRegister.registerBlockWithItem("wi_glasssugar",
				new WI_GlassSugar(glassSugarProperties().setId(ModRegister.blockKey("wi_glasssugar"))));
	}
}
