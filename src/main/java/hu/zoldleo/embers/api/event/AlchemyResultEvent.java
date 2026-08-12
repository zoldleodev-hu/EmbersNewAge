package hu.zoldleo.embers.api.event;

import hu.zoldleo.embers.api.misc.AlchemyResult;
import hu.zoldleo.embers.recipe.base.IAlchemyRecipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AlchemyResultEvent extends UpgradeEvent {
    RecipeHolder<IAlchemyRecipe> recipe;
	AlchemyResult result;
	int consumeAmount;
	boolean isFailure;
	ItemStack resultStack = null;

	public AlchemyResultEvent(BlockEntity tile, RecipeHolder<IAlchemyRecipe> recipe, AlchemyResult result, int consumeAmount) {
		super(tile);
		this.setRecipe(recipe);
		this.result = result;
		this.consumeAmount = consumeAmount;
		this.isFailure = result.blackPins != recipe.value().getInputs().size();
	}

	public int getConsumeAmount() {
		return consumeAmount;
	}

	public void setconsumeAmount(int consumeAmount) {
		this.consumeAmount = consumeAmount;
	}

	public AlchemyResult getResult() {
		return result;
	}

	public void setResult(AlchemyResult result) {
		this.result = result;
	}

	public ItemStack getResultStack() {
		if (resultStack != null)
			return resultStack;
		return isFailure ? recipe.value().getfailureItem() : recipe.value().getResultItem();
	}

	public void setResultStack(ItemStack resultStack) {
		this.resultStack = resultStack;
	}

	public boolean isFailure() {
		return isFailure;
	}

	public void setFailure(boolean isFailure) {
		this.isFailure = isFailure;
	}

	public RecipeHolder<IAlchemyRecipe> getRecipe() {
		return recipe;
	}

	public void setRecipe(RecipeHolder<IAlchemyRecipe> recipe) {
		this.recipe = recipe;
	}
}