package com.rcdmake.whimsy.item.ClassWhimsyIdea;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WI_Prospector extends Item {

    public static final Item WI_PROSPECTOR = registerItems("wi_prospector", new WI_Prospector(new FabricItemSettings().maxDamage(64)));


    // 物品注册方法
    private static Item registerItems(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, name),item);
    }

    // 作用在方块上时
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        // 获取世界
        World world = context.getWorld();
        // 如果不是服务端世界
        if (!world.isClient()){
            BlockPos blockPos = context.getBlockPos();
            PlayerEntity playerEntity = context.getPlayer();
            boolean foundBlock = false;
            for (int i = -64; i <= blockPos.getY() +64; i ++) { // loop
                BlockState state = context.getWorld().getBlockState(blockPos.down(i));
                if(isOre(state)){
                    outputMessage(blockPos.down(i),playerEntity,state.getBlock());
                    foundBlock = true;
                    break;
                }
            }
            if (!foundBlock){
                playerEntity.sendMessage(Text.literal("Not found block"), false);
            }
        }
        // 无论判断成功与否，均消耗1点耐久
        context.getStack().damage(
                1,
                context.getPlayer(),
                // 展示工具破碎效果
                playerEntity -> playerEntity.sendToolBreakStatus(playerEntity.getActiveHand())
                );
        return ActionResult.SUCCESS;
    }

    private boolean isOre(BlockState state){
        return state.isOf(Blocks.IRON_ORE);
    }
    // 发送坐标消息
    private void outputMessage(BlockPos blockPos, PlayerEntity playerEntity, Block block){
        playerEntity.sendMessage(Text.literal(
                "Found"
                + block.asItem().getName().getString()
                + "at" + "(~,"
                + blockPos.getY()
                + ",~)"
        ), false);
    }

    public WI_Prospector(Settings settings) {
        super(settings);
    }

    public static void OnInit(){

    }
}
