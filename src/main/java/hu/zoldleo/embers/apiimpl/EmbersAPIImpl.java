package hu.zoldleo.embers.apiimpl;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import hu.zoldleo.embers.datacomponents.BlockTargetComponent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.EmbersAPI;
import hu.zoldleo.embers.api.IEmbersAPI;
import hu.zoldleo.embers.api.augment.AugmentUtil;
import hu.zoldleo.embers.api.misc.HammerTarget;
import hu.zoldleo.embers.api.upgrades.UpgradeUtil;
import hu.zoldleo.embers.network.message.MessageScalesData;
import hu.zoldleo.embers.util.EmberGenUtil;
import hu.zoldleo.embers.util.EmberInventoryUtil;
import hu.zoldleo.embers.util.EmbersColors;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class EmbersAPIImpl implements IEmbersAPI {
	public static void init() {
		EmbersAPI.IMPL = new EmbersAPIImpl();
		AugmentUtil.IMPL = new AugmentUtilImpl();
		UpgradeUtil.IMPL = new UpgradeUtilImpl();
	}

	@Override
	public float getEmberDensity(long seed, int x, int z) {
		return EmberGenUtil.getEmberDensity(seed, x, z);
	}

	@Override
	public float getEmberStability(long seed, int x, int z) {
		return EmberGenUtil.getEmberStability(seed, x, z);
	}

	@Override
	public void registerLinkingHammer(Item item) {
		Misc.IS_HOLDING_HAMMER.add((player, hand) -> player.getItemInHand(hand).getItem() == item);
	}

	@Override
	public void registerLinkingHammer(BiPredicate<Player, InteractionHand> predicate) {
		Misc.IS_HOLDING_HAMMER.add(predicate);
	}

	@Override
	public void registerHammerTargetGetter(Item item) {
		Misc.GET_HAMMER_TARGET.add(player -> {
			ItemStack stack = player.getMainHandItem();
			if (stack.getItem() != item) {
				stack = player.getOffhandItem();
			}
			if (stack.getItem() == item) {
                BlockTargetComponent component = stack.get(RegistryManager.BLOCK_TARGET_COMPONENT);
                if (component != null)
                    return new HammerTarget(component.target(), component.face());
			}
			return null;
		});
	}

	@Override
	public void registerHammerTargetGetter(Function<Player, HammerTarget> predicate) {
		Misc.GET_HAMMER_TARGET.add(predicate);
	}

	@Override
	public boolean isHoldingHammer(Player player, InteractionHand hand) {
		return Misc.isHoldingHammer(player, hand);
	}

	@Override
	public HammerTarget getHammerTarget(Player player) {
		return Misc.getHammerTarget(player);
	}

	@Override
	public void registerLens(Ingredient ingredient) {
		Misc.IS_WEARING_LENS.add((player) -> ingredient.test(player.getMainHandItem()) || ingredient.test(player.getOffhandItem()));
	}

	@Override
	public void registerWearableLens(Ingredient ingredient) {
		Misc.IS_WEARING_LENS.add((player) -> {
			if (ingredient.test(player.getInventory().armor.get(EquipmentSlot.HEAD.getIndex()))) {
				return AugmentUtil.getAugmentLevel(player.getInventory().armor.get(EquipmentSlot.HEAD.getIndex()), RegistryManager.SMOKY_LENS_AUGMENT) < 1;
			}
			return false;
		});
	}

	@Override
	public void registerLens(Predicate<Player> predicate) {
		Misc.IS_WEARING_LENS.add(predicate);
	}

	@Override
	public boolean isWearingLens(Player player) {
		return Misc.isWearingLens(player);
	}

	@Override
	public void registerEmberResonance(Ingredient ingredient, double resonance) {
		Misc.GET_EMBER_RESONANCE.add((stack) -> ingredient.test(stack) ? resonance : -1.0);
	}

	@Override
	public double getEmberResonance(ItemStack stack) {
		return Misc.getEmberResonance(stack);
	}

	@Override
	public double getEmberTotal(Player player) {
		return EmberInventoryUtil.getEmberTotal(player);
	}

	@Override
	public double getEmberCapacityTotal(Player player) {
		return EmberInventoryUtil.getEmberCapacityTotal(player);
	}

	@Override
	public void removeEmber(Player player, double amount) {
		EmberInventoryUtil.removeEmber(player, amount);
	}

	@Override
	public Item getTaggedItem(TagKey<Item> tag) {
		return Misc.getTaggedItem(tag);
	}

	@Override
	public double getScales(LivingEntity entity) {
        return entity.getData(RegistryManager.SCALES_DATA);
    }

	@Override
	public void setScales(LivingEntity entity, double scales) {
		double cap = entity.getData(RegistryManager.SCALES_DATA);
        if (entity instanceof ServerPlayer player && cap != scales)
            PacketDistributor.sendToPlayer(player, new MessageScalesData());
        entity.setData(RegistryManager.SCALES_DATA, scales);
    }

	@Override
	public void registerColor(ResourceLocation id, Vector3f color) {
		EmbersColors.colors.put(id, color);
	}

	@Override
	public Vector3f getColor(ResourceLocation id, Vector3f color) {
		if (id.equals(EmbersColors.CUSTOM_ID))
			return color;
		return EmbersColors.colors.get(id);
	}

	@Override
	public Vector3f getColor(ResourceLocation id) {
		return EmbersColors.colors.get(id);
	}
}