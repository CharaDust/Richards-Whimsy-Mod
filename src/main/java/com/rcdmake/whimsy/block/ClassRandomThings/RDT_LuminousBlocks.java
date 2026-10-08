package com.rcdmake.whimsy.block.ClassRandomThings;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * 鲁米诺方块（{@code rdt_luminous_block_red} / {@code _green} / {@code _blue}）
 * —— 由旧项目 {@code RDT_LuminousBlocks} 迁来。一个类含 3 个方块（同一行为的换色版本）。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassRandomThings/RDT_LuminousBlocks.md}）：
 * <ul>
 *   <li>父级：玻璃；硬度 0.5；其余逐行「父级」，但<b>光线阻挡 FALSE</b>、<b>亮度 0</b>
 *       （wiki 原话：「不实际发光，仅视觉上发光」）</li>
 *   <li>渲染：忽略任何修改因素，将该方块渲染为纯色 —— wiki 标了<b>（未实现）</b></li>
 *   <li>改写方法 / 事件 / 方块状态：无；获取方式：无</li>
 * </ul>
 *
 * <h2>实现「始终渲染为纯色」的做法</h2>
 * 三张贴图本身就是<b>单一纯色</b>（实测：红 {@code #FF0000} / 绿 {@code #00FF00} / 蓝 {@code #0000FF}，
 * 每张只有一种像素），所以不需要任何颜色提供器（不需要 {@code BlockColorRegistry}、也不需要 {@code tints}）。
 * 缺的只是「忽略光照与其他修改因素」，由两处负责：
 * <ol>
 *   <li><b>方块属性</b>：{@code emissiveRendering(Blocks::always)} —— 让方块始终以满亮度渲染（不吃光照）。
 *       26.2 里这个开关由客户端的 {@code BlockModelResolver} 读取，是现行管线里真正生效的那个。</li>
 *   <li><b>模型</b>：{@code "ambientocclusion": false} + 每个面 {@code "shade": false} ——
 *       关掉环境光遮蔽与「面朝向明暗」，于是六个面颜色完全一致，不会出现顶面亮、侧面暗的渐变。
 *       模型因此写成显式 {@code elements}（照例给六个面都补上 {@code cullface}，否则贴合面不会被剔除）。</li>
 * </ol>
 *
 * <h2>与 1.20.4 代码的差异（都按 wiki / 现目标走）</h2>
 * <table border="1">
 *   <caption>wiki 与旧代码不一致的地方</caption>
 *   <tr><th>项目</th><th>1.20.4 旧代码</th><th>本阶段做法</th></tr>
 *   <tr><td>硬度</td><td>{@code copyOf(GLASS)}（0.3）</td><td>wiki 写 0.5 → {@code destroyTime(0.5F)}
 *       （只改硬度，爆炸抗性仍是父级玻璃的）</td></tr>
 *   <tr><td>亮度</td><td>{@code luminance(state -&gt; 1)}（真的发 1 级光）</td>
 *       <td>wiki 写 <b>0</b>（「不实际发光」）→ 不设 {@code lightLevel}</td></tr>
 *   <tr><td>渲染为纯色</td><td>{@code emissiveLighting(Blocks::always)}（单独用，且配了 luminance 1）</td>
 *       <td>保留 {@code emissiveRendering(Blocks::always)} 并补上模型侧的
 *           {@code ambientocclusion:false} + {@code shade:false}，这才是「纯色」的完整配方</td></tr>
 * </table>
 *
 * <h2>1.20.4 (Yarn) → 26.2 官方名对照（本方块实际用到的部分）</h2>
 * <table border="1">
 *   <tr><th>Yarn（旧）</th><th>26.2 官方名</th><th>说明</th></tr>
 *   <tr><td>{@code FabricBlockSettings.copyOf(Blocks.GLASS)}</td>
 *       <td>{@code BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)}</td><td>「父级 = 玻璃」</td></tr>
 *   <tr><td>{@code nonOpaque()}</td><td>{@code noOcclusion()}</td>
 *       <td>光线阻挡 FALSE —— 玻璃本身就带 {@code noOcclusion}，这里写出来是为了让 wiki 那一行可见</td></tr>
 *   <tr><td>{@code luminance(state -&gt; 1)}</td><td>{@code lightLevel(ToIntFunction)}</td>
 *       <td>本阶段按 wiki 不启用（亮度 0）</td></tr>
 *   <tr><td>{@code emissiveLighting(Blocks::always)}</td><td>{@code emissiveRendering(Predicate)}</td>
 *       <td>名称变了但语义不变，本阶段启用</td></tr>
 * </table>
 *
 * <p>三个方块除了贴图之外完全一致，所以共用同一条属性构造与同一个注册方法 —— 这正是「一种方块的不同颜色」
 * 那个被开发约定允许的例外。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code rdt_luminous_block_red} / {@code _green} / {@code _blue}
 *       　<b>模块</b> RDT　<b>父级</b> 玻璃（硬度改 0.5）</li>
 *   <li><b>一个类三个方块</b>：层 2 并列三个字段（同一行为的换色版本，开发约定的允许例外）；
 *       它们的属性、行为、渲染完全共用一条路径</li>
 *   <li><b>启用的可选层</b>：层 4 属性工厂（{@code destroyTime} / {@code noOcclusion} /
 *       {@code emissiveRendering}）—— 纯声明，<b>一个覆写点都没有</b></li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 3 状态属性（单状态）、层 6 家族、层 7 codec、层 8 生命周期、
 *       层 9 交互与判定、层 10 形状与外观、层 11 预留区、层 C 客户端、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：停在第 1 级「声明」—— 「始终渲染为纯色」由属性 {@code emissiveRendering} +
 *       模型侧的 {@code ambientocclusion:false} / {@code shade:false} 解决，<b>不需要</b>颜色提供器、也不需要 Mixin</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☑ {@code BlockStateGenerator.collectAll()}（三个都单状态）
 *       　③☒ 物品模型无例外　④☑ 中英 {@code RDT_LangEntries}（三条）　⑤☑ {@code RDT_CreativeTabs}
 *       　⑥☑ 贴图 / 模型（模型是手写的扁平 {@code elements}，六面带 {@code cullface}）
 *       　⑦☐ 配方与战利品表（wiki：获取方式「无」）</li>
 * </ul>
 */
public class RDT_LuminousBlocks extends Block {

	/** 红色鲁米诺方块 {@code rdt_luminous_block_red}。 */
	public static Block RDT_LUMINOUS_BLOCK_RED;
	/** 绿色鲁米诺方块 {@code rdt_luminous_block_green}。 */
	public static Block RDT_LUMINOUS_BLOCK_GREEN;
	/** 蓝色鲁米诺方块 {@code rdt_luminous_block_blue}。 */
	public static Block RDT_LUMINOUS_BLOCK_BLUE;

	/**
	 * 鲁米诺方块的属性（wiki「构造」表逐行落地 + 纯色渲染所需的 emissive）。
	 * <p>三个方块共用这一条 —— 换色只在贴图上体现。
	 */
	public static BlockBehaviour.Properties luminousProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
				// 硬度 0.5（爆炸抗性保持父级玻璃的值）
				.destroyTime(0.5F)
				// 光线阻挡 FALSE
				.noOcclusion()
				// 始终满亮度渲染（「忽略光照」这一半；另一半在模型的 ambientocclusion/shade 上）
				// 参数类型是 Predicate<BlockState>（单参），所以这里用 lambda 而不是 Blocks::always（三参谓词）
				.emissiveRendering(state -> true);
		// 亮度：wiki 写 0（不实际发光），故不设 lightLevel
	}

	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：只做装配，没有别的逻辑。
	 * <p>属性来自上面的 {@link #luminousProperties()} —— 三个方块共用它。
	 */
	public RDT_LuminousBlocks(BlockBehaviour.Properties properties) {
		super(properties);
	}

	public static void OnInit() {
		RDT_LUMINOUS_BLOCK_RED = ModRegister.registerBlockWithItem("rdt_luminous_block_red",
				new RDT_LuminousBlocks(luminousProperties().setId(ModRegister.blockKey("rdt_luminous_block_red"))));
		RDT_LUMINOUS_BLOCK_GREEN = ModRegister.registerBlockWithItem("rdt_luminous_block_green",
				new RDT_LuminousBlocks(luminousProperties().setId(ModRegister.blockKey("rdt_luminous_block_green"))));
		RDT_LUMINOUS_BLOCK_BLUE = ModRegister.registerBlockWithItem("rdt_luminous_block_blue",
				new RDT_LuminousBlocks(luminousProperties().setId(ModRegister.blockKey("rdt_luminous_block_blue"))));
	}
}
