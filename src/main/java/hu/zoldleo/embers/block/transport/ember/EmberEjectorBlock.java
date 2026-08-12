package hu.zoldleo.embers.block.transport.ember;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.blockentity.EmberEjectorBlockEntity;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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

public class EmberEjectorBlock extends EmberEmitterBlock {
    public static final MapCodec<EmberEjectorBlock> CODEC = simpleCodec(EmberEjectorBlock::new);

	protected static final VoxelShape UP_AABB = Shapes.or(Block.box(3,0,3,13,2,13),Block.box(4,2,4,12,8,12),Block.box(6,7,6,10,15,10),Block.box(5,8,5,11,10,11),Block.box(5,11,5,11,13,11),Block.box(6.5,0,1.5,9.5,5,14.5),Block.box(1.5,0,6.5,14.5,5,9.5));
	protected static final VoxelShape DOWN_AABB = Misc.rotateVoxelShape(Direction.DOWN, UP_AABB);
	protected static final VoxelShape NORTH_AABB = Misc.rotateVoxelShape(Direction.NORTH, UP_AABB);
	protected static final VoxelShape SOUTH_AABB = Misc.rotateVoxelShape(Direction.SOUTH, UP_AABB);
	protected static final VoxelShape WEST_AABB = Misc.rotateVoxelShape(Direction.WEST, UP_AABB);
	protected static final VoxelShape EAST_AABB = Misc.rotateVoxelShape(Direction.EAST, UP_AABB);

	public EmberEjectorBlock(Properties properties) {
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
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
		return RegistryManager.EMBER_EJECTOR_ENTITY.get().create(pPos, pState);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
		return pLevel.isClientSide ? null : createTickerHelper(pBlockEntityType, RegistryManager.EMBER_EJECTOR_ENTITY.get(), EmberEjectorBlockEntity::serverTick);
	}
}