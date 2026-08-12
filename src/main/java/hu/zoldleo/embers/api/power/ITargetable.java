package hu.zoldleo.embers.api.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public interface ITargetable {
	void setTargetPosition(BlockPos pos, Direction side);
}