package hu.zoldleo.embers.compat.jei;

import com.mojang.datafixers.util.Either;
import hu.zoldleo.embers.datagen.EmbersFluidTags;
import hu.zoldleo.embers.util.FluidAmounts;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.neoforge.NeoForgeTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class IngotTooltipCallback implements IRecipeSlotRichTooltipCallback {
	public static IngotTooltipCallback INSTANCE = new IngotTooltipCallback();

	@Override
	public void onRichTooltip(@NotNull IRecipeSlotView recipeSlotView, ITooltipBuilder tooltip) {
		//first find the index of the line we want to insert after
		int index = -1;
		for (Either<FormattedText, TooltipComponent> line : tooltip.getLines()) {
            index++;
            if (line.left().isPresent() && line.left().get() instanceof Component component && component.getContents() instanceof TranslatableContents translatable && translatable.getKey().equals("jei.tooltip.liquid.amount"))
                break;
		}

		FluidStack fluid = recipeSlotView.getDisplayedIngredient(NeoForgeTypes.FLUID_STACK).orElse(FluidStack.EMPTY);
		if (index != -1 && fluid.is(EmbersFluidTags.INGOT_TOOLTIP) && fluid.getAmount() >= FluidAmounts.nuggetValue())
			tooltip.getLines().add(index + 1, Either.left(FluidAmounts.getIngotTooltip(fluid.getAmount()).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))));
	}
}