package hu.zoldleo.embers.blockentity;

import java.util.Random;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.capabilities.EmbersCapabilities;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.api.power.IEmberPacketReceiver;
import hu.zoldleo.embers.api.tile.IEmberInputHint;
import hu.zoldleo.embers.blockentity.capability_helper.IEmberBlock;
import hu.zoldleo.embers.datagen.EmbersBlockTags;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.entity.EmberPacketEntity;
import hu.zoldleo.embers.particle.SmokeParticleOptions;
import hu.zoldleo.embers.particle.SparkParticleOptions;
import hu.zoldleo.embers.particle.StarParticleOptions;
import hu.zoldleo.embers.power.DefaultEmberCapability;
import hu.zoldleo.embers.util.EmbersColors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

public class EmberReceiverBlockEntity extends BlockEntity implements IEmberPacketReceiver, IEmberInputHint, IEmberBlock {
	public IEmberCapability capability = new DefaultEmberCapability() {
		@Override
		public void onContentsChanged() {
			super.onContentsChanged();
			EmberReceiverBlockEntity.this.setChanged();
		}
	};

	public static final int TRANSFER_RATE = 10;

	public long ticksExisted = 0;
	public Random random = new Random();

	public EmberReceiverBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.EMBER_RECEIVER_ENTITY.get(), pPos, pBlockState);
		capability.setEmberCapacity(2000);
	}

	public EmberReceiverBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
		super(pType, pPos, pBlockState);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		capability.readFromNBT(provider, nbt);
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
		capability.writeToNBT(provider, nbt);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, EmberReceiverBlockEntity blockEntity) {
		blockEntity.ticksExisted ++;
		Direction facing = state.getValue(BlockStateProperties.FACING);
		if (blockEntity.ticksExisted % 2 == 0) {
			IEmberCapability cap = level.getCapability(EmbersCapabilities.EMBER_CAPABILITY_BLOCK, pos.relative(facing, -1), facing);
            if (cap != null) {
				if (cap.getEmber() < cap.getEmberCapacity() && blockEntity.capability.getEmber() > 0) {
					double added = cap.addAmount(Math.min(TRANSFER_RATE, blockEntity.capability.getEmber()), true);
					blockEntity.capability.removeAmount(added, true);
				}
			}
		}
	}

	/*@Override
	public boolean isFull() {
		return capability.getEmber() >= capability.getEmberCapacity();
	}*/

	@Override
	public boolean hasRoomFor(double ember) {
		return capability.getEmber() * 2 <= capability.getEmberCapacity();
	}

	@Override
	public boolean onReceive(EmberPacketEntity packet) {
		if (level instanceof ServerLevel serverLevel) {
			if (capability.getEmber() + packet.value > capability.getEmberCapacity()) {
				serverLevel.sendParticles(new SparkParticleOptions(EmbersColors.EMBER_ID, random.nextFloat() * 0.75f + 0.45f), getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5, 5, 0.125f * (random.nextFloat() - 0.5f), 0.125f * (random.nextFloat()), 0.125f * (random.nextFloat() - 0.5f), 1.0);
				serverLevel.sendParticles(new SmokeParticleOptions(EmbersColors.SMOKE_ID, 2.0f + random.nextFloat() * 2.0f), getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5, 15, 0.0625f * (random.nextFloat() - 0.5f), 0.0625f + 0.0625f * (random.nextFloat() - 0.5f), 0.0625f * (random.nextFloat() - 0.5f), 1.0);
			} else {
				serverLevel.sendParticles(new StarParticleOptions(EmbersColors.EMBER_ID, 3.5f + 0.5f * random.nextFloat()), getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5, 12, 0.0125f * (random.nextFloat() - 0.5f), 0.0125f * (random.nextFloat() - 0.5f), 0.0125f * (random.nextFloat() - 0.5f), 0.0);
			}
		}
		level.playLocalSound(packet.getX(), packet.getY(), packet.getZ(), packet.value >= 100 ? EmbersSounds.EMBER_RECEIVE_BIG.get() : EmbersSounds.EMBER_RECEIVE.get(), SoundSource.BLOCKS, 1.0f, 1.0f, false);
		return true;
	}

	@Override
	public boolean shouldShowHintTooltip() {
		Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
		BlockPos attachedPos = worldPosition.relative(facing, -1);
		if (level.getBlockState(attachedPos).is(EmbersBlockTags.EMBER_WRONG_INPUT_HINTER))
            return level.getCapability(EmbersCapabilities.EMBER_CAPABILITY_BLOCK, attachedPos, facing) == null;
		return false;
	}

    @Override
    public IEmberCapability getEmberCapability(Direction side) {
        return this.remove ? null : capability;
    }
}