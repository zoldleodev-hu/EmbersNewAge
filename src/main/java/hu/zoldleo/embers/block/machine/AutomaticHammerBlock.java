package hu.zoldleo.embers.block.machine;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.blockentity.AutomaticHammerBlockEntity;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
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
import org.jetbrains.annotations.NotNull;

public class AutomaticHammerBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<AutomaticHammerBlock> CODEC = simpleCodec(AutomaticHammerBlock::new);

	public static final VoxelShape HAMMER_NORTH_AABB = Shapes.or(Block.box(0,0,12,16,4,16),Block.box(0,4,12,4,12,16),Block.box(12,4,12,16,12,16),Block.box(0,12,12,16,16,16),Block.box(6,6,2,10,10,16),Block.box(4,4,10,12,12,15),Block.box(5,5,6,11,11,10),Block.box(2,9,8,7,14,12),Block.box(9,9,8,14,14,12),Block.box(2,2,8,7,7,12),Block.box(9,2,8,14,7,12));
	public static final VoxelShape HAMMER_EAST_AABB = Misc.rotateVoxelShape(Direction.NORTH, Direction.EAST, HAMMER_NORTH_AABB);
	public static final VoxelShape HAMMER_SOUTH_AABB = Misc.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, HAMMER_NORTH_AABB);
	public static final VoxelShape HAMMER_WEST_AABB = Misc.rotateVoxelShape(Direction.NORTH, Direction.WEST, HAMMER_NORTH_AABB);
	public static final VoxelShape NORTH_INTERACTION = Block.box(2,2,2,14,14,16);
	public static final VoxelShape EAST_INTERACTION = Block.box(0,2,2,14,14,14);
	public static final VoxelShape SOUTH_INTERACTION = Block.box(2,2,0,14,14,14);
	public static final VoxelShape WEST_INTERACTION = Block.box(2,2,2,16,14,14);

	public AutomaticHammerBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.WATERLOGGED, false).setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
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
        return switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case EAST -> HAMMER_EAST_AABB;
            case WEST -> HAMMER_WEST_AABB;
            case SOUTH -> HAMMER_SOUTH_AABB;
            default -> HAMMER_NORTH_AABB;
        };
	}

	@Override
	public @NotNull VoxelShape getInteractionShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case EAST -> EAST_INTERACTION;
            case WEST -> WEST_INTERACTION;
            case SOUTH -> SOUTH_INTERACTION;
            default -> NORTH_INTERACTION;
        };
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
		return RegistryManager.AUTOMATIC_HAMMER_ENTITY.get().create(pPos, pState);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
		return pLevel.isClientSide ? createTickerHelper(pBlockEntityType, RegistryManager.AUTOMATIC_HAMMER_ENTITY.get(), AutomaticHammerBlockEntity::clientTick) : createTickerHelper(pBlockEntityType, RegistryManager.AUTOMATIC_HAMMER_ENTITY.get(), AutomaticHammerBlockEntity::serverTick);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Direction direction = pContext.getClickedFace().getAxis() != Axis.Y ?
                pContext.getClickedFace() : pContext.getHorizontalDirection().getOpposite();
		return super.getStateForPlacement(pContext).setValue(BlockStateProperties.HORIZONTAL_FACING, direction).setValue(BlockStateProperties.WATERLOGGED, pContext.getLevel().getFluidState(pContext.getClickedPos()).is(Fluids.WATER));
	}

	@Override
	public @NotNull BlockState updateShape(BlockState pState, @NotNull Direction pFacing, @NotNull BlockState pFacingState, @NotNull LevelAccessor pLevel, @NotNull BlockPos pCurrentPos, @NotNull BlockPos pFacingPos) {
		if (pState.getValue(BlockStateProperties.WATERLOGGED))
			pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
		return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
		pBuilder.add(BlockStateProperties.WATERLOGGED, BlockStateProperties.HORIZONTAL_FACING);
	}

	@Override
	public @NotNull FluidState getFluidState(BlockState pState) {
		return pState.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
	}

	@Override
	public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
	}

	@Override
	public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
		return state.setValue(BlockStateProperties.HORIZONTAL_FACING, mirror.mirror(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
	}
}