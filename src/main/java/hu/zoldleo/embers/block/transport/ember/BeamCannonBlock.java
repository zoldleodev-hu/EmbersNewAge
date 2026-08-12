package hu.zoldleo.embers.block.transport.ember;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.blockentity.BeamCannonBlockEntity;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class BeamCannonBlock extends EmberEmitterBlock {
    public static final MapCodec<BeamCannonBlock> CODEC = simpleCodec(BeamCannonBlock::new);

	protected static final VoxelShape UP_AABB = Shapes.or(Block.box(3,0,3,13,2,13),Block.box(7,-1,1,9,7,3),Block.box(7,-1,13,9,7,15),Block.box(13,-1,7,15,7,9),Block.box(1,-1,7,3,7,9),Block.box(2,2,2,4,4,14),Block.box(12,2,2,14,4,14),Block.box(4,2,2,12,4,4),Block.box(4,2,12,12,4,14),Block.box(5.5,1.5,5.5,10.5,6.5,10.5),Block.box(5,12,5,11,14,11),Block.box(5,9,5,11,11,11),Block.box(5,6,5,11,8,11),Block.box(6,6,6,10,16,10),Block.box(5.1,-2,5.1,10.9,0,10.9));
	protected static final VoxelShape DOWN_AABB = Misc.rotateVoxelShape(Direction.DOWN, UP_AABB);
	protected static final VoxelShape NORTH_AABB = Misc.rotateVoxelShape(Direction.NORTH, UP_AABB);
	protected static final VoxelShape SOUTH_AABB = Misc.rotateVoxelShape(Direction.SOUTH, UP_AABB);
	protected static final VoxelShape WEST_AABB = Misc.rotateVoxelShape(Direction.WEST, UP_AABB);
	protected static final VoxelShape EAST_AABB = Misc.rotateVoxelShape(Direction.EAST, UP_AABB);

	protected static final VoxelShape X_INTERACTION = Shapes.box(0,0.3125,0.3125,1,0.6875,0.6875);
	protected static final VoxelShape Y_INTERACTION = Shapes.box(0.3125,0,0.3125,0.6875,1,0.6875);
	protected static final VoxelShape Z_INTERACTION = Shapes.box(0.3125,0.3125,0,0.6875,0.6875,1);
	protected static final VoxelShape SUPPORT_UP = Shapes.or(Shapes.box(0,0,0,1,1,0.1), Shapes.box(0,0,0.9,1,1,1), Shapes.box(0,0,0,0.1,1,1), Shapes.box(0.9,0,0,1,1,1), Shapes.box(0,0,0,1,0.1,1));
	protected static final VoxelShape SUPPORT_DOWN = Shapes.or(Shapes.box(0,0,0,1,1,0.1), Shapes.box(0,0,0.9,1,1,1), Shapes.box(0,0,0,0.1,1,1), Shapes.box(0.9,0,0,1,1,1), Shapes.box(0,0.9,0,1,1,1));
	protected static final VoxelShape SUPPORT_NORTH = Shapes.or(Shapes.box(0,0,0,1,0.1,1), Shapes.box(0,0.9,0,1,1,1), Shapes.box(0,0,0,0.1,1,1), Shapes.box(0.9,0,0,1,1,1), Shapes.box(0,0,0.9,1,1,1));
	protected static final VoxelShape SUPPORT_SOUTH = Shapes.or(Shapes.box(0,0,0,1,0.1,1), Shapes.box(0,0.9,0,1,1,1), Shapes.box(0,0,0,0.1,1,1), Shapes.box(0.9,0,0,1,1,1), Shapes.box(0,0,0,1,1,0.1));
	protected static final VoxelShape SUPPORT_WEST = Shapes.or(Shapes.box(0,0,0,1,1,0.1), Shapes.box(0,0,0.9,1,1,1), Shapes.box(0,0,0,1,0.1,1), Shapes.box(0,0.9,0,1,1,1), Shapes.box(0.9,0,0,1,1,1));
	protected static final VoxelShape SUPPORT_EAST = Shapes.or(Shapes.box(0,0,0,1,1,0.1), Shapes.box(0,0,0.9,1,1,1), Shapes.box(0,0,0,1,0.1,1), Shapes.box(0,0.9,0,1,1,1), Shapes.box(0,0,0,0.1,1,1));

	public BeamCannonBlock(Properties properties) {
		super(properties);
	}

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
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

	@Override
	public @NotNull VoxelShape getInteractionShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return switch (state.getValue(FACING).getAxis()) {
            case X -> X_INTERACTION;
            case Y -> Y_INTERACTION;
            default -> Z_INTERACTION;
        };
	}

	@Override
	public boolean canSurvive(BlockState pState, @NotNull LevelReader pLevel, @NotNull BlockPos pPos) {
		return true;
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
		return RegistryManager.BEAM_CANNON_ENTITY.get().create(pPos, pState);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
		return pLevel.isClientSide ? createTickerHelper(pBlockEntityType, RegistryManager.BEAM_CANNON_ENTITY.get(), BeamCannonBlockEntity::clientTick) : createTickerHelper(pBlockEntityType, RegistryManager.BEAM_CANNON_ENTITY.get(), BeamCannonBlockEntity::serverTick);
	}

	@Override
	public @NotNull VoxelShape getBlockSupportShape(BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos) {
        return switch (pState.getValue(FACING)) {
            case UP -> SUPPORT_UP;
            case DOWN -> SUPPORT_DOWN;
            case EAST -> SUPPORT_EAST;
            case WEST -> SUPPORT_WEST;
            case SOUTH -> SUPPORT_SOUTH;
            default -> SUPPORT_NORTH;
        };
	}
}