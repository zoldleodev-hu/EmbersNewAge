package hu.zoldleo.embers.blockentity;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.filter.FilterItem;
import hu.zoldleo.embers.api.filter.IFilter;
import hu.zoldleo.embers.api.item.IFilterItem;
import hu.zoldleo.embers.api.tile.IExtractorPipe;
import hu.zoldleo.embers.api.tile.IPipePriority;
import hu.zoldleo.embers.blockentity.capability_helper.IInventoryBlock;
import hu.zoldleo.embers.util.FilterUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemTransferBlockEntity extends ItemPipeBlockEntityBase implements IInventoryBlock, IExtractorPipe, IPipePriority {
	public ItemStack filterItem = ItemStack.EMPTY;
	public boolean syncFilter = true;
	SortedSet<PipeNetworkConnection> networkConnections = new TreeSet<>();
	IItemHandler pushHandler = new IItemHandler() {
		@Override
		public int getSlots() {
			return 1;
		}

		@Override
		public @NotNull ItemStack getStackInSlot(int i) {
			return ItemStack.EMPTY;
		}

		@Override
		public @NotNull ItemStack insertItem(int i, @NotNull ItemStack itemStack, boolean simulate) {
			return acceptsItem(itemStack) ?
					push(itemStack, simulate) : itemStack;
		}

		@Override
		public @NotNull ItemStack extractItem(int i, int i1, boolean b) {
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int i) {
			return 64;
		}

		@Override
		public boolean isItemValid(int i, @NotNull ItemStack itemStack) {
			return acceptsItem(itemStack);
		}
	};

	IFilter filter = FilterUtil.FILTER_ANY;

	public ItemTransferBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.ITEM_TRANSFER_ENTITY.get(), pPos, pBlockState);
		syncConnections = false;
		saveConnections = false;
	}

	public boolean acceptsItem(ItemStack stack) {
		return filter.acceptsItem(stack);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		if (nbt.contains("filter"))
			filterItem = ItemStack.parseOptional(provider, nbt.getCompound("filter"));
		setupFilter();
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
        nbt.put("filter", filterItem.saveOptional(provider));
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
            nbt.put("filter", filterItem.saveOptional(provider));
		return nbt;
	}

	public void setupFilter() {
		Item item = filterItem.getItem();
		if (item instanceof IFilterItem)
			filter = ((IFilterItem) item).getFilter(filterItem);
		else if (!filterItem.isEmpty())
			filter = new FilterItem(filterItem);
		else
			filter = FilterUtil.FILTER_ANY;
		if (level != null &&
				getBlockState().hasProperty(BlockStateProperties.FACING) &&
				level.getBlockEntity(worldPosition.relative(getBlockState().getValue(BlockStateProperties.FACING))) instanceof ItemPipeBlockEntityBase pipe)
			pipe.updateNetwork();
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ItemTransferBlockEntity blockEntity) {
		ItemPipeBlockEntityBase.serverTick(level, pos, state, blockEntity);
	}

	@Override
	public PipeConnection getConnection(Direction facing) {
		return getBlockState().hasProperty(BlockStateProperties.FACING) &&
                getBlockState().getValue(BlockStateProperties.FACING).getAxis() == facing.getAxis() ?
                PipeConnection.PIPE : PipeConnection.NONE;
	}

    @Override
    public IItemHandler getInventoryCapability(Direction side) {
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

	protected ItemStack push(ItemStack stack, boolean simulate) {
		if (stack.isEmpty())
			return ItemStack.EMPTY;
		if (networkConnections.isEmpty())
			return stack;

		for (PipeNetworkConnection connection : networkConnections) {
			if (stack.isEmpty())
				return ItemStack.EMPTY;
			IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, connection.pos(), connection.side());
			if (handler == null)
				continue;
			for (int slot = 0; slot < handler.getSlots() && !stack.isEmpty(); slot++)
				stack = handler.insertItem(slot, stack, simulate);
		}
		return stack;
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