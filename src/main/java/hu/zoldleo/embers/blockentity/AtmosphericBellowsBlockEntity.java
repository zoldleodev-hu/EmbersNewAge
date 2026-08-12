package hu.zoldleo.embers.blockentity;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.upgrades.IUpgradeProvider;
import hu.zoldleo.embers.blockentity.capability_helper.IUpgradeBlock;
import hu.zoldleo.embers.upgrade.AtmosphericBellowsUpgrade;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class AtmosphericBellowsBlockEntity extends BlockEntity implements IUpgradeBlock {
	protected AtmosphericBellowsUpgrade upgrade;

	public AtmosphericBellowsBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.ATMOSPHERIC_BELLOWS_ENTITY.get(), pPos, pBlockState);
		upgrade = new AtmosphericBellowsUpgrade(this);
	}

    @Override
    public IUpgradeProvider getUpgradeCapability(Direction side) {
        if (!this.remove && getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING))
            if (side == getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING))
                return upgrade;
        return null;
    }
}