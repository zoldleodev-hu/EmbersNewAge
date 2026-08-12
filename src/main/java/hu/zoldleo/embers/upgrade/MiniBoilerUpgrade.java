package hu.zoldleo.embers.upgrade;

import java.util.List;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.api.event.EmberEvent;
import hu.zoldleo.embers.api.event.UpgradeEvent;
import hu.zoldleo.embers.api.upgrades.UpgradeContext;
import hu.zoldleo.embers.blockentity.MiniBoilerBlockEntity;

import net.minecraft.world.level.block.entity.BlockEntity;

public class MiniBoilerUpgrade extends DefaultUpgradeProvider {
	public MiniBoilerUpgrade(BlockEntity tile) {
		super(Embers.res("mini_boiler"), tile);
	}

	@Override
	public int getPriority() {
		return 100; //after everything else
	}

	@Override
	public void throwEvent(BlockEntity tile, List<UpgradeContext> upgrades, UpgradeEvent event, int distance, int count) {
		if (this.tile instanceof MiniBoilerBlockEntity boiler && event instanceof EmberEvent emberEvent && emberEvent.getType() != EmberEvent.EnumType.TRANSFER) {
			double multiplier = 1.0;
			if (distance > 1)
				multiplier /= distance * 0.75;
			if (count > 3)
				multiplier /= (count - 2.0) * 0.75;
			if (emberEvent.getType() == EmberEvent.EnumType.PRODUCE)
				multiplier *= 0.25;
			boiler.boil(emberEvent.getAmount() * multiplier);
		}
	}
}