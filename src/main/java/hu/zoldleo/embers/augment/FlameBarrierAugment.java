package hu.zoldleo.embers.augment;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.augment.AugmentUtil;
import hu.zoldleo.embers.damage.DamageEmber;
import hu.zoldleo.embers.datagen.EmbersDamageTypes;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.particle.GlowParticleOptions;
import hu.zoldleo.embers.util.EmberInventoryUtil;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber
public class FlameBarrierAugment extends AugmentBase {
	public FlameBarrierAugment() {
		super(2.0);
	}

	@SubscribeEvent
	public static void onHit(LivingIncomingDamageEvent event) {
		if (event.getEntity() instanceof Player player && event.getSource().getEntity() instanceof LivingEntity) {
            FlameBarrierAugment augment = (FlameBarrierAugment)RegistryManager.FLAME_BARRIER_AUGMENT.value();
			int barrierLevel = AugmentUtil.getArmorAugmentLevel(player, RegistryManager.FLAME_BARRIER_AUGMENT);
			float strength = (float)(2.0 * (Math.atan(0.6 * barrierLevel) / Math.PI));
			if (barrierLevel > 0 && EmberInventoryUtil.getEmberTotal(player) >= augment.cost) {
				EmberInventoryUtil.removeEmber(player, augment.cost);
				DamageSource damage = new DamageEmber(player.level().registryAccess().registry(Registries.DAMAGE_TYPE).get().getHolderOrThrow(EmbersDamageTypes.EMBER_KEY), player);
				event.getSource().getEntity().hurt(damage, strength*event.getAmount()*0.5f);
				event.getSource().getEntity().igniteForSeconds(barrierLevel+1);
				event.getEntity().level().playSound(null, event.getEntity(), EmbersSounds.FIREBALL_HIT.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

				if (event.getEntity().level() instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(GlowParticleOptions.EMBER, event.getEntity().getX(), event.getEntity().getY() + event.getEntity().getBbHeight() / 2.0, event.getEntity().getZ(), 20, 0.5, 0.5, 0.5, 0.3);
				}
			}
		}
	}
}