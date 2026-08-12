package hu.zoldleo.embers.blockentity;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.tile.IExtraCapabilityInformation;
import hu.zoldleo.embers.blockentity.capability_helper.IInventoryBlock;
import hu.zoldleo.embers.datagen.EmbersItemTags;

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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AlchemyPedestalBlockEntity extends BlockEntity implements IExtraCapabilityInformation, IInventoryBlock {
	public ItemStackHandler inventory;

	public AlchemyPedestalBlockEntity(BlockPos pPos, BlockState pBlockState) {
		this(RegistryManager.ALCHEMY_PEDESTAL_ENTITY.get(), pPos, pBlockState);
        inventory = new ItemStackHandler(1) {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return stack.is(EmbersItemTags.ASPECTUS);
            }

            @Override
            protected void onContentsChanged(int slot) {
                AlchemyPedestalBlockEntity.this.setChanged();
            }
        };
	}

	protected AlchemyPedestalBlockEntity(BlockEntityType<?> type, BlockPos pPos, BlockState pBlockState) {
		super(type, pPos, pBlockState);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider registries) {
		super.loadAdditional(nbt, registries);
		inventory.deserializeNBT(registries, nbt.getCompound("getCapability"));
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.put("getCapability", inventory.serializeNBT(registries));
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
		CompoundTag nbt = super.getUpdateTag(registries);
		nbt.put("getCapability", inventory.serializeNBT(registries));
		return nbt;
	}

    // Sync on block update
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}


	@Override
	public void setChanged() {
		super.setChanged();
		if (level instanceof ServerLevel serverLevel)
			serverLevel.getChunkSource().blockChanged(worldPosition);
	}

    @Override
    public boolean hasCapabilityDescription(BlockCapability<?, ?> capability) {
        return capability == Capabilities.ItemHandler.BLOCK;
    }

    @Override
    public void addCapabilityDescription(List<Component> strings, BlockCapability<?, ?> capability, Direction facing) {
        strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.BOTH, Embers.MODID + ".tooltip.goggles.item", Component.translatable(Embers.MODID + ".tooltip.goggles.item.aspectus")));
    }

    @Override
    public IItemHandler getInventoryCapability(Direction side) {
        return this.remove ? null : inventory;
    }
}