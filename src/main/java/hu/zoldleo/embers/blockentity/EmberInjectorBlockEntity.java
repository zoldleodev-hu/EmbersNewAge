package hu.zoldleo.embers.blockentity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import hu.zoldleo.embers.ConfigManager;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.event.DialInformationEvent;
import hu.zoldleo.embers.api.event.EmberEvent;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.api.tile.IEmberInjectable;
import hu.zoldleo.embers.api.tile.IExtraDialInformation;
import hu.zoldleo.embers.api.tile.IUpgradeable;
import hu.zoldleo.embers.api.upgrades.UpgradeContext;
import hu.zoldleo.embers.api.upgrades.UpgradeUtil;
import hu.zoldleo.embers.blockentity.capability_helper.IEmberBlock;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.particle.GlowParticleOptions;
import hu.zoldleo.embers.power.DefaultEmberCapability;
import hu.zoldleo.embers.util.EmbersColors;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

public class EmberInjectorBlockEntity extends BlockEntity implements ISoundController, IExtraDialInformation, IUpgradeable, IEmberBlock {
	public IEmberCapability capability = new DefaultEmberCapability() {
		@Override
		public void onContentsChanged() {
			super.onContentsChanged();
			EmberInjectorBlockEntity.this.setChanged();
		}
	};
	protected int ticksExisted = 0;
	protected int progress = -1;
	static Random random = new Random();
	public static final double EMBER_COST = 1.0;

	public static final int SOUND_PROCESS = 1;
	public static final int[] SOUND_IDS = new int[]{SOUND_PROCESS};

	HashSet<Integer> soundsPlaying = new HashSet<>();
	public List<UpgradeContext> upgrades = new ArrayList<>();
	public boolean isWorking;
	public int distance;

	public EmberInjectorBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.EMBER_INJECTOR_ENTITY.get(), pPos, pBlockState);
		capability.setEmberCapacity(24000);
		capability.setEmber(0);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		capability.readFromNBT(provider, nbt);
		isWorking = nbt.getBoolean("isWorking");
		distance = nbt.getInt("distance");
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
		capability.writeToNBT(provider, nbt);
		nbt.putBoolean("isWorking", isWorking);
		nbt.putInt("distance", distance);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		nbt.putBoolean("isWorking", isWorking);
		nbt.putInt("distance", distance);
		return nbt;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, EmberInjectorBlockEntity blockEntity) {
		blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos, Direction.values());
		UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
		blockEntity.handleSound();
		if (blockEntity.isWorking) {
			Direction facing = state.getValue(BlockStateProperties.FACING);
			for (int i = 0; i < 6 * blockEntity.distance; i++) {
				level.addParticle(new GlowParticleOptions(EmbersColors.EMBER_ID, 4.0F),
						pos.getX()+0.5f+random.nextFloat()*blockEntity.distance*facing.getNormal().getX(),
						pos.getY()+0.5f+random.nextFloat()*blockEntity.distance*facing.getNormal().getY(),
						pos.getZ()+0.5f+random.nextFloat()*blockEntity.distance*facing.getNormal().getZ(), 0, 0, 0);
			}
		}
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, EmberInjectorBlockEntity blockEntity) {
		blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos, Direction.values());
		UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
		boolean wasWorking = blockEntity.isWorking;
		int previousDist = blockEntity.distance;
		if (!UpgradeUtil.doTick(blockEntity, blockEntity.upgrades)) {
			Direction facing = state.getValue(BlockStateProperties.FACING);
			int maxDist = UpgradeUtil.getOtherParameter(blockEntity, "distance", (int) ConfigManager.INJECTOR_MAX_DISTANCE.get(), blockEntity.upgrades);
			BlockPos hitPos = pos;
			BlockEntity tile = null;
			for (int i = 1; i <= maxDist + 1; i++) {
				hitPos = hitPos.relative(facing);
				BlockState hitState = level.getBlockState(hitPos);
				if (!hitState.getCollisionShape(level, hitPos).isEmpty()) {
					tile = level.getBlockEntity(hitPos);
					blockEntity.distance = i;
					break;
				}
			}
			blockEntity.isWorking = false;
			double emberCost = UpgradeUtil.getTotalEmberConsumption(blockEntity, EMBER_COST, blockEntity.upgrades);
			if (tile instanceof IEmberInjectable injectable && injectable.isValid() && blockEntity.capability.getEmber() >= emberCost) {
				boolean cancel = UpgradeUtil.doWork(blockEntity, blockEntity.upgrades);
				if (!cancel) {
					double enberInjected = EMBER_COST * UpgradeUtil.getTotalSpeedModifier(blockEntity, blockEntity.upgrades);
					injectable.inject(blockEntity, enberInjected);
					UpgradeUtil.throwEvent(blockEntity, new EmberEvent(blockEntity, EmberEvent.EnumType.CONSUME, emberCost), blockEntity.upgrades);
					blockEntity.isWorking = true;
					blockEntity.capability.removeAmount(emberCost, true);
				}
			}
		}
		if (wasWorking != blockEntity.isWorking || previousDist != blockEntity.distance) {
			blockEntity.setChanged();
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
        if (id == SOUND_PROCESS)
            EmbersSounds.playMachineSound(this, SOUND_PROCESS, EmbersSounds.INJECTOR_LOOP.get(), SoundSource.BLOCKS, true, 1.0f, 1.0f, worldPosition.getX() + 0.5f, worldPosition.getY() + 0.5f, worldPosition.getZ() + 0.5f);
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
	public void addDialInformation(Direction facing, List<Component> information, String dialType) {
		UpgradeUtil.throwEvent(this, new DialInformationEvent(this, information, dialType), upgrades);
	}

	@Override
	public boolean isSideUpgradeSlot(Direction face) {
		return true;
	}

    @Override
    public IEmberCapability getEmberCapability(Direction side) {
        return this.remove ? null : capability;
    }
}