package hu.zoldleo.embers.recipe;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.recipe.base.IDawnstoneAnvilRecipe;
import hu.zoldleo.embers.recipe.base.IVisuallySplitRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.datagen.EmbersItemTags;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class AnvilRepairMateriaRecipe implements IDawnstoneAnvilRecipe, IVisuallySplitRecipe<IDawnstoneAnvilRecipe> {
	public static final Serializer SERIALIZER = new Serializer();

	public static List<IDawnstoneAnvilRecipe> visualRecipes = new ArrayList<>();

	@Override
	public boolean matches(RecipeInput context, @NotNull Level pLevel) {
		ItemStack tool = context.getItem(0);
		return tool.isRepairable() && tool.isDamaged() && !tool.is(EmbersItemTags.MATERIA_BLACKLIST) && context.getItem(1).is(RegistryManager.ISOLATED_MATERIA);
	}

	@Override
	public List<ItemStack> getOutput(RecipeInput context) {
		ItemStack result = context.getItem(0).copy();
		result.setDamageValue(Math.max(0, result.getDamageValue() - result.getMaxDamage()));
		return List.of(result);
	}

	@Override
	public List<IDawnstoneAnvilRecipe> getVisualRecipes() {
		visualRecipes.clear();
		for (Holder<Item> holder : BuiltInRegistries.ITEM.asHolderIdMap()) {
			ItemStack toolStack = new ItemStack(holder.value());
			if (toolStack.isRepairable() && !toolStack.is(EmbersItemTags.MATERIA_BLACKLIST)) {
				ItemStack brokenTool = toolStack.copy();
				brokenTool.setDamageValue(brokenTool.getMaxDamage() / 2);
				visualRecipes.add(new AnvilDisplayRecipe(List.of(toolStack), List.of(brokenTool), Ingredient.of(RegistryManager.ISOLATED_MATERIA)));
			}
		}
		return visualRecipes;
	}

	@Override
	public List<ItemStack> getDisplayInputBottom() {
		return List.of();
	}

	@Override
	public List<ItemStack> getDisplayInputTop() {
		return List.of();
	}

	@Override
	public List<ItemStack> getDisplayOutput() {
		return List.of();
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AnvilRepairMateriaRecipe;
    }

	public static class Serializer implements RecipeSerializer<AnvilRepairMateriaRecipe> {
        public static final MapCodec<AnvilRepairMateriaRecipe> CODEC = MapCodec.unit(new AnvilRepairMateriaRecipe());
        public static final StreamCodec<RegistryFriendlyByteBuf, AnvilRepairMateriaRecipe> STREAM_CODEC = StreamCodec.unit(new AnvilRepairMateriaRecipe());

        @Override
        public @NotNull MapCodec<AnvilRepairMateriaRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, AnvilRepairMateriaRecipe> streamCodec() {
            return STREAM_CODEC;
        }
	}
}