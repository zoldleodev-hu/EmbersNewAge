package hu.zoldleo.embers.blockentity;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class OpenTankBlockEntity extends BlockEntity {
    protected FluidTank tank = new FluidTank(FluidType.BUCKET_VOLUME);
	FluidStack lastEscaped = null;
	long lastEscapedTickServer;
	long lastEscapedTickClient;

	public OpenTankBlockEntity(@NotNull BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
		super(blockEntityType, pos, state);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
		super.loadAdditional(tag, registries);
        tank.readFromNBT(registries, tag.getCompound("tank"));
		if (tag.contains("lastEscaped")) {
			lastEscaped = FluidStack.parseOptional(registries, tag.getCompound("lastEscaped"));
			lastEscapedTickServer = tag.getLong("lastEscapedTick");
		}
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
		super.saveAdditional(tag, registries);
		tag.put("tank", tank.writeToNBT(registries, new CompoundTag()));
		if (lastEscaped != null) {
			tag.put("lastEscaped", lastEscaped.saveOptional(registries));
			tag.putLong("lastEscapedTick", lastEscapedTickServer);
		}
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		tag.put("tank", tank.writeToNBT(registries, new CompoundTag()));
		if (lastEscaped != null) {
			tag.put("lastEscaped", lastEscaped.saveOptional(registries));
			tag.putLong("lastEscapedTick", lastEscapedTickServer);
		}
		return tag;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (level instanceof ServerLevel)
			((ServerLevel) level).getChunkSource().blockChanged(worldPosition);
	}

	public void setEscapedFluid(FluidStack stack) {
		if (stack != null && !stack.isEmpty() && level != null) {
			lastEscaped = stack.copy();
			lastEscapedTickServer = level.getLevelData().getGameTime();

			this.setChanged();
		}
	}

	protected boolean shouldEmitParticles() {
		if (lastEscaped == null || lastEscaped.isEmpty() || level == null)
			return false;
		if (lastEscapedTickClient < lastEscapedTickServer) {
			lastEscapedTickClient = lastEscapedTickServer;
			return true;
		}
        return level.getLevelData().getGameTime() - lastEscapedTickClient < lastEscaped.getAmount() + 5;
    }

	protected abstract void updateEscapeParticles();
}