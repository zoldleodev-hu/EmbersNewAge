package hu.zoldleo.embers.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hu.zoldleo.embers.recipe.base.IGaseousFuelRecipe;
import hu.zoldleo.embers.recipe.context.FluidHandlerContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class GaseousFuelRecipe implements IGaseousFuelRecipe {
	public static final Serializer SERIALIZER = new Serializer();

	public final SizedFluidIngredient input;
	public final int burnTime;
	public final double powerMultiplier;

	public GaseousFuelRecipe(SizedFluidIngredient input, int burnTime, double powerMultiplier) {
		this.input = input;
		this.burnTime = burnTime;
		this.powerMultiplier = powerMultiplier;
	}

	@Override
	public boolean matches(@NotNull FluidHandlerContext context, @NotNull Level pLevel) {
		for (FluidStack stack : input.getFluids()) {
			if (input.test(context.fluid.drain(stack, IFluidHandler.FluidAction.SIMULATE))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int getBurnTime(FluidHandlerContext context) {
		return burnTime;
	}

	@Override
	public double getPowerMultiplier(FluidHandlerContext context) {
		return powerMultiplier;
	}

	@Override
	public int process(FluidHandlerContext context, int amount) {
		int trueAmount = amount;
		for (FluidStack stack : input.getFluids()) {
			FluidStack drainStack = stack.copyWithAmount(stack.getAmount() * amount);
			if (input.test(context.fluid.drain(drainStack, IFluidHandler.FluidAction.SIMULATE))) {
				trueAmount = context.fluid.drain(drainStack, IFluidHandler.FluidAction.EXECUTE).getAmount() / stack.getAmount();
				break;
			}
		}
		return burnTime * trueAmount;
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
	public int getDisplayBurnTime() {
		return burnTime;
	}

	@Override
	public double getDisplayMultiplier() {
		return powerMultiplier;
	}

	public static class Serializer implements RecipeSerializer<GaseousFuelRecipe> {
        private static final MapCodec<GaseousFuelRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                SizedFluidIngredient.FLAT_CODEC.fieldOf("input").forGetter(recipe -> recipe.input),
                Codec.INT.fieldOf("burn_time").forGetter(recipe -> recipe.burnTime),
                Codec.DOUBLE.fieldOf("power_multiplier").forGetter(recipe -> recipe.powerMultiplier)
        ).apply(instance, GaseousFuelRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, GaseousFuelRecipe> STREAM_CODEC = StreamCodec.composite(
                SizedFluidIngredient.STREAM_CODEC, recipe -> recipe.input,
                ByteBufCodecs.INT, recipe -> recipe.burnTime,
                ByteBufCodecs.DOUBLE, recipe -> recipe.powerMultiplier,
                GaseousFuelRecipe::new
        );

        @Override
        public @NotNull MapCodec<GaseousFuelRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, GaseousFuelRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}