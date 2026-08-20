package hu.zoldleo.embers.augment;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.augment.AugmentUtil;
import hu.zoldleo.embers.particle.GlowParticleOptions;
import hu.zoldleo.embers.util.EmberInventoryUtil;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber
public class SuperheaterAugment extends AugmentBase {
	public SuperheaterAugment() {
		super(2.0);
	}

	//for technical reasons, the autosmelt part is in a separate class
	//SuperHeaterLootModifier

	private static double getBurnBonus(double resonance) {
		if(resonance > 1)
			return 1 + (resonance - 1) * 0.5;
		else
			return resonance;
	}

	private static double getDamageBonus(double resonance) {
        return resonance;
		/*/if(resonance > 1) TODO: wtf
			return 1 + (resonance - 1) * 1.0;
		else
			return resonance;*/
	}

	@SubscribeEvent
	public static void onHit(LivingDamageEvent.Pre event) {
		if (event.getSource().getEntity() instanceof Player player) {
			ItemStack s = player.getMainHandItem();
			if (AugmentUtil.hasHeat(s)) {
                SuperheaterAugment augment = (SuperheaterAugment)RegistryManager.SUPERHEATER_AUGMENT.value();
				int level = AugmentUtil.getAugmentLevel(s, RegistryManager.SUPERHEATER_AUGMENT);
				if (level > 0 && EmberInventoryUtil.getEmberTotal(player) >= augment.cost) {
					double resonance = Misc.getEmberResonance(s);
					int burnTime = (int) (Math.pow(2, level - 1) * 5 * getBurnBonus(resonance));
					float extraDamage = (float) (level * getDamageBonus(resonance));

					if (event.getEntity().getRemainingFireTicks() < burnTime)
						event.getEntity().setRemainingFireTicks(burnTime);

					if (event.getEntity().level() instanceof ServerLevel serverLevel)
						serverLevel.sendParticles(GlowParticleOptions.EMBER, event.getEntity().getX(), event.getEntity().getY() + event.getEntity().getEyeHeight() / 1.5, event.getEntity().getZ(), 30, 0.15, 0.15, 0.15, 0.3);

                    EmberInventoryUtil.removeEmber(player, augment.cost);
					event.setNewDamage(event.getNewDamage() + extraDamage);
				}
			}
		}
	}
}