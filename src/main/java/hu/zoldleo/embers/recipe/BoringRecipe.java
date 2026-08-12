package hu.zoldleo.embers.recipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hu.zoldleo.embers.recipe.base.IBoringRecipe;
import hu.zoldleo.embers.recipe.context.BoringContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;
import hu.zoldleo.embers.util.WeightedItemStack;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BoringRecipe implements IBoringRecipe {
	public static final Serializer SERIALIZER = new Serializer();

	public final WeightedItemStack result;
	public final int minHeight;
	public final int maxHeight;
	public final List<ResourceLocation> dimensions;
	public final List<ResourceLocation> biomes;
	public final TagKey<Block> requiredBlock;
	public final int amountRequired;
	public final double chance;

    public BoringRecipe(ItemStack result, int weight, int minHeight, int maxHeight, List<ResourceLocation> dimensions, List<ResourceLocation> biomes, TagKey<Block> requiredBlock, int amountRequired, double chance) {
        this(new WeightedItemStack(result, weight), minHeight, maxHeight, dimensions, biomes, requiredBlock, amountRequired, chance);
    }

	public BoringRecipe(WeightedItemStack result, int minHeight, int maxHeight, List<ResourceLocation> dimensions, List<ResourceLocation> biomes, TagKey<Block> requiredBlock, int amountRequired, double chance) {
		this.result = result;
		this.maxHeight = maxHeight;
		this.minHeight = minHeight;
		this.dimensions = dimensions;
		this.biomes = biomes;
		this.requiredBlock = requiredBlock;
		this.amountRequired = amountRequired;
		this.chance = chance;
	}

	@Override
	public boolean matches(@NotNull BoringContext context, @NotNull Level pLevel) {
		if (!dimensions.isEmpty() && !dimensions.contains(context.dimension))
			return false;
		if (!biomes.isEmpty() && !biomes.contains(context.biome))
			return false;
		if (amountRequired > 0) {
			int amountLeft = amountRequired;
			for (BlockState state : context.blocks) {
				if (state.is(requiredBlock))
					amountLeft--;
				if (amountLeft < 1)
					break;
			}
			if (amountLeft > 0)
				return false;
		}
		return context.height >= minHeight && context.height <= maxHeight;
	}

	@Override
	public WeightedItemStack getOutput(BoringContext context) {
		return result;
	}

	@Override
	public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registry) {
		return result.getStack();
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public int getMinHeight() {
		return minHeight;
	}

	@Override
	public int getMaxHeight() {
		return maxHeight;
	}

	@Override
	public Collection<ResourceLocation> getDimensions() {
		return dimensions;
	}

	@Override
	public Collection<ResourceLocation> getBiomes() {
		return biomes;
	}

	@Override
	public double getChance() {
		return chance;
	}

	@Override
	public WeightedItemStack getDisplayOutput() {
		return result;
	}

	public List<ItemStack> getDisplayInput() {
		List<ItemStack> list = Lists.newArrayList();
		for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(requiredBlock))
			list.add(new ItemStack(holder.value(), amountRequired));
		return list;
	}

	public static class Serializer implements RecipeSerializer<BoringRecipe> {
        private static final MapCodec<BoringRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ItemStack.CODEC.fieldOf("output").forGetter(recipe -> recipe.result.getStack()),
                Codec.INT.fieldOf("weight").forGetter(recipe -> recipe.result.getWeight().asInt()),
                Codec.INT.fieldOf("min_height").forGetter(BoringRecipe::getMinHeight),
                Codec.INT.fieldOf("max_height").forGetter(BoringRecipe::getMaxHeight),
                ResourceLocation.CODEC.listOf().fieldOf("dimensions").forGetter(recipe -> recipe.dimensions),
                ResourceLocation.CODEC.listOf().fieldOf("biomes").forGetter(recipe -> recipe.biomes),
                TagKey.codec(Registries.BLOCK).fieldOf("required_block").forGetter(recipe -> recipe.requiredBlock),
                Codec.INT.fieldOf("amount").forGetter(recipe -> recipe.amountRequired),
                Codec.DOUBLE.fieldOf("chance").forGetter(recipe -> recipe.chance)
        ).apply(instance, BoringRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, BoringRecipe> STREAM_CODEC = new StreamCodec<>() {
            public @NotNull BoringRecipe decode(@NotNull RegistryFriendlyByteBuf buffer) {
                ItemStack stack = ItemStack.STREAM_CODEC.decode(buffer);
                int weight = buffer.readVarInt();
                int minHeight = buffer.readVarInt();
                int maxHeight = buffer.readVarInt();
                List<ResourceLocation> dimensions = buffer.readCollection(ArrayList::new, FriendlyByteBuf::readResourceLocation);
                List<ResourceLocation> biomes = buffer.readCollection(ArrayList::new, FriendlyByteBuf::readResourceLocation);
                ResourceLocation requiredBlock = buffer.readResourceLocation();
                int amountRequired = buffer.readVarInt();
                double chance = buffer.readDouble();
                return new BoringRecipe(stack, weight, minHeight, maxHeight, dimensions, biomes, BlockTags.create(requiredBlock), amountRequired, chance);
            }

            public void encode(@NotNull RegistryFriendlyByteBuf buffer, BoringRecipe recipe) {
                ItemStack.STREAM_CODEC.encode(buffer, recipe.result.getStack());
                buffer.writeVarInt(recipe.result.getWeight().asInt());
                buffer.writeVarInt(recipe.minHeight);
                buffer.writeVarInt(recipe.maxHeight);
                buffer.writeCollection(recipe.dimensions, FriendlyByteBuf::writeResourceLocation);
                buffer.writeCollection(recipe.biomes, FriendlyByteBuf::writeResourceLocation);
                buffer.writeResourceLocation(recipe.requiredBlock.location());
                buffer.writeVarInt(recipe.amountRequired);
                buffer.writeDouble(recipe.chance);
            }
        };

        @Override
        public @NotNull MapCodec<BoringRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, BoringRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
