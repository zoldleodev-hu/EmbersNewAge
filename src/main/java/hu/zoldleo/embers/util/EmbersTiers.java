package hu.zoldleo.embers.util;

import hu.zoldleo.embers.datagen.EmbersBlockTags;
import hu.zoldleo.embers.datagen.EmbersItemTags;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.SimpleTier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class EmbersTiers {
	public static final Tier LEAD = new SimpleTier(EmbersBlockTags.INCORRECT_FOR_LEAD_TOOL, 168, 6.0f, 2.0f, 4, () -> Ingredient.of(EmbersItemTags.LEAD_INGOT));
	public static final Tier TYRFING = new SimpleTier(EmbersBlockTags.INCORRECT_FOR_TYRFING, 512, 7.5f, 0.0f, 24, () -> Ingredient.of(EmbersItemTags.ASH_DUST));
	public static final Tier SILVER = new SimpleTier(EmbersBlockTags.INCORRECT_FOR_SILVER_TOOL, 202, 7.6f, 2.0f, 20, () -> Ingredient.of(EmbersItemTags.SILVER_INGOT));
	public static final Tier DAWNSTONE = new SimpleTier(EmbersBlockTags.INCORRECT_FOR_DAWNSTONE_TOOL, 644, 7.5f, 2.5f, 18, () -> Ingredient.of(EmbersItemTags.DAWNSTONE_INGOT));
	public static final Tier CLOCKWORK_PICK = new ClockworkTier(EmbersBlockTags.INCORRECT_FOR_CLOCKWORK_TOOL, -1, 16.0F, 4.0F, 18, () -> Ingredient.EMPTY);
	public static final Tier CLOCKWORK_AXE = new ClockworkTier(EmbersBlockTags.INCORRECT_FOR_CLOCKWORK_TOOL, -1, 16.0F, 5.0F, 18, () -> Ingredient.EMPTY);
	public static final Tier CLOCKWORK_HAMMER = new ClockworkTier(EmbersBlockTags.INCORRECT_FOR_CLOCKWORK_HAMMER, -1, 6.0F, 6.0F, 18, () -> Ingredient.EMPTY);

    public static class ClockworkTier extends SimpleTier {
        public ClockworkTier(TagKey<Block> incorrectBlocksForDrops, int uses, float speed, float attackDamageBonus, int enchantmentValue, Supplier<Ingredient> repairIngredient) {
            super(incorrectBlocksForDrops, uses, speed, attackDamageBonus, enchantmentValue, repairIngredient);
        }

        @Override
        public @NotNull Tool createToolProperties(@NotNull TagKey<Block> block) {
            return new Tool(List.of(Tool.Rule.deniesDrops(this.getIncorrectBlocksForDrops()), Tool.Rule.minesAndDrops(block, this.getSpeed())), 1.0F, 0);
        }
    }
}