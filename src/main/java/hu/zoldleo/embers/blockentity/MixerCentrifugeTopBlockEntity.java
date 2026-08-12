package hu.zoldleo.embers.blockentity;

import java.util.List;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.api.tile.IExtraCapabilityInformation;
import hu.zoldleo.embers.api.tile.IUpgradeable;
import hu.zoldleo.embers.blockentity.capability_helper.IEmberBlock;
import hu.zoldleo.embers.blockentity.capability_helper.IFluidBlock;
import hu.zoldleo.embers.power.DefaultEmberCapability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
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

public class MixerCentrifugeTopBlockEntity extends BlockEntity implements IExtraCapabilityInformation, IUpgradeable, IEmberBlock, IFluidBlock {
	public static int capacity = FluidType.BUCKET_VOLUME * 8;
    protected FluidTank tank;
	public boolean loaded = false;
	public float renderOffset;
	int previousFluid;

	public IEmberCapability capability = new DefaultEmberCapability() {
		@Override
		public void onContentsChanged() {
			super.onContentsChanged();
			MixerCentrifugeTopBlockEntity.this.setChanged();
		}
	};

	public MixerCentrifugeTopBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.MIXER_CENTRIFUGE_TOP_ENTITY.get(), pPos, pBlockState);
		tank = new FluidTank(capacity) {
			@Override
			public void onContentsChanged() {
				MixerCentrifugeTopBlockEntity.this.setChanged();
			}
		};
		capability.setEmberCapacity(8000);
		capability.setEmber(0);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
        tank.readFromNBT(provider, nbt);
		capability.readFromNBT(provider, nbt);
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
        tank.writeToNBT(provider, nbt);
		capability.writeToNBT(provider, nbt);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
        tank.writeToNBT(provider, nbt);
		return nbt;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public int getCapacity(){
		return tank.getCapacity();
	}

	public FluidStack getFluidStack() {
		return tank.getFluid();
	}

	public FluidTank getTank() {
		return tank;
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, MixerCentrifugeTopBlockEntity blockEntity) {
		//I know I'm supposed to use onLoad for stuff on the first tick but the tank isn't synced to the client yet when that happens
		if (!blockEntity.loaded) {
			blockEntity.previousFluid = blockEntity.tank.getFluidAmount();
			blockEntity.loaded = true;
		}
		if (blockEntity.tank.getFluidAmount() != blockEntity.previousFluid) {
			blockEntity.renderOffset = blockEntity.renderOffset + blockEntity.tank.getFluidAmount() - blockEntity.previousFluid;
			blockEntity.previousFluid = blockEntity.tank.getFluidAmount();
		}
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
	public void addCapabilityDescription(List<Component> strings, BlockCapability<?, ?> capability, Direction facing) {
		if (capability == Capabilities.FluidHandler.BLOCK)
			strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.OUTPUT, Embers.MODID + ".tooltip.goggles.fluid", Component.translatable(Embers.MODID + ".tooltip.goggles.fluid.metal")));
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
    public IFluidHandler getFluidCapability(Direction side) {
        return this.remove ? null : tank;
    }
}
