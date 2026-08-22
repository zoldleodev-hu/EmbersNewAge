package hu.zoldleo.embers.item;

import java.util.List;
import java.util.Optional;

import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
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
	public int getMaxStackSize(@NotNull ItemStack stack) {
        Optional<IFluidHandlerItem> tank = FluidUtil.getFluidHandler(stack);
        if (tank.isPresent() && !tank.get().getFluidInTank(0).isEmpty())
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
	public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag isAdvanced) {
        FluidUtil.getFluidHandler(stack).ifPresent(cap ->
                tooltip.add(FluidDialBlock.formatFluidStack(cap.getFluidInTank(0), cap.getTankCapacity(0)).withStyle(ChatFormatting.GRAY))
        );
	}
}