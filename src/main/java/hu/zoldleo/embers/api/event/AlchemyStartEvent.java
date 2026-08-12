package hu.zoldleo.embers.api.event;

import hu.zoldleo.embers.recipe.context.AlchemyContext;
import hu.zoldleo.embers.recipe.base.IAlchemyRecipe;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AlchemyStartEvent extends UpgradeEvent {
	public AlchemyContext context;
    RecipeHolder<IAlchemyRecipe> recipe;

	public AlchemyStartEvent(BlockEntity tile, AlchemyContext context, RecipeHolder<IAlchemyRecipe> recipe) {
		super(tile);
		this.context = context;
		this.recipe = recipe;
	}

	public AlchemyContext getContext() {
		return context;
	}

	//if this returns null, there is no recipe and alchemy does not actually start
	public RecipeHolder<IAlchemyRecipe> getRecipe() {
		return recipe;
	}

	public void setRecipe(RecipeHolder<IAlchemyRecipe> recipe) {
		this.recipe = recipe;
	}
}