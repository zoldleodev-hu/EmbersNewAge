package hu.zoldleo.embers.blockentity;

import java.util.HashSet;
import java.util.List;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.event.DialInformationEvent;
import hu.zoldleo.embers.api.event.EmberEvent;
import hu.zoldleo.embers.api.event.MachineRecipeEvent;
import hu.zoldleo.embers.api.tile.IExtraCapabilityInformation;
import hu.zoldleo.embers.api.tile.IExtraDialInformation;
import hu.zoldleo.embers.api.tile.IMechanicallyPowered;
import hu.zoldleo.embers.api.upgrades.UpgradeContext;
import hu.zoldleo.embers.api.upgrades.UpgradeUtil;
import hu.zoldleo.embers.block.FluidDialBlock;
import hu.zoldleo.embers.blockentity.capability_helper.IFluidBlock;
import hu.zoldleo.embers.datagen.EmbersFluidTags;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.recipe.base.IMixingRecipe;
import hu.zoldleo.embers.recipe.context.MixingContext;
import hu.zoldleo.embers.util.FluidAmounts;
import hu.zoldleo.embers.util.Misc;
import hu.zoldleo.embers.util.sound.ISoundController;

import net.minecraft.ChatFormatting;
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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

public class MixerCentrifugeBottomBlockEntity extends BlockEntity implements IMechanicallyPowered, ISoundController, IExtraDialInformation, IExtraCapabilityInformation, IFluidBlock {
	public static final double EMBER_COST = 2.0;

	public MixerFluidTank north = new MixerFluidTank(FluidType.BUCKET_VOLUME * 8, this);
	public MixerFluidTank south = new MixerFluidTank(FluidType.BUCKET_VOLUME * 8, this);
	public MixerFluidTank east = new MixerFluidTank(FluidType.BUCKET_VOLUME * 8, this);
	public MixerFluidTank west = new MixerFluidTank(FluidType.BUCKET_VOLUME * 8, this);
	public MixerFluidTank[] tanks = new MixerFluidTank[]{north, south, east, west};

	public boolean loaded = false;
	boolean isWorking;

	public static final int SOUND_PROCESS = 1;
	public static final int[] SOUND_IDS = new int[]{SOUND_PROCESS};

	HashSet<Integer> soundsPlaying = new HashSet<>();
	protected List<UpgradeContext> upgrades;
	private double powerRatio;
	public RecipeHolder<IMixingRecipe> cachedRecipe = null;

	public MixerCentrifugeBottomBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.MIXER_CENTRIFUGE_BOTTOM_ENTITY.get(), pPos, pBlockState);
	}

	public MixerFluidTank[] getTanks() {
		return tanks;
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		north.readFromNBT(provider, nbt.getCompound("northTank"));
		south.readFromNBT(provider, nbt.getCompound("southTank"));
		east.readFromNBT(provider, nbt.getCompound("eastTank"));
		west.readFromNBT(provider, nbt.getCompound("westTank"));
		isWorking = nbt.getBoolean("working");
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
		nbt.put("northTank", north.writeToNBT(provider, new CompoundTag()));
		nbt.put("southTank", south.writeToNBT(provider, new CompoundTag()));
		nbt.put("eastTank", east.writeToNBT(provider, new CompoundTag()));
		nbt.put("westTank", west.writeToNBT(provider, new CompoundTag()));
		nbt.putBoolean("working", isWorking);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		nbt.put("northTank", north.writeToNBT(provider, new CompoundTag()));
		nbt.put("southTank", south.writeToNBT(provider, new CompoundTag()));
		nbt.put("eastTank", east.writeToNBT(provider, new CompoundTag()));
		nbt.put("westTank", west.writeToNBT(provider, new CompoundTag()));
		nbt.putBoolean("working", isWorking);
		return nbt;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, MixerCentrifugeBottomBlockEntity blockEntity) {
		blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos.above(), Direction.values());
		UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
		blockEntity.handleSound();
		//I know I'm supposed to use onLoad for stuff on the first tick but the tanks aren't synced to the client yet when that happens
		if (!blockEntity.loaded) {
			for (MixerFluidTank tank : blockEntity.tanks) {
				tank.previousFluid = tank.getFluidAmount();
			}
			blockEntity.loaded = true;
		}
		for (MixerFluidTank tank : blockEntity.tanks) {
			if (tank.getFluidAmount() != tank.previousFluid) {
				tank.renderOffset = tank.renderOffset + tank.getFluidAmount() - tank.previousFluid;
				tank.previousFluid = tank.getFluidAmount();
			}
		}
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, MixerCentrifugeBottomBlockEntity blockEntity) {
		MixerCentrifugeTopBlockEntity top = (MixerCentrifugeTopBlockEntity) level.getBlockEntity(pos.above());
		boolean wasWorking = blockEntity.isWorking;
		blockEntity.isWorking = false;
		if (top != null) {
			blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos.above(), Direction.values());
			UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
			if (UpgradeUtil.doTick(blockEntity, blockEntity.upgrades))
				return;

			MixingContext context = new MixingContext(blockEntity.tanks);
			blockEntity.cachedRecipe = Misc.getRecipe(blockEntity.cachedRecipe, RegistryManager.MIXING.get(), context, level);
			/*if (recipe != null)
				blockEntity.powerRatio = recipe.getPowerRatio();
			else*/
			blockEntity.powerRatio = 0;
			double emberCost = UpgradeUtil.getTotalEmberConsumption(blockEntity, EMBER_COST, blockEntity.upgrades);
			if (top.capability.getEmber() >= emberCost && blockEntity.cachedRecipe != null) {
				boolean cancel = UpgradeUtil.doWork(blockEntity, blockEntity.upgrades);
				if (!cancel) {
					IFluidHandler tank = level.getCapability(Capabilities.FluidHandler.BLOCK, pos.above(), null);
					FluidStack output = blockEntity.cachedRecipe.value().getOutput(context);
					output = UpgradeUtil.transformOutput(blockEntity, output, blockEntity.upgrades);
					int amount = tank.fill(output, IFluidHandler.FluidAction.SIMULATE);
					if (amount != 0) {
						UpgradeUtil.throwEvent(blockEntity, new MachineRecipeEvent.Success<>(blockEntity, blockEntity.cachedRecipe), blockEntity.upgrades);
						blockEntity.isWorking = true;
						tank.fill(output, IFluidHandler.FluidAction.EXECUTE);
						blockEntity.cachedRecipe.value().process(context);
						UpgradeUtil.throwEvent(blockEntity, new EmberEvent(blockEntity, EmberEvent.EnumType.CONSUME, emberCost), blockEntity.upgrades);
						top.capability.removeAmount(emberCost, true);
					}
				}
			}
		}
		if (wasWorking != blockEntity.isWorking)
			blockEntity.setChanged();
	}

	@Override
	public void playSound(int id) {
        if (id == SOUND_PROCESS)
            EmbersSounds.playMachineSound(this, SOUND_PROCESS, EmbersSounds.MIXER_LOOP.get(), SoundSource.BLOCKS, true, 1.0f, 1.0f, (float) worldPosition.getX() + 0.5f, (float) worldPosition.getY() + 1.0f, (float) worldPosition.getZ() + 0.5f);
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
		return id == SOUND_PROCESS && isWorking;
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (level instanceof ServerLevel serverLevel)
			serverLevel.getChunkSource().blockChanged(worldPosition);
	}

	@Override
	public boolean hasCapabilityDescription(BlockCapability<?, ?> capability) {
		return capability == Capabilities.FluidHandler.BLOCK;
	}

	@Override
	public void addDialInformation(Direction facing, List<Component> information, String dialType) {
		if (FluidDialBlock.DIAL_TYPE.equals(dialType)) {
			information.clear();
			information.add(Component.translatable(Embers.MODID + ".tooltip.colon", Component.translatable(Embers.MODID + ".tooltip.side.north").withStyle(facing == Direction.NORTH ? ChatFormatting.BOLD : ChatFormatting.RESET), FluidDialBlock.formatFluidStack(north.getFluid(),north.getCapacity())));
			if (north.getFluid().is(EmbersFluidTags.INGOT_TOOLTIP) && north.getFluid().getAmount() >= FluidAmounts.nuggetValue())
				information.add(FluidAmounts.getIngotTooltip(north.getFluid().getAmount()));
			information.add(Component.translatable(Embers.MODID + ".tooltip.colon", Component.translatable(Embers.MODID + ".tooltip.side.east").withStyle(facing == Direction.EAST ? ChatFormatting.BOLD : ChatFormatting.RESET), FluidDialBlock.formatFluidStack(east.getFluid(), east.getCapacity())));
			if (east.getFluid().is(EmbersFluidTags.INGOT_TOOLTIP) && east.getFluid().getAmount() >= FluidAmounts.nuggetValue())
				information.add(FluidAmounts.getIngotTooltip(east.getFluid().getAmount()));
			information.add(Component.translatable(Embers.MODID + ".tooltip.colon", Component.translatable(Embers.MODID + ".tooltip.side.south").withStyle(facing == Direction.SOUTH ? ChatFormatting.BOLD : ChatFormatting.RESET), FluidDialBlock.formatFluidStack(south.getFluid(), south.getCapacity())));
			if (south.getFluid().is(EmbersFluidTags.INGOT_TOOLTIP) && south.getFluid().getAmount() >= FluidAmounts.nuggetValue())
				information.add(FluidAmounts.getIngotTooltip(south.getFluid().getAmount()));
			information.add(Component.translatable(Embers.MODID + ".tooltip.colon", Component.translatable(Embers.MODID + ".tooltip.side.west").withStyle(facing == Direction.WEST ? ChatFormatting.BOLD : ChatFormatting.RESET), FluidDialBlock.formatFluidStack(west.getFluid(), west.getCapacity())));
			if (west.getFluid().is(EmbersFluidTags.INGOT_TOOLTIP) && west.getFluid().getAmount() >= FluidAmounts.nuggetValue())
				information.add(FluidAmounts.getIngotTooltip(west.getFluid().getAmount()));
		}
		UpgradeUtil.throwEvent(this, new DialInformationEvent(this, information, dialType), upgrades);
	}

	@Override
	public void addCapabilityDescription(List<Component> strings, BlockCapability<?, ?> capability, Direction facing) {
		strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.INPUT, Embers.MODID + ".tooltip.goggles.fluid", Component.translatable(Embers.MODID + ".tooltip.goggles.fluid.metal")));
	}

	@Override
	public double getMinimumPower() {
		return 20;
	}

	@Override
	public double getMechanicalSpeed(double power) {
		return Misc.getDiminishedPower(power,80,1.5/80);
	}

	@Override
	public double getNominalSpeed() {
		return 1;
	}

	@Override
	public double getStandardPowerRatio() {
		return powerRatio;
	}

    @Override
    public IFluidHandler getFluidCapability(Direction side) {
        return switch (side) {
            case EAST -> east;
            case NORTH -> north;
            case SOUTH -> south;
            case WEST -> west;
            default -> null;
        };
    }

    public static class MixerFluidTank extends FluidTank {
		public final BlockEntity entity;
		public float renderOffset;
		public int previousFluid;

		public MixerFluidTank(int capacity, BlockEntity entity) {
			super(capacity);
			this.entity = entity;
		}

		@Override
		public void onContentsChanged() {
			entity.setChanged();
		}
	}
}