package hu.zoldleo.embers.blockentity;

import java.util.Random;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.filter.FilterItem;
import hu.zoldleo.embers.api.filter.IFilter;
import hu.zoldleo.embers.api.item.IFilterItem;
import hu.zoldleo.embers.blockentity.capability_helper.IInventoryBlock;
import hu.zoldleo.embers.particle.VaporParticleOptions;
import hu.zoldleo.embers.util.EmbersColors;
import hu.zoldleo.embers.util.FilterUtil;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class ItemTransferBlockEntity extends ItemPipeBlockEntityBase implements IInventoryBlock {
	public static final int PRIORITY_TRANSFER = -10;
	public ItemStack filterItem = ItemStack.EMPTY;
	public boolean syncFilter = true;
	IItemHandler outputSide;

	IFilter filter = FilterUtil.FILTER_ANY;

	public ItemTransferBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.ITEM_TRANSFER_ENTITY.get(), pPos, pBlockState);
		syncConnections = false;
		saveConnections = false;
	}

	@Override
	protected void initInventory() {
		inventory = new ItemStackHandler(1) {
			@Override
			public int getSlotLimit(int slot) {
				return ItemTransferBlockEntity.this.getCapacity();
			}

			@Override
			protected void onContentsChanged(int slot) {
				ItemTransferBlockEntity.this.setChanged();
			}

			@Override
			public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
				return ItemTransferBlockEntity.this.acceptsItem(stack) ?
					super.insertItem(slot, stack, simulate) : stack;
			}

			@Override
			public boolean isItemValid(int slot, @NotNull ItemStack stack) {
				return ItemTransferBlockEntity.this.acceptsItem(stack);
			}
		};
		outputSide = Misc.makeRestrictedItemHandler(inventory, false, true);
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
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		if (syncFilter)
            nbt.put("filter", filterItem.saveOptional(provider));
		return nbt;
	}

	public void setupFilter() {
		Item item = this.filterItem.getItem();
		if (item instanceof IFilterItem)
			filter = ((IFilterItem) item).getFilter(this.filterItem);
		else if(!this.filterItem.isEmpty())
			filter = new FilterItem(this.filterItem);
		else
			filter = FilterUtil.FILTER_ANY;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ItemTransferBlockEntity blockEntity) {
		if (level instanceof ServerLevel serverLevel && blockEntity.clogged && blockEntity.isAnySideUnclogged()) {
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
			serverLevel.sendParticles(new VaporParticleOptions(EmbersColors.VAPOR_ID, new Vec3(vx, vy, vz), 1.0f), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0, 0, 0, 1.0);
		}
		ItemPipeBlockEntityBase.serverTick(level, pos, state, blockEntity);
	}

	@Override
	public int getCapacity() {
		return 4;
	}

	@Override
	public int getPriority(Direction facing) {
		return PRIORITY_TRANSFER;
	}

	@Override
	public PipeConnection getConnection(Direction facing) {
		return getBlockState().hasProperty(BlockStateProperties.FACING) &&
                getBlockState().getValue(BlockStateProperties.FACING).getAxis() == facing.getAxis() ?
                PipeConnection.PIPE : PipeConnection.NONE;
	}

	@Override
	protected boolean isFrom(Direction facing) {
		return getBlockState().getValue(BlockStateProperties.FACING) == facing;
	}

    @Override
    public IItemHandler getInventoryCapability(Direction side) {
        if (!this.remove) {
            if (side == null)
                return inventory;
            if (getBlockState().hasProperty(BlockStateProperties.FACING)) {
                Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
                if (side.getOpposite() == facing)
                    return outputSide;
                else if (side.getAxis() == facing.getAxis())
                    return inventory;
            }
        }
        return null;
    }
}