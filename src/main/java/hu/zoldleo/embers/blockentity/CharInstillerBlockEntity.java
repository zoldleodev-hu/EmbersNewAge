package hu.zoldleo.embers.blockentity;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.upgrades.IUpgradeProvider;
import hu.zoldleo.embers.blockentity.capability_helper.IUpgradeBlock;
import hu.zoldleo.embers.upgrade.CharInstillerUpgrade;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class CharInstillerBlockEntity extends BlockEntity implements IUpgradeBlock {
	public CharInstillerUpgrade upgrade;

	public CharInstillerBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.CHAR_INSTILLER_ENTITY.get(), pPos, pBlockState);
		upgrade = new CharInstillerUpgrade(this);
	}

    @Override
    public IUpgradeProvider getUpgradeCapability(Direction side) {
        if (!this.remove && getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING))
            if (side == getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING))
                return upgrade;
        return null;
    }
}