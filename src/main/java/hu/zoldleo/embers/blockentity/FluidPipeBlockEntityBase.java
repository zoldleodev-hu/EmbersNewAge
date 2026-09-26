package hu.zoldleo.embers.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class FluidPipeBlockEntityBase extends PipeBlockEntityBase {
	public FluidPipeBlockEntityBase(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
		super(pType, pPos, pBlockState);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, FluidPipeBlockEntityBase blockEntity) {
		if (!blockEntity.loaded)
			blockEntity.initConnections();
	}
}