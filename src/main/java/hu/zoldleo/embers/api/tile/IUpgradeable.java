package hu.zoldleo.embers.api.tile;

import net.minecraft.core.Direction;

public interface IUpgradeable {
	boolean isSideUpgradeSlot(Direction face);
}