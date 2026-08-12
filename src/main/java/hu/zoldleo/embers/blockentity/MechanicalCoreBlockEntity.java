package hu.zoldleo.embers.blockentity;

import java.util.List;

import hu.zoldleo.embers.ConfigManager;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.capabilities.EmbersCapabilities;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.api.tile.IExtraCapabilityInformation;
import hu.zoldleo.embers.api.tile.IExtraDialInformation;
import hu.zoldleo.embers.api.tile.IUpgradeable;
import hu.zoldleo.embers.api.upgrades.IUpgradeProxy;
import hu.zoldleo.embers.api.upgrades.UpgradeContext;
import hu.zoldleo.embers.api.upgrades.UpgradeUtil;
import hu.zoldleo.embers.blockentity.capability_helper.IEmberBlock;
import hu.zoldleo.embers.blockentity.capability_helper.IFluidBlock;
import hu.zoldleo.embers.blockentity.capability_helper.IInventoryBlock;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

public class MechanicalCoreBlockEntity extends BlockEntity implements IExtraDialInformation, IExtraCapabilityInformation, IUpgradeProxy, IEmberBlock, IInventoryBlock, IFluidBlock {
	public MechanicalCoreBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.MECHANICAL_CORE_ENTITY.get(), pPos, pBlockState);
	}

    @SuppressWarnings("all")
	public BlockEntityDirection getAttachedMultiblock(int distanceLeft) {
		if (distanceLeft < 1)
			return null;
		BlockPos sidePos = worldPosition.relative(getAttachedSide());
		if (Misc.isSideProxyable(level.getBlockState(sidePos), level.getBlockEntity(sidePos), getFace()))
			return new BlockEntityDirection(level.getBlockEntity(sidePos), getFace());
		if (level.getBlockEntity(sidePos) instanceof IUpgradeProxy proxy)
			return proxy.getAttachedMultiblock(distanceLeft - 1);
		return null;
	}

    @SuppressWarnings("all")
	public BlockEntity getAttachedBlockEntity(int distanceLeft) {
		if (distanceLeft < 1)
			return null;
		BlockPos sidePos = worldPosition.relative(getAttachedSide());
		if (Misc.isSideProxyable(level.getBlockState(sidePos), level.getBlockEntity(sidePos), getFace()))
			return level.getBlockEntity(sidePos);
		if (level.getBlockEntity(sidePos) instanceof IUpgradeProxy proxy)
			return proxy.getAttachedBlockEntity(distanceLeft - 1);
		return null;
	}

	public Direction getAttachedSide() {
		return getFace().getOpposite();
	}

	public Direction getFace() {
		return getBlockState().getValue(BlockStateProperties.FACING);
	}

	public <T> T getCapability(BlockCapability<T, Direction> cap, Direction ignored) {
        if (level == null)
            return null;
		BlockEntityDirection multiblock = getAttachedMultiblock(ConfigManager.MAX_PROXY_DISTANCE.get());
		if (multiblock == null)
            return null;
        return level.getCapability(cap, multiblock.blockEntity.getBlockPos(), multiblock.direction);
	}

    public <T> T getCapability(BlockCapability<T, Void> cap) {
        if (level == null)
            return null;
        BlockEntityDirection multiblock = getAttachedMultiblock(ConfigManager.MAX_PROXY_DISTANCE.get());
        if (multiblock == null)
            return null;
        return level.getCapability(cap, multiblock.blockEntity.getBlockPos());
    }

	@Override
	public boolean isSideUpgradeSlot(Direction face) {
		BlockEntityDirection multiblock = getAttachedMultiblock(ConfigManager.MAX_PROXY_DISTANCE.get());
		if (multiblock != null)
			return multiblock.blockEntity instanceof IUpgradeable upgradeable && upgradeable.isSideUpgradeSlot(multiblock.direction);
		return false;
	}

	@Override
	public void addDialInformation(Direction facing, List<Component> information, String dialType) {
		BlockEntityDirection multiblock = getAttachedMultiblock(ConfigManager.MAX_PROXY_DISTANCE.get());
		if (multiblock != null && multiblock.blockEntity instanceof IExtraDialInformation)
			((IExtraDialInformation) multiblock.blockEntity).addDialInformation(multiblock.direction, information, dialType);
	}

	@Override
	public boolean hasCapabilityDescription(BlockCapability<?, ?> capability) {
		BlockEntity multiblock = getAttachedBlockEntity(ConfigManager.MAX_PROXY_DISTANCE.get());
		if (multiblock instanceof IExtraCapabilityInformation info)
			return info.hasCapabilityDescription(capability);
		return false;
	}

	@Override
	public void addCapabilityDescription(List<Component> strings, BlockCapability<?, ?> capability, Direction facing) {
		BlockEntityDirection multiblock = getAttachedMultiblock(ConfigManager.MAX_PROXY_DISTANCE.get());
		if (multiblock != null && multiblock.blockEntity instanceof IExtraCapabilityInformation info)
			info.addCapabilityDescription(strings, capability, multiblock.direction);
	}

	@Override
	public void addOtherDescription(List<Component> strings, Direction facing) {
		BlockEntityDirection multiblock = getAttachedMultiblock(ConfigManager.MAX_PROXY_DISTANCE.get());
		if (multiblock != null && multiblock.blockEntity instanceof IExtraCapabilityInformation info)
			info.addOtherDescription(strings, multiblock.direction);
	}

	@Override
	public void collectUpgrades(List<UpgradeContext> upgrades, int distanceLeft) {
		for (Direction facing : Direction.values())
			if (isSocket(facing))
				UpgradeUtil.collectUpgrades(level, worldPosition.relative(facing), facing.getOpposite(), upgrades, distanceLeft);
	}

	@Override
	public boolean isSocket(Direction facing) {
		return facing != getAttachedSide();
	}

	@Override
	public boolean isProvider(Direction facing) {
		return facing == getAttachedSide();
	}

    @Override
    public IEmberCapability getEmberCapability(Direction side) {
        return getCapability(EmbersCapabilities.EMBER_CAPABILITY_BLOCK, side);
    }

    @Override
    public IFluidHandler getFluidCapability(Direction side) {
        return getCapability(Capabilities.FluidHandler.BLOCK, side);
    }

    @Override
    public IItemHandler getInventoryCapability(Direction side) {
        return getCapability(Capabilities.ItemHandler.BLOCK, side);
    }

    public static class BlockEntityDirection {
		public BlockEntity blockEntity;
		public Direction direction;

		public BlockEntityDirection(BlockEntity blockEntity, Direction direction) {
			this.blockEntity = blockEntity;
			this.direction = direction;
		}
	}
}