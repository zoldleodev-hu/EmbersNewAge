package hu.zoldleo.embers.recipe.context;

import java.util.List;

import hu.zoldleo.embers.recipe.base.IAlchemyRecipe.PedestalContents;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

public class AlchemyContext implements RecipeInput {
	public ItemStack tablet;
	public List<PedestalContents> contents;
	public long seed;

	public AlchemyContext(ItemStack tablet, List<PedestalContents> contents, long seed) {
		this.tablet = tablet;
		this.contents = contents;
		this.seed = seed;
	}

	@Override
	@Deprecated
	public boolean isEmpty() {
		return true;
	}

	@Override
	@Deprecated
	public @NotNull ItemStack getItem(int pSlot) {
		return ItemStack.EMPTY;
	}

    @Override
    public int size() {
        return 0;
    }
}