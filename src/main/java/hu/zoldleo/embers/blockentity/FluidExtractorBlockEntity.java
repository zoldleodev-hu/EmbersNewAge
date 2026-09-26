package hu.zoldleo.embers.blockentity;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import hu.zoldleo.embers.api.tile.IExtractorPipe;
import hu.zoldleo.embers.blockentity.capability_helper.IFluidBlock;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.tile.IExtraCapabilityInformation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FluidExtractorBlockEntity extends FluidPipeBlockEntityBase implements IExtraCapabilityInformation, IFluidBlock, IExtractorPipe {
	boolean active;
	public static final int MAX_DRAIN = 120;
	SortedSet<PipeNetworkConnection> networkConnections = new TreeSet<>();
	IFluidHandler pushHandler = new IFluidHandler() {
		@Override
		public int getTanks() {
			return 1;
		}

		@Override
		public @NotNull FluidStack getFluidInTank(int i) {
			return FluidStack.EMPTY;
		}

		@Override
		public int getTankCapacity(int i) {
			return 240;
		}

		@Override
		public boolean isFluidValid(int i, @NotNull FluidStack fluidStack) {
			return true;
		}

		@Override
		public int fill(@NotNull FluidStack fluidStack, @NotNull FluidAction fluidAction) {
			return push(fluidStack, fluidAction);
		}

		@Override
		public @NotNull FluidStack drain(@NotNull FluidStack fluidStack, @NotNull FluidAction fluidAction) {
			return FluidStack.EMPTY;
		}

		@Override
		public @NotNull FluidStack drain(int i, @NotNull FluidAction fluidAction) {
			return FluidStack.EMPTY;
		}
	};

	public FluidExtractorBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.FLUID_EXTRACTOR_ENTITY.get(), pPos, pBlockState);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, FluidExtractorBlockEntity extractor) {
		FluidPipeBlockEntityBase.serverTick(level, pos, state, extractor);
		boolean wasActive = extractor.active;
		extractor.active = !level.hasNeighborSignal(pos);
		if (extractor.active != wasActive)
			extractor.updateNetwork();
		if (!extractor.active || extractor.networkConnections.isEmpty())
			return;

		for (Direction facing : Direction.values()) {
			if (!extractor.getConnection(facing).transfer)
				continue;
			BlockEntity tile = level.getBlockEntity(pos.relative(facing));
			if (tile == null || tile instanceof FluidPipeBlockEntityBase)
				continue;
            IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos.relative(facing), facing.getOpposite());
            if (handler == null)
				continue;
            FluidStack extracted = handler.drain(MAX_DRAIN, IFluidHandler.FluidAction.SIMULATE);
            int filled = extractor.push(extracted, IFluidHandler.FluidAction.SIMULATE);
            if (filled > 0) {
                extractor.push(extracted, IFluidHandler.FluidAction.EXECUTE);
                handler.drain(filled, IFluidHandler.FluidAction.EXECUTE);
            }
        }
	}

	public int push(FluidStack stack, IFluidHandler.FluidAction simulate) {
		if (stack.isEmpty())
			return 0;
		if (networkConnections.isEmpty())
			return 0;

		int totalFilled = 0;
		FluidStack copy = stack.copy();
		for (PipeNetworkConnection connection : networkConnections) {
			if (stack.isEmpty())
				return 0;
			IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, connection.pos(), connection.side());
			if (handler == null)
				continue;
			int filled = handler.fill(copy, simulate);
			copy.shrink(filled);
			totalFilled += filled;
		}
		return totalFilled;
	}

	@Override
	public void addOtherDescription(List<Component> strings, Direction facing) {
		strings.add(Component.translatable(Embers.MODID + ".tooltip.goggles.redstone_signal"));
	}

    @Override
    public IFluidHandler getFluidCapability(Direction side) {
		return pushHandler;
    }

	@Override
	public void addConnection(BlockPos pos, Direction side, int priority) {
		networkConnections.add(new PipeNetworkConnection(pos, side, priority));
	}

	@Override
	public void clearConnections() {
		networkConnections.clear();
	}

	@Override
	public boolean active() {
		return active;
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(tag, provider);
		if (!tag.contains("NetworkConnections"))
			return;
		networkConnections.clear();
		ListTag networkConnectionListTag = tag.getList("NetworkConnections", Tag.TAG_COMPOUND);
		for (Tag connectionTag : networkConnectionListTag)
			networkConnections.add(
					new PipeNetworkConnection(
							NbtUtils.readBlockPos((CompoundTag)connectionTag, "pos").orElseThrow(),
							Direction.from3DDataValue(((CompoundTag)connectionTag).getInt("side")),
							((CompoundTag) connectionTag).getInt("priority")
					)
			);
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(tag, provider);
		ListTag networkConnectionListTag = new ListTag(networkConnections.size());
		for (PipeNetworkConnection connection : networkConnections) {
			CompoundTag connectionTag = new CompoundTag(2);
			connectionTag.put("pos", NbtUtils.writeBlockPos(connection.pos()));
			connectionTag.putInt("side", connection.side().get3DDataValue());
			connectionTag.putInt("priority", connection.priority());
			networkConnectionListTag.add(connectionTag);
		}
		tag.put("NetworkConnections", networkConnectionListTag);
	}
}