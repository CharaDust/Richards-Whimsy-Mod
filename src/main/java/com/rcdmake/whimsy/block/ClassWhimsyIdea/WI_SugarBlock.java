package com.rcdmake.whimsy.block.ClassWhimsyIdea;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;


public class WI_SugarBlock extends Block {
    public static final Block WI_SUGARBLOCK = new WI_SugarBlock(FabricBlockSettings.copyOf(Blocks.STONE)
            .sounds(BlockSoundGroup.SAND)
    );

    public WI_SugarBlock(Settings settings) {
        super(settings);
    }

    // 右键点击事件
    // 吃
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit){
        if (!world.isClient){
            // 如果想要添加饥饿值已满的检测，请使用以下条件
            // player.canConsume(false)

            // 测试消息
//            player.sendMessage(Text.literal("I ate it..."));
            // 恢复饥饿值
            player.getHungerManager().add(4, 0.3F);
            // 播放吃的声音
            world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            // 增加统计数据（可选）
            player.incrementStat(Stats.USED.getOrCreateStat(WI_SUGARBLOCK.asItem()));
            // 吃掉了（破坏方块）
            world.breakBlock(pos, false);
        }

        return ActionResult.SUCCESS;
    }

    // 方块以及方块物品注册方法简化写法
    private static void register_Block_and_BlockItem(String path, Block block){
        Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, path), block);
        Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, path), new BlockItem(block, new FabricItemSettings()));
    }

    public static void OnInit(){
        register_Block_and_BlockItem("wi_sugarblock", WI_SUGARBLOCK);
    }
}
