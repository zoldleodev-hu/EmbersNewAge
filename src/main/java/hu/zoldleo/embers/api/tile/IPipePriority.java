package hu.zoldleo.embers.api.tile;

import net.minecraft.core.Direction;

public interface IPipePriority {
    int getPriority(Direction side);
}