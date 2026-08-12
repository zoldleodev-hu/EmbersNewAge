package hu.zoldleo.embers.item;

import hu.zoldleo.embers.datagen.EmbersBlockTags;
import hu.zoldleo.embers.util.EmbersTiers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.NotNull;

public class ClockworkHammerItem extends ClockworkToolItem {
	public ClockworkHammerItem(Properties properties) {
		super(3, -3.2f, EmbersTiers.CLOCKWORK_HAMMER, EmbersBlockTags.MINABLE_WITH_HAMMER, properties);
	}

	@Override
	public boolean canPerformAction(@NotNull ItemStack stack, @NotNull ItemAbility toolAction) {
		return hasEmber(stack) && (toolAction == ItemAbilities.PICKAXE_DIG || toolAction == ItemAbilities.SHOVEL_DIG || toolAction == ItemAbilities.AXE_DIG);
	}

	/*/@Override TODO
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchant) {
		return super.canApplyAtEnchantingTable(stack, enchant) && enchant != Enchantments.SILK_TOUCH && enchant != Enchantments.BLOCK_FORTUNE && (enchant.category == EnchantmentCategory.WEAPON || enchant.category == EnchantmentCategory.DIGGER);
	}*/

    @Override
    public boolean canAttackBlock(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player) {
        return hasEmber(player.getMainHandItem());
    }
}