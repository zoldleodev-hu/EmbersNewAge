package hu.zoldleo.embers.recipe.context;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class BlockStateContext implements RecipeInput {
	public BlockState state;

	public BlockStateContext(BlockState state) {
		this.state = state;
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