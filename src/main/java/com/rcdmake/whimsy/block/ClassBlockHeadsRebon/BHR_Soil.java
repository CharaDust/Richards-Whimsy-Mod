package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;

/**
 * 红壤（{@code bhr_soil}）—— 由旧项目 {@code BHR_Soils} 拆分迁来（旧注册名 {@code mineral_soil}）。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassBlockHeadsRebon/BHR_Soil.md}）：
 * <ul>
 *   <li>父级：<b>无</b>（自成一系）；硬度 0.6；爆炸抗性 / 阻力 / 各类乘数等逐行「默认」</li>
 *   <li>地图颜色 ORANGE；声音组 PACKED_MUD；渲染常规；改写方法 / 事件：无</li>
 *   <li>方块状态：<b>{@code GRASS_COVER}</b>（BOOLEAN）—— 覆草状态，默认 FALSE，
 *       「不拆分为独立方块」（所以它是一个状态，不是第二个方块）</li>
 *   <li>特点简介：红橙色的土壤方块，可作为种植基底</li>
 *   <li>获取方式：战利品表待「获取方式」阶段统一设定</li>
 * </ul>
 *
 * <h2>1.20.4 (Yarn) → 26.2 官方名对照（本类实际用到的部分）</h2>
 * <table border="1">
 *   <caption>改名对照</caption>
 *   <tr><th>Yarn（旧）</th><th>26.2 官方名</th><th>说明</th></tr>
 *   <tr><td>{@code FabricBlockSettings.create()}</td><td>{@code BlockBehaviour.Properties.of()}</td>
 *       <td>「父级：无」—— 注意 {@code of()} 的爆炸抗性 / 硬度默认都是 0</td></tr>
 *   <tr><td>{@code mapColor(MapColor.ORANGE)}</td><td>{@code mapColor(MapColor.COLOR_ORANGE)}</td>
 *       <td>颜色族常量改名，基础色名加了 {@code COLOR_} 前缀</td></tr>
 *   <tr><td>{@code sounds(BlockSoundGroup.PACKED_MUD)}</td><td>{@code sound(SoundType.PACKED_MUD)}</td>
 *       <td>单复数 + 类名变化</td></tr>
 *   <tr><td>{@code BooleanProperty.of("grass_cover")}</td><td>{@code BooleanProperty.create("grass_cover")}</td>
 *       <td><b>序列化名保持 {@code grass_cover}</b>，手写 blockstate 的 variants 键原样可用</td></tr>
 *   <tr><td>{@code appendProperties(StateManager.Builder)}</td>
 *       <td>{@code createBlockStateDefinition(StateDefinition.Builder)}</td><td>状态模式声明点改名</td></tr>
 *   <tr><td>{@code setDefaultState(getDefaultState().with(...))}</td>
 *       <td>{@code registerDefaultState(defaultBlockState().setValue(...))}</td><td>只能在 {@code super(...)} 之后</td></tr>
 * </table>
 *
 * <h2>与旧代码的一处刻意差异：爆炸抗性</h2>
 * 旧代码写的是 {@code strength(0.6f)} —— 这个便捷方法<b>同时</b>把硬度和爆炸抗性都设成 0.6；
 * wiki 的「爆炸抗性」一行写的是「默认」，所以本类改用 {@code destroyTime(0.6F)}，
 * 只设硬度、爆炸抗性留给默认值（{@code Properties.of()} 的默认是 0.0F）。
 * 若要让红壤更「像泥土」，把 wiki 那一行改成 0.6 或改用 {@code strength(0.6F)} 即可。
 *
 * <h2>层 6 ｜ 家族：红壤是「父级」</h2>
 * wiki 里红壤燧石矿土、红壤粘土矿土的「父级」都写「红壤」，方块状态栏也写「继承自红壤」——
 * 所以那两个类直接 {@code extends BHR_Soil}，<b>白拿</b> {@code GRASS_COVER} 的状态定义
 * （{@link #createBlockStateDefinition}）与默认值（构造器里的 {@code registerDefaultState}），
 * 自己一行状态代码都不用写。
 *
 * <h2>方块状态与手写 blockstate</h2>
 * {@code grass_cover} 只有 2 种取值，但<b>多状态方块的 blockstate 一律手写</b>（范式 §4）：
 * {@code blockstates/bhr_soil.json} 里 {@code grass_cover=false} → {@code block/bhr_soil}，
 * {@code grass_cover=true} → {@code block/bhr_soil_grass}（覆草版的顶面/侧面是另一套贴图）。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_soil}　<b>模块</b> BHR　<b>父级</b> 无（{@code Properties.of()}）</li>
 *   <li><b>启用的可选层</b>：层 3 状态属性（{@code GRASS_COVER}）、层 8 状态模式（{@code createBlockStateDefinition}）
 *       —— 另外它同时是层 6 的<b>家族父类</b>（被两个矿土类继承）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 7 codec、层 9 / 10（一个覆写点都没有）、层 11 预留区、
 *       层 C 客户端、层 M Mixin；（层 6 不是「跳过」而是「被继承」）</li>
 *   <li><b>能力阶梯</b>：停在第 2 级「覆写本类方法」—— 多状态必须覆写 {@code createBlockStateDefinition}；
 *       覆草状态目前没有任何逻辑去改写它（wiki「改写方法：无」），等后续机制（比如草蔓延）落地再归层 8</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}（必须排在两个矿土子类<b>之前</b>，见下）
 *       　②☒ <b>不在</b> {@code BlockStateGenerator.collectAll()} 里声明（多状态，手写）
 *       　③☒ 物品模型无例外　④☑ 中英 {@code BHR_LangEntries}　⑤☑ {@code BHR_CreativeTabs}
 *       　⑥☑ 贴图 {@code bhr/mineral_soil*.png}（沿用旧资源文件名）/ 模型 {@code models/block/bhr_soil.json} + {@code bhr_soil_grass.json}
 *       　⑦☐ 配方 / 战利品表 / 挖掘标签 ——「获取方式」阶段统一做</li>
 * </ul>
 */
public class BHR_Soil extends Block {

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Block BHR_SOIL;

	// ── 层 3 ｜ 状态属性字段 ──
	/** 状态属性字段（层 3）：覆草，序列化名 {@code grass_cover}（默认 FALSE）。 */
	public static final BooleanProperty GRASS_COVER = BooleanProperty.create("grass_cover");

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/**
	 * 红壤的属性（层 4）：wiki「构造」表逐行落地。
	 * <p><b>父级是「无」</b>，所以这里从 {@code Properties.of()} 起手，而不是 {@code ofFullCopy(...)}；
	 * 爆炸抗性刻意不设（wiki 写「默认」），理由见类注释。
	 * <p>这个工厂同时被两个矿土子类复用：{@code BHR_Soil.soilProperties().destroyTime(0.7F)}。
	 */
	public static BlockBehaviour.Properties soilProperties() {
		return BlockBehaviour.Properties.of()
				// 地图颜色 ORANGE
				.mapColor(MapColor.COLOR_ORANGE)
				// 硬度 0.6（爆炸抗性保持默认值）
				.destroyTime(0.6F)
				// 声音组 PACKED_MUD
				.sound(SoundType.PACKED_MUD);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：装配 + 给状态初值。
	 * <p>{@code registerDefaultState} 只能写在 {@code super(...)} 之后（状态模式已在 {@code super} 内部定死）。
	 * <p>子类（{@code BHR_OreSoilFlint} / {@code BHR_OreSoilClay}）只调 {@code super(properties)} 就继承到这一切。
	 */
	public BHR_Soil(BlockBehaviour.Properties properties) {
		super(properties);
		// 默认状态：grass_cover = false
		registerDefaultState(defaultBlockState().setValue(GRASS_COVER, false));
	}

	// ── 层 8 ｜ 状态模式与生命周期 ──
	/** 状态模式（层 8）：声明本方块有 {@code grass_cover} 这一个状态维度（子类继承本方法）。 */
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(GRASS_COVER);
	}

	// ── 层 12 ｜ 初始化 ──
	/** 注册（层 12）；主类里必须排在继承它的两个矿土<b>之前</b>（家族父类先就位）。 */
	public static void OnInit() {
		BHR_SOIL = ModRegister.registerBlockWithItem("bhr_soil",
				new BHR_Soil(soilProperties()
						.setId(ModRegister.blockKey("bhr_soil"))));
	}
}
