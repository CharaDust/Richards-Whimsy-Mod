package com.rcdmake.whimsy.block.ClassRandomThings;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 「通灵木料」三兄弟（通灵原木 {@link RDT_TreeLogSpectre}、通灵木板 {@link RDT_TreePlanksSpectre}、
 * 通灵树叶 {@link RDT_TreeLeavesSpectre}）<b>共用的一套「可穿过 + 顶面可站立」实现</b>。
 *
 * <p>本类只做一件事：把「半透明的通灵方块，实体从侧面穿得过去、站到顶面上又踩得住」
 * 这条规则收在<b>一处</b>。三个方块类各自只留一行转发 —— 以后改手感、改形状，改这里就够了。
 *
 * <h2>规则本体（照抄原版脚手架 {@code ScaffoldingBlock.getCollisionShape} 的判定式）</h2>
 * <pre>{@code
 * public static VoxelShape collisionShape(BlockPos pos, CollisionContext context, VoxelShape parentShape) {
 *     if (context.isPlacement())                      return Shapes.empty();   // ① 放置预览：不给碰撞
 *     if (context 不是实体上下文)                       return parentShape;      // ② 非实体查询：父级形状
 *     if (context.isAbove(Shapes.block(), pos, true)
 *             && !context.isDescending())             return Shapes.block();    // ③ 站在顶面且没按 Shift
 *     return Shapes.empty();                                                   // ④ 侧面 / 身位在方块内：空
 * }
 * }</pre>
 * <ul>
 *   <li><b>③ 为什么「按住 Shift」就是「穿下去」</b>：{@code Entity.isDescending()} 的实现就是
 *       {@code isShiftKeyDown()} —— 按住 Shift = 引擎眼里的「正在下降」，于是形状变空、从顶面落下去。
 *       {@code isAbove} 的判定是 {@code entityBottom > pos.getY() + 1 - 1.0E-5}，
 *       所以「脚底在顶面之上」才算站在上面，<b>站在旁边（脚底与方块底面同高）不算</b> → 侧面不挡人。</li>
 *   <li><b>④ 掉落的实体会被顶面接住</b>（不会穿过去）：{@code isDescending()} 只认 Shift、不认「正在下落」，
 *       所以下落中的实体只要脚底还在顶面上方，拿到的就是整块形状 —— 与原版脚手架一致。</li>
 * </ul>
 *
 * <h2>⭐ ② 这一条是本类与「原版脚手架写法」唯一的差别，也是本项目范式的硬约束落地</h2>
 * 原版脚手架的兜底是<b>空形状</b>，本项目<b>不能</b>照抄那一段 —— 26.2 实测（逐方法读字节码）：
 * <pre>{@code
 * BlockBehaviour.getCollisionShape(state, level, pos, context) = hasCollision ? state.getShape(…) : Shapes.empty()
 * BlockStateBase.getCollisionShape(level, pos)      → 用 CollisionContext.empty() 走上面那条   ← 「非实体查询」
 * BlockBehaviour.getBlockSupportShape(...)          → getCollisionShape(…, CollisionContext.empty())
 * BlockBehaviour.getVisualShape(...)                → getCollisionShape(...)
 * BlockStateBase.isCollisionShapeFullBlock(...)     → 看 CollisionContext.empty() 拿到的那份形状
 * BlockBehaviour.getShadeBrightness(...)            → isCollisionShapeFullBlock(...)
 * BlockBehaviour.isPathfindable(...)                → isCollisionShapeFullBlock(...)
 * }</pre>
 * 也就是说：<b>「支撑面 / 视觉形状 / 是不是整块 / 环境光遮蔽 / 寻路 / 红石导体」全都走
 * {@code CollisionContext.empty()} 那一次查询</b>。兜底若返回空，这些能力会一起退化成
 * 「一个空方块」—— 这正是范式 §7 硬约束第 4 条那句话的来由
 * （「非实体上下文的兜底分支必须返回父级形状，返回空会让 blocksMotion 变 false、方块被水冲掉、
 * 告示牌也贴不上」）。
 * <p>所以本类把兜底改成<b>调用方给的「父级形状」</b>：
 * <ul>
 *   <li>{@link RDT_TreeLogSpectre} / {@link RDT_TreePlanksSpectre} → {@link Shapes#block()}
 *       （wiki「父级」= 橡木原木 / 橡木木板，本来就是整块）→ 红石导体、寻路、支撑面全部与原版木料一致；</li>
 *   <li>{@link RDT_TreeLeavesSpectre} → {@link Shapes#empty()}（wiki「父级」= 橡树树叶，
 *       原版树叶的碰撞形状本来就是空的）→ 树叶的寻路 / 红石 / 支撑面继续与原版树叶一致，
 *       本次只多出「顶面站得住」这一条。</li>
 * </ul>
 * 参数名就叫「父级形状」，是因为它就是范式那句话里的「父级形状」——<b>照着 wiki 的「父级」一栏填</b>。
 *
 * <h2>⭐ 为什么三个方块都得是「非实体兜底 = 父级形状」而不能干脆照着脚手架返回空</h2>
 * 见上：返回空会让 {@code isCollisionShapeFullBlock} 变假。对<b>木料</b>（原木 / 木板）来说，
 * 那意味着「红石不从它导电、怪物寻路穿它而过、顶面不是支撑面」—— 与原版木料的手感差得远。
 *
 * <h2>实体能不能穿过 ≠ 水能不能冲掉（两者是两套开关，别混）</h2>
 * 本类只负责<b>实体碰撞</b>。水那一侧是另一个开关：
 * <pre>{@code
 * 实体能不能穿过   ← getCollisionShape 的形状（本类）
 * 水流能不能占据它 ← BlockStateBase.blocksMotion() → legacySolid，由 Properties 的 solid 开关决定
 * }</pre>
 * 水流判定的第一道闸门 {@code FlowingFluid.canHoldAnyFluid} 里有
 * {@code if (Block instanceof LiquidBlockContainer) return true;} 与 {@code if (!state.blocksMotion()) return true;}，
 * 于是模组方块只有两条路：
 * <ul>
 *   <li><b>不可含水的木料（原木 / 木板）</b>：走 {@link #waterProof} ——
 *       {@code noCollision()} 清掉属性层自带的碰撞开关，{@code forceSolidOn()} 把 {@code legacySolid}
 *       钉死为 true（{@code calculateSolid()} 按顺序取值，它排第一位、命中就与实际形状无关），
 *       于是<b>形状为空也不影响挡水</b>（通灵原木踩过这个坑，见 {@code RDT_TreeLogSpectre} 的旧注释）；</li>
 *   <li><b>可含水的树叶</b>：<b>不能</b>加 {@code forceSolidOn()} —— 它实现了
 *       {@code SimpleWaterloggedBlock}（→ {@code LiquidBlockContainer}），水走的是「把方块变成含水树叶」，
 *       本来就不会销毁方块；而加上 {@code forceSolidOn()} 反而会让 {@code blocksMotion()} 变真，
 *       连带把树叶的<b>寻路 / 可穿过性</b>一起改掉（原版树叶不是这个手感）。
 *       所以树叶走 {@link #passThrough} 但<b>不</b>走 {@link #waterProof}。</li>
 * </ul>
 *
 * <h2>用法（每个方块三行）</h2>
 * <pre>{@code
 * // 层 4：属性工厂
 * public static BlockBehaviour.Properties xxxProperties() {
 *     return RDT_SpectrePassThrough.waterProof(                       // 可含水的方块不要这一层
 *             RDT_SpectrePassThrough.passThrough(
 *                     BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_XXX)));
 * }
 *
 * // 层 9：实体在方块里 → 减速
 * @Override
 * protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
 *         InsideBlockEffectApplier applier, boolean inside) {
 *     RDT_SpectrePassThrough.dragInside(entity, state);
 * }
 *
 * // 层 10：碰撞形状
 * @Override
 * protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
 *         CollisionContext context) {
 *     return RDT_SpectrePassThrough.collisionShape(pos, context, Shapes.block());   // 树叶填 Shapes.empty()
 * }
 * }</pre>
 *
 * <h2>一句话记住三块的分工</h2>
 * <table border="1">
 *   <caption>三兄弟的差异只有「父级形状」与「要不要 forceSolidOn」两项</caption>
 *   <tr><th>方块</th><th>父级</th><th>兜底形状</th><th>forceSolidOn</th><th>挡水靠</th></tr>
 *   <tr><td>通灵原木</td><td>橡木原木</td><td>{@code Shapes.block()}</td><td>要</td><td>{@code forceSolidOn()}</td></tr>
 *   <tr><td>通灵木板</td><td>橡木木板</td><td>{@code Shapes.block()}</td><td>要</td><td>{@code forceSolidOn()}</td></tr>
 *   <tr><td>通灵树叶</td><td>橡树树叶</td><td>{@code Shapes.empty()}</td><td><b>不要</b></td>
 *       <td>{@code SimpleWaterloggedBlock}（原版同款）</td></tr>
 * </table>
 */
public final class RDT_SpectrePassThrough {

	/**
	 * 方块内减速乘数（xyz 各乘）。
	 * <p>wiki 三页都只写「无窒息但<b>稍有阻力</b>地穿过」，没给数 —— 这里取 0.85，
	 * 三块共用同一个数，保证同一棵树的三个方块手感一致（写法照 {@code WI_Aerogel}：
	 * 覆写 {@code entityInside} → {@code entity.makeStuckInBlock(state, vec)}）。
	 */
	public static final Vec3 INSIDE_DRAG = new Vec3(0.85, 0.85, 0.85);

	private RDT_SpectrePassThrough() {
	}

	// ── 层 4 ｜ 属性层的公共开关 ──

	/** 「表面生成生物：否」的谓词。{@code isValidSpawn} 比另两个多一个实体类型参数，所以要单独一个四参谓词。 */
	private static boolean neverSpawn(BlockState state, BlockGetter level, BlockPos pos, EntityType<?> type) {
		return false;
	}

	/**
	 * 属性层的「可穿过」开关（三块共用）：把 wiki 里那几行「✗ / FALSE」一次写全。
	 * <ul>
	 *   <li>{@code noOcclusion()} —— 光线阻挡：否；</li>
	 *   <li>{@code isSuffocating(Blocks::never)} —— 窒息方块：否；</li>
	 *   <li>{@code isValidSpawn(...)} —— 表面生成生物：否；</li>
	 *   <li>{@code isViewBlocking(Blocks::never)} —— 视野阻挡：否。
	 *       <b>这一条是必需的不是可选的</b>：原版「方块屏幕效果」会把
	 *       「眼睛所在、且视野阻挡为真的方块」的粒子贴图铺满整屏 —— 那就是
	 *       {@code RDT_TreeLogSpectre} 当年「穿过时满屏一张方块贴图」的真凶；</li>
	 *   <li>{@code noCollision()} —— 清掉父级自带的静态碰撞形状（真正的形状由
	 *       {@link #collisionShape} 按情境给）。</li>
	 * </ul>
	 * <p>⚠️ 这里<b>不含</b> {@code forceSolidOn()}：它是「挡水」那件事，只有不可含水的木料才要，
	 * 见 {@link #waterProof}。
	 */
	public static BlockBehaviour.Properties passThrough(BlockBehaviour.Properties base) {
		return base
				.noOcclusion()
				.isSuffocating(Blocks::never)
				.isValidSpawn(RDT_SpectrePassThrough::neverSpawn)
				.isViewBlocking(Blocks::never)
				.noCollision();
	}

	/**
	 * 属性层的「挡水」开关（<b>只有不可含水的木料要</b>：通灵原木 / 通灵木板）。
	 *
	 * <p>原理（26.2 逐字节读过）：{@code BlockStateBase.blocksMotion()} 读的是字段 {@code legacySolid}，
	 * 而 {@code calculateSolid()} 是<b>按顺序</b>取值的 —— {@code forceSolidOn} 排在第一位，
	 * 命中它就恒为 true、与实际形状无关。于是「空碰撞形状（实体穿得过）+
	 * {@code blocksMotion() == true}（水不来占）」可以兼得。
	 *
	 * <p>反面记录：只覆写形状、不写 {@code forceSolidOn()} 的写法会让 {@code legacySolid} 由
	 * 「缓存下来的碰撞形状」反推 —— 而本类给非实体上下文的兜底虽然是父级形状，实体上下文的兜底却是空，
	 * 缓存那一份怎么算都容易判成 false，水就把方块冲掉了（{@code RDT_GlassLapis} 踩过）。
	 * 所以木料一律走本方法，别省这一行。
	 *
	 * <p>⚠️ <b>可含水的树叶绝对不要调它</b>（理由见类注释）。
	 */
	public static BlockBehaviour.Properties waterProof(BlockBehaviour.Properties properties) {
		return properties.forceSolidOn();
	}

	// ── 层 9 ｜ 实体在方块内 ──

	/**
	 * 方块内减速（三块共用）：直接调 {@code entity.makeStuckInBlock(state, INSIDE_DRAG)}
	 * （1.20.4 的 Yarn 名是 {@code slowMovement}）。
	 * <p>调用点写在各自的 {@code entityInside} 覆写里 —— 那是 26.2 的方块内减速钩子
	 * （{@code entityInside(BlockState, Level, BlockPos, Entity, InsideBlockEffectApplier, boolean)}）。
	 */
	public static void dragInside(Entity entity, BlockState state) {
		entity.makeStuckInBlock(state, INSIDE_DRAG);
	}

	// ── 层 10 ｜ 碰撞形状 ──

	/**
	 * 三块共用的碰撞形状规则（判定式与 26.2 的原版脚手架一致，差别只有第 ② 条兜底）。
	 *
	 * @param pos         方块位置
	 * @param context     碰撞上下文（实体查询 vs 非实体查询由它区分）
	 * @param parentShape <b>父级形状</b> —— 照 wiki「父级」一栏填：
	 *                    原木 / 木板填 {@link Shapes#block()}，树叶填 {@link Shapes#empty()}。
	 *                    它只用于<b>非实体查询</b>（光照 / 支撑 / 渲染 / 红石 / 寻路），实体永远拿到
	 *                    「顶面整块 or 空」，所以它不会影响「穿得过去 / 站得住」这两条。
	 * @return 本次查询该给的形状
	 */
	public static VoxelShape collisionShape(BlockPos pos, CollisionContext context, VoxelShape parentShape) {
		// ① 放置预览：不给碰撞（照抄脚手架）
		if (context.isPlacement()) {
			return Shapes.empty();
		}

		// ② 非实体查询（光照 / 支撑 / 渲染 / 红石 / 寻路）：父级形状。
		//    判据：CollisionContext.empty() 的 getEntity() 为 null（26.2 已核对这两个类的签名）。
		//    这一条是范式 §7 硬约束第 4 条的落地，理由见类注释。
		if (!(context instanceof EntityCollisionContext entityContext) || entityContext.getEntity() == null) {
			return parentShape;
		}

		// ③ 实体站在顶面、且没按 Shift（isDescending() 的实现就是 isShiftKeyDown()）→ 整块：顶面站得住。
		//    注意 isAbove 用的是 entityBottom > pos.getY() + 1 - 1.0E-5，
		//    所以站着时算「在上方」、脚底与方块底面同高的侧面不算 → 侧面不挡人。
		if (context.isAbove(Shapes.block(), pos, true) && !context.isDescending()) {
			return Shapes.block();
		}

		// ④ 其余（实体在侧面 / 身位在方块内部）→ 空：穿得过去。
		return Shapes.empty();
	}
}