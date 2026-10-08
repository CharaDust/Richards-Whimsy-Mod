package com.rcdmake.whimsy.datagen.ClassDEV;

import com.rcdmake.whimsy.block.ClassDEV.DEV_BlocksOther;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

/**
 * 初学开发（DEV）分支的方块战利品表数据生成。
 *
 * <p>26.2 要点（已核验）：
 * <ul>
 *   <li>基类类名 {@code FabricBlockLootSubProvider}
 *       （旧名 {@code FabricBlockLootTableProvider} 已不在）。</li>
 *   <li>输出类型 {@code FabricPackOutput}（旧名 {@code FabricDataOutput}）。</li>
 *   <li>注册表类型 {@code HolderLookup.Provider}（旧名 {@code RegistryWrapper.WrapperLookup}）。</li>
 *   <li>基类构造器是 {@code protected}，所以 provider 必须继承、不能直接实例化。</li>
 *   <li>战利品表目录自 1.21 起为单数 {@code loot_table/}。</li>
 * </ul>
 *
 * <p>wiki 的「获取方式」目前统一写「待迁移后统一设定」，
 * 因此这里先按最通行的「挖掉自己掉自己」处理。
 */
public class DEV_BlockLootProvider extends FabricBlockLootSubProvider {

	public DEV_BlockLootProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	public void generate() {
		dropSelf(DEV_BlocksOther.DEV_BLOCK_NORMAL);
		dropSelf(DEV_BlocksOther.DEV_BLOCK_VECTOR_FIX_SIDE_U);
	}
}
