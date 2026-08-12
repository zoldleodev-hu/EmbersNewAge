package hu.zoldleo.embers.compat.jei;

import java.text.DecimalFormat;
import java.util.List;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.recipe.base.IGaseousFuelRecipe;
import hu.zoldleo.embers.util.DecimalFormats;
import hu.zoldleo.embers.util.Misc;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GaseousFuelCategory implements IRecipeCategory<IGaseousFuelRecipe> {
	private final IDrawable background;
	private final IDrawable icon;
	public static Component title = Component.translatable(Embers.MODID + ".jei.recipe.gaseous_fuel");
	public static ResourceLocation texture = Embers.res("textures/gui/jei_gaseous_fuel.png");

	public GaseousFuelCategory(IGuiHelper helper) {
		background = helper.createDrawable(texture, 0, 0, 126, 31);
		icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(RegistryManager.WILDFIRE_STIRLING_ITEM.get()));
	}

	@Override
	public @NotNull RecipeType<IGaseousFuelRecipe> getRecipeType() {
		return JEIPlugin.GASEOUS_FUEL;
	}

	@Override
	public @NotNull Component getTitle() {
		return title;
	}

    @Override
    public int getWidth() {
        return background.getWidth();
    }

    @Override
    public int getHeight() {
        return background.getHeight();
    }

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, IGaseousFuelRecipe recipe, @NotNull IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 6, 7)
		.addRichTooltipCallback(IngotTooltipCallback.INSTANCE)
		.setFluidRenderer(recipe.getDisplayInput().getFluids()[0].getAmount(), false, 16, 16)
		.addIngredients(NeoForgeTypes.FLUID_STACK, List.of(recipe.getDisplayInput().getFluids()));
	}

	@Override
	public void draw(IGaseousFuelRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
		DecimalFormat multiplierFormat = DecimalFormats.getDecimalFormat(Embers.MODID + ".decimal_format.ember_multiplier");
		Misc.drawComponents(Minecraft.getInstance().font, guiGraphics, 28, 5, Component.translatable(Embers.MODID + ".jei.recipe.gaseous_fuel.burn_time", recipe.getDisplayBurnTime()));
		Misc.drawComponents(Minecraft.getInstance().font, guiGraphics, 28, 17, Component.translatable(Embers.MODID + ".jei.recipe.gaseous_fuel.power_multiplier", multiplierFormat.format(recipe.getDisplayMultiplier())));
	}
}