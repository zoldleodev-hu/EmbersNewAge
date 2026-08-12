package hu.zoldleo.embers.blockentity;

import net.minecraft.core.Direction;

public interface IItemPipePriority {
	int getPriority(Direction facing);
}