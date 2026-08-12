package hu.zoldleo.embers.recipe;

import java.util.List;

import hu.zoldleo.embers.recipe.base.IDawnstoneAnvilRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class AnvilDisplayRecipe implements IDawnstoneAnvilRecipe {
	public List<ItemStack> outputs;
	public List<ItemStack> inputs;
	public Ingredient ingredient;

	public AnvilDisplayRecipe(List<ItemStack> outputs, List<ItemStack> inputs, Ingredient ingredient) {
		this.outputs = outputs;
		this.inputs = inputs;
		this.ingredient = ingredient;
	}

	@Override
	public boolean matches(@NotNull RecipeInput context, @NotNull Level pLevel) {
		return false;
	}

	@Override
	public List<ItemStack> getOutput(RecipeInput context) {
		return List.of();
	}

	@Override
	public List<ItemStack> getDisplayInputBottom() {
		return inputs;
	}

	@Override
	public List<ItemStack> getDisplayInputTop() {
		return List.of(ingredient.getItems());
	}

	@Override
	public List<ItemStack> getDisplayOutput() {
		return outputs;
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return AnvilRepairRecipe.SERIALIZER;
	}
}