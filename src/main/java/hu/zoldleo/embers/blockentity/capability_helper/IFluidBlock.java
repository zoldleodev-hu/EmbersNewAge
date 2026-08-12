package hu.zoldleo.embers.blockentity.capability_helper;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public interface IFluidBlock {
    IFluidHandler getFluidCapability(Direction side);
}