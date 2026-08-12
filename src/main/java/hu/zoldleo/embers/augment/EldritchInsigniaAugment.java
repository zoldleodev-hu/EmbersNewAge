package hu.zoldleo.embers.augment;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.augment.AugmentUtil;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

@EventBusSubscriber
public class EldritchInsigniaAugment extends AugmentBase {
	public EldritchInsigniaAugment() {
		super(0.0);
	}

	@SubscribeEvent
	public static void onEntityTarget(LivingChangeTargetEvent event) {
		if (event.getNewAboutToBeSetTarget() instanceof Player player) {
			int level = AugmentUtil.getArmorAugmentLevel(player, RegistryManager.ELDRITCH_INSIGNIA_AUGMENT);
			if ((event.getEntity().getLastDamageSource() == null
					|| event.getEntity().getLastDamageSource().getEntity() == null
					|| event.getEntity().getLastDamageSource().getEntity().getUUID().compareTo(event.getNewAboutToBeSetTarget().getUUID()) != 0)
					&& event.getEntity().getId() % (3+level) >= 2) {
				if (level > 0 && !(event.getEntity() instanceof Player))
					event.setCanceled(true);
			}
		}
	}
}