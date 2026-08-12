package hu.zoldleo.embers.upgrade;

import java.util.List;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.api.upgrades.UpgradeContext;
import hu.zoldleo.embers.blockentity.ClockworkAttenuatorBlockEntity;

import net.minecraft.world.level.block.entity.BlockEntity;


public class ClockworkAttenuatorUpgrade extends DefaultUpgradeProvider {

	public ClockworkAttenuatorUpgrade(BlockEntity tile) {
		super(Embers.res("clockwork_attenuator"), tile);
	}

	@Override
	public int getPriority() {
		return -100; //before everything else
	}

	@Override
	public int getLimit(BlockEntity tile) {
		return 1;
	}

	@Override
	public double getSpeed(BlockEntity tile, double speed, int distance, int count) {
		return speed * getSpeedModifier();
	}

	private double getSpeedModifier() {
		if (this.tile instanceof ClockworkAttenuatorBlockEntity attenuator)
			return attenuator.getSpeed();
		return 0;
	}

	@Override
	public boolean doWork(BlockEntity tile, List<UpgradeContext> upgrades, int distance, int count) {
		return getSpeedModifier() == 0;
	}
}
