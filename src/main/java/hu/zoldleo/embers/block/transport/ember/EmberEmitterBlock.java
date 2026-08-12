package hu.zoldleo.embers.block.transport.ember;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.block.transport.PipeBlockBase;
import hu.zoldleo.embers.blockentity.EmberEmitterBlockEntity;
import hu.zoldleo.embers.datagen.EmbersBlockTags;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class EmberEmitterBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<EmberEmitterBlock> CODEC = simpleCodec(EmberEmitterBlock::new);

	protected static final VoxelShape UP_AABB = Shapes.or(Block.box(5,2,5,11,11,11),Block.box(4,0,4,12,2,12),Block.box(4,7,4,12,9,12),Block.box(6,11,6,10,15,10),Block.box(7,12,5,9,14,11),Block.box(5,12,7,11,14,9));
	protected static final VoxelShape DOWN_AABB = Misc.rotateVoxelShape(Direction.DOWN, UP_AABB);
	protected static final VoxelShape NORTH_AABB = Misc.rotateVoxelShape(Direction.NORTH, UP_AABB);
	protected static final VoxelShape SOUTH_AABB = Misc.rotateVoxelShape(Direction.SOUTH, UP_AABB);
	protected static final VoxelShape WEST_AABB = Misc.rotateVoxelShape(Direction.WEST, UP_AABB);
	protected static final VoxelShape EAST_AABB = Misc.rotateVoxelShape(Direction.EAST, UP_AABB);

	protected static final VoxelShape UP_INTERACTION = Shapes.box(0.25,0,0.25,0.75,0.9375,0.75);
	protected static final VoxelShape DOWN_INTERACTION = Shapes.box(0.25,0.0625,0.25,0.75,1.0,0.75);
	protected static final VoxelShape NORTH_INTERACTION = Shapes.box(0.25,0.25,0.0625,0.75,0.75,1.0);
	protected static final VoxelShape SOUTH_INTERACTION = Shapes.box(0.25,0.25,0,0.75,0.75,0.9375);
	protected static final VoxelShape WEST_INTERACTION = Shapes.box(0.0625,0.25,0.25,1.0,0.75,0.75);
	protected static final VoxelShape EAST_INTERACTION = Shapes.box(0.0,0.25,0.25,0.9375,0.75,0.75);
	protected static final VoxelShape SUPPORT_X = Shapes.or(Shapes.box(0,0,0,1,1,0.1), Shapes.box(0,0,0.9,1,1,1), Shapes.box(0,0,0,1,0.1,1), Shapes.box(0,0.9,0,1,1,1));
	protected static final VoxelShape SUPPORT_Y = Shapes.or(Shapes.box(0,0,0,1,1,0.1), Shapes.box(0,0,0.9,1,1,1), Shapes.box(0,0,0,0.1,1,1), Shapes.box(0.9,0,0,1,1,1));
	protected static final VoxelShape SUPPORT_Z = Shapes.or(Shapes.box(0,0,0,1,0.1,1), Shapes.box(0,0.9,0,1,1,1), Shapes.box(0,0,0,0.1,1,1), Shapes.box(0.9,0,0,1,1,1));

	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	public static final Direction[] X_DIRECTIONS = { Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH };
	public static final Direction[] Y_DIRECTIONS = { Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST };
	public static final Direction[] Z_DIRECTIONS = { Direction.DOWN, Direction.UP, Direction.WEST, Direction.EAST };
	public static final BooleanProperty[] DIRECTIONS = { BooleanProperty.create("front"), BooleanProperty.create("right"), BooleanProperty.create("back"), BooleanProperty.create("left") };
	public static final BooleanProperty[] ALL_DIRECTIONS = { BlockStateProperties.DOWN, BlockStateProperties.UP, BlockStateProperties.NORTH, BlockStateProperties.SOUTH, BlockStateProperties.WEST, BlockStateProperties.EAST };

	public EmberEmitterBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(BlockStateProperties.WATERLOGGED, false)
				.setValue(DIRECTIONS[0], false)
				.setValue(DIRECTIONS[1], false)
				.setValue(DIRECTIONS[2], false)
				.setValue(DIRECTIONS[3], false));
	}

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public static int getIndexForDirection(Direction.Axis axis, Direction direction) {
		int index = direction.get3DDataValue();
        return switch (axis) {
            case Z -> index > 1 ? index - 2 : index;
            case Y -> index - 2;
            default -> index;
        };
	}

	public static Direction getDirectionForIndex(Direction.Axis axis, int index) {
        return switch (axis) {
            case X -> X_DIRECTIONS[index];
            case Z -> Z_DIRECTIONS[index];
            default -> Y_DIRECTIONS[index];
        };
	}

	public VoxelShape[][] shapeCache = new VoxelShape[6][16];

	@Override
	public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case UP -> addPipeConnections(state, UP_AABB, shapeCache);
            case DOWN -> addPipeConnections(state, DOWN_AABB, shapeCache);
            case EAST -> addPipeConnections(state, EAST_AABB, shapeCache);
            case WEST -> addPipeConnections(state, WEST_AABB, shapeCache);
            case SOUTH -> addPipeConnections(state, SOUTH_AABB, shapeCache);
            default -> addPipeConnections(state, NORTH_AABB, shapeCache);
        };
	}

	public static VoxelShape addPipeConnections(BlockState state, VoxelShape shape, VoxelShape[][] cache) {
		int face = state.getValue(FACING).get3DDataValue();
		int cacheValue = 0;
        for (BooleanProperty direction : DIRECTIONS) {
            cacheValue *= 2;
            cacheValue += state.getValue(direction).compareTo(false);
        }
		if (cache[face][cacheValue] != null)
			return cache[face][cacheValue];

		Axis axis = state.getValue(FACING).getAxis();
		for (int i = 0; i < DIRECTIONS.length; ++i) {
			if (state.getValue(DIRECTIONS[i])) {
				shape = Shapes.joinUnoptimized(shape, PipeBlockBase.END_AABBS[getDirectionForIndex(axis, i).get3DDataValue()], BooleanOp.OR);
			}				
		}
		cache[face][cacheValue] = shape.optimize();
		return cache[face][cacheValue];
	}

	@Override
	public @NotNull VoxelShape getInteractionShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return switch (state.getValue(FACING)) {
            case UP -> UP_INTERACTION;
            case DOWN -> DOWN_INTERACTION;
            case EAST -> EAST_INTERACTION;
            case WEST -> WEST_INTERACTION;
            case SOUTH -> SOUTH_INTERACTION;
            default -> NORTH_INTERACTION;
        };
	}

	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
		return RenderShape.MODEL;
	}

	@Override
	public boolean canSurvive(BlockState pState, @NotNull LevelReader pLevel, @NotNull BlockPos pPos) {
		return canAttach(pLevel, pPos, pState.getValue(FACING).getOpposite());
	}

	public static boolean canAttach(LevelReader pReader, BlockPos pPos, Direction pDirection) {
		return !pReader.getBlockState(pPos.relative(pDirection)).isAir();
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		for (Direction direction : pContext.getNearestLookingDirections()) {
			BlockState blockstate = this.defaultBlockState().setValue(FACING, direction.getOpposite());
			if (blockstate.canSurvive(pContext.getLevel(), pContext.getClickedPos())) {
				for (int i = 0; i < DIRECTIONS.length; i++) {
					Direction facing = getDirectionForIndex(direction.getAxis(), i);
					blockstate = blockstate.setValue(DIRECTIONS[i], connected(facing, pContext.getLevel().getBlockState(pContext.getClickedPos().relative(facing))));
				}
				return blockstate.setValue(BlockStateProperties.WATERLOGGED, pContext.getLevel().getFluidState(pContext.getClickedPos()).is(Fluids.WATER));
			}
		}
		return null;
	}

	@Override
	public @NotNull BlockState updateShape(BlockState pState, @NotNull Direction pFacing, @NotNull BlockState pFacingState, @NotNull LevelAccessor pLevel, @NotNull BlockPos pCurrentPos, @NotNull BlockPos pFacingPos) {
		if (pState.getValue(BlockStateProperties.WATERLOGGED)) {
			pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
		}
		if (pState.getValue(FACING).getAxis() != pFacing.getAxis()) {
			pState = pState.setValue(DIRECTIONS[getIndexForDirection(pState.getValue(FACING).getAxis(), pFacing)], connected(pFacing, pFacingState));
		}
		return pState.getValue(FACING).getOpposite() == pFacing && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
	}

	private static boolean facingConnected(Direction facing, BlockState state, DirectionProperty property) {
		return !state.hasProperty(property) || state.getValue(property) == facing;
	}

	private static boolean connected(Direction direction, BlockState state) {
		if (!state.is(EmbersBlockTags.EMITTER_CONNECTION)) {
			return false;
		}
		//always connect to floor or ceiling blocks but only on the top and bottom
		if (state.is(EmbersBlockTags.EMITTER_CONNECTION_FLOOR) || state.is(EmbersBlockTags.EMITTER_CONNECTION_CEILING)) {
			if (direction == Direction.DOWN && state.is(EmbersBlockTags.EMITTER_CONNECTION_CEILING)) {
				return true;
			}
			return direction == Direction.UP;
		}
		//if the block has a side property, use that
		BooleanProperty sideProp = ALL_DIRECTIONS[direction.getOpposite().get3DDataValue()];
		if (state.hasProperty(sideProp) && state.getValue(sideProp)) {
			return true;
		}
		//only support ceiling bells
		if (state.hasProperty(BlockStateProperties.BELL_ATTACHMENT) && state.getValue(BlockStateProperties.BELL_ATTACHMENT) == BellAttachType.CEILING && direction == Direction.DOWN) {
			return true;
		}
		//lantern hanging property
		if (state.hasProperty(BlockStateProperties.HANGING)) {
			if (direction == Direction.DOWN && state.getValue(BlockStateProperties.HANGING))
				return true;
            return direction == Direction.UP && !state.getValue(BlockStateProperties.HANGING);
        }
		//connect to blocks on the same axis
		if (state.hasProperty(BlockStateProperties.AXIS) && !state.hasProperty(BlockStateProperties.FACING)) {
			return state.getValue(BlockStateProperties.AXIS) == direction.getAxis();
		}
		//if there is a face property and it is not wall, only check floor and ceiling
		if (state.hasProperty(BlockStateProperties.ATTACH_FACE) && state.getValue(BlockStateProperties.ATTACH_FACE) != AttachFace.WALL) {
			if (direction == Direction.DOWN && state.getValue(BlockStateProperties.ATTACH_FACE) == AttachFace.CEILING)
				return true;
            return direction == Direction.UP && state.getValue(BlockStateProperties.ATTACH_FACE) == AttachFace.FLOOR;
        }
		//try relevant facing properties, if any are present must be facing this
		return facingConnected(direction, state, BlockStateProperties.HORIZONTAL_FACING)
				&& facingConnected(direction, state, BlockStateProperties.FACING)
				&& facingConnected(direction, state, BlockStateProperties.FACING_HOPPER);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
		pBuilder.add(FACING).add(DIRECTIONS).add(BlockStateProperties.WATERLOGGED);
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
		return RegistryManager.EMBER_EMITTER_ENTITY.get().create(pPos, pState);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
		return pLevel.isClientSide ? null : createTickerHelper(pBlockEntityType, RegistryManager.EMBER_EMITTER_ENTITY.get(), EmberEmitterBlockEntity::serverTick);
	}

	@Override
	public @NotNull VoxelShape getBlockSupportShape(BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos) {
        return switch (pState.getValue(FACING).getAxis()) {
            case X -> SUPPORT_X;
            case Y -> SUPPORT_Y;
            default -> SUPPORT_Z;
        };
	}

	@Override
	public @NotNull FluidState getFluidState(BlockState pState) {
		return pState.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
	}

	@Override
	public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
		return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
	}
}
