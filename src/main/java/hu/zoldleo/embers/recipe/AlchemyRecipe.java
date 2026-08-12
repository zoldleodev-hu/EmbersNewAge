package hu.zoldleo.embers.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.recipe.base.AlchemyRecipeBase;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public class AlchemyRecipe extends AlchemyRecipeBase {
	public static final Serializer SERIALIZER = new Serializer();

	public AlchemyRecipe(Ingredient tablet, List<Ingredient> aspects, List<Ingredient> inputs, ItemStack output, ItemStack failure) {
        super(tablet, aspects, inputs, output, failure);
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	public static class Serializer implements RecipeSerializer<AlchemyRecipe> {
        public static final MapCodec<AlchemyRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("tablet").forGetter(AlchemyRecipe::getCenterInput),
                Ingredient.CODEC.listOf().fieldOf("aspects").forGetter(AlchemyRecipe::getAspects),
                Ingredient.CODEC.listOf().fieldOf("inputs").forGetter(AlchemyRecipe::getInputs),
                ItemStack.CODEC.fieldOf("output").forGetter(AlchemyRecipe::getResultItem),
                ItemStack.CODEC.optionalFieldOf("failure", new ItemStack(RegistryManager.ALCHEMICAL_WASTE.get())).forGetter(AlchemyRecipe::getfailureItem)
        ).apply(instance, AlchemyRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, AlchemyRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, AlchemyRecipe::getCenterInput,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), AlchemyRecipe::getAspects,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), AlchemyRecipe::getInputs,
                ItemStack.STREAM_CODEC, AlchemyRecipe::getResultItem,
                ItemStack.OPTIONAL_STREAM_CODEC.map(x -> x.isEmpty() ? new ItemStack(RegistryManager.ALCHEMICAL_WASTE.get()) : x, Function.identity()), AlchemyRecipe::getfailureItem,
                AlchemyRecipe::new
        );

        @Override
        public @NotNull MapCodec<AlchemyRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, AlchemyRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}