package hu.zoldleo.embers.item;

import java.util.List;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.compat.curios.CuriosCompat;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.particle.GlowParticleOptions;
import hu.zoldleo.embers.particle.SmokeParticleOptions;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ExplosionEvent;

@EventBusSubscriber
public class ExplosionCharmItem extends Item implements IEmbersCurioItem {
	public static final int COOLDOWN = 100;
	public static final int MERCY_TIME = 5;

	public ExplosionCharmItem(Properties properties) {
		super(properties);
	}

    public static boolean hasItemCooledDown(ItemStack stack, long time, int cooldown) {
        Long cooldownTime = stack.get(RegistryManager.COOLDOWN_COMPONENT);
		if (cooldownTime != null)
            return (cooldownTime + cooldown) < time;
		return true;
	}

	public static boolean hasItemMercy(ItemStack stack, long time, int mercy) {
        Long cooldownTime = stack.get(RegistryManager.COOLDOWN_COMPONENT);
		if (cooldownTime != null)
            return (cooldownTime + mercy) > time;
		return true;
	}

	public static void setItemCooldown(ItemStack stack, long time) {
        stack.set(RegistryManager.COOLDOWN_COMPONENT, time);
    }

	@SubscribeEvent
	public static void onExplosion(ExplosionEvent.Start event) {
		if (event.isCanceled())
			return;
		Explosion explosion = event.getExplosion();
		float f3 = 4.0F;
		Vec3 explosionPos = explosion.center();
		int k1 = Mth.floor(explosionPos.x - (double) f3 - 1.0D);
		int l1 = Mth.floor(explosionPos.x + (double) f3 + 1.0D);
		int i2 = Mth.floor(explosionPos.y - (double) f3 - 1.0D);
		int i1 = Mth.floor(explosionPos.y + (double) f3 + 1.0D);
		int j2 = Mth.floor(explosionPos.z - (double) f3 - 1.0D);
		int j1 = Mth.floor(explosionPos.z + (double) f3 + 1.0D);
		List<Entity> entities = event.getLevel().getEntities((Entity) null, new AABB(k1, i2, j2, l1, i1, j1), EntitySelector.NO_SPECTATORS);

		for (Entity entity : entities) {
			if (event.isCanceled())
				return;

			if (entity instanceof LivingEntity) {
				CuriosCompat.checkForCurios((LivingEntity) entity, stack -> {
					if (stack.is(CuriosCompat.EXPLOSION_CHARM)) {
						long time = event.getLevel().getGameTime();
						if (hasItemCooledDown(stack, time, COOLDOWN) || hasItemMercy(stack, time, MERCY_TIME)) {
							event.getLevel().playSound(null, explosionPos.x, explosionPos.y, explosionPos.z, EmbersSounds.EXPLOSION_CHARM_ABSORB.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

							if (event.getLevel() instanceof ServerLevel server) {
								server.sendParticles(GlowParticleOptions.EMBER, explosionPos.x, explosionPos.y, explosionPos.z, 3, 0.0, 0.0, 0.0, 0.1);
								server.sendParticles(SmokeParticleOptions.BIG_SMOKE, explosionPos.x, explosionPos.y, explosionPos.z, 20, 0.25, 0.25, 0.25, 1.0);

							}
							event.setCanceled(true);
							if (hasItemCooledDown(stack, time, COOLDOWN)) {
								event.getLevel().playSound(null, explosionPos.x, explosionPos.y, explosionPos.z, EmbersSounds.EXPLOSION_CHARM_RECHARGE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
								setItemCooldown(stack, time);
							}
						}
						return true;
					}
					return false;
				});
			}
		}
	}
}