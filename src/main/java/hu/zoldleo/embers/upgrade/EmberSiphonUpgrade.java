package hu.zoldleo.embers.upgrade;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.blockentity.CopperChargerBlockEntity;

import net.minecraft.world.level.block.entity.BlockEntity;

public class EmberSiphonUpgrade extends DefaultUpgradeProvider {
	public EmberSiphonUpgrade(BlockEntity tile) {
		super(Embers.res("ember_siphon"), tile);
	}

	@Override
	public int getPriority() {
		return -100; //before everything else
	}

	@Override
	public int getLimit(BlockEntity tile) {
		return tile instanceof CopperChargerBlockEntity ? 1 : 0;
	}

	@Override
	public double getSpeed(BlockEntity tile, double speed, int distance, int count) {
		return speed * -1;
	}
}