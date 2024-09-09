package com.rcdmake.whimsy;

import com.rcdmake.whimsy.datagen.ClassWhimsyIdea.WI_LootTableProvider;
import com.rcdmake.whimsy.datagen.ModBlockTagProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class RichardsWhimsyModDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(ModBlockTagProvider::new);
		pack.addProvider(WI_LootTableProvider::new);

	}
}
