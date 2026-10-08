package com.rcdmake.whimsy.block.ClassRandomThings;

import com.rcdmake.whimsy.ModRegister;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

/**
 * 通灵原木（{@code rdt_spectre_log}）—— 由旧项目 {@code RDT_TreeLogSpectre} 迁来。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassRandomThings/RDT_TreeLogSpectre.md}）：
 * <ul>
 *   <li>父级：橡木原木；窒息 ✗、<b>碰撞「无」</b>、光线阻挡 ✗、表面生成生物 ✗，其余「父级」
 *       （其中概念性固体保持「父级」= 仍视为固体）</li>
 *   <li>引燃几率 / 烧毁几率：wiki 带星号（「应该需要比普通原木要高」）→ <b>已实现 30 / 60</b>；
 *       地图颜色仍带星号（「待定，该方块为淡蓝色」）—— <b>未实现</b>，沿用父级</li>
 *   <li>渲染：依据纹理 Alpha 通道透明（半透明档）</li>
 *   <li>改写方法：位于方块内时减速（xyz 各乘 0.85，按 {@code WI_Aerogel} 的写法实现）、环境光遮蔽为 0、
 *       返回透明方块状态、放置时轴向取决于贴合面（类似原木）</li>
 *   <li>方块状态：{@code AXIS}（多状态，故 blockstate JSON 由旧项目沿用，不走 datagen）</li>
 *   <li>获取方式：合适工具是斧、破坏后掉落自身（战利品表按全项目既定决定暂不生成）</li>
 * </ul>
 *
 * <h2>1.20.4 (Yarn) → 26.2 官方名对照（逐条核验过）</h2>
 * <table border="1">
 *   <caption>本方块涉及的改名</caption>
 *   <tr><th>Yarn（旧）</th><th>26.2 官方名</th><th>说明</th></tr>
 *   <tr><td>{@code copyOf(Blocks.OAK_LOG)}</td><td>{@code Properties.ofFullCopy(Blocks.OAK_LOG)}</td><td>「父级 = 橡木原木」</td></tr>
 *   <tr><td>{@code nonOpaque()}</td><td>{@code noOcclusion()}</td><td>光线阻挡 ✗</td></tr>
 *   <tr><td>{@code suffocates(Blocks::never)}</td><td>{@code isSuffocating(Blocks::never)}</td><td>窒息 ✗</td></tr>
 *   <tr><td>{@code allowsSpawning(Blocks::never)}</td>
 *       <td>{@code isValidSpawn((s, l, p, t) -&gt; false)}</td><td>表面生成生物 ✗</td></tr>
 *   <tr><td>{@code solidBlock(Blocks::never)} + {@code solid()}</td>
 *       <td>{@code forceSolidOff()} + {@code forceSolidOn()}</td>
 *       <td>旧代码这两行互相打架（后者胜），26.2 的 <b>forceSolidOn 才是必须的那个</b>，见下</td></tr>
 *   <tr><td>{@code appendProperties(...)} + {@code setDefaultState(...)}</td>
 *       <td><b>改用原版 {@code RotatedPillarBlock}</b>（自带 AXIS 状态与放置逻辑）</td>
 *       <td>旧代码手写的 {@code getPlacementState} 就是原版这两行；继承它等于少写一份、且不会写错</td></tr>
 *   <tr><td>{@code getAmbientOcclusionLightLevel} = 0.9f</td>
 *       <td>{@code getShadeBrightness} = 0.9F</td><td>wiki 写「环境光遮蔽为 0」，旧代码取 0.9（近满亮），保持一致</td></tr>
 *   <tr><td>{@code isTransparent}</td><td>{@code propagatesSkylightDown}</td><td>返回透明方块状态</td></tr>
 *   <tr><td>{@code isSideInvisible(s, from, dir)}</td><td>{@code skipRendering(s, neighbor, dir)}</td>
 *       <td>同类贴合面不可见（wiki 的「改写方法」没列这一条，但 1.20.4 有、故保留）</td></tr>
 *   <tr><td>{@code BlockRenderLayerMap}（半透明）</td><td><b>已删除</b></td>
 *       <td>26.1 起自动判定：{@code rdt/spectre_trsp_*.png} 含半透明像素 → 半透明</td></tr>
 * </table>
 *
 * <h2>⭐ 为什么这里是 {@code noCollision()} + {@code forceSolidOn()}（本项目踩过坑）</h2>
 * 本方块要「实体能穿过」，又<b>不能</b>被水流冲掉 —— 这两件事在 26.2 里由两个互相独立的开关控制：
 * <pre>{@code
 * 实体能不能穿过      ← getCollisionShape（noCollision() 令其为空）
 * 水流能不能占据它    ← BlockStateBase.blocksMotion() → legacySolid（来自 Properties 的 solid 开关）
 * }</pre>
 * 水流的判定链（{@code FlowingFluid.canHoldAnyFluid}，26.2 逐字节读过）：
 * <pre>{@code
 * if (Block instanceof LiquidBlockContainer) return true;
 * if (!state.blocksMotion())                 return true;   // ← 不阻挡运动 → 水可占 → 方块被冲掉
 * ...
 * }</pre>
 * 所以只写 {@code noCollision()} 会让 {@code legacySolid} 变 false，方块<b>会被水冲走</b>；
 * 补上 {@code forceSolidOn()}（旧 Yarn 的 {@code .solid()}）把 {@code legacySolid} 拉回 true，水就不来占了 ——
 * 而且它与「实体穿过」并不冲突，因为后者读的是形状、不是这个字段。
 * 本类另外覆写 {@link #canBeReplaced(BlockState, Fluid)} 返回 {@code false} 作第二道保险
 * （与 {@code DEV_GhostBlock} 同一套做法，那次是花了大量时间才定位到的）。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code rdt_spectre_log}　<b>模块</b> RDT　<b>父级</b> 橡木原木
 *       （<b>层 6 家族</b>：继承原版 {@code RotatedPillarBlock}，AXIS 状态与放置逻辑白拿）</li>
 *   <li><b>启用的可选层</b>：层 1 规格常量（{@code INSIDE_DRAG} / {@code IGNITE_ODDS} / {@code BURN_ODDS}）、
 *       层 3 状态属性（{@code AXIS}，由父类提供）、层 8 生命周期（构造器里的 {@code registerDefaultState}）、
 *       层 9 交互与判定（{@code entityInside} 减速、{@code canBeReplaced}）、
 *       层 10 形状与外观（{@code getShadeBrightness} / {@code propagatesSkylightDown} / {@code skipRendering}）、
 *       层 12 额外注册表（{@code FlammableBlockRegistry}）</li>
 *   <li><b>跳过的可选层</b>：层 7 codec（父类 {@code RotatedPillarBlock} 已提供）、层 11 预留区、
 *       层 C 客户端（半透明由贴图自动判定）</li>
 *   <li><b>能力阶梯</b>：用到第 0 级（继承家族类）、第 1 级（属性声明）、第 2 级（覆写）、
 *       第 3 级（可燃性登记表）—— 最高停在<b>第 3 级</b>，<b>没有</b>动 Mixin</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☑ <b>手写</b> {@code blockstates/rdt_spectre_log.json}
 *       （{@code AXIS} 是多状态，datagen 不生成、也不在 {@code collectAll()} 里声明）
 *       　③☒ 物品模型无例外　④☑ 中英 {@code RDT_LangEntries}　⑤☑ {@code RDT_CreativeTabs}
 *       　⑥☑ 贴图 / 模型　⑦☐ 配方与战利品表（wiki：合适工具斧、战利品表暂缓）</li>
 * </ul>
 */
public class RDT_TreeLogSpectre extends RotatedPillarBlock {

	// ── 层 1 ｜ 规格常量 ──
	/** 方块内减速乘数（xyz）。wiki 原先标「未实现」，现已按 0.85 实现 —— 较轻的阻力。 */
	private static final Vec3 INSIDE_DRAG = new Vec3(0.85, 0.85, 0.85);

	/**
	 * 引燃几率（Fabric {@code FlammableBlockRegistry} 的 {@code igniteOdds}，也是原版
	 * {@code FireBlock.setFlammable(block, igniteOdds, burnOdds)} 的第一个参数）。
	 * <p>参考值：原版橡木原木 (5, 5)、木板 (5, 20)、树叶 / 羊毛 (30, 60)。
	 */
	private static final int IGNITE_ODDS = 30;

	/** 烧毁几率（{@code burnOdds} / {@code setFlammable} 的第二个参数）。wiki 的「父级*（要比原木高）」→ 60。 */
	private static final int BURN_ODDS = 60;

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block RDT_SPECTRE_LOG;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/** 通灵原木的属性（wiki「构造」表逐行落地）。 */
	public static BlockBehaviour.Properties spectreLogProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)
				// 光线阻挡：否
				.noOcclusion()
				// 窒息方块：否
				.isSuffocating(Blocks::never)
				// 表面生成生物：否
				.isValidSpawn((state, level, pos, entityType) -> false)
				// 碰撞：无 —— 实体可穿过
				.noCollision()
				// 但「概念性固体」保持父级（true）：水流不会来占（旧 Yarn 的 .solid()）
				.forceSolidOn();
		// 视野阻挡：wiki 写「父级」，故不覆写（引擎默认谓词生效）
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：装配 + 给状态初值。
	 * <p>属性来自上面的 {@link #spectreLogProperties()}；{@code registerDefaultState} 只能写在
	 * {@code super(...)} 之后（状态模式已在 {@code super} 内部定死）。
	 */
	public RDT_TreeLogSpectre(BlockBehaviour.Properties properties) {
		super(properties);
		// 旧代码的默认轴是 X（放置时会被原版 RotatedPillarBlock 的放置逻辑覆盖成贴合面的轴）
		registerDefaultState(defaultBlockState().setValue(AXIS, Direction.Axis.X));
	}

	/**
	 * 方块内减速（旧 wiki 曾标「未实现」，现已实现）。
	 * <p>与 {@code WI_Aerogel} 同一套写法：26.2 的方块内减速覆写点是
	 * {@code entityInside(BlockState, Level, BlockPos, Entity, InsideBlockEffectApplier, boolean)}，
	 * 直接调 {@code entity.makeStuckInBlock(state, vec)}（旧 Yarn 是 {@code slowMovement}）。
	 * 乘数见 {@link #INSIDE_DRAG}：三个方向各保留绝大部分速度，即「无窒息但稍有阻力地穿过」。
	 */
	@Override
	// ── 层 9 ｜ 交互与判定 ──
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
			InsideBlockEffectApplier effectApplier, boolean inside) {
		entity.makeStuckInBlock(state, INSIDE_DRAG);
	}

	/** 流体一律不可替换本方块：水流冲不掉它（第二道保险，理由见类注释）。 */
	@Override
	protected boolean canBeReplaced(BlockState state, Fluid fluid) {
		return false;
	}

	/** 环境光遮蔽：旧代码 0.9f。 */
	@Override
	// ── 层 10 ｜ 形状与外观 ──
	protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		return 0.9F;
	}

	/** 返回透明方块状态：旧 {@code isTransparent} 恒为 true。 */
	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		return true;
	}

	/** 两个相同的通灵原木相连时，贴合的那一面不渲染（旧 {@code isSideInvisible}）。 */
	@Override
	protected boolean skipRendering(BlockState state, BlockState neighbor, Direction direction) {
		if (neighbor.getBlock() == this) {
			return true;
		}
		return super.skipRendering(state, neighbor, direction);
	}

	// ── 层 12 ｜ 初始化 ──
	/**
	 * 注册方块与方块物品，并登记可燃性。
	 *
	 * <p>可燃性用的是 Fabric 的 {@link FlammableBlockRegistry}（26.2 版 API 的形参名就是
	 * {@code igniteOdds, burnOdds}，与原版 {@code FireBlock} 内部同名）——
	 * 原版没有「按方块设置可燃性」的 {@code Properties}，只能走这个注册表。
	 * 必须在模组初始化阶段登记，且方块本体注册之后。
	 */
	public static void OnInit() {
		RDT_SPECTRE_LOG = ModRegister.registerBlockWithItem("rdt_spectre_log",
				new RDT_TreeLogSpectre(spectreLogProperties().setId(ModRegister.blockKey("rdt_spectre_log"))));

		// 引燃几率 30 / 烧毁几率 60（wiki 那两行带星号的值）
		FlammableBlockRegistry.getDefaultInstance().add(RDT_SPECTRE_LOG, IGNITE_ODDS, BURN_ODDS);
	}
}
