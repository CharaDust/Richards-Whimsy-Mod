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
 * 模组级<b>英文</b>语言文件提供器 —— 全模组<b>唯一</b>的 {@code en_us} provider
 * （与 {@link ModLangProviderZh} 对称，理由见该类的说明）。
 */
public class ModLangProviderEn extends FabricLanguageProvider {

	public ModLangProviderEn(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, "en_us", registries);
	}

	@Override
	public void generateTranslations(HolderLookup.Provider registries, TranslationBuilder builder) {
		BHR_LangEntries.en(builder);
		DEV_LangEntries.en(builder);
		WI_LangEntries.en(builder);
		RDT_LangEntries.en(builder);
	}
}
