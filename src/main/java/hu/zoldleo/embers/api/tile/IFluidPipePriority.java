package hu.zoldleo.embers.api.tile;

import net.minecraft.core.Direction;

public interface IFluidPipePriority {
    int getPriority(Direction facing);
}