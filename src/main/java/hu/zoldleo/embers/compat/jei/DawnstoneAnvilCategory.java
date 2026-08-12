package hu.zoldleo.embers.compat.jei;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.recipe.base.IDawnstoneAnvilRecipe;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DawnstoneAnvilCategory implements IRecipeCategory<IDawnstoneAnvilRecipe> {
	private final IDrawable background;
	private final IDrawable icon;
	public static Component title = Component.translatable(Embers.MODID + ".jei.recipe.dawnstone_anvil");
	public static ResourceLocation texture = Embers.res("textures/gui/jei_dawnstone_anvil.png");

	public DawnstoneAnvilCategory(IGuiHelper helper) {
		background = helper.createDrawable(texture, 0, 0, 110, 58);
		icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(RegistryManager.DAWNSTONE_ANVIL_ITEM.get()));
	}

	@Override
	public @NotNull RecipeType<IDawnstoneAnvilRecipe> getRecipeType() {
		return JEIPlugin.DAWNSTONE_ANVIL;
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
	public void setRecipe(IRecipeLayoutBuilder builder, IDawnstoneAnvilRecipe recipe, @NotNull IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 22, 19).addItemStacks(recipe.getDisplayInputTop());
		builder.addSlot(RecipeIngredientRole.INPUT, 22, 37).addItemStacks(recipe.getDisplayInputBottom());
		builder.addSlot(RecipeIngredientRole.OUTPUT, 77, 28).addItemStacks(recipe.getDisplayOutput());
	}

    @Override
    public void draw(@NotNull IDawnstoneAnvilRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
    }
}