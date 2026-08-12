package hu.zoldleo.embers.recipe;

import java.util.HashSet;
import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hu.zoldleo.embers.recipe.base.IMixingRecipe;
import hu.zoldleo.embers.recipe.context.MixingContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;

import hu.zoldleo.embers.util.FluidOutput;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class MixingRecipe implements IMixingRecipe {
	public static final Serializer SERIALIZER = new Serializer();

	public final List<SizedFluidIngredient> inputs;
	public final FluidOutput output;

	public MixingRecipe(List<SizedFluidIngredient> inputs, FluidOutput output) {
		this.inputs = inputs;
		this.output = output;
	}

	@Override
	public boolean matches(MixingContext context, @NotNull Level pLevel) {
        HashSet<SizedFluidIngredient> remaining = new HashSet<>(inputs);

		for (IFluidHandler handler : context.fluids) {
			boolean matched = false;
			for (SizedFluidIngredient fluid : remaining) {
				for (FluidStack stack : fluid.getFluids()) {
					if (fluid.test(handler.drain(stack, IFluidHandler.FluidAction.SIMULATE))) {
						remaining.remove(fluid);
						matched = true;
						break;
					}
				}
				if (matched)
					break;
			}
			if (!matched && !handler.drain(1, IFluidHandler.FluidAction.SIMULATE).isEmpty())
				return false;
		}
		return remaining.isEmpty();
	}

	@Override
	public FluidStack getOutput(MixingContext context) {
		return output.getStack();
	}

	@Override
	public FluidStack process(MixingContext context) {
        HashSet<SizedFluidIngredient> remaining = new HashSet<>(inputs);

		for (IFluidHandler handler : context.fluids) {
			for (SizedFluidIngredient fluid : remaining) {
				boolean matched = false;
				for (FluidStack stack : fluid.getFluids()) {
					if (fluid.test(handler.drain(stack, IFluidHandler.FluidAction.SIMULATE))) {
						handler.drain(stack, IFluidHandler.FluidAction.EXECUTE);
						matched = true;
						break;
					}
				}
				if (matched)
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
	public List<SizedFluidIngredient> getDisplayInputFluids() {
		return inputs;
	}

	@Override
	public FluidStack getDisplayOutput() {
		return output.getStack();
	}

	public static class Serializer implements RecipeSerializer<MixingRecipe> {
        private static final MapCodec<MixingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                SizedFluidIngredient.FLAT_CODEC.listOf().fieldOf("inputs").forGetter(recipe -> recipe.inputs),
                FluidOutput.CODEC.fieldOf("output").forGetter(recipe -> recipe.output)
        ).apply(instance, MixingRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, MixingRecipe> STREAM_CODEC = StreamCodec.composite(
                SizedFluidIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), recipe -> recipe.inputs,
                FluidOutput.STREAM_CODEC, recipe -> recipe.output,
                MixingRecipe::new
        );

        @Override
        public @NotNull MapCodec<MixingRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, MixingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}