package hu.zoldleo.embers.blockentity;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.capabilities.EmbersCapabilities;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.api.upgrades.IUpgradeProvider;
import hu.zoldleo.embers.blockentity.capability_helper.IEmberBlock;
import hu.zoldleo.embers.blockentity.capability_helper.IUpgradeBlock;
import hu.zoldleo.embers.upgrade.EmberSiphonUpgrade;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EmberSiphonBlockEntity extends BlockEntity implements IUpgradeBlock, IEmberBlock {
	public EmberSiphonUpgrade upgrade;

	public EmberSiphonBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.EMBER_SIPHON_ENTITY.get(), pPos, pBlockState);
		upgrade = new EmberSiphonUpgrade(this);
	}

    @Override
    public IUpgradeProvider getUpgradeCapability(Direction side) {
        if (!this.remove && (side == null || side == Direction.UP))
            return upgrade;
        return null;
    }

    @Override
    public IEmberCapability getEmberCapability(Direction side) {
        if (!this.remove && (side == null || side.getAxis() != Direction.Axis.Y))
            return level.getCapability(EmbersCapabilities.EMBER_CAPABILITY_BLOCK, worldPosition.above(), Direction.DOWN);
        return null;
    }
}