package hu.zoldleo.embers.upgrade;

import java.util.HashSet;
import java.util.List;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.upgrades.UpgradeContext;
import hu.zoldleo.embers.blockentity.WildfireStirlingBlockEntity;
import hu.zoldleo.embers.recipe.context.FluidHandlerContext;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.world.level.block.entity.BlockEntity;

public class WildfireStirlingUpgrade extends DefaultUpgradeProvider {
	private static HashSet<Class<? extends BlockEntity>> blacklist = new HashSet<>();

	public static void registerBlacklistedTile(Class<? extends BlockEntity> tile) {
		blacklist.add(tile);
	}

	public WildfireStirlingUpgrade(BlockEntity tile) {
		super(Embers.res("wildfire_stirling"), tile);
	}

	public static double getMultiplier(double multiplier, int distance, int count) {
		if (distance > 1) {
			multiplier = 1.0 + (multiplier - 1.0) / (distance * 0.5);
		}
		if (count > 2) {
			multiplier = 1.0 + (multiplier - 1.0) / (count * 0.4);
		}
		return multiplier;
	}

	@Override
	public int getLimit(BlockEntity tile) {
		return blacklist.contains(tile.getClass()) ? 0 : super.getLimit(tile);
	}

	@Override
	public double transformEmberConsumption(BlockEntity tile, double ember, int distance, int count) {
		return ember / getMultiplier(getCatalystMultiplier(), distance, count);
	}

	@Override
	public boolean doWork(BlockEntity tile, List<UpgradeContext> upgrades, int distance, int count) {
		if (getCatalystMultiplier() != 1.0 && this.tile instanceof WildfireStirlingBlockEntity stirling) {
			depleteCatalyst();
			stirling.setActive(20);
		}
		return false; //No cancel
	}

	private double getCatalystMultiplier() {
		if (this.tile instanceof WildfireStirlingBlockEntity stirling) {
			FluidHandlerContext context = new FluidHandlerContext(stirling.tank);
			if (stirling.burnTime <= 0 || stirling.cachedRecipe == null)
				stirling.cachedRecipe = Misc.getRecipe(stirling.cachedRecipe, RegistryManager.GASEOUS_FUEL.get(), context, stirling.getLevel());
			return stirling.cachedRecipe == null ? 1.0 : stirling.cachedRecipe.value().getPowerMultiplier(context);
		}
		return 1.0;
	}

	private void depleteCatalyst() {
		if (this.tile instanceof WildfireStirlingBlockEntity stirling) {
			stirling.burnTime--;
			if (stirling.burnTime < 0) {
				FluidHandlerContext context = new FluidHandlerContext(stirling.tank);
				stirling.cachedRecipe = Misc.getRecipe(stirling.cachedRecipe, RegistryManager.GASEOUS_FUEL.get(), context, stirling.getLevel());
				while (stirling.burnTime < 0 && stirling.cachedRecipe != null && stirling.cachedRecipe.value().matches(context, stirling.getLevel())) {
					stirling.burnTime += stirling.cachedRecipe.value().process(context, 1);
				}
				if (stirling.burnTime < 0)
					stirling.burnTime = 0;
			}
		}
	}
}