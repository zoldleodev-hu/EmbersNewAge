package hu.zoldleo.embers.blockentity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.event.DialInformationEvent;
import hu.zoldleo.embers.api.event.EmberEvent;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.api.tile.IBin;
import hu.zoldleo.embers.api.tile.IExtraCapabilityInformation;
import hu.zoldleo.embers.api.tile.IExtraDialInformation;
import hu.zoldleo.embers.api.tile.IUpgradeable;
import hu.zoldleo.embers.api.upgrades.UpgradeContext;
import hu.zoldleo.embers.api.upgrades.UpgradeUtil;
import hu.zoldleo.embers.blockentity.capability_helper.IEmberBlock;
import hu.zoldleo.embers.blockentity.capability_helper.IInventoryBlock;
import hu.zoldleo.embers.datagen.EmbersItemTags;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.particle.SmokeParticleOptions;
import hu.zoldleo.embers.power.DefaultEmberCapability;
import hu.zoldleo.embers.util.EmbersColors;
import hu.zoldleo.embers.util.Misc;
import hu.zoldleo.embers.util.sound.ISoundController;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class CinderPlinthBlockEntity extends BlockEntity implements ISoundController, IExtraDialInformation, IExtraCapabilityInformation, IUpgradeable, IEmberBlock, IInventoryBlock {
	public static double EMBER_COST = 0.5;
	public static int PROCESS_TIME = 40;
	int progress = 0;
	public IEmberCapability capability = new DefaultEmberCapability() {
		@Override
		public void onContentsChanged() {
			super.onContentsChanged();
			CinderPlinthBlockEntity.this.setChanged();
		}
	};
	public ItemStackHandler inventory = new ItemStackHandler(1) {

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return !stack.is(EmbersItemTags.CINDER_PLINTH_BLACKLIST);
		}

		@Override
		protected void onContentsChanged(int slot) {
			CinderPlinthBlockEntity.this.setChanged();
		}
	};
	static Random random = new Random();
	protected List<UpgradeContext> upgrades = new ArrayList<>();

	public static final int SOUND_PROCESS = 1;
	public static final int[] SOUND_IDS = new int[]{SOUND_PROCESS};

	HashSet<Integer> soundsPlaying = new HashSet<>();

	public CinderPlinthBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.CINDER_PLINTH_ENTITY.get(), pPos, pBlockState);
		capability.setEmberCapacity(4000);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		capability.readFromNBT(provider, nbt);
		if (nbt.contains("progress"))
			progress = nbt.getInt("progress");
		inventory.deserializeNBT(provider, nbt.getCompound("Inventory"));
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
        capability.writeToNBT(provider, nbt);
		nbt.putInt("progress", progress);
        nbt.put("Inventory", inventory.serializeNBT(provider));
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		capability.writeToNBT(provider, nbt);
		nbt.put("Inventory", inventory.serializeNBT(provider));
		return nbt;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, CinderPlinthBlockEntity blockEntity) {
		blockEntity.handleSound();
		blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos, Direction.values());
		UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, CinderPlinthBlockEntity blockEntity) {
		blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos, Direction.values());
		UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
		if (UpgradeUtil.doTick(blockEntity, blockEntity.upgrades))
			return;

		if (blockEntity.shouldWork()) {
			boolean cancel = UpgradeUtil.doWork(blockEntity, blockEntity.upgrades);
			if (!cancel) {
				blockEntity.progress++;
				((ServerLevel) level).sendParticles(new SmokeParticleOptions(EmbersColors.SMOKE_ID, 3.0f + random.nextFloat() * 0.4f), pos.getX() + 0.5f, pos.getY() + 0.875f, pos.getZ() + 0.5f, 1, 0.0125, 0.025, 0.0125, 1.0);
				double emberCost = UpgradeUtil.getTotalEmberConsumption(blockEntity, EMBER_COST, blockEntity.upgrades);
				UpgradeUtil.throwEvent(blockEntity, new EmberEvent(blockEntity, EmberEvent.EnumType.CONSUME, emberCost), blockEntity.upgrades);
				blockEntity.capability.removeAmount(emberCost, true);
				if (blockEntity.progress > UpgradeUtil.getWorkTime(blockEntity, PROCESS_TIME, blockEntity.upgrades)) {
					blockEntity.progress = 0;
					BlockEntity tile = level.getBlockEntity(pos.below());
					List<ItemStack> outputs = Lists.newArrayList(new ItemStack(Misc.getTaggedItem(EmbersItemTags.ASH_DUST), 1));
					UpgradeUtil.transformOutput(blockEntity, outputs, blockEntity.upgrades);
					blockEntity.inventory.extractItem(0, 1, false);
					for (ItemStack remainder : outputs) {
						if (tile instanceof IBin bin)
							remainder = bin.getInventory().insertItem(0, remainder, false);
						if (!remainder.isEmpty())
							level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, remainder));
					}
					((ServerLevel) level).sendParticles(new SmokeParticleOptions(EmbersColors.SMOKE_ID, 3.0f), pos.getX() + 0.5f, pos.getY() + 1.1f, pos.getZ() + 0.5f, 9, 0.0125, 0.025, 0.0125, 1.0);
				}
			}
		} else {
			if (blockEntity.progress != 0) {
				blockEntity.progress = 0;
				blockEntity.setChanged();
			}
		}
	}

	private boolean shouldWork() {
		return !inventory.getStackInSlot(0).isEmpty() && capability.getEmber() > 0;
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (level instanceof ServerLevel serverLevel)
			serverLevel.getChunkSource().blockChanged(worldPosition);
	}

	@Override
	public void playSound(int id) {
        if (id == SOUND_PROCESS)
            EmbersSounds.playMachineSound(this, SOUND_PROCESS, EmbersSounds.PLINTH_LOOP.get(), SoundSource.BLOCKS, true, 1.0f, 1.0f, (float) worldPosition.getX() + 0.5f, (float) worldPosition.getY() + 0.5f, (float) worldPosition.getZ() + 0.5f);
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
		return id == SOUND_PROCESS && shouldWork();
	}

	@Override
	public void addDialInformation(Direction facing, List<Component> information, String dialType) {
		UpgradeUtil.throwEvent(this, new DialInformationEvent(this, information, dialType), upgrades);
	}

	@Override
	public boolean hasCapabilityDescription(BlockCapability<?, ?> capability) {
		return capability == Capabilities.ItemHandler.BLOCK;
	}

	@Override
	public void addCapabilityDescription(List<Component> strings, BlockCapability<?, ?> capability, Direction facing) {
		if(capability == Capabilities.ItemHandler.BLOCK) {
			strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.INPUT, Embers.MODID + ".tooltip.goggles.item", null));
			strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.OUTPUT, Embers.MODID + ".tooltip.goggles.item", Component.translatable(Embers.MODID + ".tooltip.goggles.item.ash")));
		}
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