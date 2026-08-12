package hu.zoldleo.embers.blockentity;

import java.util.ArrayList;
import java.util.List;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.event.DialInformationEvent;
import hu.zoldleo.embers.api.event.EmberEvent;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.api.tile.IExtraCapabilityInformation;
import hu.zoldleo.embers.api.tile.IExtraDialInformation;
import hu.zoldleo.embers.api.tile.IHammerable;
import hu.zoldleo.embers.api.tile.IMechanicallyPowered;
import hu.zoldleo.embers.api.tile.IUpgradeable;
import hu.zoldleo.embers.api.upgrades.UpgradeContext;
import hu.zoldleo.embers.api.upgrades.UpgradeUtil;
import hu.zoldleo.embers.blockentity.capability_helper.IEmberBlock;
import hu.zoldleo.embers.power.DefaultEmberCapability;
import hu.zoldleo.embers.util.Misc;

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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

public class AutomaticHammerBlockEntity extends BlockEntity implements IMechanicallyPowered, IExtraDialInformation, IUpgradeable, IExtraCapabilityInformation, IEmberBlock {
	public static final double EMBER_COST = 40.0;
	public static final int PROCESS_TIME = 20;
	public IEmberCapability capability = new DefaultEmberCapability() {
		@Override
		public void onContentsChanged() {
			super.onContentsChanged();
			AutomaticHammerBlockEntity.this.setChanged();
		}
	};
	public long startTime = -1;
	public int processTime = -1;
	protected List<UpgradeContext> upgrades = new ArrayList<>();

	public AutomaticHammerBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.AUTOMATIC_HAMMER_ENTITY.get(), pPos, pBlockState);
		capability.setEmberCapacity(12000);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
		super.loadAdditional(tag, registries);
		capability.readFromNBT(registries, tag);
		startTime = tag.getLong("startTime");
		processTime = tag.getInt("processTime");
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
		super.saveAdditional(tag, registries);
        capability.writeToNBT(registries, tag);
        tag.putLong("startTime", startTime);
        tag.putInt("processTime", processTime);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
		CompoundTag nbt = super.getUpdateTag(registries);
		nbt.putLong("startTime", startTime);
		nbt.putInt("processTime", processTime);
		return nbt;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, AutomaticHammerBlockEntity blockEntity) {
		Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
		blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos, new Direction[]{facing.getOpposite()});
		UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, AutomaticHammerBlockEntity blockEntity) {
		Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
		blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos, new Direction[]{facing.getOpposite()});
		UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
		if (UpgradeUtil.doTick(blockEntity, blockEntity.upgrades))
			return;
		BlockEntity tile = level.getBlockEntity(pos.below().relative(facing));
		if (tile instanceof IHammerable hammerable) {
			double ember_cost = UpgradeUtil.getTotalEmberConsumption(blockEntity, EMBER_COST, blockEntity.upgrades);
			boolean redstoneEnabled = level.hasNeighborSignal(pos);
			if (hammerable.isValid() && redstoneEnabled && blockEntity.capability.getEmber() >= ember_cost) {
				boolean cancel = UpgradeUtil.doWork(blockEntity, blockEntity.upgrades);
				int processTime = UpgradeUtil.getWorkTime(blockEntity, PROCESS_TIME, blockEntity.upgrades);
				if (!cancel && blockEntity.startTime + processTime < level.getGameTime()) {
					blockEntity.startTime = level.getGameTime();
					blockEntity.processTime = processTime;
					blockEntity.setChanged();
				}
			}
			if (blockEntity.startTime + blockEntity.processTime / 2 == level.getGameTime() && blockEntity.capability.getEmber() >= ember_cost) {
				UpgradeUtil.throwEvent(blockEntity, new EmberEvent(blockEntity, EmberEvent.EnumType.CONSUME, ember_cost), blockEntity.upgrades);
				blockEntity.capability.removeAmount(ember_cost, true);
				hammerable.onHit(blockEntity);
			}
		}
	}

	@Override
	public IEmberCapability getEmberCapability(Direction side) {
        return this.remove ? null : capability;
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (level instanceof ServerLevel)
			((ServerLevel) level).getChunkSource().blockChanged(worldPosition);
	}

	@Override
	public double getMechanicalSpeed(double power) {
		return Misc.getDiminishedPower(power,20,1.5/20);
	}

	@Override
	public double getNominalSpeed() {
		return 1;
	}

	@Override
	public double getMinimumPower() {
		return 10;
	}

	@Override
	public void addDialInformation(Direction facing, List<Component> information, String dialType) {
		UpgradeUtil.throwEvent(this, new DialInformationEvent(this, information, dialType), upgrades);
	}

	@Override
	public boolean isSideUpgradeSlot(Direction face) {
		return getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING) && getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite() == face;
	}

	@Override
	public void addOtherDescription(List<Component> strings, Direction facing) {
		strings.add(Component.translatable(Embers.MODID + ".tooltip.goggles.redstone_signal"));
	}
}