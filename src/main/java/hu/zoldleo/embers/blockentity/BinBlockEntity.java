package hu.zoldleo.embers.blockentity;

import java.util.List;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.tile.IBin;

import hu.zoldleo.embers.blockentity.capability_helper.IInventoryBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class BinBlockEntity extends BlockEntity implements IBin, IInventoryBlock {

	int ticksExisted = 0;

	public ItemStackHandler inventory = new ItemStackHandler(1) {
		@Override
		protected void onContentsChanged(int slot) {
			BinBlockEntity.this.setChanged();
		}
	};

	public BinBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.BIN_ENTITY.get(), pPos, pBlockState);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		inventory.deserializeNBT(provider, nbt.getCompound("Inventory"));
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
		nbt.put("Inventory", inventory.serializeNBT(provider));
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		nbt.put("Inventory", inventory.serializeNBT(provider));
		return nbt;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, BinBlockEntity blockEntity) {
		blockEntity.ticksExisted ++;
		if (blockEntity.ticksExisted % 10 == 0){
			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos.getX(),pos.getY(),pos.getZ(),pos.getX()+1,pos.getY()+1.25,pos.getZ()+1));
            for (ItemEntity item : items) {
                ItemStack stack = blockEntity.inventory.insertItem(0, item.getItem(), false);
                if (!stack.isEmpty()) {
                    item.setItem(stack);
                } else {
                    item.remove(RemovalReason.DISCARDED);
                }
            }
		}
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (level instanceof ServerLevel serverLevel)
			serverLevel.getChunkSource().blockChanged(worldPosition);
	}

	@Override
	public IItemHandler getInventory() {
		return inventory;
	}

    @Override
    public IItemHandler getInventoryCapability(Direction side) {
        return this.remove ? null : inventory;
    }
}