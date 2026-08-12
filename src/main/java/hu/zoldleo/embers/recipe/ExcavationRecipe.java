package hu.zoldleo.embers.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import hu.zoldleo.embers.RegistryManager;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ExcavationRecipe extends BoringRecipe {
	public static final Serializer SERIALIZER = new Serializer();

	public ExcavationRecipe(BoringRecipe recipe) {
		super(recipe.result, recipe.minHeight, recipe.maxHeight, recipe.dimensions, recipe.biomes, recipe.requiredBlock, recipe.amountRequired, recipe.chance);
	}

	@Override
	public @NotNull RecipeType<?> getType() {
		return RegistryManager.EXCAVATION.get();
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	public static class Serializer implements RecipeSerializer<ExcavationRecipe> {
        public static final MapCodec<ExcavationRecipe> CODEC = BoringRecipe.SERIALIZER.codec().xmap(ExcavationRecipe::new, x -> x);
        public static final StreamCodec<RegistryFriendlyByteBuf, ExcavationRecipe> STREAM_CODEC = BoringRecipe.SERIALIZER.streamCodec().map(ExcavationRecipe::new, x -> x);

        @Override
        public @NotNull MapCodec<ExcavationRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, ExcavationRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}