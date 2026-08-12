package hu.zoldleo.embers.blockentity.capability_helper;

import hu.zoldleo.embers.api.power.IEmberCapability;
import net.minecraft.core.Direction;

public interface IEmberBlock {
    IEmberCapability getEmberCapability(Direction side);
}