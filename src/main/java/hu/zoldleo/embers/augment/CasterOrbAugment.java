package hu.zoldleo.embers.augment;

import java.util.HashMap;
import java.util.UUID;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.augment.AugmentUtil;
import hu.zoldleo.embers.network.message.MessageCasterOrb;
import hu.zoldleo.embers.util.EmberInventoryUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber
public class CasterOrbAugment extends AugmentBase {
	public static float prevCooledStrength = 0;
	public static float cooldownTicks = 0;
	public static HashMap<UUID,Float> cooldownTicksServer = new HashMap<>();

	public CasterOrbAugment() {
		super(2.0);
	}

	public static void setCooldown(UUID uuid, float ticks) {
		cooldownTicksServer.put(uuid,ticks);
	}

	public static boolean hasCooldown(UUID uuid) {
		return cooldownTicksServer.getOrDefault(uuid,0.0f) > 0;
	}

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Pre event) {
        for (UUID uuid : cooldownTicksServer.keySet()) {
            Float ticks = cooldownTicksServer.get(uuid) - 1;
            cooldownTicksServer.put(uuid, ticks);
        }
    }

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null)
            prevCooledStrength = mc.player.getAttackStrengthScale(0);
        if (cooldownTicks > 0)
            cooldownTicks--;
    }

	@SubscribeEvent
	public static void onSwing(PlayerInteractEvent.LeftClickBlock event) {
		Player player = event.getEntity();
		Level world = event.getLevel();
		ItemStack heldStack = event.getItemStack();
        tryShoot(player, world, heldStack);
	}

	@SubscribeEvent
	public static void onSwing(PlayerInteractEvent.LeftClickEmpty event) {
		Player player = event.getEntity();
		Level world = event.getLevel();
		ItemStack heldStack = event.getItemStack();
		tryShoot(player, world, heldStack);
	}

	private static void tryShoot(Player player, Level world, ItemStack heldStack) {
		if (prevCooledStrength == 1.0f) {
			if (AugmentUtil.hasHeat(heldStack)) {
                CasterOrbAugment augment = (CasterOrbAugment)RegistryManager.CASTER_ORB_AUGMENT.value();
				int level = AugmentUtil.getAugmentLevel(heldStack, RegistryManager.CASTER_ORB_AUGMENT);
				if (world.isClientSide() && level > 0 && EmberInventoryUtil.getEmberTotal(player) > augment.cost && cooldownTicks == 0) {
                    PacketDistributor.sendToServer(new MessageCasterOrb(player.getLookAngle().x, player.getLookAngle().y, player.getLookAngle().z));
					cooldownTicks = 20;
				}
			}
		}
	}
}