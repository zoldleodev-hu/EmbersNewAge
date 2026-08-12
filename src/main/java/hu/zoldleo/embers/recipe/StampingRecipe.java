package hu.zoldleo.embers.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hu.zoldleo.embers.recipe.base.IStampingRecipe;
import hu.zoldleo.embers.recipe.context.StampingContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;

import com.mojang.datafixers.util.Either;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class StampingRecipe implements IStampingRecipe {
	public static final Serializer SERIALIZER = new Serializer();

	public final Ingredient stamp;
	public final Ingredient input;
	public final SizedFluidIngredient fluid;

	public final Either<ItemStack, TagAmount> output;

	public StampingRecipe(Ingredient stamp, Ingredient input, SizedFluidIngredient fluid, TagAmount output) {
		this(stamp, input, fluid, Either.right(output));
	}

	public StampingRecipe(Ingredient stamp, Ingredient input, SizedFluidIngredient fluid, ItemStack output) {
		this(stamp, input, fluid, Either.left(output));
	}

	public StampingRecipe(Ingredient stamp, Ingredient input, SizedFluidIngredient fluid, Either<ItemStack, TagAmount> output) {
		this.stamp = stamp;
		this.input = input;
		this.fluid = fluid;
		this.output = output;
	}

	@Override
	public boolean matches(StampingContext context, @NotNull Level pLevel) {
		for (int i = 0; i < context.size(); i++)
			if (input.test(context.getItem(i)))
                return stamp.test(context.stamp) && fluid.test(context.fluids.getFluidInTank(0));
		return false;
	}

    @Override
	public ItemStack getOutput(RecipeWrapper context) {
		return getResultItem();
	}

	@Override
	public @NotNull ItemStack assemble(StampingContext context, HolderLookup.@NotNull Provider registry) {
		for (int i = 0; i < context.size(); i++) {
			if (input.test(context.getItem(i))) {
				context.getItem(i).shrink(1);
				break;
			}
		}
		for (FluidStack stack : fluid.getFluids()) {
			if (fluid.test(context.fluids.drain(stack, IFluidHandler.FluidAction.SIMULATE))) {
				context.fluids.drain(stack, IFluidHandler.FluidAction.EXECUTE);
				break;
			}
		}
		return this.getOutput(context);
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public ItemStack getResultItem() {
		if (output.left().isPresent())
			return output.left().get();
		return new ItemStack(Misc.getTaggedItem(output.right().get().tag), output.right().get().amount);
	}

	@Override
	public SizedFluidIngredient getDisplayInputFluid() {
		return fluid;
	}

	@Override
	public Ingredient getDisplayInput() {
		return input;
	}

	@Override
	public Ingredient getDisplayStamp() {
		return stamp;
	}

	public static class TagAmount {
        public static final Codec<TagAmount> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                TagKey.hashedCodec(Registries.ITEM).fieldOf("tag").forGetter(x -> x.tag),
                Codec.INT.fieldOf("amount").forGetter(x -> x.amount)
        ).apply(instance, TagAmount::new));

        public static final StreamCodec<ByteBuf, TagAmount> STREAM_CODEC = StreamCodec.composite(
                Misc.tagKeyStreamCodec(Registries.ITEM), x -> x.tag,
                ByteBufCodecs.INT, x -> x.amount,
                TagAmount::new
        );

		public TagKey<Item> tag;
		public int amount;

		public TagAmount(TagKey<Item> tag, int amount) {
			this.tag = tag;
			this.amount = amount;
		}
	}

	public static class Serializer implements RecipeSerializer<StampingRecipe> {
        private static final MapCodec<StampingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("stamp").forGetter(recipe -> recipe.stamp),
                Ingredient.CODEC.optionalFieldOf("input", Ingredient.EMPTY).forGetter(recipe -> recipe.input),
                SizedFluidIngredient.FLAT_CODEC.optionalFieldOf("fluid", Misc.EMPTY_FLUID_INGREDIENT).forGetter(recipe -> recipe.fluid),
                Codec.either(ItemStack.CODEC, TagAmount.CODEC).fieldOf("output").forGetter(recipe -> recipe.output)
        ).apply(instance, StampingRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, StampingRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.stamp,
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.input,
                SizedFluidIngredient.STREAM_CODEC, recipe -> recipe.fluid,
                ByteBufCodecs.either(ItemStack.STREAM_CODEC, TagAmount.STREAM_CODEC), recipe -> recipe.output,
                StampingRecipe::new
        );

        @Override
        public @NotNull MapCodec<StampingRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, StampingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}