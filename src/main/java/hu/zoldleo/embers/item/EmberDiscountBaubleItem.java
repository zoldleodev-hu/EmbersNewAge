package hu.zoldleo.embers.item;

import hu.zoldleo.embers.api.event.EmberRemoveEvent;
import hu.zoldleo.embers.compat.curios.CuriosCompat;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;

public class EmberDiscountBaubleItem extends Item implements IEmbersCurioItem {
	public double reduction;

	public EmberDiscountBaubleItem(Properties properties, double reduction) {
		super(properties);
		this.reduction = reduction;
        NeoForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onTake(EmberRemoveEvent event) {
		CuriosCompat.checkForCurios(event.getPlayer(), stack -> {
			if (stack.getItem() == this)
				event.addReduction(reduction);
			return false;
		});
	}
}