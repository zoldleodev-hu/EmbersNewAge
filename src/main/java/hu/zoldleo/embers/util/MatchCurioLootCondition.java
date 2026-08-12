package hu.zoldleo.embers.util;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hu.zoldleo.embers.compat.curios.CuriosCompat;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;

/**
 * A LootItemCondition that checks worn curios against an {@link ItemPredicate}.
 */
public record MatchCurioLootCondition(ItemPredicate predicate) implements LootItemCondition {
    public static final MapCodec<MatchCurioLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemPredicate.CODEC.fieldOf("predicate").forGetter(MatchCurioLootCondition::predicate)
    ).apply(instance, MatchCurioLootCondition::new));

    public static final LootItemConditionType LOOT_CONDITION_TYPE = new LootItemConditionType(CODEC);

    public @NotNull LootItemConditionType getType() {
        return LOOT_CONDITION_TYPE;
    }

    public @NotNull Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.ATTACKING_ENTITY, LootContextParams.THIS_ENTITY);
    }

    public boolean test(LootContext context) {
        if (!ModList.get().isLoaded("curios"))
            return false;

        Entity user = null;
        if (context.hasParam(LootContextParams.ATTACKING_ENTITY))
            user = context.getParam(LootContextParams.ATTACKING_ENTITY);
        else if (context.hasParam(LootContextParams.THIS_ENTITY))
            user = context.getParam(LootContextParams.THIS_ENTITY);

		if (user instanceof LivingEntity)
			return CuriosCompat.checkForCurios((LivingEntity) user, predicate);
		return false;
	}

	public static LootItemCondition.Builder curioMatches(ItemPredicate.Builder pToolPredicateBuilder) {
		return () -> new MatchCurioLootCondition(pToolPredicateBuilder.build());
	}
}