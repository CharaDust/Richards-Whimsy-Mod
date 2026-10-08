package com.rcdmake.whimsy.datagen;

import com.rcdmake.whimsy.datagen.ClassBlockHeadsRebon.BHR_LangEntries;
import com.rcdmake.whimsy.datagen.ClassDEV.DEV_LangEntries;
import com.rcdmake.whimsy.datagen.ClassRandomThings.RDT_LangEntries;
import com.rcdmake.whimsy.datagen.ClassWhimsyIdea.WI_LangEntries;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

/**
 * 模组级<b>简体中文</b>语言文件提供器 —— 全模组<b>唯一</b>的 {@code zh_cn} provider。
 *
 * <h2>为什么必须唯一</h2>
 * 26.2 的 {@code FabricLanguageProvider#getName()} 只由语言码决定
 * （{@code "Language (zh_cn)"}），两个同语言码的 provider 会被 {@code DataGenerator}
 * 以 {@code Duplicate provider} 直接拒绝；即使绕过检查，它们写的也是同一个
 * {@code lang/zh_cn.json}，后写的会盖掉先写的。
 *
 * <p>因此语言层与创造模式物品栏同构：<b>模块各自出条目，模组级统一汇总</b>。
 * 各模块的条目在 {@code datagen/Class<模块>/<模块>_LangEntries} 里，
 * 新增模块只要在这里加一行调用。
 */
public class ModLangProviderZh extends FabricLanguageProvider {

	public ModLangProviderZh(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		// "zh_cn"
		super(output, "zh_cn", registries);
	}

	@Override
	public void generateTranslations(HolderLookup.Provider registries, TranslationBuilder builder) {
		BHR_LangEntries.zh(builder);
		DEV_LangEntries.zh(builder);
		WI_LangEntries.zh(builder);
		RDT_LangEntries.zh(builder);
	}
}
