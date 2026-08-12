package hu.zoldleo.embers.augment;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.augment.AugmentUtil;
import hu.zoldleo.embers.util.EmberInventoryUtil;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;

@EventBusSubscriber
public class IntelligentApparatusAugment extends AugmentBase {
	public IntelligentApparatusAugment() {
		super(4.0);
	}

	@SubscribeEvent
	public static void onXPDrop(LivingExperienceDropEvent event) {
		if (event.getAttackingPlayer() != null) {
			Player player = event.getAttackingPlayer();
			int level = AugmentUtil.getArmorAugmentLevel(player, RegistryManager.INTELLIGENT_APPARATUS_AUGMENT);
            IntelligentApparatusAugment augment = (IntelligentApparatusAugment)RegistryManager.INTELLIGENT_APPARATUS_AUGMENT.value();
			if (level > 0 && EmberInventoryUtil.getEmberTotal(player) >= augment.cost) {
				EmberInventoryUtil.removeEmber(player, augment.cost);
				event.setDroppedExperience(event.getDroppedExperience() * (level + 1));
			}
		}
	}
}