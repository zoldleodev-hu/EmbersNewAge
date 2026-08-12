package hu.zoldleo.embers.block.upgrade;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.RegistryManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class HeatInsulationBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<HeatInsulationBlock> CODEC = simpleCodec(HeatInsulationBlock::new);

	protected static final VoxelShape UP_AABB = Shapes.or(Block.box(2,13,2,14,15,14), Block.box(0,12,0,4,16,4), Block.box(12,12,0,16,16,4), Block.box(12,12,12,16,16,16), Block.box(0,12,12,4,16,16), Block.box(1,4,1,15,13,15), Block.box(0,0,0,16,4,16));
	protected static final VoxelShape DOWN_AABB = Shapes.or(Block.box(2,1,2,14,3,14), Block.box(0,0,12,4,4,16), Block.box(12,0,12,16,4,16), Block.box(12,0,0,16,4,4), Block.box(0,0,0,4,4,4), Block.box(1,3,1,15,12,15), Block.box(0,12,0,16,16,16));
	protected static final VoxelShape NORTH_AABB = Shapes.or(Block.box(2,2,1,14,14,3), Block.box(0,0,0,4,4,4), Block.box(12,0,0,16,4,4), Block.box(12,12,0,16,16,4), Block.box(0,12,0,4,16,4), Block.box(1,1,3,15,15,12), Block.box(0,0,12,16,16,16));
	protected static final VoxelShape SOUTH_AABB = Shapes.or(Block.box(2,2,13,14,14,15), Block.box(12,0,12,16,4,16), Block.box(0,0,12,4,4,16), Block.box(0,12,12,4,16,16), Block.box(12,12,12,16,16,16), Block.box(1,1,4,15,15,13), Block.box(0,0,0,16,16,4));
	protected static final VoxelShape WEST_AABB = Shapes.or(Block.box(1,2,2,3,14,14), Block.box(0,0,12,4,4,16), Block.box(0,0,0,4,4,4), Block.box(0,12,0,4,16,4), Block.box(0,12,12,4,16,16), Block.box(3,1,1,12,15,15), Block.box(12,0,0,16,16,16));
	protected static final VoxelShape EAST_AABB = Shapes.or(Block.box(13,2,2,15,14,14), Block.box(12,0,0,16,4,4), Block.box(12,0,12,16,4,16), Block.box(12,12,12,16,16,16), Block.box(12,12,0,16,16,4), Block.box(4,1,1,13,15,15), Block.box(0,0,0,4,16,16));

	public HeatInsulationBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.FACING, Direction.UP).setValue(BlockStateProperties.WATERLOGGED, false));
	}

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
	public @NotNull VoxelShape getShape(BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return switch (pState.getValue(BlockStateProperties.FACING)) {
            case UP -> UP_AABB;
            case DOWN -> DOWN_AABB;
            case EAST -> EAST_AABB;
            case WEST -> WEST_AABB;
            case SOUTH -> SOUTH_AABB;
            default -> NORTH_AABB;
        };
	}

	@Override
	public @NotNull VoxelShape getInteractionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
		return Shapes.block();
	}

	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		for (Direction direction : pContext.getNearestLookingDirections()) {
			BlockState blockstate = this.defaultBlockState().setValue(BlockStateProperties.FACING, direction.getOpposite());
			return blockstate.setValue(BlockStateProperties.WATERLOGGED, pContext.getLevel().getFluidState(pContext.getClickedPos()).is(Fluids.WATER));
		}
		return null;
	}

	@Override
	public @NotNull BlockState updateShape(BlockState pState, @NotNull Direction pFacing, @NotNull BlockState pFacingState, @NotNull LevelAccessor pLevel, @NotNull BlockPos pCurrentPos, @NotNull BlockPos pFacingPos) {
		if (pState.getValue(BlockStateProperties.WATERLOGGED))
			pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
		return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
		pBuilder.add(BlockStateProperties.FACING).add(BlockStateProperties.WATERLOGGED);
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
		return RegistryManager.HEAT_INSULATION_ENTITY.get().create(pPos, pState);
	}

	@Override
	public @NotNull FluidState getFluidState(BlockState pState) {
		return pState.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
	}

	@Override
	public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(BlockStateProperties.FACING, rotation.rotate(state.getValue(BlockStateProperties.FACING)));
	}

	@Override
	public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
		return state.setValue(BlockStateProperties.FACING, mirror.mirror(state.getValue(BlockStateProperties.FACING)));
	}
}