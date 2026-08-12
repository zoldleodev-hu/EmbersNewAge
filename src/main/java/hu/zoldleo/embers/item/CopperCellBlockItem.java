package hu.zoldleo.embers.item;

import java.text.DecimalFormat;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.capabilities.EmbersCapabilities;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.util.DecimalFormats;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public class CopperCellBlockItem extends BlockItem {
	public CopperCellBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return stack.getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM) != null;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		IEmberCapability cap = stack.getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM);
		if (cap != null)
			return Math.round(13.0F - (float) (cap.getEmberCapacity()-cap.getEmber()) * 13.0F / (float) cap.getEmberCapacity());
		return 0;
	}

	@Override
	public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
		return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
	}

	@Override
	public int getBarColor(@NotNull ItemStack pStack) {
		return 0xFF6600;
	}

	public static ItemStack getCharged() {
		ItemStack chargedCell = new ItemStack(RegistryManager.COPPER_CELL_ITEM.get());
		IEmberCapability cap = chargedCell.getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM);
        if (cap != null)
		    cap.setEmber(cap.getEmberCapacity());
		return chargedCell;
	}

	/*/@Override TODO
	protected boolean updateCustomBlockEntityTag(BlockPos pPos, Level pLevel, @Nullable Player pPlayer, ItemStack pStack, BlockState pState) {
		IEmberCapability cap = pStack.getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM);
		if (cap != null) {
			cap.writeToNBT(pStack.getOrCreateTagElement("BlockEntityTag"));
		}
		return BlockItem.updateCustomBlockEntityTag(pLevel, pPlayer, pPos, pStack);
	}*/

	@Override
	public void appendHoverText(ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag isAdvanced) {
		IEmberCapability cap = stack.getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM);
		if (cap != null) {
			DecimalFormat emberFormat = DecimalFormats.getDecimalFormat(Embers.MODID + ".decimal_format.ember");
			tooltip.add(Component.translatable(Embers.MODID + ".tooltip.item.ember", emberFormat.format(cap.getEmber()),  emberFormat.format(cap.getEmberCapacity())).withStyle(ChatFormatting.GRAY));
		}
	}
}