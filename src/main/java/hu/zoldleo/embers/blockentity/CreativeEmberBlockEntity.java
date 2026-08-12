package hu.zoldleo.embers.blockentity;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.blockentity.capability_helper.IEmberBlock;
import hu.zoldleo.embers.power.DefaultEmberCapability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CreativeEmberBlockEntity extends BlockEntity implements IEmberBlock {
	public IEmberCapability capability = new DefaultEmberCapability() {
		@Override
		public void onContentsChanged() {
			super.onContentsChanged();
			CreativeEmberBlockEntity.this.setChanged();
		}

		@Override
		public double getEmber() {
			return getEmberCapacity() / 2.0;
		}

		@Override
		public double addAmount(double value, boolean doAdd) {
			return value;
		}

		@Override
		public double removeAmount(double value, boolean doRemove) {
			return value;
		}
	};

	public CreativeEmberBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.CREATIVE_EMBER_ENTITY.get(), pPos, pBlockState);
		capability.setEmberCapacity(80000);
	}

    @Override
    public IEmberCapability getEmberCapability(Direction side) {
        return this.remove ? null : capability;
    }
}