package hu.zoldleo.embers.blockentity;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.tile.IExtractorPipe;
import hu.zoldleo.embers.api.tile.IPipePriority;
import hu.zoldleo.embers.blockentity.capability_helper.IFluidBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidTransferBlockEntity extends FluidPipeBlockEntityBase implements IFluidBlock, IExtractorPipe, IPipePriority {
	public FluidStack filterFluid = FluidStack.EMPTY;
	public boolean syncFilter = true;
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
			return acceptsFluid(fluidStack);
		}

		@Override
		public int fill(@NotNull FluidStack fluidStack, @NotNull FluidAction fluidAction) {
			return acceptsFluid(fluidStack) ? push(fluidStack, fluidAction) : 0;
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

	public FluidTransferBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.FLUID_TRANSFER_ENTITY.get(), pPos, pBlockState);
		syncConnections = false;
		saveConnections = false;
	}

	public boolean acceptsFluid(FluidStack stack) {
		return filterFluid.isEmpty() || (filterFluid.isComponentsPatchEmpty() ? FluidStack.isSameFluid(stack, filterFluid) : FluidStack.isSameFluidSameComponents(stack, filterFluid));
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		if (nbt.contains("filter"))
			filterFluid = FluidStack.parseOptional(provider, nbt.getCompound("filter"));
		if (!nbt.contains("NetworkConnections"))
			return;
		networkConnections.clear();
		ListTag networkConnectionListTag = nbt.getList("NetworkConnections", Tag.TAG_COMPOUND);
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
	protected boolean requiresSync() {
		return syncFilter || super.requiresSync();
	}

	@Override
	protected void resetSync() {
		super.resetSync();
		syncFilter = false;
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
        nbt.put("filter", filterFluid.saveOptional(provider));
		ListTag networkConnectionListTag = new ListTag(networkConnections.size());
		for (PipeNetworkConnection connection : networkConnections) {
			CompoundTag connectionTag = new CompoundTag(2);
			connectionTag.put("pos", NbtUtils.writeBlockPos(connection.pos()));
			connectionTag.putInt("side", connection.side().get3DDataValue());
			connectionTag.putInt("priority", connection.priority());
			networkConnectionListTag.add(connectionTag);
		}
		nbt.put("NetworkConnections", networkConnectionListTag);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		if (syncFilter)
            nbt.put("filter", filterFluid.saveOptional(provider));
		return nbt;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, FluidTransferBlockEntity blockEntity) {
		FluidPipeBlockEntityBase.serverTick(level, pos, state, blockEntity);
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
	public PipeConnection getConnection(Direction facing) {
		return getBlockState().hasProperty(BlockStateProperties.FACING) &&
                getBlockState().getValue(BlockStateProperties.FACING).getAxis() == facing.getAxis() ?
                PipeConnection.PIPE : PipeConnection.NONE;
	}

    @Override
    public IFluidHandler getFluidCapability(Direction side) {
		if (side == null)
			return pushHandler;
		if (getBlockState().hasProperty(BlockStateProperties.FACING)) {
			Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
			if (side.getOpposite() != facing && side.getAxis() == facing.getAxis())
				return pushHandler;
		}
		return null;
    }

	@Override
	protected void collectPipesInNetwork(Set<PipeBlockEntityBase> set, Consumer<PipeBlockEntityBase> action, @Nullable Direction connectFrom) {
		if (!getBlockState().hasProperty(BlockStateProperties.FACING))
			return;
		Direction facing = getBlockState().getValue(BlockStateProperties.FACING); // input direction
		if (connectFrom == null || connectFrom == facing.getOpposite()) {
			if (set.contains(this))
				return;
			set.add(this);
			action.accept(this);
			if (connectFrom == null &&
					connections[facing.get3DDataValue()] == PipeConnection.PIPE &&
					level.getBlockEntity(worldPosition.relative(facing.getOpposite())) instanceof PipeBlockEntityBase pipe)
				pipe.collectPipesInNetwork(set, action, facing);
		}
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
		return true;
	}

	@Override
	public boolean acceptsPipes() {
		return true;
	}

	@Override
	public int getPriority(Direction side) {
		return 10;
	}
}