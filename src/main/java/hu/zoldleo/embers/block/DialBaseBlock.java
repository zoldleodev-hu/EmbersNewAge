package hu.zoldleo.embers.block;

import hu.zoldleo.embers.api.block.IDial;
import hu.zoldleo.embers.api.tile.IExtraDialInformation;
import hu.zoldleo.embers.network.message.MessageDialUpdateRequest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

// TODO: read dial info using null as capability context
public abstract class DialBaseBlock extends DirectionalBlock implements IDial, EntityBlock, SimpleWaterloggedBlock {
	protected static final VoxelShape UP_INTERACTION = Shapes.box(0.3125,0,0.3125,0.6875,0.125,0.6875);
	protected static final VoxelShape DOWN_INTERACTION = Shapes.box(0.3125,0.875,0.3125,0.6875,1.0,0.6875);
	protected static final VoxelShape NORTH_INTERACTION = Shapes.box(0.3125,0.3125,0.875,0.6875,0.6875,1.0);
	protected static final VoxelShape SOUTH_INTERACTION = Shapes.box(0.3125,0.3125,0,0.6875,0.6875,0.125);
	protected static final VoxelShape WEST_INTERACTION = Shapes.box(0.875,0.3125,0.3125,1.0,0.6875,0.6875);
	protected static final VoxelShape EAST_INTERACTION = Shapes.box(0.0,0.3125,0.3125,0.125,0.6875,0.6875);

	protected static final VoxelShape UP_AABB = Shapes.join(UP_INTERACTION, Shapes.box(0.375,0.0625,0.375,0.625,0.125,0.625), BooleanOp.ONLY_FIRST);
	protected static final VoxelShape DOWN_AABB = Shapes.join(DOWN_INTERACTION, Shapes.box(0.375,0.875,0.375,0.625,0.9375,0.625), BooleanOp.ONLY_FIRST);
	protected static final VoxelShape NORTH_AABB = Shapes.join(NORTH_INTERACTION, Shapes.box(0.375,0.375,0.875,0.625,0.625,0.9375), BooleanOp.ONLY_FIRST);
	protected static final VoxelShape SOUTH_AABB = Shapes.join(SOUTH_INTERACTION, Shapes.box(0.375,0.375,0.0625,0.625,0.625,0.125), BooleanOp.ONLY_FIRST);
	protected static final VoxelShape WEST_AABB = Shapes.join(WEST_INTERACTION, Shapes.box(0.875,0.375,0.375,0.9375,0.625,0.625), BooleanOp.ONLY_FIRST);
	protected static final VoxelShape EAST_AABB = Shapes.join(EAST_INTERACTION, Shapes.box(0.0625,0.375,0.375,0.125,0.625,0.625), BooleanOp.ONLY_FIRST);

	public DialBaseBlock(Properties pProperties) {
		super(pProperties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(BlockStateProperties.POWER, 0).setValue(BlockStateProperties.WATERLOGGED, false));
	}

	@Override
	public @NotNull VoxelShape getShape(BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return switch (pState.getValue(FACING)) {
            case UP -> UP_AABB;
            case DOWN -> DOWN_AABB;
            case EAST -> EAST_AABB;
            case WEST -> WEST_AABB;
            case SOUTH -> SOUTH_AABB;
            default -> NORTH_AABB;
        };
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

	//okay this is jank but I don't see how this could possibly go wrong
	@Override
	public void onNeighborChange(BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos, @NotNull BlockPos neighbor) {
		if (state.hasAnalogOutputSignal()) {
			BlockEntity blockEntity = level.getBlockEntity(pos.relative(state.getValue(FACING), -1));
			if (blockEntity != null && blockEntity.hasLevel()) {
                //noinspection DataFlowIssue -> Level should not be null
				int power = state.getAnalogOutputSignal(blockEntity.getLevel(), pos);
				if (state.getValue(BlockStateProperties.POWER) != power) {
					blockEntity.getLevel().setBlock(pos, state.setValue(BlockStateProperties.POWER, power), 1);
					blockEntity.getLevel().updateNeighbourForOutputSignal(pos, state.getBlock());
				}
			}
		}
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
		for(Direction direction : pContext.getNearestLookingDirections()) {
			BlockState blockstate = this.defaultBlockState().setValue(FACING, direction.getOpposite());
			if (blockstate.canSurvive(pContext.getLevel(), pContext.getClickedPos()))
				return blockstate.setValue(BlockStateProperties.WATERLOGGED, pContext.getLevel().getFluidState(pContext.getClickedPos()).is(Fluids.WATER));
		}
		return null;
	}

	@Override
	public @NotNull BlockState updateShape(BlockState pState, @NotNull Direction pFacing, @NotNull BlockState pFacingState, @NotNull LevelAccessor pLevel, @NotNull BlockPos pCurrentPos, @NotNull BlockPos pFacingPos) {
		if (pState.getValue(BlockStateProperties.WATERLOGGED))
			pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
		return pState.getValue(FACING).getOpposite() == pFacing && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
		pBuilder.add(FACING).add(BlockStateProperties.POWER).add(BlockStateProperties.WATERLOGGED);
	}

	@Override
	public List<Component> getDisplayInfo(Level world, BlockPos pos, BlockState state, int maxLines) {
		ArrayList<Component> text = new ArrayList<>();
		BlockEntity tileEntity = world.getBlockEntity(pos);
		if (tileEntity != null) {
			Direction facing = state.getValue(FACING);
			getBEData(facing, text, tileEntity, maxLines);
			if (world.getBlockEntity(pos.relative(facing, -1)) instanceof IExtraDialInformation facingTile)
				facingTile.addDialInformation(facing, text, getDialType());
		}
		return text;
	}

	protected abstract void getBEData(Direction facing, ArrayList<Component> text, BlockEntity blockEntity, int maxLines);

	@Override
	public void updateBEData(BlockPos pos, int maxLines) {
        PacketDistributor.sendToServer(new MessageDialUpdateRequest(pos, maxLines));
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