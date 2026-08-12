package hu.zoldleo.embers.recipe.context;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public class FluidHandlerContext implements RecipeInput {
	public IFluidHandler fluid;

	public FluidHandlerContext(IFluidHandler fluid) {
		this.fluid = fluid;
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