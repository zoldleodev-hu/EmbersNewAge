package hu.zoldleo.embers.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hu.zoldleo.embers.recipe.base.IBoilingRecipe;
import hu.zoldleo.embers.recipe.context.FluidHandlerContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;

import hu.zoldleo.embers.util.FluidOutput;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class BoilingRecipe implements IBoilingRecipe {
	public static final Serializer SERIALIZER = new Serializer();

	public final SizedFluidIngredient input;
	public final FluidOutput output;

	public BoilingRecipe(SizedFluidIngredient input, FluidOutput output) {
		this.input = input;
		this.output = output;
	}

	@Override
	public boolean matches(@NotNull FluidHandlerContext context, @NotNull Level pLevel) {
		for (FluidStack stack : input.getFluids())
			if (input.test(context.fluid.drain(stack, IFluidHandler.FluidAction.SIMULATE)))
				return true;
		return false;
	}

	@Override
	public FluidStack getOutput(FluidHandlerContext context) {
		return output.getStack();
	}

	@Override
	public FluidStack process(FluidHandlerContext context, int amount) {
		int trueAmount = amount;
		for (FluidStack stack : input.getFluids()) {
			FluidStack drainStack = stack.copyWithAmount(stack.getAmount() * amount);
			if (input.test(context.fluid.drain(drainStack, IFluidHandler.FluidAction.SIMULATE))) {
				trueAmount = context.fluid.drain(drainStack, IFluidHandler.FluidAction.EXECUTE).getAmount() / stack.getAmount();
				break;
			}
		}
		return output.getStack().copyWithAmount(output.getStack().getAmount() * trueAmount);
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public SizedFluidIngredient getDisplayInput() {
		return input;
	}

	@Override
	public FluidStack getDisplayOutput() {
		return output.getStack();
	}

	public static class Serializer implements RecipeSerializer<BoilingRecipe> {
        private static final MapCodec<BoilingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                SizedFluidIngredient.FLAT_CODEC.fieldOf("input").forGetter(recipe -> recipe.input),
                FluidOutput.CODEC.fieldOf("output").forGetter(recipe -> recipe.output)
        ).apply(instance, BoilingRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, BoilingRecipe> STREAM_CODEC = StreamCodec.composite(
                SizedFluidIngredient.STREAM_CODEC, recipe -> recipe.input,
                FluidOutput.STREAM_CODEC, recipe -> recipe.output,
                BoilingRecipe::new
        );

        @Override
        public @NotNull MapCodec<BoilingRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, BoilingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}