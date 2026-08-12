package hu.zoldleo.embers.util;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

/**
 * Builds a recipe consumer wrapper, which adds some extra properties to wrap the result of another recipe (stolen from mantle <a href="https://github.com/SlimeKnights/Mantle/blob/1.18.2/src/main/java/slimeknights/mantle/recipe/data/ConsumerWrapperBuilder.java">...</a>)
 */
public class ConsumerWrapperBuilder {
	private final List<ICondition> conditions = new ArrayList<>();
	@Nullable
	private final RecipeSerializer<?> override;
	@Nullable
	private final ResourceLocation overrideName;

	private ConsumerWrapperBuilder(@Nullable RecipeSerializer<?> override, @Nullable ResourceLocation overrideName) {
		this.override = override;
		this.overrideName = overrideName;
	}

	/**
	 * Creates a wrapper builder with the default serializer
	 * @return Default serializer builder
	 */
	public static ConsumerWrapperBuilder wrap() {
		return new ConsumerWrapperBuilder(null, null);
	}

	/**
	 * Creates a wrapper builder with a serializer override
	 * @param override Serializer override
	 * @return Default serializer builder
	 */
	public static ConsumerWrapperBuilder wrap(RecipeSerializer<?> override) {
		return new ConsumerWrapperBuilder(override, null);
	}

	/**
	 * Creates a wrapper builder with a serializer name override
	 * @param override Serializer override
	 * @return Default serializer builder
	 */
	public static ConsumerWrapperBuilder wrap(ResourceLocation override) {
		return new ConsumerWrapperBuilder(null, override);
	}

	/**
	 * Adds a conditional to the consumer
	 * @param condition Condition to add
	 * @return Added condition
	 */
	public ConsumerWrapperBuilder addCondition(ICondition condition) {
		conditions.add(condition);
		return this;
	}

	/**
	 * Builds the output for the wrapper builder
	 * @param output Base output
	 * @return Built wrapper consumer
	 */
	public RecipeOutput build(RecipeOutput output) {
		return new Wrapped(output, conditions, override, overrideName);
	}

	private static class Wrapped implements RecipeOutput {
		private final RecipeOutput original;
		private final List<ICondition> conditions;
		@Nullable
		private final RecipeSerializer<?> override;
		@Nullable
		private final ResourceLocation overrideName;

		private Wrapped(RecipeOutput original, List<ICondition> conditions, @Nullable RecipeSerializer<?> override, @Nullable ResourceLocation overrideName) {
			// if wrapping another wrapper result, merge the two together
			if (original instanceof Wrapped toMerge) {
                this.original = toMerge.original;
				this.conditions = ImmutableList.<ICondition>builder().addAll(toMerge.conditions).addAll(conditions).build();
				// consumer wrappers are processed inside out, so the innermost wrapped recipe is the one with the most recent serializer override
				if (toMerge.override != null || toMerge.overrideName != null) {
					this.override = toMerge.override;
					this.overrideName = toMerge.overrideName;
				} else {
					this.override = override;
					this.overrideName = overrideName;
				}
			} else {
				this.original = original;
				this.conditions = conditions;
				this.override = override;
				this.overrideName = overrideName;
			}
		}

        @Override
        public Advancement.@NotNull Builder advancement() {
            return original.advancement();
        }

        @Override
        public void accept(@NotNull ResourceLocation resourceLocation, @NotNull Recipe<?> recipe, @org.jetbrains.annotations.Nullable AdvancementHolder advancementHolder, ICondition @NotNull ... iConditions) {
            if (override != null)
                recipe = new WrappedRecipe<>(recipe, override);
            if (overrideName != null)
                resourceLocation = overrideName;
            ArrayList<ICondition> newConditions = new ArrayList<>(List.of(iConditions));
            newConditions.addAll(conditions);
            original.accept(resourceLocation, recipe, advancementHolder, newConditions.toArray(new ICondition[0]));
        }

         private record WrappedRecipe<T extends RecipeInput>(Recipe<T> original, RecipeSerializer<?> override) implements Recipe<T> {
            @Override
            public boolean matches(@NotNull T recipeInput, @NotNull Level level) {
                return original.matches(recipeInput, level);
            }

            @Override
            public @NotNull ItemStack assemble(@NotNull T recipeInput, HolderLookup.@NotNull Provider provider) {
                return original.assemble(recipeInput, provider);
            }

            @Override
            public boolean canCraftInDimensions(int i, int i1) {
                return original.canCraftInDimensions(i, i1);
            }

            @Override
            public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider) {
                return original.getResultItem(provider);
            }

            @Override
            public @NotNull RecipeSerializer<?> getSerializer() {
                return override;
            }

            @Override
            public @NotNull RecipeType<?> getType() {
                return original.getType();
            }
        }
    }
}