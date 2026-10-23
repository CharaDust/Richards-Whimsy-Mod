package com.rcdmake.whimsy.block.ClassRandomThings;

import com.rcdmake.whimsy.ModRegister;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 通灵木板（{@code rdt_spectre_planks}）—— 「通灵木料」三兄弟的第三块
 * （另两块：{@link RDT_TreeLogSpectre} 通灵原木、{@link RDT_TreeLeavesSpectre} 通灵树叶）。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassRandomThings/RDT_TreePlanksSpectre.md}）：
 * <ul>
 *   <li>父级：橡木木板；窒息 ✗、视野阻挡 ✗、光线阻挡 ✗、表面生成生物 ✗，其余「父级」</li>
 *   <li><b>碰撞：顶面可站立；按住 Shift 穿过</b> —— 与通灵原木 / 通灵树叶<b>同一套逻辑</b></li>
 *   <li>引燃几率 / 烧毁几率：30 / 60（与通灵原木同档，比原版木板 (5, 20) 高得多）</li>
 *   <li>渲染：依据纹理 Alpha 通道透明（{@code rdt/spectre_trsp_planks.png} 整图 Alpha 恒 195 → 半透明档）</li>
 *   <li>方块状态：<b>无</b>（单状态）—— 所以 blockstate 交给 datagen 生成，不手写</li>
 *   <li>获取方式：合适工具是斧（战利品表按全项目既定决定暂缓）</li>
 * </ul>
 *
 * <h2>1.20.4 (Yarn) → 26.2 官方名对照（本类用到的部分）</h2>
 * <table border="1">
 *   <caption>改名 / 语义变化</caption>
 *   <tr><th>1.20.4（Yarn）</th><th>26.2 官方名</th><th>说明</th></tr>
 *   <tr><td>{@code AbstractBlock.Settings.copy(Blocks.OAK_PLANKS)}</td>
 *       <td>{@code BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)}</td><td>「父级 = 橡木木板」</td></tr>
 *   <tr><td>{@code slowMovement(...)}</td><td>{@code Entity.makeStuckInBlock(state, vec)}</td>
 *       <td>方块内减速（与另两块同一套写法）</td></tr>
 *   <tr><td>{@code ShapeContext}</td><td>{@code CollisionContext}</td><td>碰撞上下文</td></tr>
 *   <tr><td>手写 blockstate JSON</td><td><b>datagen 生成</b></td>
 *       <td>单状态方块由 {@code BlockStateGenerator} 产出（范式登记点②的「☑」那一支）</td></tr>
 * </table>
 *
 * <h2>⭐⭐ 碰撞：与通灵原木 / 通灵树叶共用同一套实现</h2>
 * 规则本体、判定式、以及「为什么非实体上下文的兜底必须给父级形状」这一整段推导，
 * <b>全部写在 {@link RDT_SpectrePassThrough} 的类注释里</b>（那是三块唯一的手感来源）。
 * 本类只填两个参数：
 * <pre>{@code
 * 属性层：RDT_SpectrePassThrough.waterProof(passThrough(ofFullCopy(Blocks.OAK_PLANKS)))
 *         ├─ passThrough  → 不遮光 / 不窒息 / 不刷怪 / 不挡视野 / 清掉父级碰撞
 *         └─ waterProof   → forceSolidOn()：木板不可含水，必须靠它挡水（否则水流会把木板冲掉）
 * 形状层：collisionShape(pos, context, Shapes.block())   ← 第三个参数 = wiki 的「父级形状」= 整块
 * 层 9  ：entityInside → dragInside(entity, state)      ← 方块内减速 0.85
 * }</pre>
 * 因为兜底给的是「整块」，本类<b>不需要</b>再覆写 {@code getBlockSupportShape} 之类：
 * 26.2 的 {@code BlockBehaviour.getBlockSupportShape} 默认就是「拿 {@code CollisionContext.empty()}
 * 去问 {@code getCollisionShape}」，于是支撑面 / 视觉形状 / {@code isCollisionShapeFullBlock}
 * （进而红石导体、寻路）全部自动等于原版木板的整块行为。
 *
 * <h2>关键机制与踩坑</h2>
 * <ol>
 *   <li><b>why forceSolidOn 不能省</b>：{@code noCollision()} 把属性层的碰撞开关关掉了，
 *       而 {@code BlockStateBase.blocksMotion()} 在没写 {@code forceSolidOn/Off} 时是由缓存下来的
 *       碰撞形状反推的 —— 那是「一个实体穿得过、但水冲不掉的方块」最难缠的地方。
 *       一行 {@code forceSolidOn()} 把 {@code legacySolid} 钉死为 true，问题就消失了
 *       （通灵原木当年花了大量时间才定位到，见 {@code RDT_TreeLogSpectre} 的反面记录）。
 *       <br>树叶是唯一的例外：它可含水，<b>不能</b>加这一行，理由写在
 *       {@link RDT_SpectrePassThrough#waterProof} 的注释里。</li>
 *   <li><b>环境光遮蔽取 0.9F</b>：与通灵原木同档（wiki 里通灵原木那页写「环境光遮蔽为 0」、
 *       旧代码取 0.9f 近满亮）。木料三块里只有原木与木板要这一行 —— 树叶保持原版树叶的默认值，
 *       免得把原版树冠的明暗关系一起改掉。</li>
 *   <li><b>单状态方块的 blockstate 走 datagen</b>：本类没有 {@code Property}，
 *       {@code getStateDefinition().isSingletonState()} 为真 →
 *       必须在 {@code BlockStateGenerator.collectAll()} 里声明一行，
 *       由生成器产出 {@code blockstates/rdt_spectre_planks.json}。
 *       <b>手写那一份会被下一次 datagen 覆盖成一样的形状</b>，但多一份手工维护没有意义。</li>
 *   <li><b>物品模型不需要例外表</b>：原版木板这类整块方块的物品形式<b>就是</b>方块模型
 *       （{@code items/oak_planks.json} → {@code minecraft:block/oak_planks}），
 *       所以走 {@code ItemModelGenerator} 的默认规则即可，<b>不要</b>往
 *       {@code ITEM_MODEL_OVERRIDES} 里加（那是给 {@code block/cross} 型植物准备的，
 *       见通灵树苗那一页）。</li>
 * </ol>
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code rdt_spectre_planks}　<b>模块</b> RDT　<b>父级</b> 橡木木板
 *       （<b>层 6 家族</b>：直接继承 {@code Block} —— 木板本来就是普通整块方块，没有家族类可继承）</li>
 *   <li><b>启用的可选层</b>：层 1 规格常量（可燃性）、层 9 交互与判定（{@code entityInside} 减速、
 *       {@code canBeReplaced} 第二道保险）、层 10 形状与外观（{@code getCollisionShape} / {@code getShadeBrightness}）、
 *       层 12 额外注册表（{@code FlammableBlockRegistry}）</li>
 *   <li><b>跳过的可选层</b>：层 3 状态属性（<b>单状态</b>）、层 7 codec（继承 {@code Block} 已实现）、
 *       层 8 生命周期（木板没有任何随时间的状态变化）、层 11 预留区、层 C 客户端、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：用到第 0 级（继承 {@code Block}）、第 1 级（属性声明）、
 *       第 2 级（形状 / 减速覆写）、第 3 级（可燃性登记表）—— 最高停在<b>第 3 级</b>，没有动 Mixin。
 *       形状之所以必须覆写（第 2 级）：原版没有「按实体区分碰撞」的 {@code Properties} 开关。</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☑ <b>已在</b> {@code BlockStateGenerator.collectAll()} 声明
 *       （单状态 → blockstate 由 datagen 生成）
 *       　③☒ 物品模型无例外（原版木板就是指向方块模型）　④☑ 中英 {@code RDT_LangEntries}
 *       　⑤☑ {@code RDT_CreativeTabs}（「木材」组，排在通灵原木之后）
 *       　⑥☑ 贴图 {@code rdt/spectre_trsp_planks.png}（现成，Alpha 恒 195）；
 *       模型 {@code models/block/rdt_spectre_planks.json}（{@code cube_all}）
 *       　⑦☐ 配方 / 战利品表 / 挖掘标签 —— 用户明确说本轮不管；「获取方式」阶段统一做</li>
 * </ul>
 */
public class RDT_TreePlanksSpectre extends Block {

	// ── 层 1 ｜ 规格常量 ──
	/**
	 * 引燃几率（Fabric {@code FlammableBlockRegistry} 的 {@code igniteOdds}）。
	 * <p>取 <b>30</b>：与通灵原木同档（同属通灵木料），比原版木板的 5 高得多。
	 * 参考值：原版木板 (5, 20)、原版原木 (5, 5)、树叶 / 羊毛 (30, 60)、树苗 (60, 100)。
	 */
	private static final int IGNITE_ODDS = 30;

	/** 烧毁几率（{@code burnOdds}）：取 <b>60</b>，同样与通灵原木同档。 */
	private static final int BURN_ODDS = 60;

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Block RDT_SPECTRE_PLANKS;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/**
	 * 通灵木板的属性（层 4）：wiki「构造」表逐行都是「父级」+ 四行「✗」，
	 * 于是 = 复制橡木木板 + {@link RDT_SpectrePassThrough} 那两个公共开关。
	 *
	 * <p><b>{@code waterProof} 不能省</b>：木板不可含水，没有 {@code forceSolidOn()}
	 * 就会被水流冲掉（通灵原木踩过）。树叶是唯一不加这一层的方块。
	 */
	public static BlockBehaviour.Properties spectrePlanksProperties() {
		return RDT_SpectrePassThrough.waterProof(
				RDT_SpectrePassThrough.passThrough(
						BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)));
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器（层 5）：只做装配。
	 * <p>单状态方块没有 {@code registerDefaultState} 可言（状态模式只有一种），
	 * 也没有 {@code createBlockStateDefinition} 要写。
	 */
	public RDT_TreePlanksSpectre(BlockBehaviour.Properties properties) {
		super(properties);
	}

	// ── 层 9 ｜ 交互与判定 ──
	/**
	 * 方块内减速（层 9）：wiki 的「可穿过」在手感上的那一半 —— 穿过时不窒息、但稍有阻力。
	 * <p>转发给三块共用的 {@link RDT_SpectrePassThrough#dragInside}（乘数 0.85，三块同值）。
	 */
	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
			InsideBlockEffectApplier effectApplier, boolean inside) {
		RDT_SpectrePassThrough.dragInside(entity, state);
	}

	/**
	 * 流体一律不可替换本方块（层 9）：水流冲不掉它的<b>第二道保险</b>。
	 * <p>第一道是属性层的 {@code forceSolidOn()}（见 {@link #spectrePlanksProperties()}）——
	 * 两者互补：前者挡住「流体直接替换」，后者挡住「水能否流入」。
	 */
	@Override
	protected boolean canBeReplaced(BlockState state, net.minecraft.world.level.material.Fluid fluid) {
		return false;
	}

	// ── 层 10 ｜ 形状与外观 ──
	/**
	 * 碰撞形状（层 10）：<b>顶面可站立；按住 Shift 从顶面穿下去；侧面与身位在方块内一律穿得过</b>。
	 * <p>规则本体在 {@link RDT_SpectrePassThrough#collisionShape}，这里只填「父级形状」——
	 * 木板的父级是橡木木板（整块），所以填 {@link Shapes#block()}。
	 */
	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
			CollisionContext context) {
		return RDT_SpectrePassThrough.collisionShape(pos, context, Shapes.block());
	}

	/**
	 * 环境光遮蔽（层 10）：0.9F（近满亮），与通灵原木同档。
	 * <p>若照默认实现走，兜底形状是整块 → {@code isCollisionShapeFullBlock} 为真 → 会取 0.2F
	 * （原版木板那一档，阴影比较重）。通灵木料是半透明的，压成 0.2 会显得「发乌」，
	 * 所以跟通灵原木一起显式抬到 0.9F。
	 */
	@Override
	protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		return 0.9F;
	}

	// ── 层 12 ｜ 初始化 ──
	/**
	 * 注册方块与方块物品，并登记可燃性（层 12）。
	 *
	 * <p>可燃性走 Fabric 的 {@link FlammableBlockRegistry} —— 原版没有「按方块设可燃性」的
	 * {@code Properties} 开关（{@code ofFullCopy} 只复制方块属性，火焰那张表在另一套注册表里）。
	 * 值取 30 / 60，与通灵原木同档。
	 *
	 * <p>⚠️ 本类没有方块状态，所以 blockstate JSON 由 datagen 出（已在
	 * {@code BlockStateGenerator.collectAll()} 声明），不在这里写任何资源。
	 */
	public static void OnInit() {
		RDT_SPECTRE_PLANKS = ModRegister.registerBlockWithItem("rdt_spectre_planks",
				new RDT_TreePlanksSpectre(spectrePlanksProperties()
						.setId(ModRegister.blockKey("rdt_spectre_planks"))));

		// 引燃几率 30 / 烧毁几率 60（与通灵原木同档）
		FlammableBlockRegistry.getDefaultInstance().add(RDT_SPECTRE_PLANKS, IGNITE_ODDS, BURN_ODDS);
	}
}