package hu.zoldleo.embers.blockentity;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.upgrades.IUpgradeProvider;
import hu.zoldleo.embers.blockentity.capability_helper.IUpgradeBlock;
import hu.zoldleo.embers.upgrade.HeatInsulationUpgrade;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class HeatInsulationBlockEntity extends BlockEntity implements IUpgradeBlock {
	public HeatInsulationUpgrade upgrade;

	public HeatInsulationBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.HEAT_INSULATION_ENTITY.get(), pPos, pBlockState);
		upgrade = new HeatInsulationUpgrade(this);
	}

    @Override
    public IUpgradeProvider getUpgradeCapability(Direction side) {
        if (!this.remove && getBlockState().hasProperty(BlockStateProperties.FACING))
            if (side.getOpposite() == getBlockState().getValue(BlockStateProperties.FACING))
                return upgrade;
        return null;
    }
}