package hu.zoldleo.embers.datagen;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.compat.curios.CuriosCompat;
import hu.zoldleo.embers.util.AshenAmuletLootModifier;
import hu.zoldleo.embers.util.AugmentPredicate;
import hu.zoldleo.embers.util.GrandhammerLootModifier;
import hu.zoldleo.embers.util.MatchCurioLootCondition;
import hu.zoldleo.embers.util.SuperHeaterLootModifier;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class EmbersLootModifiers extends GlobalLootModifierProvider {
	public EmbersLootModifiers(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
		super(output, provider, Embers.MODID);
	}

	@Override
	protected void start() {
		add("grandhammer", new GrandhammerLootModifier(new LootItemCondition[]{MatchTool.toolMatches(ItemPredicate.Builder.item().of(RegistryManager.GRANDHAMMER.get())).build()}));
		add("superheater", new SuperHeaterLootModifier(new LootItemCondition[]{new MatchTool(Optional.of(ItemPredicate.Builder.item().withSubPredicate(AugmentPredicate.TYPE, new AugmentPredicate(RegistryManager.SUPERHEATER_AUGMENT, 1)).build()))}));
		add("ashenamulet", new AshenAmuletLootModifier(new LootItemCondition[]{MatchCurioLootCondition.curioMatches(ItemPredicate.Builder.item().of(CuriosCompat.ASHEN_AMULET.get())).build()}));
	}
}