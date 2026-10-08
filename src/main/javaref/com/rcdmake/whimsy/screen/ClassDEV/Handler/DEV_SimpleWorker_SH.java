package com.rcdmake.whimsy.screen.ClassDEV.Handler;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import com.rcdmake.whimsy.block.ClassDEV.BlockEntity.Entity.DEV_SimpleWorker_E;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

public class DEV_SimpleWorker_SH extends ScreenHandler {
    // 创建物品栏GUI，需要初始化
    private final Inventory inventory;
    // 创建同步，需要初始化
    private final PropertyDelegate propertyDelegate;
    // 应用方块实体，需要初始化
    public final DEV_SimpleWorker_E blockEntity;

    //
    public DEV_SimpleWorker_SH(int syncID, PlayerInventory inventory, PacketByteBuf buf) {
        this(syncID,inventory,inventory.player.getWorld().getBlockEntity(buf.readBlockPos()),
                // 2个参数
                new ArrayPropertyDelegate(2));
    }

    // super
    public DEV_SimpleWorker_SH(int syncId, PlayerInventory playerInventory, BlockEntity blockEntity, PropertyDelegate arrayPropertyDelegate) {
        super(DEV_SIMPLE_WORKER_H,syncId);
        // 2个物品栏
        checkSize((Inventory) blockEntity,2);
        // 强制变量类型转换
        this.inventory = (Inventory) blockEntity;
        this.propertyDelegate = arrayPropertyDelegate;
        this.blockEntity = (DEV_SimpleWorker_E) blockEntity;
        // 给玩家打开物品栏
        inventory.onOpen(playerInventory.player);

        // 输入与输出格子的位置
        this.addSlot(new Slot(inventory,0,80,11));
        this.addSlot(new Slot(inventory,1,80,59));

        // 添加玩家物品栏与快捷栏，自定义方法
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        addProperties(arrayPropertyDelegate);

    }

    /**
     * 处理玩家在用户界面中快速移动物品（通常是通过Shift+点击）的逻辑。
     *<p>
     * 这个方法的目的是在不同的物品槽之间自动传输物品，通常是从玩家的物品栏到容器中，或反之。
     *</p>
     * @param player The player performing the action.
     * @param invSlot The index of the slot being interacted with.
     * @return A copy of the item stack that was moved, or an empty stack if the move was not successful.
     */
    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        // 创建一个空的 ItemStack 实例，用于返回操作结果
        ItemStack newStack = ItemStack.EMPTY;
        // 根据传入的索引 invSlot，从 slots 集合中获取对应的 Slot 对象
        Slot slot = this.slots.get(invSlot);
        // 判断槽位不为空且槽位中有物品
        if (slot != null && slot.hasStack()) {
            // 创建一个当前物品堆栈的副本
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            // 如果槽位位于容器的物品部分（不是玩家背包部分）
            if (invSlot < this.inventory.size()) {
                // 尝试将物品移动到玩家的背包部分
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    // 将槽位设置为空
                    return ItemStack.EMPTY;
                }
                // 否则，尝试将物品移动到容器的物品部分
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                // 将槽位设置为空
                return ItemStack.EMPTY;
            }

            //如果原始堆栈为空（物品已经完全移动）
            if (originalStack.isEmpty()) {
                // 将槽位设置为空
                slot.setStack(ItemStack.EMPTY);
            } else {
                // 标记槽位为脏，需要更新
                slot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    // 添加玩家物品栏
    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    // 添加玩家快捷栏
    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    // 从同步获取工作状态
    public boolean isCrafting(){
        // get(0)表示从index=0（输入格子）获取数据
        return propertyDelegate.get(0) > 0;
    }

    public int getScaledProgress() {
        int progress = this.propertyDelegate.get(0);
        int maxProgress = this.propertyDelegate.get(1);
        // 进度条长度为26像素
        int progressArrowSize = 26;

        // 返回渲染状态，若均不为零则返回长度，若为零则返回0
        // （又TM是三元运算，lz迟早要给你们全部改成if）
        return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
    }

    // 创建屏幕处理器
    public static final ScreenHandlerType<DEV_SimpleWorker_SH> DEV_SIMPLE_WORKER_H =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(RichardsWhimsyMod.MOD_ID, "dev_simple_worker"),
                    new ExtendedScreenHandlerType<>(DEV_SimpleWorker_SH::new));
    // OnInit
    public static void OnInit(){}
}
