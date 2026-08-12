package hu.zoldleo.embers.api.misc;

import java.util.ArrayList;
import java.util.List;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.datacomponents.HintComponent;
import hu.zoldleo.embers.recipe.base.IAlchemyRecipe.PedestalContents;

import net.minecraft.world.item.ItemStack;

public class AlchemyResult {
	public List<PedestalContents> contents;
	public ItemStack result;
	public int blackPins;
	public int whitePins;

	public AlchemyResult(List<PedestalContents> contents, ItemStack result, int blackPins, int whitePins) {
		this.contents = contents;
		this.result = result;
		this.blackPins = blackPins;
		this.whitePins = whitePins;
	}

	public ItemStack createResultStack(ItemStack stack) {
        List<ItemStack> aspects = new ArrayList<>();
        List<ItemStack> inputs = new ArrayList<>();
		for (PedestalContents contents : contents) {
            aspects.add(contents.aspect);
            inputs.add(contents.input);
		}
        stack.set(RegistryManager.HINT_COMPONENT, new HintComponent(blackPins, whitePins, aspects, inputs, result));
		return stack;
	}
}