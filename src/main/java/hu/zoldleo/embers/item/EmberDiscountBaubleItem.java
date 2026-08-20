package hu.zoldleo.embers.item;

import hu.zoldleo.embers.api.event.EmberModificationEvent;
import hu.zoldleo.embers.compat.curios.CuriosCompat;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
	public void onTake(EmberModificationEvent.Remove event) {
		CuriosCompat.checkForCurios(event.getEntity(), stack -> {
			if (stack.getItem() == this)
				event.addModifier(-reduction, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
			return false;
		});
	}
}