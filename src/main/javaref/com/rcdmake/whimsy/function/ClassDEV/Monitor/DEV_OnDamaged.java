package com.rcdmake.whimsy.function.ClassDEV.Monitor;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

public class DEV_OnDamaged {
    /*
    private ActionResult onEntityDamage(ServerPlayerEntity player, net.minecraft.world.World world, net.minecraft.entity.Entity entity, net.minecraft.util.Hand hand) {
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            DamageSource source = DamageSource.player(player);

            // 获取实体名称
            String entityName = livingEntity.getDisplayName().getString();

            // 获取伤害来源
            String damageSourceName = source.getName();

            // 创建聊天信息
            Text message = Text.of(entityName + " 因 " + damageSourceName + " 所致伤");

            // 向所有玩家发送消息
            for (ServerPlayerEntity serverPlayer : world.getPlayers()) {
                serverPlayer.sendMessage(message, false);
            }
        }
        return ActionResult.PASS;
    }
    */
    private boolean onEntityDamage(Entity entity, DamageSource source, float amount) {
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;

            // 获取实体名称
            String entityName = livingEntity.getDisplayName().getString();

            // 获取伤害来源
            String damageSourceName = source.getName();

            // 创建聊天信息
            Text message = Text.of(entityName + " 因 " + damageSourceName + " 所致伤");

            // 向所有玩家发送消息
            for (ServerPlayerEntity player : livingEntity.getServer().getPlayerManager().getPlayerList()) {
                player.sendMessage(message, false);
            }
        }
        return false; // 返回 false 以允许其他处理继续
    }

    public void OnInit(){
        
    }
}
