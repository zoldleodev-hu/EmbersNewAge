package hu.zoldleo.embers.blockentity;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.event.DialInformationEvent;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.api.tile.IExtraCapabilityInformation;
import hu.zoldleo.embers.api.tile.IExtraDialInformation;
import hu.zoldleo.embers.api.tile.IUpgradeable;
import hu.zoldleo.embers.api.upgrades.UpgradeContext;
import hu.zoldleo.embers.api.upgrades.UpgradeUtil;
import hu.zoldleo.embers.blockentity.capability_helper.IEmberBlock;
import hu.zoldleo.embers.blockentity.capability_helper.IInventoryBlock;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.network.message.MessageCrystalCellGrowFX;
import hu.zoldleo.embers.particle.GlowParticleOptions;
import hu.zoldleo.embers.power.DefaultEmberCapability;
import hu.zoldleo.embers.recipe.base.IEmberActivationRecipe;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class CrystalCellBlockEntity extends BlockEntity implements ISoundController, IExtraDialInformation, IExtraCapabilityInformation, IUpgradeable, IEmberBlock, IInventoryBlock {
	public static final int MAX_CAPACITY = 1440000;
	Random random = new Random();
	public long ticksExisted = 0;
	public float angle = 0;
	public long seed;
	public double renderCapacity;
	public double renderCapacityLast;
	public IEmberCapability capability = new DefaultEmberCapability() {
		@Override
		public void onContentsChanged() {
			super.onContentsChanged();
			CrystalCellBlockEntity.this.setChanged();
		}
	};
	public ItemStackHandler inventory = new ItemStackHandler(1) {
		@Override
		protected void onContentsChanged(int slot) {
			CrystalCellBlockEntity.this.setChanged();
		}

		@Override
		public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
			if (Misc.getRecipe(cachedRecipe, RegistryManager.EMBER_ACTIVATION.get(), new SingleRecipeInput(stack), level) != null)
				return super.insertItem(slot, stack, simulate);
			return stack;
		}
	};
	public RecipeHolder<IEmberActivationRecipe> cachedRecipe = null;
	protected List<UpgradeContext> upgrades;

	public static final int SOUND_AMBIENT = 1;
	public static final int[] SOUND_IDS = new int[]{SOUND_AMBIENT};

	HashSet<Integer> soundsPlaying = new HashSet<>();

	public CrystalCellBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.CRYSTAL_CELL_ENTITY.get(), pPos, pBlockState);
		capability.setEmberCapacity(64000);
		seed = random.nextLong();
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		seed = nbt.getLong("seed");
		if (nbt.contains("Inventory"))
			inventory.deserializeNBT(provider, nbt.getCompound("Inventory"));
		capability.deserializeNBT(provider, nbt);
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
		nbt.putLong("seed", seed);
		nbt.put("Inventory", inventory.serializeNBT(provider));
		capability.writeToNBT(provider, nbt);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		nbt.putLong("seed", seed);
		capability.writeToNBT(provider, nbt);
		return nbt;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public static void commonTick(Level level, BlockPos pos, BlockState state, CrystalCellBlockEntity blockEntity) {
		blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos, new Direction[]{Direction.DOWN});
		UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
		if (UpgradeUtil.doTick(blockEntity, blockEntity.upgrades))
			return;
		if (level.isClientSide)
			blockEntity.handleSound();
		blockEntity.ticksExisted++;
		blockEntity.renderCapacityLast = blockEntity.renderCapacity;
		if (blockEntity.renderCapacity < blockEntity.capability.getEmberCapacity())
			blockEntity.renderCapacity += Math.min(10000, blockEntity.capability.getEmberCapacity() - blockEntity.renderCapacity);
		else
			blockEntity.renderCapacity -= Math.min(10000, blockEntity.renderCapacity - blockEntity.capability.getEmberCapacity());
		if (!blockEntity.inventory.getStackInSlot(0).isEmpty() && blockEntity.ticksExisted % 4 == 0) {
			boolean cancel = UpgradeUtil.doWork(blockEntity, blockEntity.upgrades);
			if (!cancel) {
				RecipeWrapper wrapper = new RecipeWrapper(blockEntity.inventory);
				blockEntity.cachedRecipe = Misc.getRecipe(blockEntity.cachedRecipe, RegistryManager.EMBER_ACTIVATION.get(), wrapper, level);
				if (blockEntity.cachedRecipe != null) {
					double emberValue = blockEntity.cachedRecipe.value().process(wrapper);
					if (level instanceof ServerLevel serverLevel) {
						blockEntity.inventory.extractItem(0, 1, false);
						int maxCapacity = UpgradeUtil.getOtherParameter(blockEntity, "max_capacity", MAX_CAPACITY, blockEntity.upgrades);
						if (blockEntity.capability.getEmberCapacity() < maxCapacity) {
							blockEntity.capability.setEmberCapacity(Math.min(maxCapacity, blockEntity.capability.getEmberCapacity() + emberValue * 10));
							blockEntity.setChanged();
						}
                        PacketDistributor.sendToPlayersTrackingChunk(serverLevel, level.getChunkAt(pos).getPos(), new MessageCrystalCellGrowFX(pos, blockEntity.capability.getEmberCapacity()));
					}
				}
			}
		}
		float numLayers = 2 + (float) Math.floor(blockEntity.capability.getEmberCapacity() / 120000.0f);
		if (level.isClientSide) {
			for (int i = 0; i < numLayers / 2; i++) {
				float layerHeight = 0.25f;
				float height = layerHeight * numLayers;
				float xDest = pos.getX() + 0.5f;
				float yDest = pos.getY() + height / 2.0f + 1.5f;
				float zDest = pos.getZ() + 0.5f;
				float x = pos.getX() + 0.5f + 2.0f * (blockEntity.random.nextFloat() - 0.5f);
				float z = pos.getZ() + 0.5f + 2.0f * (blockEntity.random.nextFloat() - 0.5f);
				float y = pos.getY() + 1.0f;
				level.addParticle(new GlowParticleOptions(EmbersColors.EMBER_ID, new Vec3((xDest - x) / 1.0f * blockEntity.random.nextFloat(), (yDest - y) / 1.0f * blockEntity.random.nextFloat(), (zDest - z) / 1.0f * blockEntity.random.nextFloat()), 2.0F), x, y, z, 0, 0, 0);
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
	public void playSound(int id) {
		switch (id) {
		case SOUND_AMBIENT:
			EmbersSounds.playMachineSound(this, SOUND_AMBIENT, EmbersSounds.CRYSTAL_CELL_LOOP.get(), SoundSource.BLOCKS, true, 1.0f, 1.0f, worldPosition.getX() + 0.5f, worldPosition.getY() - 0.5f, worldPosition.getZ() + 0.5f);
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
		return id == SOUND_AMBIENT;
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
		if (capability == Capabilities.ItemHandler.BLOCK)
			strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.INPUT, Embers.MODID + ".tooltip.goggles.item", Component.translatable(Embers.MODID + ".tooltip.goggles.item.ember")));
	}

	@Override
	public boolean isSideUpgradeSlot(Direction face) {
		return face == Direction.DOWN;
	}

    @Override
    public IEmberCapability getEmberCapability(Direction side) {
        if (!this.remove && (side == null || side == Direction.DOWN))
            return capability;
        return null;
    }

    @Override
    public IItemHandler getInventoryCapability(Direction side) {
        if (!this.remove && (side == null || side == Direction.DOWN))
            return inventory;
        return null;
    }
}
