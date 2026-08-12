package hu.zoldleo.embers.recipe;

import java.util.List;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.recipe.base.IDawnstoneAnvilRecipe;
import hu.zoldleo.embers.recipe.base.IVisuallySplitRecipe;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

import hu.zoldleo.embers.api.augment.AugmentUtil;
import hu.zoldleo.embers.api.augment.IAugment;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class AnvilAugmentRemoveRecipe implements IDawnstoneAnvilRecipe, IVisuallySplitRecipe<IDawnstoneAnvilRecipe> {
	public static final Serializer SERIALIZER = new Serializer();

	@Override
	public boolean matches(RecipeInput context, @NotNull Level pLevel) {
		if (context.getItem(1).isEmpty())
			for (Holder<IAugment> augment : AugmentUtil.getAugments(context.getItem(0)))
				if (augment.value().canRemove())
					return true;
		return false;
	}

	@Override
	public List<ItemStack> getOutput(RecipeInput context) {
		ItemStack tool = context.getItem(0).copy();
		List<ItemStack> outputs = AugmentUtil.removeAllAugments(tool);
		outputs.add(tool);
		return outputs;
	}

	@Override
	public List<IDawnstoneAnvilRecipe> getVisualRecipes() {
		return List.of();
	}

	@Override
	public List<ItemStack> getDisplayInputBottom() {
		return List.of();
	}

	@Override
	public List<ItemStack> getDisplayInputTop() {
		return List.of();
	}

	@Override
	public List<ItemStack> getDisplayOutput() {
		return List.of();
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AnvilAugmentRemoveRecipe;
    }

	public static class Serializer implements RecipeSerializer<AnvilAugmentRemoveRecipe> {
        public static final MapCodec<AnvilAugmentRemoveRecipe> CODEC = MapCodec.unit(new AnvilAugmentRemoveRecipe());
        public static final StreamCodec<RegistryFriendlyByteBuf, AnvilAugmentRemoveRecipe> STREAM_CODEC = StreamCodec.unit(new AnvilAugmentRemoveRecipe());

        @Override
        public @NotNull MapCodec<AnvilAugmentRemoveRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, AnvilAugmentRemoveRecipe> streamCodec() {
            return STREAM_CODEC;
        }
	}
}