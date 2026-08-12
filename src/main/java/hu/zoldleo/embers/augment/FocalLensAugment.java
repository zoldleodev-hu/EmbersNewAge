package hu.zoldleo.embers.augment;

import java.util.ListIterator;

import hu.zoldleo.embers.ConfigManager;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.augment.AugmentUtil;
import hu.zoldleo.embers.api.event.EmberProjectileEvent;
import hu.zoldleo.embers.api.projectile.IProjectilePreset;
import hu.zoldleo.embers.api.projectile.ProjectileFireball;
import hu.zoldleo.embers.api.projectile.ProjectileRay;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public class FocalLensAugment extends AugmentBase {
	public FocalLensAugment() {
		super(10.0);
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onProjectileFire(EmberProjectileEvent event) {
		ListIterator<IProjectilePreset> projectiles = event.getProjectiles().listIterator();

		ItemStack weapon = event.getStack();
		if(!weapon.isEmpty() && AugmentUtil.hasHeat(weapon)) {
			int level = AugmentUtil.getAugmentLevel(weapon, RegistryManager.FOCAL_LENS_AUGMENT);
			int index = 0;
			int modulo = 1 + (level-1) * 2;
			if (level > 0) {
				while (projectiles.hasNext()) {
					IProjectilePreset projectile = projectiles.next();
					if (projectile instanceof ProjectileRay ray) {
						ray.setPierceEntities(true);
					} else if (projectile instanceof ProjectileFireball fireball) {
						fireball.setHoming(level * 10, 4.0 + level * 1.0, index, modulo, EntitySelector.NO_SPECTATORS.and(EntitySelector.LIVING_ENTITY_STILL_ALIVE)
								.and(entity -> {
									Entity shooter = projectile.getShooter();
									if (shooter != null && entity.isAlliedTo(shooter))
										return false;
									if (entity instanceof Player && shooter instanceof Player && !isPVPEnabled(entity.level()))
										return false;
									return shooter != entity;
								}));
					}
					index++;
				}
			}
		}
	}

	public static boolean isPVPEnabled(Level world) {
		MinecraftServer server = world.getServer();
		return server != null && server.isPvpAllowed() && ConfigManager.PVP_EVERYBODY_IS_ENEMY.get(); //oh the misery
	}
}