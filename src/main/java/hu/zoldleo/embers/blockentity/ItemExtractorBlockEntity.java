package hu.zoldleo.embers.blockentity;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.tile.IExtraCapabilityInformation;
import hu.zoldleo.embers.api.tile.IExtractorPipe;
import hu.zoldleo.embers.blockentity.capability_helper.IInventoryBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class ItemExtractorBlockEntity extends ItemPipeBlockEntityBase implements IExtraCapabilityInformation, IInventoryBlock, IExtractorPipe {
	boolean active;
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
			return push(itemStack, simulate);
		}

		@Override
		public @NotNull ItemStack extractItem(int i, int i1, boolean b) {
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int i) {
			return 4;
		}

		@Override
		public boolean isItemValid(int i, @NotNull ItemStack itemStack) {
			return true;
		}
	};

	public ItemExtractorBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.ITEM_EXTRACTOR_ENTITY.get(), pPos, pBlockState);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ItemExtractorBlockEntity extractor) {
		ItemPipeBlockEntityBase.serverTick(level, pos, state, extractor);
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
			if (tile == null || tile instanceof ItemPipeBlockEntityBase)
				continue;
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.relative(facing), facing.getOpposite());
            if (handler == null)
				continue;
            int slot = -1;
            for (int j = 0; j < handler.getSlots(); j++) {
                ItemStack extracted = handler.extractItem(j, 1, true);
                if (!extracted.isEmpty()) {
                    slot = j;
                    break;
                }
            }
			if (slot == -1)
				continue;
            ItemStack extracted = handler.extractItem(slot, 1, true);
            if (extractor.push(extracted, true).isEmpty()) {
                handler.extractItem(slot, 1, false);
                extractor.push(extracted, false);
            }
        }
	}

	@Override
	public void addOtherDescription(List<Component> strings, Direction facing) {
		strings.add(Component.translatable(Embers.MODID + ".tooltip.goggles.redstone_signal"));
	}

    @Override
    public IItemHandler getInventoryCapability(Direction side) {
		return pushHandler;
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