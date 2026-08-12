package hu.zoldleo.embers.compat.jei;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.recipe.base.IMixingRecipe;

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
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MixingCategory implements IRecipeCategory<IMixingRecipe> {
	private final IDrawable background;
	private final IDrawable icon;
	public static Component title = Component.translatable(Embers.MODID + ".jei.recipe.mixing");
	public static ResourceLocation texture = Embers.res("textures/gui/jei_mixer.png");
	double scale = 1.0 / 1000.0;

	public MixingCategory(IGuiHelper helper) {
		background = helper.createDrawable(texture, 0, 0, 108, 124);
		icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(RegistryManager.MIXER_CENTRIFUGE_ITEM.get()));
	}

	@Override
	public @NotNull RecipeType<IMixingRecipe> getRecipeType() {
		return JEIPlugin.MIXING;
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
	public void setRecipe(@NotNull IRecipeLayoutBuilder builder, IMixingRecipe recipe, @NotNull IFocusGroup focuses) {
		int count = 0;
		int capacity = FluidType.BUCKET_VOLUME * 8;

		for (SizedFluidIngredient ingredient : recipe.getDisplayInputFluids()) {
			if (count > 3)
				break;
			if (count == 2) {
				builder.addSlot(RecipeIngredientRole.INPUT, 9, 7)
                .addRichTooltipCallback(IngotTooltipCallback.INSTANCE)
				.setFluidRenderer((int) (capacity * scale + ingredient.getFluids()[0].getAmount() * (1.0 - scale)), false, 16, 32)
				.addIngredients(NeoForgeTypes.FLUID_STACK, List.of(ingredient.getFluids()));
			} else if (count == 0) {
				builder.addSlot(RecipeIngredientRole.INPUT, 34, 7)
				.addRichTooltipCallback(IngotTooltipCallback.INSTANCE)
				.setFluidRenderer((int) (capacity * scale + ingredient.getFluids()[0].getAmount() * (1.0 - scale)), false, 16, 32)
				.addIngredients(NeoForgeTypes.FLUID_STACK, List.of(ingredient.getFluids()));
			} else if (count == 1) {
				builder.addSlot(RecipeIngredientRole.INPUT, 59, 7)
				.addRichTooltipCallback(IngotTooltipCallback.INSTANCE)
				.setFluidRenderer((int) (capacity * scale + ingredient.getFluids()[0].getAmount() * (1.0 - scale)), false, 16, 32)
				.addIngredients(NeoForgeTypes.FLUID_STACK, List.of(ingredient.getFluids()));
			} else if (count == 3) {
				builder.addSlot(RecipeIngredientRole.INPUT, 83, 7)
				.addRichTooltipCallback(IngotTooltipCallback.INSTANCE)
				.setFluidRenderer((int) (capacity * scale + ingredient.getFluids()[0].getAmount() * (1.0 - scale)), false, 16, 32)
				.addIngredients(NeoForgeTypes.FLUID_STACK, List.of(ingredient.getFluids()));
			}
			count++;
		}

		builder.addSlot(RecipeIngredientRole.OUTPUT, 46, 84)
		.addRichTooltipCallback(IngotTooltipCallback.INSTANCE)
		.setFluidRenderer((int) (capacity * scale + recipe.getDisplayOutput().getAmount() * (1.0 - scale)), false, 16, 32)
		.addIngredient(NeoForgeTypes.FLUID_STACK, recipe.getDisplayOutput());
	}

    @Override
    public void draw(@NotNull IMixingRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
    }
}