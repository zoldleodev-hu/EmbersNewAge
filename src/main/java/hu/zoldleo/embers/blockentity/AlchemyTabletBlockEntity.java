package hu.zoldleo.embers.blockentity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import hu.zoldleo.embers.blockentity.capability_helper.IInventoryBlock;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.event.AlchemyResultEvent;
import hu.zoldleo.embers.api.event.AlchemyStartEvent;
import hu.zoldleo.embers.api.event.MachineRecipeEvent;
import hu.zoldleo.embers.api.misc.AlchemyResult;
import hu.zoldleo.embers.api.tile.IBin;
import hu.zoldleo.embers.api.tile.IExtraCapabilityInformation;
import hu.zoldleo.embers.api.tile.ISparkable;
import hu.zoldleo.embers.api.tile.IUpgradeable;
import hu.zoldleo.embers.api.upgrades.UpgradeContext;
import hu.zoldleo.embers.api.upgrades.UpgradeUtil;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.particle.AlchemyCircleParticleOptions;
import hu.zoldleo.embers.particle.GlowParticleOptions;
import hu.zoldleo.embers.particle.StarParticleOptions;
import hu.zoldleo.embers.recipe.context.AlchemyContext;
import hu.zoldleo.embers.recipe.base.IAlchemyRecipe;
import hu.zoldleo.embers.recipe.base.IAlchemyRecipe.PedestalContents;
import hu.zoldleo.embers.util.EmbersColors;
import hu.zoldleo.embers.util.Misc;
import hu.zoldleo.embers.util.sound.ISoundController;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AlchemyTabletBlockEntity extends BlockEntity implements ISparkable, ISoundController, IExtraCapabilityInformation, IUpgradeable, IInventoryBlock {
	public static final Direction[] UPGRADE_SIDES = {
			Direction.NORTH,
			Direction.SOUTH,
			Direction.WEST,
			Direction.EAST,
			Direction.DOWN
	};
	public static final int CONSUME_AMOUNT = 2;
	public static final int SPARK_THRESHOLD = 1000;
	public static final int PROCESSING_TIME = 40;

	public TabletItemStackHandler inventory = new TabletItemStackHandler(1, this);
	public IItemHandler outputHandler = new IItemHandler() {
		@Override
		public int getSlots() {
			return inventory.getSlots();
		}

		@Override
		public @NotNull ItemStack getStackInSlot(int slot) {
			return inventory.getStackInSlot(slot);
		}

		@Override
		public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
			return inventory.insertItem(slot, stack, simulate);
		}

		@Override
		public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (AlchemyTabletBlockEntity.this.outputMode)
				return inventory.extractItem(slot,amount,simulate);
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot) {
			return inventory.getSlotLimit(slot);
		}

		@Override
		public boolean isItemValid(int slot, @NotNull ItemStack stack) {
			return inventory.isItemValid(slot, stack);
		}
	};
	public boolean outputMode = false;
	public int progress = 0;
	public int process = 0;
	static Random rand = new Random();
	public RecipeHolder<IAlchemyRecipe> cachedRecipe = null;
	protected List<UpgradeContext> upgrades = new ArrayList<>();

	public static final int SOUND_PROCESS = 1;
	public static final int[] SOUND_IDS = new int[]{SOUND_PROCESS};

	HashSet<Integer> soundsPlaying = new HashSet<>();

	public AlchemyTabletBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.ALCHEMY_TABLET_ENTITY.get(), pPos, pBlockState);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider registries) {
		super.loadAdditional(nbt, registries);
		if (nbt.contains("outputMode"))
			outputMode = nbt.getBoolean("outputMode");
		progress = nbt.getInt("progress");
		inventory.deserializeNBT(registries, nbt.getCompound("getCapability"));
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putBoolean("outputMode", outputMode);
		nbt.putInt("progress", progress);
		nbt.put("getCapability", inventory.serializeNBT(registries));
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
		CompoundTag nbt = super.getUpdateTag(registries);
		nbt.putInt("progress", progress);
		nbt.put("getCapability", inventory.serializeNBT(registries));
		return nbt;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, AlchemyTabletBlockEntity blockEntity) {
		blockEntity.handleSound();
		if (blockEntity.progress > 0) {
			if (blockEntity.process < 20)
				blockEntity.process++;

			List<AlchemyPedestalTopBlockEntity> pedestals = getNearbyPedestals(level, pos);

			for (AlchemyPedestalTopBlockEntity pedestal : pedestals) {
				pedestal.setActive(3);

				level.addParticle(StarParticleOptions.EMBER, pedestal.getBlockPos().getX() + 0.5f, pedestal.getBlockPos().getY() + 0.75f, pedestal.getBlockPos().getZ() + 0.5f, 0, 0.00001, 0);
				for (int j = 0; j < 16; j++) {
					float coeff = rand.nextFloat();
					float x = (pos.getX() + 0.5f) * coeff + (1.0f - coeff) * (pedestal.getBlockPos().getX() + 0.5f);
					float y = (pos.getY() + 0.875f) * coeff + (1.0f - coeff) * (pedestal.getBlockPos().getY() + 0.75f);
					float z = (pos.getZ() + 0.5f) * coeff + (1.0f - coeff) * (pedestal.getBlockPos().getZ() + 0.5f);
					level.addParticle(GlowParticleOptions.EMBER, x, y, z, 0, 0.00001, 0);
				}
			}

			if (level.getGameTime() % 10 == 0 && !pedestals.isEmpty()) {
				AlchemyPedestalTopBlockEntity pedestal = pedestals.get(rand.nextInt(pedestals.size()));
				float dx = (pos.getX() + 0.5f) - (pedestal.getBlockPos().getX() + 0.5f);
				float dy = (pos.getY() + 0.875f) - (pedestal.getBlockPos().getY() + 0.75f);
				float dz = (pos.getZ() + 0.5f) - (pedestal.getBlockPos().getZ() + 0.5f);
				float speed = 0.5f;
				for (int j = 0; j < 20; j++) {
					level.addParticle(StarParticleOptions.EMBER, pedestal.getBlockPos().getX() + 0.5f, pedestal.getBlockPos().getY() + 0.75f, pedestal.getBlockPos().getZ() + 0.5f, dx * speed, dy * speed, dz * speed);
				}
			}
		} else if (blockEntity.progress == 0) {
			if (blockEntity.process > 0) {
				blockEntity.process--;
			}
		}
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, AlchemyTabletBlockEntity blockEntity) {
		blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos, UPGRADE_SIDES); //Defer to when events are added to the getCapability system
		UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
		if (blockEntity.progress > 0) {
			UpgradeUtil.doWork(blockEntity, blockEntity.upgrades);
			if (level.getGameTime() % 10 == 0) {
				List<AlchemyPedestalTopBlockEntity> pedestals = getNearbyPedestals(level, pos);
				if (blockEntity.progress < UpgradeUtil.getWorkTime(blockEntity, PROCESSING_TIME, blockEntity.upgrades)) {
					blockEntity.progress++;
					blockEntity.setChanged();
				} else {
					List<PedestalContents> contents = getPedestalContents(pedestals);
					AlchemyContext context = new AlchemyContext(blockEntity.inventory.getStackInSlot(0), contents, ((ServerLevel) level).getSeed());
					blockEntity.cachedRecipe = Misc.getRecipe(blockEntity.cachedRecipe, RegistryManager.ALCHEMY.get(), context, level);
					if (blockEntity.cachedRecipe != null) {
						AlchemyResult result = blockEntity.cachedRecipe.value().getResult(context, blockEntity.cachedRecipe.id());

						AlchemyResultEvent event = new AlchemyResultEvent(blockEntity, blockEntity.cachedRecipe, result, CONSUME_AMOUNT);
						UpgradeUtil.throwEvent(blockEntity, event, blockEntity.upgrades);

						ItemStack stack = event.isFailure() ? event.getResult().createResultStack(event.getResultStack().copy()) : event.getResultStack().copy();
						SoundEvent finishSound = event.isFailure() ? EmbersSounds.ALCHEMY_FAIL.get() : EmbersSounds.ALCHEMY_SUCCESS.get();
						level.playSound(null, pos, finishSound, SoundSource.BLOCKS, 1.0f, 1.0f);

						if (!event.isFailure()) {
							UpgradeUtil.throwEvent(blockEntity, new MachineRecipeEvent.Success<>(blockEntity, blockEntity.cachedRecipe), blockEntity.upgrades);
							for (AlchemyPedestalTopBlockEntity pedestal : pedestals)
								pedestal.inventory.setStackInSlot(0, ItemStack.EMPTY);
						} else {
							//this doesn't always consume the same amount of ingredients
							//there is a chance the same pedestal gets cleared multiple times
							for (int i = 0; i < event.getConsumeAmount(); i++)
								pedestals.get(rand.nextInt(pedestals.size())).inventory.setStackInSlot(0, ItemStack.EMPTY);
						}

						((ServerLevel) level).sendParticles(new GlowParticleOptions(EmbersColors.EMBER_ID, 4.0f), pos.getX() + 0.5f, pos.getY() + 0.875, pos.getZ() + 0.5f, 24, 0.1, 0.1, 0.1, 0.5);

						blockEntity.progress = 0;

						BlockEntity outputTile = level.getBlockEntity(pos.below());
						if (outputTile instanceof IBin bin && bin.getInventory().insertItem(0, stack, true).isEmpty()) {
							blockEntity.inventory.extractItem(0, 1, false);
							bin.getInventory().insertItem(0, stack, false);
						} else {
							blockEntity.outputMode = true;
							blockEntity.inventory.extractItem(0, 1, false);
							ItemStack remainder = blockEntity.inventory.forceInsertItem(0, stack, false);
							if (!remainder.isEmpty()) {
								level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, remainder));
							}
							blockEntity.outputMode = true;
						}
					} else {
						blockEntity.progress = 0;
						blockEntity.setChanged();
					}
				}
			}
		}
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (level instanceof ServerLevel serverLevel)
			serverLevel.getChunkSource().blockChanged(worldPosition);
	}

	@Override
	public void sparkProgress(BlockEntity tile, double ember) {
		if (progress != 0 || ember < SPARK_THRESHOLD || !(level instanceof ServerLevel serverLevel))
			return;

		List<PedestalContents> pedestals = getPedestalContents(getNearbyPedestals(level, worldPosition));
		AlchemyContext context = new AlchemyContext(inventory.getStackInSlot(0), pedestals, serverLevel.getSeed());
		cachedRecipe = Misc.getRecipe(cachedRecipe, RegistryManager.ALCHEMY.get(), context, level);

		AlchemyStartEvent event = new AlchemyStartEvent(this, context, cachedRecipe);
		UpgradeUtil.throwEvent(this, event, upgrades);

		if (event.getRecipe() != null) {
			int time = UpgradeUtil.getWorkTime(this, PROCESSING_TIME * 10, upgrades);
			serverLevel.sendParticles(new AlchemyCircleParticleOptions(EmbersColors.EMBER_ID, 1.0F, time + 20), worldPosition.getX() + 0.5, worldPosition.getY() + 1.01, worldPosition.getZ() + 0.5, 5, 0, 0, 0, 1);
			progress = 1;
			setChanged();
			level.playSound(null, worldPosition, EmbersSounds.ALCHEMY_START.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
		}
	}

	public static ArrayList<PedestalContents> getPedestalContents(List<AlchemyPedestalTopBlockEntity> pedestals) {
		ArrayList<PedestalContents> contents = new ArrayList<>();
		for (AlchemyPedestalTopBlockEntity pedestal : pedestals) {
			contents.add(pedestal.getContents());
		}
		return contents;
	}

	public static ArrayList<AlchemyPedestalTopBlockEntity> getNearbyPedestals(Level world, BlockPos pos) {
		ArrayList<AlchemyPedestalTopBlockEntity> pedestals = new ArrayList<>();
		BlockPos.MutableBlockPos pedestalPos = pos.mutable();
		for (int i = -3; i < 4; i ++) {
			for (int j = -3; j < 4; j ++) {
				pedestalPos.set(pos.getX()+i,pos.getY()+1,pos.getZ()+j);
				BlockEntity tile = world.getBlockEntity(pedestalPos);
				if (tile instanceof AlchemyPedestalTopBlockEntity) {
					if (((AlchemyPedestalTopBlockEntity) tile).isValid())
						pedestals.add(((AlchemyPedestalTopBlockEntity) tile));
				}

			}
		}
		return pedestals;
	}

	@Override
	public void playSound(int id) {
        if (id == SOUND_PROCESS) {
            EmbersSounds.playMachineSound(this, SOUND_PROCESS, EmbersSounds.ALCHEMY_LOOP.get(), SoundSource.BLOCKS, true, 1.5f, 1.0f, (float) worldPosition.getX() + 0.5f, (float) worldPosition.getY() + 1.0f, (float) worldPosition.getZ() + 0.5f);
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
		return id == SOUND_PROCESS && progress > 0;
	}

	@Override
	public boolean hasCapabilityDescription(BlockCapability<?, ?> capability) {
		return capability == Capabilities.ItemHandler.BLOCK;
	}

	@Override
	public void addCapabilityDescription(List<Component> strings, BlockCapability<?, ?> capability, Direction facing) {
		if (capability == Capabilities.ItemHandler.BLOCK && facing == Direction.DOWN) {
			strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.OUTPUT, Embers.MODID + ".tooltip.goggles.item", Component.translatable(Embers.MODID + ".tooltip.goggles.item.alchemy_result")));
		} else {
			strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.BOTH, Embers.MODID + ".tooltip.goggles.item", null));
		}
	}

	@Override
	public boolean isSideUpgradeSlot(Direction face) {
		return face != Direction.UP;
	}

    @Override
    public IItemHandler getInventoryCapability(Direction side) {
        if (!this.remove)
            return side == Direction.DOWN ? outputHandler : inventory;
        return null;
    }

    public static class TabletItemStackHandler extends ItemStackHandler {
		AlchemyTabletBlockEntity entity;

		public TabletItemStackHandler(int size, AlchemyTabletBlockEntity entity) {
			super(size);
			this.entity = entity;
		}

		@Override
		public int getSlotLimit(int slot) {
			return 1;
		}

		public ItemStack forceInsertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
			if (stack.isEmpty())
				return ItemStack.EMPTY;

			if (!isItemValid(slot, stack))
				return stack;

			validateSlotIndex(slot);

			ItemStack existing = this.stacks.get(slot);

			int limit = 64;

			if (!existing.isEmpty()) {
				if (!ItemStack.isSameItemSameComponents(stack, existing))
					return stack;

				limit -= existing.getCount();
			}
			if (limit <= 0)
				return stack;

			boolean reachedLimit = stack.getCount() > limit;

			if (!simulate) {
				if (existing.isEmpty()) {
					this.stacks.set(slot, reachedLimit ? stack.copyWithCount(limit) : stack);
				} else {
					existing.grow(reachedLimit ? limit : stack.getCount());
				}
				onContentsChanged(slot);
			}
			return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
		}

		@Override
		protected void onContentsChanged(int slot) {
			if (getStackInSlot(slot).isEmpty())
				entity.outputMode = false;
			entity.setChanged();
		}
	}
}