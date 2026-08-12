package hu.zoldleo.embers.blockentity.capability_helper;

import hu.zoldleo.embers.api.upgrades.IUpgradeProvider;
import net.minecraft.core.Direction;

public interface IUpgradeBlock {
    IUpgradeProvider getUpgradeCapability(Direction side);
}