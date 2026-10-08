package com.rcdmake.whimsy.block.ClassWhimsyIdea;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * 固化水方块（{@code wi_static_liquid_water}）—— 由旧项目 {@code WI_StaticLiquidWater} 迁来。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassWhimsyIdea/WI_StaticLiquidWater.md}）：
 * <ul>
 *   <li>父级：无（全新属性）；硬度/爆炸抗性均为占位值 100（wiki 原话：「随意填写的值，重制后修改」）</li>
 *   <li>地图颜色 WATER_BLUE、声音组 MUD、亮度 0</li>
 *   <li>窒息 ✗、视野阻挡 ✗、概念性固体 ✗；碰撞：默认（有碰撞箱）；表面生成生物 ✗</li>
 *   <li>活塞推动行为：仅推动 —— wiki 注明<b>代码未实现</b>，故本类不设 {@code pushReaction}</li>
 *   <li>渲染：贴图 Alpha 透明（半透明档）；后期上色与「原版水」同原理</li>
 *   <li>改写方法：环境光遮蔽为 0、返回透明方块状态（{@link #skipRendering} 见下）</li>
 *   <li>事件：无</li>
 * </ul>
 *
 * <h2>1.20.4 (Yarn) → 26.2 官方名对照（逐条核验过）</h2>
 * <table border="1">
 *   <caption>本方块涉及的改名</caption>
 *   <tr><th>Yarn（旧）</th><th>26.2 官方名</th><th>说明</th></tr>
 *   <tr><td>{@code FabricBlockSettings.create()}</td><td>{@code BlockBehaviour.Properties.of()}</td><td>Fabric 设置类已废弃</td></tr>
 *   <tr><td>{@code MapColor.WATER_BLUE}</td><td>{@code MapColor.WATER}</td><td>26.2 的地图色常量去掉了后缀</td></tr>
 *   <tr><td>{@code BlockSoundGroup.MUD}</td><td>{@code SoundType.MUD}</td><td>声音组</td></tr>
 *   <tr><td>{@code suffocates / blockVision / allowsSpawning / solidBlock}</td>
 *       <td>{@code isSuffocating / isViewBlocking / isValidSpawn / forceSolidOff}</td><td>同 {@code WI_Aerogel}</td></tr>
 *   <tr><td>{@code getAmbientOcclusionLightLevel} = 0.9f</td><td>{@code getShadeBrightness} = 0.9F</td>
 *       <td>wiki 写「环境光遮蔽设置为 0」，旧代码取的是 0.9（近满亮）；这里保持旧代码的 0.9F</td></tr>
 *   <tr><td>{@code isTransparent}</td><td>{@code propagatesSkylightDown}</td><td>让天光透过</td></tr>
 *   <tr><td>{@code WI_ColorProvider}（Fabric {@code ColorProviderRegistry}）</td>
 *       <td>{@code BlockColorRegistry} + {@code BlockTintSources.water()}</td>
 *       <td>方块用<b>灰度</b>贴图 + 按生物群系水色上色；物品另用
 *           {@code wi/static_liquid_water_mapped.png}（已乘好水色）的专用物品模型，
 *           所以物品侧<b>没有</b>颜色提供器</td></tr>
 * </table>
 *
 * <h2>{@code skipRendering}（贴合面剔除）的完整链路 —— 以及它为什么一开始没生效</h2>
 * 26.2 里这条链路是成立的，逐层核验过：
 * <pre>{@code
 * SectionCompiler.compile           → new ModelBlockRenderer(ambientOcclusion, cull = true, blockColors)
 *   → ModelBlockRenderer.tesselateBlock / tesselateAmbientOcclusion / tesselateFlat
 *     → ModelBlockRenderer.shouldRenderFace(level, state, direction, neighborPos)
 *       → Block.shouldRenderFace(state, neighbor, direction)
 *         → 第 1 关：neighbor.getFaceOcclusionShape(dir.getOpposite()) == Shapes.block() → 剔除
 *         → 第 2 关：state.skipRendering(neighbor, dir) → 本类的覆写 → true → 该面不渲染
 * }</pre>
 * ⚠️ <b>但第 2 关只对「带 cullface 的面」生效</b>：模型里按面方向分组的是 {@code cullface}，
 * 没有写 {@code cullface} 的面会落进「无方向」那一组，渲染时<b>根本不问</b>
 * {@code shouldRenderFace}，于是贴合面永远被画出来。
 * 本方块原来的模型是自己手写 {@code elements}（为了给每个面加 {@code tintindex}），
 * 六个面都没写 {@code cullface} —— 这才是「两块贴合还能看到里面那张面」的真正原因，
 * 与方块类里的 {@code skipRendering} 无关。<b>修的是模型的 JSON，不是 Java。</b>
 * <p>原版范本：{@code models/block/cube.json} 的六个面都带 {@code cullface}
 * （{@code cube_all} 继承它，所以 {@code WI_Aerogel} 等直接用 {@code cube_all} 的方块天生就有）；
 * {@code grass_block.json} / {@code leaves.json} 则示范了 {@code tintindex} 与 {@code cullface} 可以同面共存。
 *
 * <p>同批实测（datagen 时直接调 {@code Block.shouldRenderFace}，{@code false} = 判定为「不渲染」）：
 * <pre>
 *   固化水 | 固化水 = false      糖玻璃 | 糖玻璃 = false      冰 | 冰     = false
 *   气凝胶 | 气凝胶 = true       玻璃（原版）| 玻璃 = true      石头 | 石头 = false
 * </pre>
 * 注意最后两行：气凝胶（{@code cube_all} + 无 {@code skipRendering} 覆写）与原版玻璃都不剔除贴合面，
 * 属预期。判定为 false 之后是否真的不画，还要看模型那一面有没有 {@code cullface}。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code wi_static_liquid_water}　<b>模块</b> WI　<b>父级</b> 无（全新属性）</li>
 *   <li><b>启用的可选层</b>：层 10 形状与外观（{@code getShadeBrightness} / {@code propagatesSkylightDown} /
 *       {@code skipRendering}）、<b>层 C 客户端接线</b>（{@code BlockColorRegistry} + {@code BlockTintSources.water()}，
 *       写在 {@code RichardsWhimsyModClient}，本类不出现任何客户端类型）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 3 状态属性（单状态）、层 6 家族、层 7 codec、层 8 生命周期、
 *       层 9 交互与判定（wiki 无事件；旧代码的「右键吃掉」是文档抄错，已删）、层 11 预留区</li>
 *   <li><b>能力阶梯</b>：停在第 3 级「登记其他注册表 / 数据驱动制品」—— 群系水色上色用的是颜色注册表 +
 *       灰度贴图（物品另用乘好水色的 {@code _mapped} 贴图），既没有客户端着色器，也没有 Mixin</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☑ {@code BlockStateGenerator.collectAll()}（单状态）
 *       　③☑ {@code ItemModelGenerator.ITEM_MODEL_OVERRIDES}（物品用 {@code item/wi_static_liquid_water}）
 *       　④☑ 中英 {@code WI_LangEntries}　⑤☑ {@code WI_CreativeTabs} 方块页　⑥☑ 贴图 / 模型（模型手写，
 *       六面都带 {@code cullface}）　⑦☐ 配方与战利品表</li>
 * </ul>
 */
public class WI_StaticLiquidWater extends Block {

	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block WI_STATIC_LIQUID_WATER;

	/** 固化水方块的属性（wiki「构造」表逐行落地）。 */
	public static BlockBehaviour.Properties staticLiquidWaterProperties() {
		// 碰撞：wiki 写「默认」→ 这里刻意不调 noCollision()，保持完整碰撞箱
		return BlockBehaviour.Properties.of()
				// 地图颜色 WATER_BLUE
				.mapColor(MapColor.WATER)
				// 硬度 / 爆炸抗性（wiki 注明是随意填的占位值）
				.strength(100.0F, 100.0F)
				// 声音组 MUD
				.sound(SoundType.MUD)
				// 不遮挡
				.noOcclusion()
				// 表面生成生物：否
				.isValidSpawn((state, level, pos, entityType) -> false)
				// 窒息方块：否
				.isSuffocating(Blocks::never)
				// 视野阻挡：否
				.isViewBlocking(Blocks::never)
				// 概念性固体：否（旧 solidBlock(Blocks::never)；
				// 26.2 只剩 legacySolid 一个开关，noCollision() 不顶用，实测见 WI_Aerogel 类注释。
				// 副作用：流体可以占据本方块 —— 1.20.4 的 solidBlock(never) 同样如此）
				.forceSolidOff();
	}

	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：只做装配，没有别的逻辑。
	 * <p>属性来自上面的 {@link #staticLiquidWaterProperties()} —— 它是本构造器实参的来源。
	 */
	public WI_StaticLiquidWater(BlockBehaviour.Properties properties) {
		super(properties);
	}

	/** 环境光遮蔽：旧代码 0.9f。 */
	@Override
	protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		return 0.9F;
	}

	/** 返回透明方块状态：旧 {@code isTransparent} 恒为 true。 */
	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		return true;
	}

	/**
	 * 两个相同的固化水方块相连时，贴合的那一面不渲染（旧 {@code isSideInvisible}）。
	 * <p>26.2 的这条链路成立（{@code Block.shouldRenderFace} → {@code state.skipRendering}），
	 * 判定结果也是「剔除」；但真正让它生效的还有模型那一侧的 {@code cullface} ——
	 * 详见类注释（这是本方块当初「贴合面照样渲染」的真正原因）。
	 */
	@Override
	protected boolean skipRendering(BlockState state, BlockState neighbor, Direction direction) {
		if (neighbor.getBlock() == this) {
			return true;
		}
		return super.skipRendering(state, neighbor, direction);
	}

	public static void OnInit() {
		WI_STATIC_LIQUID_WATER = ModRegister.registerBlockWithItem("wi_static_liquid_water",
				new WI_StaticLiquidWater(staticLiquidWaterProperties()
						.setId(ModRegister.blockKey("wi_static_liquid_water"))));
	}
}
