package hu.zoldleo.embers.augment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.augment.AugmentUtil;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.particle.SmokeParticleOptions;
import hu.zoldleo.embers.util.EmberInventoryUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber
public class CinderJetAugment extends AugmentBase {
	public CinderJetAugment() {
		super(2.0);
	}

	public static Map<UUID, Boolean> sprintingClient = new HashMap<>();
	public static Map<UUID, Boolean> sprintingServer = new HashMap<>();

	public static Map<UUID, Boolean> getSprinting(Level level) {
		if (level.isClientSide())
			return sprintingClient;
		return sprintingServer;
	}

	@SubscribeEvent
	public static void onLivingTick(EntityTickEvent.Post event) { // TODO
		if (event.getEntity() instanceof Player player && (!player.level().isClientSide() || player == Minecraft.getInstance().player)) {
			UUID id = player.getUUID();
			if (getSprinting(player.level()).containsKey(id)) {
				if (player.isSprinting() && !getSprinting(player.level()).get(id)) {
                    CinderJetAugment augment = (CinderJetAugment)RegistryManager.CINDER_JET_AUGMENT.value();
					int level = AugmentUtil.getArmorAugmentLevel(player, RegistryManager.CINDER_JET_AUGMENT);
					float dashStrength = (float)(2.0*(Math.atan(0.6*(level))/(1.25)));
					if (dashStrength > 0 && player.onGround() && EmberInventoryUtil.getEmberTotal(player) > augment.cost) {
						EmberInventoryUtil.removeEmber((player), augment.cost);
                        player.setDeltaMovement(player.getDeltaMovement().add(new Vec3(2.0 * player.getLookAngle().x*dashStrength, 0.4, 2.0* player.getLookAngle().z*dashStrength)));
                        player.level().playSound(player, player, EmbersSounds.CINDER_JET.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
						if (player.level() instanceof ServerLevel serverLevel)
							serverLevel.sendParticles(SmokeParticleOptions.BIG_SMOKE, player.getX() - player.getLookAngle().x * 0.5f, player.getY() + player.getBbHeight() / 4.0f, player.getZ() - (float) player.getLookAngle().z * 0.5f, 40, 0.1, 0.1, 0.1, 1.0);
					}
				}
				getSprinting(player.level()).replace(id, player.isSprinting());
			} else {
				getSprinting(player.level()).put(id, player.isSprinting());
			}
		}
	}
}