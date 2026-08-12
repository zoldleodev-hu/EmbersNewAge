package hu.zoldleo.embers.blockentity;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import hu.zoldleo.embers.blockentity.capability_helper.IEmberBlock;
import hu.zoldleo.embers.blockentity.capability_helper.IInventoryBlock;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import hu.zoldleo.embers.ConfigManager;
import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.capabilities.EmbersCapabilities;
import hu.zoldleo.embers.api.event.DialInformationEvent;
import hu.zoldleo.embers.api.event.EmberEvent;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.api.tile.IExtraCapabilityInformation;
import hu.zoldleo.embers.api.tile.IExtraDialInformation;
import hu.zoldleo.embers.api.tile.IUpgradeable;
import hu.zoldleo.embers.api.upgrades.UpgradeContext;
import hu.zoldleo.embers.api.upgrades.UpgradeUtil;
import hu.zoldleo.embers.block.EmberDialBlock;
import hu.zoldleo.embers.block.ItemDialBlock;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.particle.GlowParticleOptions;
import hu.zoldleo.embers.power.DefaultEmberCapability;
import hu.zoldleo.embers.util.sound.ISoundController;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CopperChargerBlockEntity extends BlockEntity implements ISoundController, IExtraDialInformation, IExtraCapabilityInformation, IUpgradeable, IEmberBlock, IInventoryBlock {
	public IEmberCapability capability = new DefaultEmberCapability() {
		@Override
		public void onContentsChanged() {
			super.onContentsChanged();
			CopperChargerBlockEntity.this.setChanged();
		}
	};

	public ItemStackHandler inventory = new ItemStackHandler(1) {
		@Override
		public int getSlotLimit(int slot) {
			return 1;
		}

		@Override
		public boolean isItemValid(int slot, @NotNull ItemStack stack) {
			return stack.getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM, null) != null;
		}

		@Override
		protected void onContentsChanged(int slot) {
			CopperChargerBlockEntity.this.setChanged();
		}
	};

	public int angle = 0;
	public int turnRate = 1;
	static Random random = new Random();
	public boolean isWorking;
	public boolean wasWorking;
	public boolean reverse = false;
	protected List<UpgradeContext> upgrades;

	public static final int SOUND_PROCESS = 1;
	public static final int SOUND_REVERSE = 2;
	public static final int[] SOUND_IDS = new int[]{SOUND_PROCESS, SOUND_REVERSE};

	HashSet<Integer> soundsPlaying = new HashSet<>();

	public CopperChargerBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.COPPER_CHARGER_ENTITY.get(), pPos, pBlockState);
		capability.setEmberCapacity(24000);
		capability.setEmber(0);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		capability.readFromNBT(provider, nbt);
		if (nbt.contains("Inventory")) // TODO: why conditional?
			inventory.deserializeNBT(provider, nbt.getCompound("Inventory"));
		isWorking = nbt.getBoolean("working");
		reverse = nbt.getBoolean("reverse");
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
		capability.writeToNBT(provider, nbt);
		nbt.put("Inventory", inventory.serializeNBT(provider));
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		capability.writeToNBT(provider, nbt);
		nbt.put("Inventory", inventory.serializeNBT(provider));
		nbt.putBoolean("working", isWorking);
		nbt.putBoolean("reverse", reverse);
		return nbt;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, CopperChargerBlockEntity blockEntity) {
		blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos, Direction.values());
		UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
		blockEntity.handleSound();
		blockEntity.angle += blockEntity.turnRate;

		if (blockEntity.isWorking && blockEntity.capability.getEmber() > 0) {
			for (int i = 0; i < Math.ceil(blockEntity.capability.getEmber() / 500.0); i++) {
				level.addParticle(GlowParticleOptions.EMBER, pos.getX()+0.25f+random.nextFloat()*0.5f, pos.getY()+0.25f+random.nextFloat()*0.5f, pos.getZ()+0.25f+random.nextFloat()*0.5f,
						(Math.random() * 2.0D - 1.0D) * 0.2D, (Math.random() * 2.0D - 1.0D) * 0.2D, (Math.random() * 2.0D - 1.0D) * 0.2D);
			}
		}
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, CopperChargerBlockEntity blockEntity) {
		blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos, Direction.values());
		UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
		if (UpgradeUtil.doTick(blockEntity, blockEntity.upgrades))
			return;

		ItemStack stack = blockEntity.inventory.getStackInSlot(0);
		blockEntity.wasWorking = blockEntity.isWorking;
		blockEntity.isWorking = false;

		IEmberCapability itemCapability = stack.getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM, null);
		boolean cancel = UpgradeUtil.doWork(blockEntity, blockEntity.upgrades);
		if (!cancel && itemCapability != null) {
			double transferRate = UpgradeUtil.getTotalSpeedModifier(blockEntity, blockEntity.upgrades) * ConfigManager.CHARGER_MAX_TRANSFER.get();
			double emberAdded;
			if (transferRate > 0) {
				emberAdded = itemCapability.addAmount(Math.min(Math.abs(transferRate), blockEntity.capability.getEmber()), true);
				blockEntity.capability.removeAmount(emberAdded, true);
				blockEntity.reverse = false;
			} else {
				emberAdded = blockEntity.capability.addAmount(Math.min(Math.abs(transferRate), itemCapability.getEmber()), true);
				itemCapability.removeAmount(emberAdded, true);
				blockEntity.reverse = true;
			}
			if (emberAdded > 0) {
				UpgradeUtil.throwEvent(blockEntity, new EmberEvent(blockEntity, EmberEvent.EnumType.TRANSFER, emberAdded), blockEntity.upgrades);
				blockEntity.isWorking = true;
			}
		}
		if (blockEntity.wasWorking != blockEntity.isWorking) {
			blockEntity.setChanged();
		}
	}

	@Override
	public void playSound(int id) {
		switch (id) {
		case SOUND_PROCESS:
			EmbersSounds.playMachineSound(this, SOUND_PROCESS, EmbersSounds.COPPER_CHARGER_LOOP.get(), SoundSource.BLOCKS, true, 1.0f, 1.0f, (float)worldPosition.getX()+0.5f,(float)worldPosition.getY()+0.5f,(float)worldPosition.getZ()+0.5f);
			break;
		case SOUND_REVERSE:
			EmbersSounds.playMachineSound(this, SOUND_REVERSE, EmbersSounds.COPPER_CHARGER_SIPHON_LOOP.get(), SoundSource.BLOCKS, true, 1.0f, 1.0f, (float)worldPosition.getX()+0.5f,(float)worldPosition.getY()+0.5f,(float)worldPosition.getZ()+0.5f);
			break;
		}
		soundsPlaying.add(id);
	}

	@Override
	public void stopSound(int id) {
		soundsPlaying.remove(id);
	}

	@Override
	public boolean isSoundPlaying(int id) {
		return soundsPlaying.contains(id);
	}

	@Override
	public int[] getSoundIDs() {
		return SOUND_IDS;
	}

	@Override
	public boolean shouldPlaySound(int id) {
		return isWorking && (reverse ? id == SOUND_REVERSE : id == SOUND_PROCESS);
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (level instanceof ServerLevel)
			((ServerLevel) level).getChunkSource().blockChanged(worldPosition);
	}

	@Override
	public void addDialInformation(Direction facing, List<Component> information, String dialType) {
		if (EmberDialBlock.DIAL_TYPE.equals(dialType)) {
			ItemStack stack = inventory.getStackInSlot(0);
			IEmberCapability itemCapability = stack.getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM,null);
			if (itemCapability != null) {
				information.add(ItemDialBlock.formatItemStack(stack));
				information.add(EmberDialBlock.formatEmber(itemCapability.getEmber(),itemCapability.getEmberCapacity()));
			}
		}
		UpgradeUtil.throwEvent(this, new DialInformationEvent(this, information, dialType), upgrades);
	}

	@Override
	public boolean hasCapabilityDescription(BlockCapability<?, ?> capability) {
		return capability == Capabilities.ItemHandler.BLOCK;
	}

	@Override
	public void addCapabilityDescription(List<Component> strings, BlockCapability<?, ?> capability, Direction facing) {
		if(capability == Capabilities.ItemHandler.BLOCK)
			strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.BOTH, Embers.MODID + ".tooltip.goggles.item", Component.translatable(Embers.MODID + ".tooltip.goggles.item.ember_storage")));
	}

	@Override
	public boolean isSideUpgradeSlot(Direction face) {
		return true;
	}

    @Override
    public IEmberCapability getEmberCapability(Direction side) {
        return this.remove ? null : capability;
    }

    @Override
    public IItemHandler getInventoryCapability(Direction side) {
        return this.remove ? null : inventory;
    }
}