package hu.zoldleo.embers.block.transport.fluid;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.block.transport.ExtractorBlockBase;
import hu.zoldleo.embers.blockentity.FluidExtractorBlockEntity;
import hu.zoldleo.embers.blockentity.FluidPipeBlockEntityBase;
import hu.zoldleo.embers.datagen.EmbersBlockTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public class FluidExtractorBlock extends ExtractorBlockBase {
    public static final MapCodec<FluidExtractorBlock> CODEC = simpleCodec(FluidExtractorBlock::new);

	public FluidExtractorBlock(Properties pProperties) {
		super(pProperties);
	}

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
		return RegistryManager.FLUID_EXTRACTOR_ENTITY.get().create(pPos, pState);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
		return pLevel.isClientSide ? createTickerHelper(pBlockEntityType, RegistryManager.FLUID_EXTRACTOR_ENTITY.get(), FluidExtractorBlockEntity::clientTick) : createTickerHelper(pBlockEntityType, RegistryManager.FLUID_EXTRACTOR_ENTITY.get(), FluidExtractorBlockEntity::serverTick);
	}

	@Override
	public TagKey<Block> getConnectionTag() {
		return EmbersBlockTags.FLUID_PIPE_CONNECTION;
	}

	@Override
	public TagKey<Block> getToggleConnectionTag() {
		return EmbersBlockTags.FLUID_PIPE_CONNECTION_TOGGLEABLE;
	}

	@Override
	public boolean connectToBlock(Level level, BlockPos pos, Direction direction) {
		return level.getCapability(Capabilities.FluidHandler.BLOCK, pos, direction.getOpposite()) != null;
	}

	@Override
	public boolean unclog(BlockEntity blockEntity, Level level, BlockPos pos) {
		if (blockEntity instanceof FluidPipeBlockEntityBase pipeEntity && pipeEntity.clogged) {
			IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
			handler.drain(handler.getTankCapacity(0), IFluidHandler.FluidAction.EXECUTE);
			level.updateNeighbourForOutputSignal(pos, this);
			return true;
		}
		return false;
	}
}