package hu.zoldleo.embers.blockentity;

import hu.zoldleo.embers.RegistryManager;

import hu.zoldleo.embers.blockentity.capability_helper.IInventoryBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class ItemDropperBlockEntity extends BlockEntity implements IItemPipePriority, IInventoryBlock {
	public ItemStackHandler inventory = new ItemStackHandler(1) {
		@Override
		protected void onContentsChanged(int slot) {
			ItemDropperBlockEntity.this.setChanged();
		}
	};

	public ItemDropperBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.ITEM_DROPPER_ENTITY.get(), pPos, pBlockState);
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

	public static void serverTick(Level level, BlockPos pos, BlockState state, ItemDropperBlockEntity blockEntity) {
		if (!blockEntity.inventory.getStackInSlot(0).isEmpty())
			level.addFreshEntity(new ItemEntity(level, pos.getX()+0.5, pos.getY(), pos.getZ()+0.5, blockEntity.inventory.extractItem(0, 1, false), 0, -0.1, 0));
	}

	@Override
	public int getPriority(Direction facing) {
		return 50;
	}

    @Override
    public IItemHandler getInventoryCapability(Direction side) {
        if (!this.remove && side == Direction.UP)
            return inventory;
        return null;
    }
}