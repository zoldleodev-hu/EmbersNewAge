package hu.zoldleo.embers.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hu.zoldleo.embers.recipe.base.IEmberActivationRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class EmberActivationRecipe implements IEmberActivationRecipe {
	public static final Serializer SERIALIZER = new Serializer();

	public final Ingredient ingredient;
	public final int ember;

	public EmberActivationRecipe(Ingredient ingredient, int ember) {
		this.ingredient = ingredient;
		this.ember = ember;
	}

	@Override
	public boolean matches(RecipeInput context, @NotNull Level pLevel) {
		for (int i = 0; i < context.size(); i++)
			if (ingredient.test(context.getItem(i)))
				return true;
		return false;
	}

	@Override
	public int getOutput(RecipeInput context) {
		return ember;
	}

	@Override
	public int process(RecipeInput context) {
		for (int i = 0; i < context.size(); i++) {
			if (ingredient.test(context.getItem(i))) {
				context.getItem(i).shrink(1);
				break;
			}
		}
		return ember;
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public Ingredient getDisplayInput() {
		return ingredient;
	}

	@Override
	public int getDisplayOutput() {
		return ember;
	}

	public static class Serializer implements RecipeSerializer<EmberActivationRecipe> {
        private static final MapCodec<EmberActivationRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("input").forGetter(recipe -> recipe.ingredient),
                Codec.INT.fieldOf("ember").forGetter(recipe -> recipe.ember)
        ).apply(instance, EmberActivationRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, EmberActivationRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.ingredient,
                ByteBufCodecs.INT, recipe -> recipe.ember,
                EmberActivationRecipe::new
        );

        @Override
        public @NotNull MapCodec<EmberActivationRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, EmberActivationRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}