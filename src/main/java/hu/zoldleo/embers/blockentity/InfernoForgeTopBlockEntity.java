package hu.zoldleo.embers.blockentity;

import hu.zoldleo.embers.RegistryManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class InfernoForgeTopBlockEntity extends BlockEntity {
	public long lastToggle = -1;
	public boolean open = false;

	public InfernoForgeTopBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.INFERNO_FORGE_TOP_ENTITY.get(), pPos, pBlockState);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		lastToggle = nbt.getLong("lastToggle");
		open = nbt.getBoolean("open");
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
		nbt.putLong("lastToggle", lastToggle);
		nbt.putBoolean("open", open);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		nbt.putLong("lastToggle", lastToggle);
		nbt.putBoolean("open", open);
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
}