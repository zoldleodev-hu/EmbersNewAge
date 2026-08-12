package hu.zoldleo.embers.compat.jei;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.recipe.base.IStampingRecipe;

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
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StampingCategory implements IRecipeCategory<IStampingRecipe> {
	private final IDrawable background;
	private final IDrawable icon;
	public static Component title = Component.translatable(Embers.MODID + ".jei.recipe.stamping");
	public static ResourceLocation texture = Embers.res("textures/gui/jei_stamp.png");
	double scale = 1.0 / 16.0;

	public StampingCategory(IGuiHelper helper) {
		background = helper.createDrawable(texture, 0, 0, 108, 83);
		icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(RegistryManager.STAMPER_ITEM.get()));
	}

	@Override
	public @NotNull RecipeType<IStampingRecipe> getRecipeType() {
		return JEIPlugin.STAMPING;
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
	public void setRecipe(@NotNull IRecipeLayoutBuilder builder, IStampingRecipe recipe, @NotNull IFocusGroup focuses) {
		int amount = 0;

		for (FluidStack fluid : recipe.getDisplayInputFluid().getFluids()) // TODO: why?
			amount = fluid.getAmount();

		builder.addSlot(RecipeIngredientRole.INPUT, 8, 28).addIngredients(recipe.getDisplayInput());

		builder.addSlot(RecipeIngredientRole.CATALYST, 47, 7).addIngredients(recipe.getDisplayStamp());

		builder.addSlot(RecipeIngredientRole.OUTPUT, 84, 28).addItemStack(recipe.getResultItem());

		builder.addSlot(RecipeIngredientRole.INPUT, 47, 48)
		.addRichTooltipCallback(IngotTooltipCallback.INSTANCE)
		.setFluidRenderer((int) (1500 * scale + amount * (1.0 - scale)), false, 16, 32)
		.addIngredients(NeoForgeTypes.FLUID_STACK, List.of(recipe.getDisplayInputFluid().getFluids()));
	}

    @Override
    public void draw(@NotNull IStampingRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
    }
}