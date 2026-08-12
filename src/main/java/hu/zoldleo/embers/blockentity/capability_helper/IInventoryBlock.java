package hu.zoldleo.embers.blockentity.capability_helper;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.items.IItemHandler;

public interface IInventoryBlock {
    IItemHandler getInventoryCapability(Direction side);
}