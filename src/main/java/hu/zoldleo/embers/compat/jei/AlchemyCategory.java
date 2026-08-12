package hu.zoldleo.embers.compat.jei;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.recipe.base.IAlchemyRecipe;

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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import org.jetbrains.annotations.NotNull;

public class AlchemyCategory implements IRecipeCategory<IAlchemyRecipe> {
	private final IDrawable background;
	private final IDrawable pillar;
	private final IDrawable icon;
	public static Component title = Component.translatable(Embers.MODID + ".jei.recipe.alchemy");
	public static ResourceLocation texture = Embers.res("textures/gui/jei_alchemy.png");
	public static ResourceLocation pillarTexture = Embers.res("textures/gui/jei_alchemy.png");

	public AlchemyCategory(IGuiHelper helper) {
		background = helper.createDrawable(texture, 0, 0, 126, 108);
		pillar = helper.createDrawable(pillarTexture, 126, 0, 16, 16);
		icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(RegistryManager.ALCHEMY_TABLET_ITEM.get()));
	}

	@Override
	public @NotNull RecipeType<IAlchemyRecipe> getRecipeType() {
		return JEIPlugin.ALCHEMY;
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
	public void setRecipe(IRecipeLayoutBuilder builder, IAlchemyRecipe recipe, @NotNull IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 32, 37).addIngredients(recipe.getCenterInput());
		builder.addSlot(RecipeIngredientRole.OUTPUT, 101, 37).addItemStack(recipe.getResultItem());
		Vec3 center = new Vec3(0, 30, 0);

		int aspecti = recipe.getAspects().size();
		int inputs = recipe.getInputs().size();

		Ingredient[][] aspectusCombinations = new Ingredient[inputs][(int) Math.pow(aspecti, inputs)];
		for (int i = 0; i < inputs; i++) {
			for (int j = 0; j < ((int) Math.pow(aspecti, inputs)); j++) {
				aspectusCombinations[i][j] = recipe.getAspects().get((j / ((int) Math.pow(aspecti, i))) % aspecti);
			}
		}

		for (int i = 0; i < inputs; i++) {
			Vec3 rotated = center.zRot((float) (i * 2.0 * Math.PI / recipe.getInputs().size()));
			builder.addSlot(RecipeIngredientRole.INPUT, (int) (32 + rotated.x()), (int) (29 + rotated.y())).addIngredients(recipe.getInputs().get(i));

			builder.addSlot(RecipeIngredientRole.CATALYST, (int) (32 + rotated.x()), (int) (45 + rotated.y())).addIngredients(CompoundIngredient.of(aspectusCombinations[i])).setBackground(pillar, 0, 0);
		}
		for (int i = 0; i < aspecti; i++) {
			builder.addSlot(RecipeIngredientRole.CATALYST, 63 - 8 * recipe.getAspects().size() + 16 * i, 90).addIngredients(recipe.getAspects().get(i));
		}
	}

    @Override
    public void draw(@NotNull IAlchemyRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
    }
}