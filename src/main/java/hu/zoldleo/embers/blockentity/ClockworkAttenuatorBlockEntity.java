package hu.zoldleo.embers.blockentity;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.upgrades.IUpgradeProvider;
import hu.zoldleo.embers.blockentity.capability_helper.IUpgradeBlock;
import hu.zoldleo.embers.upgrade.ClockworkAttenuatorUpgrade;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

public class ClockworkAttenuatorBlockEntity extends BlockEntity implements IUpgradeBlock {
	public ClockworkAttenuatorUpgrade upgrade;
	public double activeSpeed = 0, inactiveSpeed = 1;

	public double[] validSpeeds = new double[]{0.0, 0.0625, 0.125, 0.25, 0.5, 1.0};

	public ClockworkAttenuatorBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.CLOCKWORK_ATTENUATOR_ENTITY.get(), pPos, pBlockState);
		upgrade = new ClockworkAttenuatorUpgrade(this);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		activeSpeed = nbt.getDouble("active_speed");
		inactiveSpeed = nbt.getDouble("inactive_speed");
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
		nbt.putDouble("active_speed", activeSpeed);
		nbt.putDouble("inactive_speed", inactiveSpeed);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		nbt.putDouble("active_speed", activeSpeed);
		nbt.putDouble("inactive_speed", inactiveSpeed);
		return nbt;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public double getSpeed() {
		return level.hasNeighborSignal(worldPosition) ? activeSpeed : inactiveSpeed;
	}

	public double getNext(double current) {
		for (int i = 0; i < validSpeeds.length - 1; i++) {
			double a = validSpeeds[i];
			double b = validSpeeds[i + 1];

			if (b > current && a <= current)
				return b;
		}
		return current;
	}

	public double getPrevious(double current) {
		for (int i = 0; i < validSpeeds.length - 1; i++) {
			double a = validSpeeds[i];
			double b = validSpeeds[i + 1];

			if (b >= current && a < current)
				return a;
		}
		return current;
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (level instanceof ServerLevel)
			((ServerLevel) level).getChunkSource().blockChanged(worldPosition);
	}

    @Override
    public IUpgradeProvider getUpgradeCapability(Direction side) {
        if (!this.remove && (side == null || getBlockState().getValue(BlockStateProperties.FACING).getOpposite() == side))
            return upgrade;
        return null;
    }
}