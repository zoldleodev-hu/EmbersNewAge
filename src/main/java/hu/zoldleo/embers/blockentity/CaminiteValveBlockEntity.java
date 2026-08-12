package hu.zoldleo.embers.blockentity;

import hu.zoldleo.embers.blockentity.capability_helper.IFluidBlock;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.block.MechEdgeBlockBase;
import hu.zoldleo.embers.datagen.EmbersBlockTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CaminiteValveBlockEntity extends BlockEntity implements IFluidBlock {
	int ticksExisted = 0;
	ReservoirBlockEntity reservoir;
	IFluidHandler fluidHandler;

	public CaminiteValveBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.CAMINITE_VALVE_ENTITY.get(), pPos, pBlockState);
		fluidHandler = new IFluidHandler() {
			@Override
			public int getTanks() {
				if (reservoir != null)
					return reservoir.getTank().getTanks();
				return 0;
			}

			@Override
			public @NotNull FluidStack getFluidInTank(int tank) {
				if (reservoir != null)
					return reservoir.getTank().getFluidInTank(tank);
				return FluidStack.EMPTY;
			}

			@Override
			public int getTankCapacity(int tank) {
				if (reservoir != null)
					return reservoir.getTank().getTankCapacity(tank);
				return 0;
			}

			@Override
			public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
				if (reservoir != null)
					return reservoir.getTank().isFluidValid(tank, stack);
				return false;
			}

			@Override
			public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
				if (reservoir != null)
					return reservoir.getTank().fill(resource, action);
				return 0;
			}

			@Override
			public @NotNull FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) {
				if (reservoir != null)
					return reservoir.getTank().drain(resource, action);
				return FluidStack.EMPTY;
			}

			@Override
			public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action) {
				if (reservoir != null)
					return reservoir.getTank().drain(maxDrain, action);
				return FluidStack.EMPTY;
			}
		};
	}

	public ReservoirBlockEntity getReservoir() {
		return reservoir;
	}

	public void updateTank() {
		if (isRemoved() || !getBlockState().hasProperty(MechEdgeBlockBase.EDGE))
			return;
		reservoir = null;
		BlockPos basePos = worldPosition.offset(getBlockState().getValue(MechEdgeBlockBase.EDGE).centerPos);
		for (int i = 1; i < 64; i++) { // TODO
			BlockPos pos = basePos.below(i);
			if (!level.getBlockState(pos).is(EmbersBlockTags.RESERVOIR_EXPANSION)) {
				BlockEntity tile = level.getBlockEntity(pos);
				if (tile instanceof ReservoirBlockEntity) {
					reservoir = (ReservoirBlockEntity) tile;
				}
				break;
			}
		}
	}

	public static void commonTick(Level level, BlockPos pos, BlockState state, CaminiteValveBlockEntity blockEntity) {
		blockEntity.ticksExisted++;
		if (blockEntity.reservoir != null && blockEntity.reservoir.isRemoved())
			blockEntity.reservoir = null;
		if (blockEntity.ticksExisted % 20 == 0)
			blockEntity.updateTank();
	}

    @Override
    public IFluidHandler getFluidCapability(Direction side) {
        if (!this.remove && (side == null || side.getAxis() != Direction.Axis.Y))
            return fluidHandler;
        return null;
    }
}
