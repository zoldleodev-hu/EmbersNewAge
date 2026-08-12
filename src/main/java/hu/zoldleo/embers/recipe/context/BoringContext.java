package hu.zoldleo.embers.recipe.context;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class BoringContext implements RecipeInput {
	public ResourceLocation dimension;
	public ResourceLocation biome;
	public int height;
	public BlockState[] blocks;

	public BoringContext(ResourceLocation dimension, ResourceLocation biome, int height, BlockState[] blocks) {
		this.dimension = dimension;
		this.biome = biome;
		this.height = height;
		this.blocks = blocks;
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