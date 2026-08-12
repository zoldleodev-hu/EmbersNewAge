package hu.zoldleo.embers.blockentity;

import hu.zoldleo.embers.RegistryManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class AtmosphericGaugeBlockEntity extends BlockEntity {
	int power = 0;

	public AtmosphericGaugeBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.ATMOSPHERIC_GAUGE_ENTITY.get(), pPos, pBlockState);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider registries) {
		super.loadAdditional(nbt, registries);
		power = nbt.getInt("power");
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putInt("power", power);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, AtmosphericGaugeBlockEntity blockEntity) {
		if (blockEntity.power != state.getAnalogOutputSignal(level, pos)) {
			level.updateNeighbourForOutputSignal(pos, state.getBlock());
			blockEntity.power = state.getAnalogOutputSignal(level, pos);
			blockEntity.setChanged();
		}
	}
}