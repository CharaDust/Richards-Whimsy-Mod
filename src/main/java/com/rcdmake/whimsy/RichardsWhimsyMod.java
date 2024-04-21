package com.rcdmake.whimsy;

import com.rcdmake.whimsy.ItemGroup.BHR_General;
import com.rcdmake.whimsy.ItemGroup.RDT_General;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_Rocks;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_Soils;
import com.rcdmake.whimsy.block.ClassDEV.*;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_GlassLapis;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_GlassQuartz;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_SuperLubricentIce;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_Aerogel;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_StaticLiquidWater;
import com.rcdmake.whimsy.block.ModBlocks;
import com.rcdmake.whimsy.item.ClassBlockHeadRebon.BHR_FoodLime;
import com.rcdmake.whimsy.item.ClassBlockHeadRebon.BHR_FoodOther;
import com.rcdmake.whimsy.item.ClassBlockHeadRebon.BHR_FuelEmber;
import com.rcdmake.whimsy.item.ClassBlockHeadRebon.BHR_OtherItems;
import com.rcdmake.whimsy.item.ClassDEV.DEVItems;
import com.rcdmake.whimsy.item.ClassWhimsyIdea.WI_Prospector;
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
		// 初始化 ModBlocks
		ModBlocks.register();
		// 初始化 ModItems
		ModItems.registerModItems();
		// 初始化 ModItemGroup
		ModItemGroup.register();

		// 初始化 DEV 分支内容
		DEV_StateBoolean.OnInit();
		DEV_StateInt.OnInit();
		DEV_StateEnum.OnInit();
		DEV_StateEnumAxis.OnInit();
		DEV_Blocks.OnInit();
		DEV_GhostBlock.OnInit();
		DEVItems.registerDEVItems();

		// 初始化 BHR 分支内容
		BHR_Rocks.OnInit();
		BHR_Soils.OnInit();
		BHR_General.OnInit();
		BHR_OtherItems.OnInit();
		BHR_FoodOther.OnInit();
		BHR_FoodLime.OnInit();
		BHR_FuelEmber.OnInit();

		// 初始化 WI 分支内容
		WI_Aerogel.OnInit();
		WI_StaticLiquidWater.OnInit();
		WI_Prospector.OnInit();

		// 初始化 RDT 分支内容
		RDT_SuperLubricentIce.OnInit();
		RDT_GlassLapis.OnInit();
		RDT_GlassQuartz.OnInit();
		RDT_General.OnInit();

	}
}