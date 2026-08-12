package hu.zoldleo.embers.augment;

import java.util.ListIterator;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.augment.AugmentUtil;
import hu.zoldleo.embers.api.event.EmberProjectileEvent;
import hu.zoldleo.embers.api.projectile.EffectArea;
import hu.zoldleo.embers.api.projectile.EffectDamage;
import hu.zoldleo.embers.api.projectile.EffectMulti;
import hu.zoldleo.embers.api.projectile.IProjectileEffect;
import hu.zoldleo.embers.api.projectile.IProjectilePreset;
import hu.zoldleo.embers.api.projectile.ProjectileFireball;
import hu.zoldleo.embers.api.projectile.ProjectileRay;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public class DiffractionBarrelAugment extends AugmentBase {
	public DiffractionBarrelAugment() {
		super(2.0);
	}

	@SubscribeEvent
	public static void onProjectileFire(EmberProjectileEvent event) {
		ListIterator<IProjectilePreset> projectiles = event.getProjectiles().listIterator();

		ItemStack weapon = event.getStack();
		if (!weapon.isEmpty() && AugmentUtil.hasHeat(weapon)) {
			int level = AugmentUtil.getAugmentLevel(weapon, RegistryManager.DIFFRACTION_BARREL_AUGMENT);
			if (level > 0) {
				while (projectiles.hasNext()) {
					IProjectilePreset projectile = projectiles.next();
					Vec3 velocity = projectile.getVelocity();
					double speed = velocity.length();
					int bullets = 3 + level;
					IProjectileEffect effect = projectile.getEffect();
					if (projectile instanceof ProjectileRay) {
						double newspeed = 1.0;
                        adjustEffect(effect, 1.0 / 3.0);
						projectiles.remove();
						for (int i = 0; i < bullets; i++) {
							double spread = 0.1 * level;
							Vec3 newVelocity = velocity.add((Misc.random.nextDouble() - 0.5) * speed * 2 * spread, (Misc.random.nextDouble() - 0.5) * speed * 2 * spread, (Misc.random.nextDouble() - 0.5) * speed * 2 * spread).scale(newspeed / speed);
							IProjectilePreset newProjectile = new ProjectileFireball(projectile.getShooter(), projectile.getPos(), newVelocity, 2.4, 80, effect);
							newProjectile.setColor(projectile.getColor());
							newProjectile.setColor(projectile.getColorId());
							projectiles.add(newProjectile);
						}
					} else if (projectile instanceof ProjectileFireball fireball) {
                        adjustEffect(effect, 1.0 / 3.0);
						projectiles.remove();
						for (int i = 0; i < bullets; i++) {
							double spread = 0.1 * level;
							Vec3 newVelocity = velocity.add((Misc.random.nextDouble() - 0.5) * speed * 2 * spread, (Misc.random.nextDouble() - 0.5) * speed * 2 * spread, (Misc.random.nextDouble() - 0.5) * speed * 2 * spread);
							IProjectilePreset newProjectile = new ProjectileFireball(projectile.getShooter(), projectile.getPos(), newVelocity, fireball.getSize() / 3, fireball.getLifetime() / 2, effect);
							newProjectile.setColor(projectile.getColor());
							newProjectile.setColor(projectile.getColorId());
							projectiles.add(newProjectile);
						}
					}
				}
			}
		}
	}

	private static void adjustEffect(IProjectileEffect effect, double multiplier) {
		if (effect instanceof EffectArea areaEffect) {
            adjustEffect(areaEffect.getEffect(), multiplier);
		} else if (effect instanceof EffectMulti) {
			for (IProjectileEffect subEffect : ((EffectMulti) effect).getEffects())
				adjustEffect(subEffect,multiplier);
		} else if (effect instanceof EffectDamage damageEffect) {
            damageEffect.setDamage((float) (damageEffect.getDamage() * multiplier));
			damageEffect.setInvinciblityMultiplier(0.0);
		}
	}
}