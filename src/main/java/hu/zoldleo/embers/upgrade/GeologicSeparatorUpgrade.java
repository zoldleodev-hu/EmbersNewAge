package hu.zoldleo.embers.upgrade;

import java.util.List;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.api.event.MachineRecipeEvent;
import hu.zoldleo.embers.api.event.UpgradeEvent;
import hu.zoldleo.embers.api.upgrades.UpgradeContext;
import hu.zoldleo.embers.blockentity.GeologicSeparatorBlockEntity;
import hu.zoldleo.embers.recipe.MeltingRecipe;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class GeologicSeparatorUpgrade extends DefaultUpgradeProvider {
	public GeologicSeparatorUpgrade(BlockEntity tile) {
		super(Embers.res("geologic_separator"), tile);
	}

	@Override
	public int getPriority() {
		return 100; //after everything else
	}

	@Override
	public void throwEvent(BlockEntity tile, List<UpgradeContext> upgrades, UpgradeEvent event, int distance, int count) {
        if (tile.getLevel() == null)
            return;
		if (distance <= 0 && event instanceof MachineRecipeEvent.Success<?> recipeEvent) {
			Object recipe = recipeEvent.getRecipe();
			if (recipe instanceof MeltingRecipe meltingRecipe) {
				FluidStack bonus = meltingRecipe.getBonus();
                 if (!bonus.isEmpty() && this.tile instanceof GeologicSeparatorBlockEntity separatorTile)
                    separatorTile.getTank().fill(bonus.copy(), IFluidHandler.FluidAction.EXECUTE);
			}
		}
	}
}