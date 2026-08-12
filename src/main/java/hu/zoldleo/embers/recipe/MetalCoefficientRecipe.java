package hu.zoldleo.embers.recipe;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hu.zoldleo.embers.recipe.base.IMetalCoefficientRecipe;
import hu.zoldleo.embers.recipe.context.BlockStateContext;
import hu.zoldleo.embers.util.Misc;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class MetalCoefficientRecipe implements IMetalCoefficientRecipe {

	public static final Serializer SERIALIZER = new Serializer();

	public final TagKey<Block> blockTag;
	public final double coefficient;

	public MetalCoefficientRecipe(TagKey<Block> blockTag, double coefficient) {
		this.blockTag = blockTag;
		this.coefficient = coefficient;
	}

	@Override
	public boolean matches(BlockStateContext context, @NotNull Level pLevel) {
		return context.state.is(blockTag);
	}

	public double getCoefficient(BlockStateContext context) {
		return coefficient;
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public List<ItemStack> getDisplayInput() {
		List<ItemStack> list = Lists.newArrayList();
		for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(blockTag)) {
			list.add(new ItemStack(holder.value()));
		}
		return list;
	}

	@Override
	public double getDisplayCoefficient() {
		return coefficient;
	}

	public static class Serializer implements RecipeSerializer<MetalCoefficientRecipe> {
        private static final MapCodec<MetalCoefficientRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                TagKey.hashedCodec(Registries.BLOCK).fieldOf("block_tag").forGetter(recipe -> recipe.blockTag),
                Codec.DOUBLE.fieldOf("coefficient").forGetter(recipe -> recipe.coefficient)
        ).apply(instance, MetalCoefficientRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, MetalCoefficientRecipe> STREAM_CODEC = StreamCodec.composite(
                Misc.tagKeyStreamCodec(Registries.BLOCK), recipe -> recipe.blockTag,
                ByteBufCodecs.DOUBLE, recipe -> recipe.coefficient,
                MetalCoefficientRecipe::new
        );

        @Override
        public @NotNull MapCodec<MetalCoefficientRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, MetalCoefficientRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
