package hu.zoldleo.embers.blockentity;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.upgrades.IUpgradeProvider;
import hu.zoldleo.embers.blockentity.capability_helper.IUpgradeBlock;
import hu.zoldleo.embers.upgrade.ExcavationBucketsUpgrade;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ExcavationBucketsBlockEntity extends BlockEntity implements IUpgradeBlock {
	public ExcavationBucketsUpgrade upgrade;
	public float angle = 0;
	public float lastAngle;

	public ExcavationBucketsBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.EXCAVATION_BUCKETS_ENTITY.get(), pPos, pBlockState);
		upgrade = new ExcavationBucketsUpgrade(this);
	}

    @Override
    public IUpgradeProvider getUpgradeCapability(Direction side) {
        if (!this.remove && getBlockState().hasProperty(BlockStateProperties.FACING))
            if (side.getOpposite() == getBlockState().getValue(BlockStateProperties.FACING))
                return upgrade;
        return null;
    }
}