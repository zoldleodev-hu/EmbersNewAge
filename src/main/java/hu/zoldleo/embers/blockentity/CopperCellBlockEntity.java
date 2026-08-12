package hu.zoldleo.embers.blockentity;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.blockentity.capability_helper.IEmberBlock;
import hu.zoldleo.embers.power.DefaultEmberCapability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class CopperCellBlockEntity extends BlockEntity implements IEmberBlock {
    public static final double CAPACITY = 24000;

	public IEmberCapability capability = new DefaultEmberCapability() {
		@Override
		public void onContentsChanged() {
			super.onContentsChanged();
			CopperCellBlockEntity.this.setChanged();
		}
	};

	public CopperCellBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.COPPER_CELL_ENTITY.get(), pPos, pBlockState);
		capability.setEmberCapacity(CAPACITY);
		capability.setEmber(0);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		capability.readFromNBT(provider, nbt);
		if (capability.getEmberCapacity() == 0)
			capability.setEmberCapacity(24000);
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
		capability.writeToNBT(provider, nbt);
	}

    @Override
    public IEmberCapability getEmberCapability(Direction side) {
        return this.remove ? null : capability;
    }
}