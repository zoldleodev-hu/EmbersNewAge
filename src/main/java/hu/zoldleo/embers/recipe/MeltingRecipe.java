package hu.zoldleo.embers.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hu.zoldleo.embers.recipe.base.IMeltingRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import hu.zoldleo.embers.util.FluidOutput;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class MeltingRecipe implements IMeltingRecipe {
	public static final Serializer SERIALIZER = new Serializer();

	public final Ingredient ingredient;
	public final FluidOutput output;
	public final FluidOutput bonus;

	public MeltingRecipe(Ingredient ingredient, FluidOutput output, FluidOutput bonus) {
		this.ingredient = ingredient;
		this.output = output;
		this.bonus = bonus;
	}

	public MeltingRecipe(Ingredient ingredient, FluidOutput output) {
		this(ingredient, output, FluidOutput.EMPTY);
	}

	@Override
	public boolean matches(RecipeInput context, @NotNull Level pLevel) {
		for (int i = 0; i < context.size(); i++)
			if (ingredient.test(context.getItem(i)))
				return true;
		return false;
	}

	@Override
	public FluidStack getOutput(RecipeInput context) {
		return output.getStack();
	}

	@Override
	public FluidStack getBonus() {
		return bonus.getStack();
	}

	@Override
	public FluidStack process(RecipeInput context) {
		for (int i = 0; i < context.size(); i++) {
			if (ingredient.test(context.getItem(i))) {
				context.getItem(i).shrink(1);
				break;
			}
		}
		return output.getStack();
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public FluidStack getDisplayOutput() {
		return output.getStack();
	}

	@Override
	public Ingredient getDisplayInput() {
		return ingredient;
	}

	public static class Serializer implements RecipeSerializer<MeltingRecipe> {
        private static final MapCodec<MeltingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("inputs").forGetter(recipe -> recipe.ingredient),
                FluidOutput.CODEC.fieldOf("output").forGetter(recipe -> recipe.output),
                FluidOutput.CODEC.optionalFieldOf("bonus", FluidOutput.EMPTY).forGetter(recipe -> recipe.bonus)
        ).apply(instance, MeltingRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, MeltingRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.ingredient,
                FluidOutput.STREAM_CODEC, recipe -> recipe.output,
                FluidOutput.STREAM_CODEC, recipe -> recipe.bonus,
                MeltingRecipe::new
        );

        @Override
        public @NotNull MapCodec<MeltingRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, MeltingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}