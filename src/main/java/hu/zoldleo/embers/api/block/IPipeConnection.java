package hu.zoldleo.embers.api.block;

import hu.zoldleo.embers.blockentity.PipeBlockEntityBase.PipeConnection;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public interface IPipeConnection {
	PipeConnection getPipeConnection(BlockState state, Direction direction);
}