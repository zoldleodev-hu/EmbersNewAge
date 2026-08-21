package hu.zoldleo.embers.augment;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.augment.AugmentUtil;
import hu.zoldleo.embers.datagen.EmbersDamageTypes;
import hu.zoldleo.embers.util.EmberInventoryUtil;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber
public class EmberRedirectionModuleAugment extends AugmentBase {
    public EmberRedirectionModuleAugment() {
        super(0);
    }

    @SubscribeEvent
    public static void onLivingDamaged(LivingDamageEvent.Pre event) {
        if (!event.getSource().is(EmbersDamageTypes.EMBER_KEY))
            return;
        int level = AugmentUtil.getArmorAugmentLevel(event.getEntity(), RegistryManager.EMBER_REDIRECTION_MODULE_AUGMENT);
        if (level <= 0)
            return;

        double conversionRatio = 0.06 * level / Math.PI;
        double absorbed = event.getNewDamage() * conversionRatio;

        EmberInventoryUtil.addEmber(event.getEntity(), absorbed);
        event.setNewDamage((float)(event.getNewDamage() - absorbed));
    }
}