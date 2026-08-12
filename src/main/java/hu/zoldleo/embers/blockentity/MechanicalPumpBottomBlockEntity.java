package hu.zoldleo.embers.blockentity;

import java.util.List;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.event.DialInformationEvent;
import hu.zoldleo.embers.api.event.EmberEvent;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.api.tile.IExtraDialInformation;
import hu.zoldleo.embers.api.tile.IMechanicallyPowered;
import hu.zoldleo.embers.api.tile.IUpgradeable;
import hu.zoldleo.embers.api.upgrades.UpgradeContext;
import hu.zoldleo.embers.api.upgrades.UpgradeUtil;
import hu.zoldleo.embers.blockentity.capability_helper.IEmberBlock;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.power.DefaultEmberCapability;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public class MechanicalPumpBottomBlockEntity extends BlockEntity implements IMechanicallyPowered, IExtraDialInformation, IUpgradeable, IEmberBlock {
	public IEmberCapability capability = new DefaultEmberCapability() {
		@Override
		public void onContentsChanged() {
			super.onContentsChanged();
			MechanicalPumpBottomBlockEntity.this.setChanged();
		}
	};
	public static final double EMBER_COST = 0.5;

	public int progress;
	public int totalProgress;
	public int lastProgress;
	private List<UpgradeContext> upgrades;

	public MechanicalPumpBottomBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.MECHANICAL_PUMP_BOTTOM_ENTITY.get(), pPos, pBlockState);
		capability.setEmberCapacity(1000);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		capability.readFromNBT(provider, nbt);
		if (nbt.contains("progress"))
			progress = nbt.getInt("progress");
		totalProgress = nbt.getInt("totalProgress");
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
		capability.writeToNBT(provider, nbt);
		nbt.putInt("progress", progress);
		nbt.putInt("totalProgress", totalProgress);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		nbt.putInt("totalProgress", totalProgress);
		return nbt;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, MechanicalPumpBottomBlockEntity blockEntity) {
		blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos, Misc.horizontals);
		UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
		if (UpgradeUtil.doTick(blockEntity, blockEntity.upgrades))
			return;
		double emberCost = UpgradeUtil.getTotalEmberConsumption(blockEntity, EMBER_COST, blockEntity.upgrades);
		if (blockEntity.capability.getEmber() >= emberCost) {
			boolean cancel = UpgradeUtil.doWork(blockEntity, blockEntity.upgrades);
			if (!cancel) {
				int speed = (int) UpgradeUtil.getTotalSpeedModifier(blockEntity, blockEntity.upgrades);
				blockEntity.progress += speed;
				blockEntity.totalProgress += speed;
				UpgradeUtil.throwEvent(blockEntity, new EmberEvent(blockEntity, EmberEvent.EnumType.CONSUME, emberCost), blockEntity.upgrades);
				blockEntity.capability.removeAmount(emberCost, true);
				if (blockEntity.progress > 400) {
					blockEntity.progress -= 400;
					/*boolean doContinue = true;
					for (int r = 0; r < 6 && doContinue; r++) {
						for (int i = -r; i < r + 1 && doContinue; i++) {
							for (int j = -r; j < 1 && doContinue; j++) {
								for (int k = -r; k < r + 1 && doContinue; k++) {
									doContinue = blockEntity.attemptPump(pos.offset(i, j - 1, k));
								}
							}
						}
					}*/
					blockEntity.attemptPump(pos.below());
					blockEntity.playSound(speed);
				}
				blockEntity.setChanged();
			}
		}
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, MechanicalPumpBottomBlockEntity blockEntity) {
		blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos, Misc.horizontals);
		UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
		blockEntity.lastProgress = blockEntity.totalProgress;
	}

	public boolean attemptPump(BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.getBlock() instanceof BucketPickup pickup && !state.getFluidState().isEmpty() && state.getFluidState().isSource()) {
			FluidStack stack = new FluidStack(state.getFluidState().holder().value(), FluidType.BUCKET_VOLUME);
            MechanicalPumpTopBlockEntity t = (MechanicalPumpTopBlockEntity) level.getBlockEntity(worldPosition.above());
            int filled = t.getTank().fill(stack, IFluidHandler.FluidAction.SIMULATE);
            if (filled == stack.getAmount()) {
                t.getTank().fill(stack, IFluidHandler.FluidAction.EXECUTE);
                pickup.pickupBlock(null, level, pos, state);
                return false;
            }
        }
		return true;
	}

	public void playSound(int speed) {
		float pitch;
		SoundEvent sound;
		if (speed >= 20) {
			sound = EmbersSounds.PUMP_FAST.get();
			pitch = speed / 20f;
		} else if(speed >= 10) {
			sound = EmbersSounds.PUMP_MID.get();
			pitch = speed / 10f;
		} else {
			sound = EmbersSounds.PUMP_SLOW.get();
			pitch = speed;
		}
		level.playSound(null, worldPosition.above(), sound, SoundSource.BLOCKS, 1.0f, pitch);
	}

	@Override
	public double getMechanicalSpeed(double power) {
		return Math.min(power/2, 100);
	}

	@Override
	public double getMinimumPower() {
		return 2;
	}

	@Override
	public double getNominalSpeed() {
		return 10;
	}

	@Override
	public void addDialInformation(Direction facing, List<Component> information, String dialType) {
		UpgradeUtil.throwEvent(this, new DialInformationEvent(this, information, dialType), upgrades);
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (level instanceof ServerLevel serverLevel)
			serverLevel.getChunkSource().blockChanged(worldPosition);
	}

	@Override
	public boolean isSideUpgradeSlot(Direction face) {
		return face.getAxis() != Axis.Y;
	}

    @Override
    public IEmberCapability getEmberCapability(Direction side) {
        if (!this.remove)
            return capability;
        return null;
    }
}