package com.rcdmake.whimsy.block.ClassDEV.BlockEntity.Entity;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import com.rcdmake.whimsy.block.ClassDEV.BlockEntity.DEV_EntityInt;
import com.rcdmake.whimsy.block.ClassDEV.DEV_StateInt;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DEV_EntityInt_E extends BlockEntity {
    // super
    /**
     * 最简单的方块实体仅继承 BlockEntity，并使用默认构造函数。 这是完全有效的，但不会给予方块任何特殊功能。
     * <br>
     * 请确保这个构造方法只接收这两个参数:
     * @param pos
     * @param state
     * @return null
     * <br>
     * 否则我们后面写的方法引用 DemoBlockEntity::new 将会无效。
     * */
    public DEV_EntityInt_E(BlockPos pos, BlockState state) {
        super(DEV_EntityInt_E.DEV_ENTITY_INT_E, pos, state);
    }

    // 注册方块实体
    /**
     * 这个方块实体类型定义了只有 DEV_ENTITY_INT 可以拥有这个方块实体类型。
     * 如果你想要让方块实体类型支持更多方块，只需要将其添加到 FabricBlockEntityTypeBuilder.create 的参数中即可。
     * <br>
     * 如果方法引用 DemoBlockEntity::new 无法解析，检查 Block类中的 的构造方法的参数是否正确。
     */
    public static final BlockEntityType<DEV_EntityInt_E> DEV_ENTITY_INT_E =
            Registry.register(Registries.BLOCK_ENTITY_TYPE,
                    new Identifier(RichardsWhimsyMod.MOD_ID, "dev_block_entity_int_e"),
            // 如果没有在方块类实现抽象方法的话，此处代码将会报错
            FabricBlockEntityTypeBuilder.create(DEV_EntityInt_E::new, DEV_EntityInt.DEV_ENTITY_INT).build()
    );

    // 序列化与反序列化数据
    // 储存当前固定数值
    private int be_num = 0;

    // 发送值
    public int getBe_num() {
        return be_num;
    }

    // 保存与读取方块实体NBT（在保存世界时暂停工作进度而不是直接清空）
    /**
     * writeNbt() 将会修改其参数 nbt 的内容，这个 nbt 包含了方块实体中的所有数据。
     * 该方法通常不会修改方块实体本身。
     * 方块实体数据将会存储在磁盘中，并且如果您需要将 BlockEntity 数据与客户端同步，则会通过封包发送。
     * 调用 super.writeNbt() 非常重要，因为方块实体的坐标及其方块实体类型 id 保存到 nbt 中。
     * 否则，您尝试保存的所有其他数据都将丢失，因为它与位置和 BlockEntityType 不相关。
     * <br>
     * 在此示例中，整数保存在键 be_num 下——您可以将其替换为任何字符串，但是标签中的每个键只能有一个项，并且需要记住该键以便以后读取数据。
     * */
    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        be_num = nbt.getInt("be_num");
    }
    /**
     * 为了以后读取数据，您还需要覆盖 readNBT。
     * 此方法与 writeNBT 相反，不会将数据保存到 NBTCompound，而是您已经有了之前保存的 nbt 数据，使您可以检索所需的任何数据。
     * 该方法会修改方块实体本身，不会修改这个 nbt 参数。
     * 与 writeNbt 一样，必须调用 super.readNbt，并且您将需要使用相同的键来检索保存的数据。
     * 要检索我们之前保存的数字，请参见下面的示例。
     * */
    @Override
    public void writeNbt(NbtCompound nbt) {
        nbt.putInt("be_num", be_num);
        super.writeNbt(nbt);
    }

    // 双端同步
    // 需要调用 world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
    // 来触发数据的同步，否则客户端不会知道方块实体已经改变。
    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    // 方块实体刻，规定每一刻如何运行
    /**
     *一旦实现了 writeNbt 和 readNbt 方法，您只需要确保在合适的时候调用即可。
     * 每当您的 BlockEntity 数据发生更改并需要保存时，请调用 markDirty()。
     * 这会将方块所在的区块标记为dirty，在世界下次保存时强制调用 writeNbt 方法。
     * 原则上，只要修改了 BlockEntity 类中的任何一个自定义变量，就只需要简单调用 markDirty，
     * 否则当你退出并重进世界后，这个方块实体依然会是没有修改过的。
     * */
    public void tick(World world, BlockPos pos, BlockState state, DEV_EntityInt_E be) {
        if(world.isClient()){
            return;
        }
        be_num++;
        // 标准的方法是没有参数的markDirty()。除非你有特别重写，否则应使用无参的markDirty()
        markDirty();
    }

    // OnInit
    public static void OnInit(){}

}
