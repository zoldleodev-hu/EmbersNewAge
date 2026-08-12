package hu.zoldleo.embers.block;

import java.util.ArrayList;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.blockentity.FluidDialBlockEntity;
import hu.zoldleo.embers.datagen.EmbersFluidTags;
import hu.zoldleo.embers.util.FluidAmounts;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public class FluidDialBlock extends DialBaseBlock {
    public static final MapCodec<FluidDialBlock> CODEC = simpleCodec(FluidDialBlock::new);
	public static final String DIAL_TYPE = "fluid";

	public FluidDialBlock(Properties pProperties) {
		super(pProperties);
	}

    @Override
    protected @NotNull MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
	public boolean hasAnalogOutputSignal(@NotNull BlockState pState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
		BlockEntity blockEntity = level.getBlockEntity(pos.relative(state.getValue(FACING), -1));
		if (blockEntity != null) {
			IFluidHandler cap = level.getCapability(Capabilities.FluidHandler.BLOCK, pos.relative(state.getValue(FACING), -1), state.getValue(FACING).getOpposite());
            if (cap != null) {
				int totalCapacity = 0;
				int totalContents = 0;
				for (int i = 0; i < cap.getTanks(); i++) {
					totalCapacity += cap.getTankCapacity(i);
					totalContents += cap.getFluidInTank(i).getAmount();
				}
				if (totalContents >= totalCapacity)
					return 15;
				return (int) (Math.ceil(14.0 * totalContents / totalCapacity));
			}
		}
		return 0;
	}

	@Override
	protected void getBEData(Direction facing, ArrayList<Component> text, BlockEntity blockEntity, int maxLines) {
		if (blockEntity instanceof FluidDialBlockEntity dial && dial.display) {
			int extraLines = 0;
			for (int i = 0; i < dial.fluids.length && (i + extraLines) < maxLines; i++) {
				FluidStack contents = dial.fluids[i];
				text.add(formatFluidStack(contents, dial.capacities[i]));
				if (contents.is(EmbersFluidTags.INGOT_TOOLTIP) && contents.getAmount() >= FluidAmounts.nuggetValue()) {
					if ((i + extraLines + 1) < maxLines)
						text.add(FluidAmounts.getIngotTooltip(contents.getAmount()));
					extraLines++;
				}
			}
			if ((dial.fluids.length + dial.extraLines + extraLines) > Math.min(maxLines, dial.fluids.length + extraLines)) {
				text.add(Component.translatable(Embers.MODID + ".tooltip.too_many", dial.fluids.length + extraLines - Math.min(maxLines, dial.fluids.length + extraLines) + dial.extraLines));
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static MutableComponent formatFluidStack(FluidStack contents, int capacity) {
		if (!contents.isEmpty())
			return Component.translatable(Embers.MODID + ".tooltip.fluiddial.fluid", contents.getHoverName().getString(), contents.getAmount(), capacity);
		else
			return Component.translatable(Embers.MODID + ".tooltip.fluiddial.nofluid", capacity);
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
		return RegistryManager.FLUID_DIAL_ENTITY.get().create(pPos, pState);
	}

	@Override
	public String getDialType() {
		return DIAL_TYPE;
	}
}