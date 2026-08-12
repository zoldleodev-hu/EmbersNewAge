package hu.zoldleo.embers.augment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.EmbersAPI;
import hu.zoldleo.embers.api.augment.AugmentUtil;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.particle.XRayGlowParticleOptions;
import hu.zoldleo.embers.util.EmberInventoryUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber
public class ResonatingBellAugment extends AugmentBase {
	public static HashMap<UUID, Float> cooldownTicksServer = new HashMap<>();

	public ResonatingBellAugment() {
		super(5.0);
	}

	public static void setCooldown(UUID uuid, float ticks) {
		cooldownTicksServer.put(uuid, ticks);
	}

	public static boolean hasCooldown(UUID uuid) {
		return cooldownTicksServer.getOrDefault(uuid, 0.0f) > 0;
	}

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Pre event) {
        for (UUID uuid : cooldownTicksServer.keySet()) {
            Float ticks = cooldownTicksServer.get(uuid) - 1;
            cooldownTicksServer.put(uuid, ticks);
        }
    }

	@SubscribeEvent
	public static void onClick(PlayerInteractEvent.RightClickBlock event) {
		ItemStack heldStack = event.getItemStack();
		Level world = event.getLevel();
		Player player = event.getEntity();
		BlockPos pos = event.getPos();
		if (AugmentUtil.hasHeat(heldStack)) {
            ResonatingBellAugment augment = (ResonatingBellAugment)RegistryManager.RESONATING_BELL_AUGMENT.value();
			int level = AugmentUtil.getAugmentLevel(heldStack, RegistryManager.RESONATING_BELL_AUGMENT);
			UUID uuid = player.getUUID();
			if (!world.isClientSide() && level > 0 && EmberInventoryUtil.getEmberTotal(player) >= augment.cost && !hasCooldown(uuid)) {
				double resonance = EmbersAPI.getEmberResonance(heldStack);
				int blockLimit = (int) (150 * level * resonance);
				int radius = (int) (1 + 3 * level * resonance);

				setCooldown(uuid, 80);
				BlockState state = world.getBlockState(pos);
				int count = 0;
				List<BlockPos> positions = new ArrayList<>();
				BlockPos.MutableBlockPos mutablePos = pos.mutable();
				int baseX = pos.getX();
				int baseY = pos.getY();
				int baseZ = pos.getZ();
				for (int i = -radius; i <= radius; i++) {
					for (int j = -radius; j <= radius; j++) {
						for (int k = -radius; k <= radius; k++) {
							mutablePos.set(baseX + i, baseY + j, baseZ + k);
							if (world.getBlockState(mutablePos) == state) {
								positions.add(mutablePos.immutable());
								count++;
								if (count > blockLimit)
									break;
							}
						}
					}
				}
				if (count <= blockLimit) {
					if (world instanceof ServerLevel serverLevel) {
						for (BlockPos p : positions) {
							serverLevel.sendParticles(XRayGlowParticleOptions.EMBER_BIG_NOMOTION, p.getX() + 0.5f, p.getY() + 0.5f, p.getZ() + 0.5f, 3, 0.0625, 0.0625, 0.0625, 1.0);
						}
					}
					world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), EmbersSounds.RESONATING_BELL.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
				} else {
					world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), EmbersSounds.RESONATING_BELL.get(), SoundSource.PLAYERS, 1.0f, 0.1f);
				}
				EmberInventoryUtil.removeEmber(player, augment.cost);
			}
		}
	}
}