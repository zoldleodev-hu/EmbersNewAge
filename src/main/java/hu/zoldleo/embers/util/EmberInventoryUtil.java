package hu.zoldleo.embers.util;

import hu.zoldleo.embers.api.capabilities.EmbersCapabilities;
import hu.zoldleo.embers.api.entity.IEmberEntity;
import hu.zoldleo.embers.api.event.EmberModificationEvent;
import hu.zoldleo.embers.api.item.IHeldEmberCell;
import hu.zoldleo.embers.api.item.IInventoryEmberCell;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.compat.curios.CuriosCompat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;

public class EmberInventoryUtil {
	public static double getEmberCapacityTotal(LivingEntity entity) {
		double amount = 0;
        if (entity instanceof Player player) {
            for (int i = 0; i < 36; i++) {
                IEmberCapability capability = player.getInventory().getItem(i).getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM, null);
                if (capability instanceof IInventoryEmberCell)
                    amount += capability.getEmberCapacity();
            }
        }
		IEmberCapability capabilityOffhand = entity.getOffhandItem().getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM, null);
		if (capabilityOffhand instanceof IHeldEmberCell)
			amount += capabilityOffhand.getEmberCapacity();
		IEmberCapability capabilityMainHand = entity.getMainHandItem().getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM, null);
		if (capabilityMainHand instanceof IHeldEmberCell)
			amount += capabilityMainHand.getEmberCapacity();
		if (ModList.get().isLoaded("curios"))
			amount += CuriosCompat.getEmberCapacityTotal(entity);
        if (entity instanceof IEmberEntity emberEntity)
            amount += emberEntity.getEmberCapability().getEmberCapacity();
		return amount;
	}

	public static double getEmberTotal(LivingEntity entity) {
		double amount = 0;
        if (entity instanceof Player player) {
            for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
                IEmberCapability capability = player.getInventory().getItem(i).getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM, null);
                if (capability instanceof IInventoryEmberCell)
                    amount += capability.getEmber();
            }
        }
		IEmberCapability capabilityOffhand = entity.getOffhandItem().getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM, null);
		if (capabilityOffhand instanceof IHeldEmberCell)
			amount += capabilityOffhand.getEmber();
		IEmberCapability capabilityMainHand = entity.getMainHandItem().getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM, null);
		if (capabilityMainHand instanceof IHeldEmberCell)
			amount += capabilityMainHand.getEmber();
		if (ModList.get().isLoaded("curios"))
			amount += CuriosCompat.getEmberTotal(entity);
        if (entity instanceof IEmberEntity emberEntity)
            amount += emberEntity.getEmberCapability().getEmber();
		return amount;
	}

	public static void removeEmber(LivingEntity entity, double amount) {
		EmberModificationEvent.Remove event = new EmberModificationEvent.Remove(entity, amount);
		NeoForge.EVENT_BUS.post(event);
		double temp = event.getFinal();

		IEmberCapability capabilityOffhand = entity.getOffhandItem().getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM, null);
		if (capabilityOffhand instanceof IHeldEmberCell) {
			temp -= capabilityOffhand.removeAmount(temp, true);
			if (temp <= 0)
				return;
		}
		IEmberCapability capabilityMainHand = entity.getMainHandItem().getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM, null);
		if (capabilityMainHand instanceof IHeldEmberCell) {
			temp -= capabilityMainHand.removeAmount(temp, true);
			if (temp <= 0)
				return;
		}
		if (ModList.get().isLoaded("curios")) {
			temp = CuriosCompat.removeEmber(entity, temp);
			if (temp <= 0)
				return;
		}
        if (entity instanceof Player player) {
            for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
                IEmberCapability capability = player.getInventory().getItem(i).getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM, null);
                if (capability instanceof IInventoryEmberCell) {
                    temp -= capability.removeAmount(temp, true);
                    if (temp <= 0)
                        return;
                }
            }
        }
        if (entity instanceof IEmberEntity emberEntity) {
            /*/temp -= */emberEntity.getEmberCapability().removeAmount(temp, true);
            /*/if (temp <= 0)
                return;*/
        }
	}

    public static void addEmber(LivingEntity entity, double amount) {
        EmberModificationEvent.Add event = new EmberModificationEvent.Add(entity, amount);
        NeoForge.EVENT_BUS.post(event);
        double temp = amount;

        IEmberCapability capabilityOffhand = entity.getOffhandItem().getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM, null);
        if (capabilityOffhand instanceof IHeldEmberCell) {
            temp -= capabilityOffhand.addAmount(temp, true);
            if (temp <= 0)
                return;
        }
        IEmberCapability capabilityMainHand = entity.getMainHandItem().getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM, null);
        if (capabilityMainHand instanceof IHeldEmberCell) {
            temp -= capabilityMainHand.addAmount(temp, true);
            if (temp <= 0)
                return;
        }
        if (ModList.get().isLoaded("curios")) {
            temp = CuriosCompat.addEmber(entity, temp);
            if (temp <= 0)
                return;
        }
        if (entity instanceof Player player) {
            for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
                IEmberCapability capability = player.getInventory().getItem(i).getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM, null);
                if (capability instanceof IInventoryEmberCell) {
                    temp -= capability.addAmount(temp, true);
                    if (temp <= 0)
                        return;
                }
            }
        }
        if (entity instanceof IEmberEntity emberEntity) {
            /*/temp -= */emberEntity.getEmberCapability().addAmount(temp, true);
            /*/if (temp <= 0)
                return;*/
        }
    }
}