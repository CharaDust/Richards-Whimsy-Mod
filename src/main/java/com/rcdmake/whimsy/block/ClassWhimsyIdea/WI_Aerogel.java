package com.rcdmake.whimsy.block.ClassWhimsyIdea;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 气凝胶（{@code wi_aerogel}）—— 由旧项目 {@code WI_Aerogel} 迁来（1.20.4 Yarn → 26.2 官方名）。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassWhimsyIdea/WI_Aerogel.md}）：
 * <ul>
 *   <li>父级：无（全新属性）</li>
 *   <li>硬度 0.3 / 爆炸抗性 15.533（TNT 恰好炸穿 1 层）</li>
 *   <li>声音组 SNOW；窒息 ✗、视野阻挡 ✗、概念性固体 ✗、表面生成生物 ✗</li>
 *   <li>碰撞：无；亮度 0；渲染：依据纹理 Alpha 通道透明（半透明档）</li>
 *   <li>改写方法：<b>位于方块内时减速（类似蜘蛛网）</b>、环境光遮蔽为 0、返回透明状态</li>
 *   <li>事件：无</li>
 * </ul>
 *
 * <h2>1.20.4 (Yarn) → 26.2 官方名对照（逐条对 26.2 minecraft-merged-deobf 核验过）</h2>
 * <table border="1">
 *   <caption>本方块涉及的改名 / 移除</caption>
 *   <tr><th>Yarn（旧）</th><th>26.2 官方名</th><th>说明</th></tr>
 *   <tr><td>{@code FabricBlockSettings.create()}</td><td>{@code BlockBehaviour.Properties.of()}</td><td>Fabric 设置类已废弃</td></tr>
 *   <tr><td>{@code .sounds(BlockSoundGroup.SNOW)}</td><td>{@code .sound(SoundType.SNOW)}</td><td>声音组</td></tr>
 *   <tr><td>{@code .nonOpaque()}</td><td>{@code .noOcclusion()}</td><td>不遮挡（不挡光/不遮面）</td></tr>
 *   <tr><td>{@code .suffocates(Blocks::never)}</td><td>{@code .isSuffocating(Blocks::never)}</td><td>窒息方块</td></tr>
 *   <tr><td>{@code .blockVision(Blocks::never)}</td><td>{@code .isViewBlocking(Blocks::never)}</td><td>视野阻挡</td></tr>
 *   <tr><td>{@code .allowsSpawning(Blocks::never)}</td><td>{@code .isValidSpawn((s, l, p, t) -&gt; false)}</td><td>表面生成生物</td></tr>
 *   <tr><td>{@code .solidBlock(Blocks::never)}</td><td>{@code .forceSolidOff()}</td><td>概念性固体（26.2 只剩 {@code legacySolid} 这一个开关）</td></tr>
 *   <tr><td>{@code getAmbientOcclusionLightLevel}</td><td>{@code getShadeBrightness}</td><td>环境光遮蔽</td></tr>
 *   <tr><td>{@code isTransparent}</td><td>{@code propagatesSkylightDown}</td><td>让天光透过</td></tr>
 *   <tr><td>{@code onEntityCollision}</td><td>{@code entityInside}</td><td>多了 {@code InsideBlockEffectApplier} 与 boolean 两个参数</td></tr>
 *   <tr><td>{@code entity.slowMovement(state, vec)}</td><td>{@code entity.makeStuckInBlock(state, vec)}</td><td>方块内减速</td></tr>
 *   <tr><td>{@code BlockRenderLayerMap.putBlock(..., getTranslucent())}</td><td><b>已删除</b></td>
 *       <td>26.1 起半透明由贴图 Alpha 自动判定（本方块贴图 Alpha=32 → 半透明）</td></tr>
 * </table>
 *
 * <h2>关于「减速」的取值</h2>
 * 1.20.4 传的是 {@code new Vec3d(1, 1, 1)} —— 那是<b>不减速</b>（乘数 1，等于没实现）。
 * 重制后按 wiki 的「实体可以无窒息但<b>稍有阻力</b>地穿过」取 {@link #INSIDE_DRAG}
 * = {@code (0.9, 0.9, 0.9)}：水平与垂直都只掉 10%，是「稍有阻力」而不是蜘蛛网那种黏住
 * （原版蜘蛛网是 {@code (0.25, 0.05, 0.25)}，人进去几乎出不来）。
 *
 * <h2>「概念性固体 = FALSE」是怎么落地的</h2>
 * 26.2 已经没有 {@code Properties} 级的 solid 谓词（旧 {@code solidBlock(...)} 不在），
 * 唯一的落点是 {@code BlockStateBase.legacySolid}（{@code isSolid()} → {@code blocksMotion()}）。
 * datagen 时打印 {@code isSolid()} 的实测结果：
 * <pre>
 *   气凝胶（noCollision + forceSolidOff）= false      ← 本方块
 *   脚手架 / 细雪（只有 noCollision）      = false
 *   蜘蛛网（forceSolidOn + noCollision）   = true
 *   石头 / 玻璃                            = true
 * </pre>
 * 即：{@code noCollision()} 自己就已经给出 false（脚手架、细雪都是这么来的），
 * {@code forceSolidOn()} 才是把它强行拉回 true 的那个（蜘蛛网故意不让水冲刷）。
 * 所以本方块即便不写 {@code forceSolidOff()} 结果也相同 —— 保留它是为了让「构造表逐行落地」看得见，
 * 真正离不开它的是<b>保留碰撞箱</b>的方块（见 {@link WI_StaticLiquidWater}）。
 * 附带效果是流体可占据本方块 —— 它本来就没有碰撞箱，属预期。
 * <p>{@code Properties.forceSolidOff()} 在 26.2 已标 {@code @Deprecated}（编译会给提示），
 * 但原版自己仍在用（杜鹃花、大滴水叶等），且它是目前唯一能表达「有碰撞箱但不概念性固体」的开关，故保留。
 */
public class WI_Aerogel extends Block {

	/**
	 * 方块内减速乘数（水平 x/z、垂直 y）。
	 * <p>26.2 的 {@code makeStuckInBlock} 是「把速度直接乘以该向量」，1 = 不减速。
	 * <p>取值 = {@code (0.9, 0.9, 0.9)}：轻度阻力，对应 wiki 的「稍有阻力地穿过」
	 * （原版蜘蛛网是 {@code (0.25, 0.05, 0.25)}，本方块不用那么夸张）。
	 */
	private static final Vec3 INSIDE_DRAG = new Vec3(0.9, 0.9, 0.9);

	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block WI_AEROGEL;

	public WI_Aerogel(BlockBehaviour.Properties properties) {
		super(properties);
	}

	/**
	 * 气凝胶的属性（wiki「构造」表的逐行落地）。
	 *
	 * <p><b>注意 {@code setId}</b>：26.2 的方块在构造时就把 id 存进 {@code Properties}
	 * （原版方块也是这么写的），漏写在注册时会报 key 缺失。
	 */
	public static BlockBehaviour.Properties aerogelProperties() {
		return BlockBehaviour.Properties.of()
				// 硬度 0.3 / 爆炸抗性 15.533
				.strength(0.3F, 15.533F)
				// 声音组 SNOW
				.sound(SoundType.SNOW)
				// 不遮挡：不挡光、不遮邻面
				.noOcclusion()
				// 表面生成生物：否
				.isValidSpawn((state, level, pos, entityType) -> false)
				// 窒息方块：否
				.isSuffocating(Blocks::never)
				// 视野阻挡：否
				.isViewBlocking(Blocks::never)
				// 碰撞：无（实体可穿过）
				.noCollision()
				// 概念性固体：否（旧 solidBlock(Blocks::never)；理由见类注释的实测表）
				.forceSolidOff();
	}

	/**
	 * 方块内减速（旧 {@code onEntityCollision}）。
	 * <p>26.2 的签名末尾多了 {@code InsideBlockEffectApplier}（新的「方块内效果」分发器）与
	 * {@code inside} 标记；蜘蛛网、细雪也走这个覆写点，本方块不用分发器，直接改实体速度。
	 */
	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
			InsideBlockEffectApplier effectApplier, boolean inside) {
		entity.makeStuckInBlock(state, INSIDE_DRAG);
	}

	/** 环境光遮蔽为 0（即亮度不被削弱）：旧 {@code getAmbientOcclusionLightLevel} 返回 1.0。 */
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
		WI_AEROGEL = ModRegister.registerBlockWithItem("wi_aerogel",
				new WI_Aerogel(aerogelProperties().setId(ModRegister.blockKey("wi_aerogel"))));
	}
}
