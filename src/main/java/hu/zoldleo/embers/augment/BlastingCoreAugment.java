package hu.zoldleo.embers.augment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.EmbersAPI;
import hu.zoldleo.embers.api.augment.AugmentUtil;
import hu.zoldleo.embers.util.EmberInventoryUtil;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber
public class BlastingCoreAugment extends AugmentBase {
	public BlastingCoreAugment() {
		super(2.0);
	}

	private double getChanceBonus(double resonance) {
		if (resonance > 1)
			return 1 + (resonance - 1) * 0.5;
		else
			return resonance;
	}

	@SubscribeEvent
	public static void onBreak(BlockEvent.BreakEvent event) {
		LevelAccessor world = event.getLevel();
		BlockPos pos = event.getPos();
        if (!event.getPlayer().getMainHandItem().isEmpty()) {
            BlastingCoreAugment augment = (BlastingCoreAugment)RegistryManager.BLASTING_CORE_AUGMENT.value();
            ItemStack s = event.getPlayer().getMainHandItem();
            int blastingLevel = AugmentUtil.getAugmentLevel(s, RegistryManager.BLASTING_CORE_AUGMENT);
            if (blastingLevel > 0 && EmberInventoryUtil.getEmberTotal(event.getPlayer()) >= augment.cost) {
                double resonance = EmbersAPI.getEmberResonance(s);
                double chance = (double) blastingLevel / (blastingLevel + 1) * augment.getChanceBonus(resonance);
                double x = pos.getX() + 0.5;
                double y = pos.getY() + 0.5;
                double z = pos.getZ() + 0.5;

                ToolExplosion explosion = new ToolExplosion(s, event.getPlayer().level(), event.getPlayer(), x, y, z, 0.5f, false, BlockInteraction.DESTROY, augment.getBlastCube(world, pos, event.getPlayer(), chance));
                spawnExplosion(event.getPlayer().level(), explosion, x, y, z, 0.5f);
                EmberInventoryUtil.removeEmber(event.getPlayer(), augment.cost);
            }
        }
    }

	public Iterable<BlockPos> getBlastAdjacent(LevelAccessor world, BlockPos pos, Player player, double chance) {
		ArrayList<BlockPos> posList = new ArrayList<>();
		for (Direction face : Direction.values())
			if (Misc.random.nextDouble() < chance)
				posList.add(pos.relative(face));
		return posList;
	}

	public List<BlockPos> getBlastCube(LevelAccessor world, BlockPos pos, Player player, double chance) {
		ArrayList<BlockPos> posList = new ArrayList<>();
		for (Direction facePrimary : Direction.values()) {
			if (Misc.random.nextDouble() < chance) {
				BlockPos posPrimary = pos.relative(facePrimary);
				posList.add(posPrimary);

				for (Direction faceSecondary : Direction.values()) {
					if(faceSecondary.getAxis() == facePrimary.getAxis())
						continue;
					if (Misc.random.nextDouble() < chance - 0.5) {
						BlockPos posSecondary = posPrimary.relative(faceSecondary);
						posList.add(posSecondary);

						for (Direction faceTertiary : Direction.values()) {
							if (faceTertiary.getAxis() == facePrimary.getAxis() || faceTertiary.getAxis() == faceSecondary.getAxis())
								continue;
							if (Misc.random.nextDouble() < chance - 1.0) {
								BlockPos posTertiary = posSecondary.relative(faceTertiary);
								posList.add(posTertiary);
							}
						}
					}
				}
			}
		}
		return posList;
	}

	public static HashSet<Entity> blastedEntities = new HashSet<>();

	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public static void onHit(LivingIncomingDamageEvent event) {
		if (!blastedEntities.contains(event.getEntity()) && event.getSource().getEntity() != event.getEntity() && event.getSource().getDirectEntity() != event.getEntity()) {
			try {
                BlastingCoreAugment augment = (BlastingCoreAugment)RegistryManager.BLASTING_CORE_AUGMENT.value();
				if (event.getSource().getEntity() instanceof Player damager) {
					blastedEntities.add(damager);
					ItemStack s = damager.getMainHandItem();
					if (!s.isEmpty()) {
						int blastingLevel = AugmentUtil.getAugmentLevel(s, RegistryManager.BLASTING_CORE_AUGMENT);
						if (blastingLevel > 0 && EmberInventoryUtil.getEmberTotal(damager) >= augment.cost) {
							double resonance = EmbersAPI.getEmberResonance(s);
							float strength = (float) ((resonance + 1) * (Math.atan(0.6 * (blastingLevel)) / (Math.PI)));

							EmberInventoryUtil.removeEmber(damager, augment.cost);
							blastedEntities.add(event.getEntity());
							List<? extends Entity> entities = damager.level().getEntitiesOfClass(LivingEntity.class, new AABB(event.getEntity().getX() - 4.0 * strength, event.getEntity().getY() - 4.0 * strength, event.getEntity().getZ() - 4.0 * strength,
									event.getEntity().getX() + 4.0 * strength, event.getEntity().getY() + 4.0 * strength, event.getEntity().getZ() + 4.0 * strength));

							double x = event.getEntity().getX();
							double y = event.getEntity().getY() + event.getEntity().getBbHeight() / 2.0;
							double z = event.getEntity().getZ();
							blastedEntities.addAll(entities);

							BlastingExplosion explosion = new BlastingExplosion((List<Entity>) entities, event.getAmount() * strength, damager.level(), damager, x, y, z, strength * 5f, false, BlockInteraction.KEEP);
							spawnExplosion(damager.level(), explosion, x, y, z, strength * 1.5f);
						}
					}
				}
				if (event.getEntity() instanceof Player damager) {
					int blastingLevel = AugmentUtil.getArmorAugmentLevel(damager, RegistryManager.BLASTING_CORE_AUGMENT);

					if (blastingLevel > 0 && EmberInventoryUtil.getEmberTotal(damager) >= augment.cost) {
						float strength = (float) (2.0 * (Math.atan(0.6 * (blastingLevel)) / (Math.PI)));
						EmberInventoryUtil.removeEmber(damager, augment.cost);
						List<? extends Entity> entities = damager.level().getEntitiesOfClass(LivingEntity.class, new AABB(damager.getX() - 4.0 * strength, damager.getY() - 4.0 * strength, damager.getZ() - 4.0 * strength,
								damager.getX() + 4.0 * strength, damager.getY() + 4.0 * strength, damager.getZ() + 4.0 * strength));

						double x = event.getEntity().getX();
						double y = event.getEntity().getY() + event.getEntity().getBbHeight() / 2.0;
						double z = event.getEntity().getZ();
						blastedEntities.addAll(entities);

						BlastingExplosion explosion = new BlastingExplosion((List<Entity>) entities, event.getAmount() * strength * 0.25f, damager.level(), damager, x, y, z, strength * 5f, false, BlockInteraction.KEEP);
						spawnExplosion(damager.level(), explosion, x, y, z, strength * 1.5f);
					}
				}
			} finally {
				blastedEntities.clear();
			}
		}
	}

	public static void spawnExplosion(Level level, Explosion explosion, double x, double y, double z, float radius) {
		if (!EventHooks.onExplosionStart(level, explosion)) {
			explosion.explode();
			explosion.finalizeExplosion(true);

			if (level instanceof ServerLevel server) {
				if (!explosion.interactsWithBlocks())
					explosion.clearToBlow();
				for (ServerPlayer serverplayer : server.players())
					if (serverplayer.distanceToSqr(x, y, z) < 4096.0D)
						serverplayer.connection.send(new ClientboundExplodePacket(
                                x,
                                y,
                                z,
                                radius,
                                explosion.getToBlow(),
                                explosion.getHitPlayers().get(serverplayer),
                                explosion.getBlockInteraction(),
                                explosion.getSmallExplosionParticles(),
                                explosion.getLargeExplosionParticles(),
                                explosion.getExplosionSound()));
			}
		}
	}
}