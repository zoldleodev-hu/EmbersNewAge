package hu.zoldleo.embers.blockentity;

import java.util.List;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.tile.IExtraCapabilityInformation;
import hu.zoldleo.embers.blockentity.capability_helper.IInventoryBlock;
import hu.zoldleo.embers.recipe.context.CatalysisCombustionContext;
import hu.zoldleo.embers.recipe.base.ICatalysisCombustionRecipe;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class CombustionChamberBlockEntity extends BlockEntity implements IExtraCapabilityInformation, IInventoryBlock {
	public static ItemStack machine = new ItemStack(RegistryManager.COMBUSTION_CHAMBER_ITEM.get());
	int progress = 0;
	double multiplier = 0;
	public ItemStackHandler inventory = new ItemStackHandler(1) {
		@Override
		protected void onContentsChanged(int slot) {
			CombustionChamberBlockEntity.this.setChanged();
		}
	};
	public RecipeHolder<ICatalysisCombustionRecipe> cachedRecipe = null;

	public CombustionChamberBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.COMBUSTION_CHAMBER_ENTITY.get(), pPos, pBlockState);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		if (nbt.contains("Inventory"))
			inventory.deserializeNBT(provider, nbt.getCompound("Inventory"));
		if (nbt.contains("progress"))
			progress = nbt.getInt("progress");
		multiplier = nbt.getDouble("multiplier");
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
		nbt.put("Inventory", inventory.serializeNBT(provider));
		nbt.putInt("progress", progress);
		nbt.putDouble("multiplier", multiplier);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		nbt.putDouble("multiplier", multiplier);
		return nbt;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, CombustionChamberBlockEntity blockEntity) {
		if (blockEntity.progress > 0) {
			blockEntity.progress --;
			if (blockEntity.progress == 0) {
				blockEntity.multiplier = 0;
				((ServerLevel) level).getChunkSource().blockChanged(pos);
			}
			blockEntity.setChanged();
		}
		if (blockEntity.progress == 0 && !blockEntity.inventory.getStackInSlot(0).isEmpty()) {
			CatalysisCombustionContext wrapper = new CatalysisCombustionContext(blockEntity.inventory, machine);
			blockEntity.cachedRecipe = Misc.getRecipe(blockEntity.cachedRecipe, RegistryManager.CATALYSIS_COMBUSTION.get(), wrapper, level);
			if (blockEntity.cachedRecipe != null) {
				blockEntity.multiplier = blockEntity.cachedRecipe.value().getmultiplier(wrapper);
				blockEntity.progress = blockEntity.cachedRecipe.value().getBurnTIme(wrapper);

				//the recipe is responsible for taking items from the getCapability
				blockEntity.cachedRecipe.value().process(wrapper);
				((ServerLevel) level).getChunkSource().blockChanged(pos);
			}
		}
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (level instanceof ServerLevel)
			((ServerLevel) level).getChunkSource().blockChanged(worldPosition);
	}

	@Override
	public boolean hasCapabilityDescription(BlockCapability<?, ?> capability) {
		return capability == Capabilities.ItemHandler.BLOCK;
	}

	@Override
	public void addCapabilityDescription(List<Component> strings, BlockCapability<?, ?> capability, Direction facing) {
		if (capability == Capabilities.ItemHandler.BLOCK)
			strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.INPUT, Embers.MODID + ".tooltip.goggles.item", Component.translatable(Embers.MODID + ".tooltip.goggles.item.combustion")));
	}

    @Override
    public IItemHandler getInventoryCapability(Direction side) {
        return this.remove ? null : inventory;
    }
}