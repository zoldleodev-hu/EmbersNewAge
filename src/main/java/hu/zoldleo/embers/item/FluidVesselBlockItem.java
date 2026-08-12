package hu.zoldleo.embers.item;

import java.util.List;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import hu.zoldleo.embers.block.FluidDialBlock;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public class FluidVesselBlockItem extends BlockItem {
	public FluidVesselBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public int getMaxStackSize(ItemStack stack) {
        IFluidHandler tank = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (tank != null && !tank.getFluidInTank(0).isEmpty())
            return 1;
		return super.getMaxStackSize(stack);
	}

	/*@Override TODO
	protected boolean updateCustomBlockEntityTag(BlockPos pPos, Level pLevel, @Nullable Player pPlayer, ItemStack pStack, BlockState pState) {
		CompoundTag nbt = pStack.getOrCreateTag();
		if (nbt.contains(FluidHandlerItemStack.FLUID_NBT_KEY)) {
			nbt.put("BlockEntityTag", nbt.get(FluidHandlerItemStack.FLUID_NBT_KEY));
		}
		return BlockItem.updateCustomBlockEntityTag(pLevel, pPlayer, pPos, pStack);
	}*/

	@Override
	public void appendHoverText(ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag isAdvanced) {
		IFluidHandler cap = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (cap == null)
            return;
        tooltip.add(FluidDialBlock.formatFluidStack(cap.getFluidInTank(0), cap.getTankCapacity(0)).withStyle(ChatFormatting.GRAY));
	}
}