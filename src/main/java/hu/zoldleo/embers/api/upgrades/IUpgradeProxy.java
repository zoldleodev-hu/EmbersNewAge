package hu.zoldleo.embers.api.upgrades;

import java.util.List;

import hu.zoldleo.embers.api.tile.IUpgradeable;
import hu.zoldleo.embers.blockentity.MechanicalCoreBlockEntity.BlockEntityDirection;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IUpgradeProxy extends IUpgradeable {
	void collectUpgrades(List<UpgradeContext> upgrades, int distanceLeft);
	boolean isSocket(Direction facing);
	boolean isProvider(Direction facing);
	BlockEntityDirection getAttachedMultiblock(int distanceLeft);
	BlockEntity getAttachedBlockEntity(int distanceLeft);
}