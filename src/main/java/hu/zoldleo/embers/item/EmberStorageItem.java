package hu.zoldleo.embers.item;

import java.text.DecimalFormat;
import java.util.List;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.EmbersClientEvents;
import hu.zoldleo.embers.api.capabilities.EmbersCapabilities;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.power.DefaultEmberItemCapability;
import hu.zoldleo.embers.util.DecimalFormats;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.ChatFormatting;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public abstract class EmberStorageItem extends Item {
	public EmberStorageItem(Properties properties) {
		super(properties);
	}

	public static ItemStack withFill(Item item, double ember) {
		ItemStack stack = new ItemStack(item);
		IEmberCapability cap = stack.getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM);
        if (cap != null)
		    cap.setEmber(ember);
		return stack;
	}

	public abstract double getCapacity();

    public IEmberCapability getEmberCapability(ItemStack stack) {
        return new DefaultEmberItemCapability(stack);
    }

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return stack.getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM) != null;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		IEmberCapability cap = stack.getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM);
		if (cap != null)
			return Math.round(13.0F - (float) (cap.getEmberCapacity()-cap.getEmber()) * 13.0F / (float) cap.getEmberCapacity());
		return 0;
	}

	@Override
	public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
		return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
	}

	@Override
	public int getBarColor(@NotNull ItemStack pStack) {
		return 0xFF6600;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag isAdvanced) {
		IEmberCapability cap = stack.getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM);
		if (cap != null) {
			DecimalFormat emberFormat = DecimalFormats.getDecimalFormat(Embers.MODID + ".decimal_format.ember");
			tooltip.add(Component.translatable(Embers.MODID + ".tooltip.item.ember", emberFormat.format(cap.getEmber()),  emberFormat.format(cap.getEmberCapacity())).withStyle(ChatFormatting.GRAY));
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static class ColorHandler implements ItemColor {
		@Override
		public int getColor(@NotNull ItemStack stack, int tintIndex) {
			if (tintIndex == 0) {
				IEmberCapability capability = stack.getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM, null);
				if (capability != null) {
					float coeff = (float)(capability.getEmber() / capability.getEmberCapacity());
					float timerSine = ((float)Math.sin(8.0*Math.toRadians(EmbersClientEvents.ticks % 360))+1.0f)/2.0f;
					int r = 255;
					int g = (int)(255.0f*(1.0f-coeff) + (64.0f*timerSine+64.0f)*coeff);
					int b = (int)(255.0f*(1.0f-coeff) + 16.0f*coeff);
					return Misc.intColor(0xFF, r, g, b);
				}
			}
			return 0xFFFFFFFF;
		}
	}
}