package hu.zoldleo.embers.block;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
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

public class MechanicalCoreBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<MechanicalCoreBlock> CODEC = simpleCodec(MechanicalCoreBlock::new);

	public static final VoxelShape UP_AABB = Shapes.or(Block.box(0,12,12,16,16,16),Block.box(0,12,0,16,16,4),Block.box(12,12,4,16,16,12),Block.box(0,12,4,4,16,12),Block.box(0,0,0,4,12,4),Block.box(0,0,12,4,12,16),Block.box(12,0,12,16,12,16),Block.box(12,0,0,16,12,4),Block.box(4,4,2,12,12,14),Block.box(3,2,3,13,14,13),Block.box(2,4,4,14,12,12),Block.box(0,6,6,16,10,10),Block.box(6,6,0,10,10,16),Block.box(6,0,6,10,16,10));
	public static final VoxelShape DOWN_AABB = Misc.rotateVoxelShape(Direction.DOWN, UP_AABB);
	public static final VoxelShape NORTH_AABB = Misc.rotateVoxelShape(Direction.NORTH, UP_AABB);
	public static final VoxelShape SOUTH_AABB = Misc.rotateVoxelShape(Direction.SOUTH, UP_AABB);
	public static final VoxelShape WEST_AABB = Misc.rotateVoxelShape(Direction.WEST, UP_AABB);
	public static final VoxelShape EAST_AABB = Misc.rotateVoxelShape(Direction.EAST, UP_AABB);

	public MechanicalCoreBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.FACING, Direction.UP).setValue(BlockStateProperties.WATERLOGGED, false));
	}

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
		return RenderShape.MODEL;
	}

	@Override
	public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return switch (state.getValue(BlockStateProperties.FACING)) {
            case UP -> UP_AABB;
            case DOWN -> DOWN_AABB;
            case EAST -> EAST_AABB;
            case WEST -> WEST_AABB;
            case SOUTH -> SOUTH_AABB;
            default -> NORTH_AABB;
        };
	}

	@Override
	public @NotNull VoxelShape getBlockSupportShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos) {
		return Shapes.block();
	}

	@Override
	public @NotNull VoxelShape getInteractionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
		return Shapes.block();
	}

	//okay this is jank but I don't see how this could possibly go wrong
	@Override
	public void onNeighborChange(BlockState state, @NotNull LevelReader level, BlockPos pos, @NotNull BlockPos neighbor) {
		if (pos.relative(state.getValue(BlockStateProperties.FACING), -1).equals(neighbor))
			level.getBlockEntity(pos).getLevel().updateNeighbourForOutputSignal(pos, state.getBlock());
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
		return RegistryManager.MECHANICAL_CORE_ENTITY.get().create(pPos, pState);
	}

	@Override
	public BlockState getStateForPlacement(@NotNull BlockPlaceContext pContext) {
		return super.getStateForPlacement(pContext).setValue(BlockStateProperties.FACING, pContext.getClickedFace()).setValue(BlockStateProperties.WATERLOGGED, pContext.getLevel().getFluidState(pContext.getClickedPos()).is(Fluids.WATER));
	}

	@Override
	public @NotNull BlockState updateShape(BlockState pState, @NotNull Direction pFacing, @NotNull BlockState pFacingState, @NotNull LevelAccessor pLevel, @NotNull BlockPos pCurrentPos, @NotNull BlockPos pFacingPos) {
		if (pState.getValue(BlockStateProperties.WATERLOGGED))
			pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
		return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
		pBuilder.add(BlockStateProperties.WATERLOGGED).add(BlockStateProperties.FACING);
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
