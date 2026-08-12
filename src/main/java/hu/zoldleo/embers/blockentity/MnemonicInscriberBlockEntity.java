package hu.zoldleo.embers.blockentity;

import java.util.List;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.tile.IExtraCapabilityInformation;
import hu.zoldleo.embers.api.upgrades.IUpgradeProvider;
import hu.zoldleo.embers.blockentity.capability_helper.IInventoryBlock;
import hu.zoldleo.embers.blockentity.capability_helper.IUpgradeBlock;
import hu.zoldleo.embers.datagen.EmbersItemTags;
import hu.zoldleo.embers.upgrade.MnemonicInscriberUpgrade;

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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class MnemonicInscriberBlockEntity extends BlockEntity implements IExtraCapabilityInformation, IUpgradeBlock, IInventoryBlock {
	public ItemStackHandler inventory = new ItemStackHandler(1) {
		@Override
		public int getSlotLimit(int slot) {
			return 1;
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return stack.is(EmbersItemTags.INSCRIBABLE_PAPER);
		}

		@Override
		protected void onContentsChanged(int slot) {
			MnemonicInscriberBlockEntity.this.setChanged();
		}
	};

	public MnemonicInscriberUpgrade upgrade;

	public MnemonicInscriberBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.MNEMONIC_INSCRIBER_ENTITY.get(), pPos, pBlockState);
		upgrade = new MnemonicInscriberUpgrade(this);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		inventory.deserializeNBT(provider, nbt.getCompound("Inventory"));
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
		nbt.put("Inventory", inventory.serializeNBT(provider));
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		nbt.put("Inventory", inventory.serializeNBT(provider));
		return nbt;
	}

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
		if (capability == Capabilities.ItemHandler.BLOCK)
			strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.INPUT, Embers.MODID + ".tooltip.goggles.item", Component.translatable(Embers.MODID + ".tooltip.goggles.item.paper")));
	}

    @Override
    public IUpgradeProvider getUpgradeCapability(Direction side) {
        if (this.remove)
            return null;
        if (side == null)
            return upgrade;
        if (level != null && getBlockState().hasProperty(BlockStateProperties.FACING))
            if (side.getOpposite() == getBlockState().getValue(BlockStateProperties.FACING))
                return upgrade;
        return null;
    }

    @Override
    public IItemHandler getInventoryCapability(Direction side) {
        if (this.remove)
            return null;
        if (side == null)
            return inventory;
        if (level != null && getBlockState().hasProperty(BlockStateProperties.FACING))
            if (side.getOpposite() != getBlockState().getValue(BlockStateProperties.FACING))
                return inventory;
        return null;
    }
}