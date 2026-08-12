package hu.zoldleo.embers.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hu.zoldleo.embers.recipe.base.AlchemyRecipeBase;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import hu.zoldleo.embers.RegistryManager;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class AlchemyRecipeForBabies extends AlchemyRecipeBase {
	public static final Serializer SERIALIZER = new Serializer();

	public AlchemyRecipeForBabies(Ingredient tablet, List<Ingredient> aspects, List<Ingredient> inputs, ItemStack output, ItemStack failure) {
		super(tablet, aspects, inputs, output, failure);
	}

	public Long cachedSeed = null;
	public ArrayList<Ingredient> code = null;

	@Override
	public ArrayList<Ingredient> getCode(long seed, ResourceLocation id) {
		if (cachedSeed == null || cachedSeed != seed) {
			int incr = 0;
			boolean incorrectCode = true;
			while (incorrectCode) {
				code = super.getCode(seed + incr, id);
				incorrectCode = false;
				for (Ingredient ingredient : aspects) {
					//only return this recipe if it contains all possible aspecti
					if (!code.contains(ingredient)) {
						incorrectCode = true;
						break;
					}
				}
				incr++;
			}
			cachedSeed = seed;
		}
		return code;
	}

    @Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	public static class Serializer implements RecipeSerializer<AlchemyRecipeForBabies> {
        public static final MapCodec<AlchemyRecipeForBabies> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("tablet").forGetter(AlchemyRecipeForBabies::getCenterInput),
                Ingredient.CODEC.listOf().fieldOf("aspects").forGetter(AlchemyRecipeForBabies::getAspects),
                Ingredient.CODEC.listOf().fieldOf("inputs").forGetter(AlchemyRecipeForBabies::getInputs),
                ItemStack.CODEC.fieldOf("output").forGetter(AlchemyRecipeForBabies::getResultItem),
                ItemStack.CODEC.optionalFieldOf("failure", new ItemStack(RegistryManager.ALCHEMICAL_WASTE.get())).forGetter(AlchemyRecipeForBabies::getfailureItem)
        ).apply(instance, AlchemyRecipeForBabies::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, AlchemyRecipeForBabies> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, AlchemyRecipeForBabies::getCenterInput,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), AlchemyRecipeForBabies::getAspects,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), AlchemyRecipeForBabies::getInputs,
                ItemStack.STREAM_CODEC, AlchemyRecipeForBabies::getResultItem,
                ItemStack.OPTIONAL_STREAM_CODEC.map(x -> x.isEmpty() ? new ItemStack(RegistryManager.ALCHEMICAL_WASTE.get()) : x, Function.identity()), AlchemyRecipeForBabies::getfailureItem,
                AlchemyRecipeForBabies::new
        );

        @Override
        public @NotNull MapCodec<AlchemyRecipeForBabies> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, AlchemyRecipeForBabies> streamCodec() {
            return STREAM_CODEC;
        }
    }
}