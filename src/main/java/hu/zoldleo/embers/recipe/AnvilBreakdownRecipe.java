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

import hu.zoldleo.embers.api.augment.AugmentUtil;
import hu.zoldleo.embers.datagen.EmbersItemTags;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class AnvilBreakdownRecipe implements IDawnstoneAnvilRecipe, IVisuallySplitRecipe<IDawnstoneAnvilRecipe> {
	public static final Serializer SERIALIZER = new Serializer();

	public static List<IDawnstoneAnvilRecipe> visualRecipes = new ArrayList<>();
    public static Ingredient blacklist = Ingredient.of(EmbersItemTags.BREAKDOWN_BLACKLIST);

	@Override
	public boolean matches(RecipeInput context, @NotNull Level pLevel) {
		ItemStack tool = context.getItem(0);
		return tool.isRepairable() && !blacklist.test(tool) && context.getItem(1).isEmpty() && !AugmentUtil.hasHeat(tool) && !Misc.getRepairIngredient(tool.getItem()).hasNoItems();
	}

	@Override
	public List<ItemStack> getOutput(RecipeInput context) {
		Ingredient repairMaterial = Misc.getRepairIngredient(context.getItem(0).getItem());
		if (!repairMaterial.isEmpty())
			return List.of(Misc.getPreferredItem(repairMaterial.getItems()));
		return List.of();
	}

	@Override
	public List<IDawnstoneAnvilRecipe> getVisualRecipes() {
		visualRecipes.clear();
		for (Holder<Item> holder : BuiltInRegistries.ITEM.asHolderIdMap()) {
			Ingredient repairMaterial = Misc.getRepairIngredient(holder.value());
			ItemStack toolStack = new ItemStack(holder.value());
			if (!repairMaterial.isEmpty() && !repairMaterial.hasNoItems() && toolStack.isRepairable() && !blacklist.test(toolStack)) {
				ItemStack brokenTool = toolStack.copy();
				brokenTool.setDamageValue(brokenTool.getMaxDamage() / 2);
				visualRecipes.add(new AnvilDisplayRecipe(List.of(Misc.getPreferredItem(repairMaterial.getItems())), List.of(brokenTool), Ingredient.EMPTY));
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
        return obj instanceof AnvilBreakdownRecipe;
    }

	public static class Serializer implements RecipeSerializer<AnvilBreakdownRecipe> {
        public static final MapCodec<AnvilBreakdownRecipe> CODEC = MapCodec.unit(new AnvilBreakdownRecipe());
        public static final StreamCodec<RegistryFriendlyByteBuf, AnvilBreakdownRecipe> STREAM_CODEC = StreamCodec.unit(new AnvilBreakdownRecipe());

        @Override
        public @NotNull MapCodec<AnvilBreakdownRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, AnvilBreakdownRecipe> streamCodec() {
            return STREAM_CODEC;
        }
	}
}
