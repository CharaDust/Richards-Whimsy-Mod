package com.rcdmake.whimsy;

import com.rcdmake.whimsy.datagen.BlockStateGenerator;
import com.rcdmake.whimsy.datagen.ItemModelGenerator;
import com.rcdmake.whimsy.datagen.ModLangProviderEn;
import com.rcdmake.whimsy.datagen.ModLangProviderZh;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

import java.nio.file.Path;

/**
 * 数据生成入口（fabric-datagen）：{@code gradlew runDatagen} 时执行。
 *
 * <p>provider 按<b>模块</b>分包，放在 {@code datagen.Class<模块>/<模块>_<用途>Provider.java}。
 * 本类只负责登记 provider，以及调用不属于 provider 体系的生成器（见下）。
 *
 * <p><b>blockstate 的特殊处理</b>：26.2 没有「只生成 blockstate」的公共 API
 * （详见 {@link BlockStateGenerator} 的说明），所以它不走 provider 体系，
 * 而是在这里直接调用。产出路径与其它 datagen 产出一致，
 * 都在本模组的运行时资源根下。
 *
 * <p><b>用法</b>：先 {@code gradlew runDatagen}，再 {@code gradlew build}
 * （顺序不能反，否则产出进不了 jar）。日常用 {@code gradlew syncDatagen}，
 * 它会把产出复制进 {@code src/main/resources}。
 *
 * <p>26.2 的坑（已对 26.2 的 fabric-data-generation-api-v1-25.6.0 逐条核验）：
 * <ul>
 *   <li>{@code FabricDataOutput} 已改名 {@code FabricPackOutput}</li>
 *   <li>{@code RegistryWrapper.WrapperLookup} → {@code HolderLookup.Provider}</li>
 *   <li>{@code FabricBlockLootTableProvider} → {@code FabricBlockLootSubProvider}</li>
 *   <li>{@code FabricTagProvider} → {@code FabricTagsProvider}</li>
 *   <li>{@code valueLookupBuilder} 已被删除</li>
 *   <li>{@code FabricDataProvider} <b>不存在</b>（服务端唯一通用基类是 {@code FabricCodecDataProvider}）</li>
 *   <li>26.2 <b>没有客户端 datagen 入口点</b>；{@code FabricModelProvider} 在
 *       {@code api.client.datagen.v1.provider} 下，无法在服务端 datagen 里用</li>
 * </ul>
 */
public class RichardsWhimsyModDataGenerator implements DataGeneratorEntrypoint {

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		FabricDataGenerator.Pack pack = generator.createPack();

		// --- 语言文件（模组级，每个语言码只能有一个 provider） ---
		// 各模块的条目在 datagen/Class<模块>/<模块>_LangEntries 里，
		// 由这两个 provider 汇总（理由与写法见 ModLangProviderZh 的类注释）。
		pack.addProvider(ModLangProviderEn::new);
		pack.addProvider(ModLangProviderZh::new);

		// --- 战利品表：暂不生成 ---
		// 用户决定：所有方块和物品暂不实现战利品表功能。
		// DEV_BlockLootProvider 保留在 datagen/ClassDEV/ 下作为以后恢复的模板；
		// WI 各页的「获取方式」（挖掘工具标签 mineable/shovel、掉落物、精准采集分支）
		// 同样留到后续「获取方式」阶段统一落地。
		// 需要时在这里加：pack.addProvider(<模块>_BlockLootProvider::new);

		// --- 方块状态 / 物品模型定义（不走 provider，原因见各自类注释） ---
		// 26.2 的物品模型是「两段式」：assets/<ns>/items/<id>.json 做定义，
		// 再指向 models/<...> 里的模型。缺第一段 → MissingItemModel（暗淡紫黑贴图）。
		try {
			Path modRoot = generator.getModContainer().getRootPath();
			BlockStateGenerator.generate(modRoot);
			ItemModelGenerator.generate(modRoot);
		} catch (Exception e) {
			throw new RuntimeException("blockstate / 物品模型定义生成失败", e);
		}
	}
}
