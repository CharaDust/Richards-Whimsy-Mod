package com.rcdmake.whimsy;

import com.rcdmake.whimsy.block.Chargeableblock;
import com.rcdmake.whimsy.block.DEVBlocks;
import com.rcdmake.whimsy.block.ModBlocks;
import com.rcdmake.whimsy.item.DEVItems;
import com.rcdmake.whimsy.item.ModItemGroup;
import com.rcdmake.whimsy.item.ModItems;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RichardsWhimsyMod implements ModInitializer {
	// 此记录器用于将文本写入控制台和日志文件。
	// 最佳做法是使用您的 mod ID 作为记录器的名称。
	// 这样一来，哪个模组写了信息、警告和错误就很清楚了。
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MOD_ID = "richards-whimsy-mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


	@Override
	public void onInitialize() {
		// 这些代码会在 Minecraft mod加载阶段运行
		// 然而，一些文件（例如资源文件）可能不会初始化
		// 请谨慎行事
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
		// 每一次写一个文件都要在此处引用，格式为`类名.方法名`，写完后记得import
		// 初始化 DEVItems
		DEVBlocks.registerDEVBlocks();
		// 初始化 DEVItems
		DEVItems.registerDEVItems();
		// 初始化 ModBlocks
		ModBlocks.registerModBlocks();
		// 初始化 ModItems
		ModItems.registerModItems();
		// 初始化 ModItemGroup
		ModItemGroup.registerModItemGroup();
		// 初始化 Chargeableblock
		Chargeableblock.registerConfigBlockSettings();

	}
}