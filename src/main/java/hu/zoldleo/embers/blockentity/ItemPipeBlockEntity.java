package hu.zoldleo.embers.blockentity;

import hu.zoldleo.embers.RegistryManager;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ItemPipeBlockEntity extends ItemPipeBlockEntityBase {
	public ItemPipeBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.ITEM_PIPE_ENTITY.get(), pPos, pBlockState);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ItemPipeBlockEntity blockEntity) {
		ItemPipeBlockEntityBase.serverTick(level, pos, state, blockEntity);
	}
}