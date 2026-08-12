package hu.zoldleo.embers.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hu.zoldleo.embers.recipe.base.ICatalysisCombustionRecipe;
import hu.zoldleo.embers.recipe.context.CatalysisCombustionContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class CatalysisCombustionRecipe implements ICatalysisCombustionRecipe {
	public static final Serializer SERIALIZER = new Serializer();

	public final Ingredient ingredient;
	public final Ingredient machine;
	public final int burnTime;
	public final double multiplier;

	public CatalysisCombustionRecipe(Ingredient ingredient, Ingredient machine, int burnTime, double multiplier) {
		this.ingredient = ingredient;
		this.machine = machine;
		this.burnTime = burnTime;
		this.multiplier = multiplier;
	}

	@Override
	public boolean matches(CatalysisCombustionContext context, @NotNull Level pLevel) {
		if (machine.test(context.machine)) {
			for (int i = 0; i < context.size(); i++) {
				if (ingredient.test(context.getItem(i)))
					return true;
			}
		}
		return false;
	}

	@Override
	public int getBurnTIme(CatalysisCombustionContext context) {
		return burnTime;
	}

	@Override
	public double getmultiplier(CatalysisCombustionContext context) {
		return multiplier;
	}

	@Override
	public int process(CatalysisCombustionContext context) {
		for (int i = 0; i < context.size(); i++) {
			if (ingredient.test(context.getItem(i))) {
				context.getItem(i).shrink(1);
				break;
			}
		}
		return burnTime;
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
	public Ingredient getDisplayMachine() {
		return machine;
	}

	@Override
	public int getDisplayTime() {
		return burnTime;
	}

	@Override
	public double getDisplayMultiplier() {
		return multiplier;
	}

	public static class Serializer implements RecipeSerializer<CatalysisCombustionRecipe> {
        private static final MapCodec<CatalysisCombustionRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("input").forGetter(recipe -> recipe.ingredient),
                Ingredient.CODEC.fieldOf("machine").forGetter(recipe -> recipe.machine),
                Codec.INT.fieldOf("burn_time").forGetter(recipe -> recipe.burnTime),
                Codec.DOUBLE.fieldOf("multiplier").forGetter(recipe -> recipe.multiplier)
        ).apply(instance, CatalysisCombustionRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, CatalysisCombustionRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.ingredient,
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.machine,
                ByteBufCodecs.INT, recipe -> recipe.burnTime,
                ByteBufCodecs.DOUBLE, recipe -> recipe.multiplier,
                CatalysisCombustionRecipe::new
        );

        @Override
        public @NotNull MapCodec<CatalysisCombustionRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, CatalysisCombustionRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}