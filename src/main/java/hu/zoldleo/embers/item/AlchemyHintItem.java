package hu.zoldleo.embers.item;

import java.util.ArrayList;
import java.util.List;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.datacomponents.HintComponent;
import org.jetbrains.annotations.NotNull;

import hu.zoldleo.embers.Embers;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class AlchemyHintItem extends Item {
	public AlchemyHintItem(Properties pProperties) {
		super(pProperties);
	}

	public static int getBlackPins(ItemStack stack) {
        HintComponent component = stack.get(RegistryManager.HINT_COMPONENT);
        return component == null ? 0 : component.blackPins();
	}

	public static int getWhitePins(ItemStack stack) {
        HintComponent component = stack.get(RegistryManager.HINT_COMPONENT);
        return component == null ? 0 : component.whitePins();
	}

	public static ArrayList<ItemStack> getAspects(ItemStack stack) {
        HintComponent component = stack.get(RegistryManager.HINT_COMPONENT);
        return component == null ? new ArrayList<>() : new ArrayList<>(component.aspects());
	}

	public static ArrayList<ItemStack> getInputs(ItemStack stack) {
        HintComponent component = stack.get(RegistryManager.HINT_COMPONENT);
        return component == null ? new ArrayList<>() : new ArrayList<>(component.inputs());
	}

	public static ItemStack getResult(ItemStack stack) {
        HintComponent component = stack.get(RegistryManager.HINT_COMPONENT);
        return component == null ? ItemStack.EMPTY : component.result();
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag isAdvanced) {
		int blackPins = getBlackPins(stack);
		int whitePins = getWhitePins(stack);
		if (blackPins != 0)
			tooltip.add(Component.translatable(blackPins == 1 ? Embers.MODID + ".alchemy_hint.black.one" : Embers.MODID + ".alchemy_hint.black", blackPins).withStyle(ChatFormatting.GRAY));
		if (whitePins != 0)
			tooltip.add(Component.translatable(whitePins == 1 ? Embers.MODID + ".alchemy_hint.white.one" : Embers.MODID + ".alchemy_hint.white", whitePins).withStyle(ChatFormatting.GRAY));
		if (blackPins == 0 && whitePins == 0)
			tooltip.add(Component.translatable(Embers.MODID + ".alchemy_hint.none").withStyle(ChatFormatting.GRAY));
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide)
			return InteractionResultHolder.consume(stack);
		int blackPins = getBlackPins(stack);
		int whitePins = getWhitePins(stack);
		String black = blackPins == 1 ? I18n.get(Embers.MODID + ".alchemy_hint.black.one") : I18n.get(Embers.MODID + ".alchemy_hint.black", blackPins); // TODO: why???
		String white = whitePins == 1 ? I18n.get(Embers.MODID + ".alchemy_hint.white.one") : I18n.get(Embers.MODID + ".alchemy_hint.white", whitePins);
		if (blackPins != 0) {
			if (whitePins != 0) {
				player.displayClientMessage(Component.translatable(Embers.MODID + ".alchemy_hint", I18n.get(Embers.MODID + ".alchemy_hint.and", black, white)), false);
				return InteractionResultHolder.consume(stack);
			}
			player.displayClientMessage(Component.translatable(Embers.MODID + ".alchemy_hint", black), false);
			return InteractionResultHolder.consume(stack);
		}
		if (whitePins != 0) {
			player.displayClientMessage(Component.translatable(Embers.MODID + ".alchemy_hint", white), false);
			return InteractionResultHolder.consume(stack);
		}
		player.displayClientMessage(Component.translatable(Embers.MODID + ".alchemy_hint", I18n.get(Embers.MODID + ".alchemy_hint.none")), false);
		return InteractionResultHolder.consume(stack);
	}
}