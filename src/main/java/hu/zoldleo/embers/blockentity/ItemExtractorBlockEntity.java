package hu.zoldleo.embers.blockentity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import javax.annotation.Nonnull;

import hu.zoldleo.embers.blockentity.capability_helper.IInventoryBlock;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.filter.FilterAny;
import hu.zoldleo.embers.api.filter.IFilter;
import hu.zoldleo.embers.api.tile.IExtraCapabilityInformation;
import hu.zoldleo.embers.api.tile.IOrderDestination;
import hu.zoldleo.embers.api.tile.IOrderSource;
import hu.zoldleo.embers.api.tile.OrderStack;
import hu.zoldleo.embers.particle.VaporParticleOptions;
import hu.zoldleo.embers.util.EmbersColors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ItemExtractorBlockEntity extends ItemPipeBlockEntityBase implements IOrderDestination, IExtraCapabilityInformation, IInventoryBlock {
	IItemHandler[] sideHandlers;
	boolean active;
	List<OrderStack> orders = new ArrayList<>();

	public ItemExtractorBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.ITEM_EXTRACTOR_ENTITY.get(), pPos, pBlockState);
	}

	@Override
	protected void initInventory() {
		super.initInventory();
		sideHandlers = new IItemHandler[Direction.values().length];
		for (Direction facing : Direction.values()) {
			sideHandlers[facing.get3DDataValue()] = new IItemHandler() {
				@Override
				public int getSlots() {
					return inventory.getSlots();
				}

				@Nonnull
				@Override
				public ItemStack getStackInSlot(int slot) {
					return inventory.getStackInSlot(slot);
				}

				@Nonnull
				@Override
				public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
					if (active)
						return stack;
					if (!simulate)
						setFrom(facing, true);
					return inventory.insertItem(slot, stack, simulate);
				}

				@Nonnull
				@Override
				public ItemStack extractItem(int slot, int amount, boolean simulate) {
					return inventory.extractItem(slot, amount, simulate);
				}

				@Override
				public int getSlotLimit(int slot) {
					return inventory.getSlotLimit(slot);
				}

				@Override
				public boolean isItemValid(int slot, @NotNull ItemStack stack) {
					return true;
				}
			};
		}
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		if (nbt.contains("orders")) {
			ListTag tagOrders = nbt.getList("orders",Tag.TAG_COMPOUND);
			orders.clear();
			for (Tag tagOrder : tagOrders)
				orders.add(new OrderStack((CompoundTag) tagOrder, provider));
		}
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
		ListTag tagOrders = new ListTag();
		for (OrderStack order : orders)
			tagOrders.add(order.writeToNBT(new CompoundTag(), provider));
		nbt.put("orders", tagOrders);
	}

	public static IFilter FILTER_ANY = new FilterAny();

	public static void serverTick(Level level, BlockPos pos, BlockState state, ItemExtractorBlockEntity blockEntity) {
		if (level instanceof ServerLevel && blockEntity.clogged && blockEntity.isAnySideUnclogged()) {
			Random posRand = new Random(pos.asLong());
			double angleA = posRand.nextDouble() * Math.PI * 2;
			double angleB = posRand.nextDouble() * Math.PI * 2;
			float xOffset = (float) (Math.cos(angleA) * Math.cos(angleB));
			float yOffset = (float) (Math.sin(angleA) * Math.cos(angleB));
			float zOffset = (float) Math.sin(angleB);
			float speed = 0.1f;
			float vx = xOffset * speed + posRand.nextFloat() * speed * 0.3f;
			float vy = yOffset * speed + posRand.nextFloat() * speed * 0.3f;
			float vz = zOffset * speed + posRand.nextFloat() * speed * 0.3f;
			((ServerLevel) level).sendParticles(new VaporParticleOptions(EmbersColors.VAPOR_ID, new Vec3(vx, vy, vz), 1.0f), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0, 0, 0, 1.0);
		}
		blockEntity.cleanupOrders();
		blockEntity.active = level.hasNeighborSignal(pos);
		OrderStack currentOrder = blockEntity.orders.isEmpty() ? null : blockEntity.orders.getFirst();
		IFilter filter = /*FilterUtil.*/FILTER_ANY;
		if (blockEntity.active)
			currentOrder = null;
		else if (currentOrder != null)
			filter = currentOrder.getFilter();

		IItemHandler invDest = null;
		if(currentOrder != null) {
			IOrderSource destination = currentOrder.getSource(level);
			if(destination != null)
				invDest = destination.getItemHandler();
		}

		for (Direction facing : Direction.values()) {
			if (!blockEntity.getConnection(facing).transfer)
				continue;
			BlockEntity tile = level.getBlockEntity(pos.relative(facing));

			if (tile != null && !(tile instanceof ItemPipeBlockEntityBase)) {
				IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.relative(facing), facing.getOpposite());
                if (handler != null && (blockEntity.active || (currentOrder != null && currentOrder.getSize() > 0))) {
					int slot = -1;
					for (int j = 0; j < handler.getSlots() && slot == -1; j++) {
						ItemStack extracted = handler.extractItem(j, 1, true);
						if (!extracted.isEmpty() && filter.acceptsItem(extracted, invDest)) {
							slot = j;
						}
					}
					if (slot != -1) {
						ItemStack extracted = handler.extractItem(slot, 1, true);
						if (blockEntity.inventory.insertItem(0, extracted, true).isEmpty()) {
							handler.extractItem(slot, 1, false);
							blockEntity.inventory.insertItem(0, extracted, false);
							if(currentOrder != null)
								currentOrder.deplete(extracted.getCount());
						}
					}
					blockEntity.setFrom(facing, true);
				} else {
					blockEntity.setFrom(facing, false);
				}
			}
		}
		ItemPipeBlockEntityBase.serverTick(level, pos, state, blockEntity);
	}

	@Override
	public int getCapacity() {
		return 4;
	}

	@Override
	public void order(BlockEntity source, IFilter filter, int orderSize) {
		OrderStack order = getOrder(source);
		if (order == null)
			orders.add(new OrderStack(source.getBlockPos(), filter, orderSize));
		else if(Objects.equals(order.getFilter(), filter))
			order.increment(orderSize);
		else {
			order.reset(filter, orderSize);
		}
	}

	@Override
	public void resetOrder(BlockEntity source) {
		orders.removeIf(order -> order.getPos().equals(source.getBlockPos()));
	}

	public OrderStack getOrder(BlockEntity source) {
		for (OrderStack order : orders) {
			if (order.getPos().equals(source.getBlockPos()))
				return order;
		}
		return null;
	}

	private void cleanupOrders() {
		orders.removeIf(this::isOrderInvalid);
	}

	private boolean isOrderInvalid(OrderStack order) {
		return order.getSize() <= 0 || order.getSource(level) == null;
	}

	@Override
	public void addOtherDescription(List<Component> strings, Direction facing) {
		strings.add(Component.translatable(Embers.MODID + ".tooltip.goggles.redstone_signal"));
	}

    @Override
    public IItemHandler getInventoryCapability(Direction side) {
        if (!this.remove) {
            if (side == null)
                return inventory;
            else if (getConnection(side).transfer)
                return sideHandlers[side.get3DDataValue()];
        }
        return null;
    }
}