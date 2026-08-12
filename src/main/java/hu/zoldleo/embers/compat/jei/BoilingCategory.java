package hu.zoldleo.embers.compat.jei;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.recipe.base.IBoilingRecipe;

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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BoilingCategory implements IRecipeCategory<IBoilingRecipe> {
	private final IDrawable background;
	private final IDrawable icon;
	public static Component title = Component.translatable(Embers.MODID + ".jei.recipe.boiling");
	public static ResourceLocation texture = Embers.res("textures/gui/jei_boiler.png");

	public BoilingCategory(IGuiHelper helper) {
		background = helper.createDrawable(texture, 0, 0, 126, 28);
		icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(RegistryManager.MINI_BOILER_ITEM.get()));
	}

	@Override
	public @NotNull RecipeType<IBoilingRecipe> getRecipeType() {
		return JEIPlugin.BOILING;
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
	public void setRecipe(IRecipeLayoutBuilder builder, IBoilingRecipe recipe, @NotNull IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 26, 6)
		.addRichTooltipCallback(IngotTooltipCallback.INSTANCE)
		.setFluidRenderer(recipe.getDisplayInput().getFluids()[0].getAmount(), false, 16, 16)
		.addIngredients(NeoForgeTypes.FLUID_STACK, List.of(recipe.getDisplayInput().getFluids()));

		builder.addSlot(RecipeIngredientRole.OUTPUT, 84, 6)
		.addRichTooltipCallback(IngotTooltipCallback.INSTANCE)
		.setFluidRenderer(recipe.getDisplayOutput().getAmount(), false, 16, 16)
		.addIngredient(NeoForgeTypes.FLUID_STACK, recipe.getDisplayOutput());
	}

    @Override
    public void draw(@NotNull IBoilingRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
    }
}