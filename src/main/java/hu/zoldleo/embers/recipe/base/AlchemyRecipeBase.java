package hu.zoldleo.embers.recipe.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import hu.zoldleo.embers.ConfigManager;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.misc.AlchemyResult;

import hu.zoldleo.embers.datacomponents.HintComponent;
import hu.zoldleo.embers.recipe.context.AlchemyContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public abstract class AlchemyRecipeBase implements IAlchemyRecipe {
	public final Ingredient tablet;
	public final List<Ingredient> aspects;
	public final List<Ingredient> inputs;

	public final ItemStack output;
	public final ItemStack failure;

	public Long cachedSeed = null;
	public ArrayList<Ingredient> code = new ArrayList<>();

	public AlchemyRecipeBase(Ingredient tablet, List<Ingredient> aspects, List<Ingredient> inputs, ItemStack output, ItemStack failure) {
		this.tablet = tablet;
		this.aspects = aspects;
		this.inputs = inputs;
		this.output = output;
		this.failure = failure;
	}

	@Override
	public ArrayList<Ingredient> getCode(long seed, ResourceLocation id) {
		if (cachedSeed == null || cachedSeed != seed) {
			code.clear();
			Random rand = new Random(seed - id.hashCode());
			for (int i = 0; i < inputs.size(); i++)
				code.add(aspects.get(rand.nextInt(aspects.size())));
			cachedSeed = seed;
		}
		return code;
	}

	@Override
	public boolean matches(AlchemyContext context, @NotNull Level pLevel) {
		if (!tablet.test(context.tablet) || inputs.size() != context.contents.size())
			return false;

		ArrayList<PedestalContents> remaining = new ArrayList<>(context.contents);
        for (Ingredient input : inputs) {
            boolean matched = false;
            for (int j = 0; j < remaining.size(); j++) {
                if (input.test(remaining.get(j).input)) {
                    matched = true;
                    remaining.remove(j);
                    break;
                }
            }
            if (!matched)
                return false;
        }
		return true;
	}

	@Override
	public boolean matchesCorrect(AlchemyContext context, Level pLevel, ResourceLocation id) {
        if (ConfigManager.EASY_ALCHEMY_RECIPES.get())
            return matches(context, pLevel);
		getCode(context.seed, id);
		if (!tablet.test(context.tablet) || code.size() != context.contents.size())
			return false;

		ArrayList<PedestalContents> remaining = new ArrayList<>(context.contents);
		for (int i = 0; i < inputs.size(); i++) {
			boolean matched = false;
			for (int j = 0; j < remaining.size(); j++) {
				if (code.get(i).test(remaining.get(j).aspect) && inputs.get(i).test(remaining.get(j).input)) {
					matched = true;
					remaining.remove(j);
					break;
				}
			}
			if (!matched)
				return false;
		}
		return true;
	}

	@Override
	public @NotNull ItemStack assemble(@NotNull AlchemyContext context, HolderLookup.@NotNull Provider registry) {
        return assemble(context, registry, registry.lookupOrThrow(Registries.RECIPE).listElements().filter(x -> x.value() == this).findFirst().orElseThrow().key().location());
	}

    @Override
    public ItemStack assemble(AlchemyContext context, HolderLookup.Provider registry, ResourceLocation id) {
        getCode(context.seed, id);
        int blackPins = 0;
        int whitePins = 0;

        ArrayList<Ingredient> remainingCode = new ArrayList<>(code);
        for (int i = 0; i < context.contents.size(); i++) {
            for (int j = 0; j < remainingCode.size(); j++) {
                if (remainingCode.get(j).test(context.contents.get(i).aspect)) {
                    whitePins++;
                    remainingCode.remove(j);
                    break;
                }
            }
        }

        ArrayList<PedestalContents> remaining = new ArrayList<>(context.contents);
        for (int i = 0; i < inputs.size(); i++) {
            for (int j = 0; j < remaining.size(); j++) {
                if (code.get(i).test(remaining.get(j).aspect) && inputs.get(i).test(remaining.get(j).input)) {
                    blackPins++;
                    remaining.remove(j);
                    break;
                }
            }
        }
        whitePins -= blackPins;

        if (blackPins < code.size() && !ConfigManager.EASY_ALCHEMY_RECIPES.get()) {
            ItemStack waste = failure.copy();

            List<ItemStack> aspects = new ArrayList<>();
            List<ItemStack> inputs = new ArrayList<>();
            context.contents.forEach(content -> {
                aspects.add(content.aspect);
                inputs.add(content.input);
            });

            waste.set(RegistryManager.HINT_COMPONENT, new HintComponent(blackPins, whitePins, aspects, inputs, output));
            return waste;
        }
        return output;
    }

	@Override
	public AlchemyResult getResult(AlchemyContext context, ResourceLocation id) {
		getCode(context.seed, id);
		int blackPins = 0;
		int whitePins = 0;

		ArrayList<Ingredient> remainingCode = new ArrayList<>(code);
		for (int i = 0; i < context.contents.size(); i++) {
			for (int j = 0; j < remainingCode.size(); j++) {
				if (remainingCode.get(j).test(context.contents.get(i).aspect)) {
					whitePins++;
					remainingCode.remove(j);
					break;
				}
			}
		}

		ArrayList<PedestalContents> remaining = new ArrayList<>(context.contents);
		for (int i = 0; i < inputs.size(); i++) {
			for (int j = 0; j < remaining.size(); j++) {
				if (code.get(i).test(remaining.get(j).aspect) && inputs.get(i).test(remaining.get(j).input)) {
					blackPins++;
					remaining.remove(j);
					break;
				}
			}
		}
		whitePins -= blackPins;

		//ensure that the ingredient order matches the recipe
		List<PedestalContents> contents = new ArrayList<>(context.contents);
		List<PedestalContents> sortedContents = new ArrayList<>();
		for (Ingredient input : inputs) {
			for (PedestalContents pedestal : contents) {
				if (input.test(pedestal.input)) {
					sortedContents.add(pedestal);
					contents.remove(pedestal);
					break;
				}
			}
		}

		return new AlchemyResult(sortedContents, getResultItem(), blackPins, whitePins);
	}

	@Override
	public Ingredient getCenterInput() {
		return tablet;
	}

	@Override
	public List<Ingredient> getInputs() {
		return inputs;
	}

	@Override
	public List<Ingredient> getAspects() {
		return aspects;
	}

	@Override
	public ItemStack getResultItem() {
		return output;
	}

	@Override
	public ItemStack getfailureItem() {
		return failure;
	}
}