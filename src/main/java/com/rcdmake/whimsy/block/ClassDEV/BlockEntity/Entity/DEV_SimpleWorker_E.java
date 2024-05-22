package com.rcdmake.whimsy.block.ClassDEV.BlockEntity.Entity;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import com.rcdmake.whimsy.block.ClassDEV.BlockEntity.DEV_SimpleWorker;
import com.rcdmake.whimsy.block.ClassDEV.DEV_StateEnum;
import com.rcdmake.whimsy.block.ClassDEV.DEV_StateEnumAxis;
import com.rcdmake.whimsy.recipe.ClassDEV.DEV_SimpleWorker_R;
import com.rcdmake.whimsy.screen.ClassDEV.Handler.DEV_SimpleWorker_SH;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

// 调用了 @Juzz 的物品栏接口
public class DEV_SimpleWorker_E extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {
    // 定义格子数量：2个空格子
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);

    // 给物品栏格子编号
    private static final int INPUT_SLOT_INDEX = 0;
    private static final int OUTPUT_SLOT_INDEX = 1;

    // 客户端与服务端同步
    protected final PropertyDelegate propertyDelegate;

    // 工作进度，从 0 -> 2520 （1~10的公倍数）
    private int progress = 0;
    private int maxprogress = 9*8*5*7;

    // 构造函数
    public DEV_SimpleWorker_E(BlockPos pos, BlockState state) {
        super(DEV_SIMPLE_WORKER_ENTITY, pos, state);

        // 重设 propertyDelegate
        this.propertyDelegate = new PropertyDelegate() {

            // 获取方块实体参数
            @Override
            public int get(int index) {
                return switch(index){
                    case 0 -> DEV_SimpleWorker_E.this.progress;
                    case 1 -> DEV_SimpleWorker_E.this.maxprogress;
                    default -> 0;
                };
            }

            // 设定方块实体的参数
            @Override
            public void set(int index, int value) {
                switch(index){
                    case 0 -> DEV_SimpleWorker_E.this.progress = value;
                    case 1 -> DEV_SimpleWorker_E.this.maxprogress = value;
                };
            }

            // 传参数量
            @Override
            public int size() {
                return 2;
            }
        };
    }

    //
    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    /**
     * 该条帮助内容摘自 Chat-GPT4 <br />
     *
     * writeScreenOpeningData 方法是用于处理与玩家打开方块实体相关联的界面（如容器GUI）时的数据同步。
     * 这个方法特别用于在服务器端向客户端发送必要的数据以便正确渲染和交互界面。
     * @param player 这是尝试与界面互动的玩家实例，通常用于获取玩家相关的数据或状态。
     * @param buf 这是用于数据传输的缓冲区，允许你在打开GUI时向客户端发送自定义数据。
     *
     * buf.writeBlockPos(this.pos) 这行代码将当前方块实体的位置（pos）写入缓冲区。
     * 这是必需的，因为客户端需要知道它正在与哪个方块实体交互，尤其是在渲染界面或处理与方块实体相关的逻辑时。
     * 以及检测玩家是否真的在器范围内。
    */
    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    // GUI标题显示
    @Override
    public Text getDisplayName() {
        return Text.translatable("gui.Title.richards-whimsy-mod.DEV_blockEntity_BEE");
    }

    // 创建一个屏幕GUI
    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new DEV_SimpleWorker_SH(syncId, playerInventory,this, this.propertyDelegate);
    }

    // 保存与读取方块实体NBT（在保存世界时暂停工作进度而不是直接清空）
    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt,inventory);
        nbt.putInt("DEV_BlockEntity",progress);
    }
    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt,inventory);
        progress = nbt.getInt("DEV_BlockEntity");
    }

    // 游戏刻的运算，规定每一刻如何运行
    public void tick(World world1, BlockPos pos, BlockState state1) {
        // 判断终端类型
        if (world1.isClient()){
            return;
        }
        // 判断输出物品格是否被占用，自定义的参数
        if (isOutputSlotAvailable()){
            // 判断是否有配方，在其他文件写自定义配方
            if (this.hasRecipe()){
                // 进行工作，增加进度条进度
                this.increaseCraftProgress();
                markDirty(world1,pos,state1);
                // 判断进度条是否已满
                if (hasCraftingFinished()){
                    // 进行工作，将输入变为输出
                    this.craftItem();
                    // 重置进度条
                    this.resetProgress();
                }
            }else { // 如果没有配方，则重置工作进度
                this.resetProgress();
            }
        }else { // 如果被占用，则重置工作进度
            this.resetProgress();
            markDirty(world1,pos,state1);
        }
    }

    // 重置工作进度
    private void resetProgress() {
        this.progress = 0;
    }

    // 进行工作
    private void craftItem() {
        this.removeStack(INPUT_SLOT_INDEX,1);
        // 临时配方，仅做测试
        ItemStack result = new ItemStack(DEV_StateEnumAxis.DEV_BLOCK_STATE_AXIS);

        this.setStack(OUTPUT_SLOT_INDEX,new ItemStack(result.getItem(),getStack(OUTPUT_SLOT_INDEX).getCount() + result.getCount()));
    }

    // 进度条是否已满
    private boolean hasCraftingFinished() {
        return progress >= maxprogress;
    }

    // 进度条分量+20
    private void increaseCraftProgress() {
        progress = progress + 20;
    }

    // 是否存在配方
    private boolean hasRecipe() {
        // 临时配方
        ItemStack result = new ItemStack(DEV_StateEnumAxis.DEV_BLOCK_STATE_AXIS);
        // 如果你需要将一个方块作为物品来使用，需要在其后面加上.asItem()
        boolean hasInput = getStack(INPUT_SLOT_INDEX).getItem() == DEV_StateEnum.DEV_BLOCK_STATE_ENUM.asItem();


        // 临时配方旧写法
        return hasInput && canInsertAmountIntoOutputSlot(result) && canInsertItemIntoOutputSlot(result.getItem());
    }

    // 确认配方输入
    private boolean canInsertAmountIntoOutputSlot(ItemStack result) {
        return this.getStack(OUTPUT_SLOT_INDEX).getCount() + result.getCount() <= getStack(OUTPUT_SLOT_INDEX).getMaxCount();
    }

    // 确认配方输出
    private boolean canInsertItemIntoOutputSlot(Item item) {
        return this.getStack(OUTPUT_SLOT_INDEX).getItem() == item || this.getStack(OUTPUT_SLOT_INDEX).isEmpty();
    }

    // 输出格是否占用
    private boolean isOutputSlotAvailable() {
        return this.getStack(OUTPUT_SLOT_INDEX).isEmpty() || this.getStack(OUTPUT_SLOT_INDEX).getCount() < this.getStack(OUTPUT_SLOT_INDEX).getMaxCount();
    }

    // 创建方块实体
    public static final BlockEntityType DEV_SIMPLE_WORKER_ENTITY =
            Registry.register(Registries.BLOCK_ENTITY_TYPE,
                    new Identifier(RichardsWhimsyMod.MOD_ID,"dev_simple_worker_entity"),
                    FabricBlockEntityTypeBuilder.create(DEV_SimpleWorker_E::new, DEV_SimpleWorker.DEV_SIMPLE_WORKER).build(null));

    public static void OnInit(){}
}
