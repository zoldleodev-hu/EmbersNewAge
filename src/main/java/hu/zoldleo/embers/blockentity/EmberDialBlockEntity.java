package hu.zoldleo.embers.blockentity;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.capabilities.EmbersCapabilities;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.api.tile.IDialEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

public class EmberDialBlockEntity extends BlockEntity implements IDialEntity {
	public double ember = 0;
	public double capacity = 0;
	public boolean display = false;

	public EmberDialBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.EMBER_DIAL_ENTITY.get(), pPos, pBlockState);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
        super.loadAdditional(nbt, provider); // TODO: why wasn't this called?
		if (nbt.contains("ember"))
			ember = nbt.getDouble("ember");
		if (nbt.contains("capacity"))
			capacity = nbt.getDouble("capacity");
		if (nbt.contains("display"))
			display = nbt.getBoolean("display");
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		boolean display = false;
		if (level != null && getBlockState().hasProperty(BlockStateProperties.FACING)) {
			Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
            IEmberCapability cap = level.getCapability(EmbersCapabilities.EMBER_CAPABILITY_BLOCK, worldPosition.relative(facing, -1), facing);
            if (cap == null)
                cap = level.getCapability(EmbersCapabilities.EMBER_CAPABILITY_BLOCK, worldPosition.relative(facing, -1), null);
            if (cap != null) {
                nbt.putDouble("ember", cap.getEmber());
                nbt.putDouble("capacity", cap.getEmberCapacity());
                display = true;
            }
		}
		nbt.putBoolean("display", display);
		return nbt;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket(int maxLines) {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}