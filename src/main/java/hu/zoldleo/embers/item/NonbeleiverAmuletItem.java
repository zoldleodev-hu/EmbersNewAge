package hu.zoldleo.embers.item;

import hu.zoldleo.embers.compat.curios.CuriosCompat;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber
public class NonbeleiverAmuletItem extends Item implements IEmbersCurioItem {
	public NonbeleiverAmuletItem(Properties properties) {
		super(properties);
	}

	@SubscribeEvent
	public static void onDamage(LivingIncomingDamageEvent event) {
		DamageSource source = event.getSource();

		if (!(source.is(DamageTypeTags.WITCH_RESISTANT_TO) || source.is(Tags.DamageTypes.IS_MAGIC)) || event.getAmount() < 0.5f)
			return;

		CuriosCompat.checkForCurios(event.getEntity(), stack -> {
			if (stack.is(CuriosCompat.NONBELEIVER_AMULET)) {
				event.setAmount(Math.max(event.getAmount() * 0.1f, 0.5f));
				return true;
			}
			return false;
		});
	}
}