package hu.zoldleo.embers.block.machine;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.blockentity.IgnemReactorBlockEntity;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class IgnemReactorBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<IgnemReactorBlock> CODEC = simpleCodec(IgnemReactorBlock::new);

	protected static final VoxelShape REACTOR_AABB = Shapes.or(Block.box(3,4,3,5,16,5),Block.box(7,4,3,9,16,5),Block.box(11,4,3,13,16,5),Block.box(11,4,7,13,16,9),Block.box(11,4,11,13,16,13),Block.box(7,4,11,9,16,13),Block.box(3,4,11,5,16,13),Block.box(3,4,7,5,16,9),Block.box(3,6,5,5,8,11),Block.box(5,6,3,11,8,5),Block.box(11,6,5,13,8,11),Block.box(5,6,11,11,8,13),Block.box(5,10,11,11,12,13),Block.box(11,10,5,13,12,11),Block.box(5,10,3,11,12,5),Block.box(3,10,5,5,12,11),Block.box(13,6,6,15,10,10),Block.box(1,6,6,3,10,10),Block.box(6,6,1,10,10,3),Block.box(6,6,13,10,10,15),Block.box(0,5,5,2,11,11),Block.box(14,5,5,16,11,11),Block.box(5,5,14,11,11,16),Block.box(5,5,0,11,11,2),Block.box(4.5,0,4.5,11.5,7,11.5),Block.box(2,0,2,6,4,6),Block.box(10,0,2,14,4,6),Block.box(2,0,10,6,4,14),Block.box(10,0,10,14,4,14));
	protected static final VoxelShape REACTOR_INTERACTION = Shapes.or(Block.box(3,4,3,13,16,13),Block.box(2,0,2,14,4,14));

	public IgnemReactorBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.WATERLOGGED, false));
	}

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
	public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
            if (handler != null) {
                Misc.spawnInventoryInWorld(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, handler);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, isMoving);
		}
	}

	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
		return RenderShape.MODEL;
	}

	@Override
	public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return REACTOR_AABB;
	}

	@Override
	public @NotNull VoxelShape getInteractionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
		return REACTOR_INTERACTION;
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
		return RegistryManager.IGNEM_REACTOR_ENTITY.get().create(pPos, pState);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
		return pLevel.isClientSide ? createTickerHelper(pBlockEntityType, RegistryManager.IGNEM_REACTOR_ENTITY.get(), IgnemReactorBlockEntity::clientTick) : createTickerHelper(pBlockEntityType, RegistryManager.IGNEM_REACTOR_ENTITY.get(), IgnemReactorBlockEntity::serverTick);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(@NotNull BlockPlaceContext pContext) {
		return super.getStateForPlacement(pContext).setValue(BlockStateProperties.WATERLOGGED, pContext.getLevel().getFluidState(pContext.getClickedPos()).is(Fluids.WATER));
	}

	@Override
	public @NotNull BlockState updateShape(BlockState pState, @NotNull Direction pFacing, @NotNull BlockState pFacingState, @NotNull LevelAccessor pLevel, @NotNull BlockPos pCurrentPos, @NotNull BlockPos pFacingPos) {
		if (pState.getValue(BlockStateProperties.WATERLOGGED))
			pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
		return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
		pBuilder.add(BlockStateProperties.WATERLOGGED);
	}

	@Override
	public @NotNull FluidState getFluidState(BlockState pState) {
		return pState.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
	}
}