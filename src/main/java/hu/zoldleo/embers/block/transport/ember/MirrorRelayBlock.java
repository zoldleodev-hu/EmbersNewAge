package hu.zoldleo.embers.block.transport.ember;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.RegistryManager;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class MirrorRelayBlock extends EmberReceiverBlock {
    public static final MapCodec<MirrorRelayBlock> CODEC = simpleCodec(MirrorRelayBlock::new);

	protected static final VoxelShape UP_AABB = Shapes.or(box(5,0,5,11,1,11), box(6,1,6,10,2,10),
			Shapes.join(box(4,0,4,12,3,12), Shapes.or(box(4,0,7,12,3,9), box(7,0,4,9,3,12)), BooleanOp.ONLY_FIRST));
	protected static final VoxelShape DOWN_AABB = Shapes.or(box(5,15,5,11,16,11), box(6,14,6,10,15,10),
			Shapes.join(box(4,13,4,12,16,12), Shapes.or(box(4,13,7,12,16,9), box(7,13,4,9,16,12)), BooleanOp.ONLY_FIRST));
	protected static final VoxelShape NORTH_AABB = Shapes.or(box(5,5,15,11,11,16), box(6,6,14,10,10,15),
			Shapes.join(box(4,4,13,12,12,16), Shapes.or(box(4,7,13,12,9,16), box(7,4,13,9,12,16)), BooleanOp.ONLY_FIRST));
	protected static final VoxelShape SOUTH_AABB = Shapes.or(box(5,5,0,11,11,1), box(6,6,1,10,10,2),
			Shapes.join(box(4,4,0,12,12,3), Shapes.or(box(4,7,0,12,9,3), box(7,4,0,9,12,3)), BooleanOp.ONLY_FIRST));
	protected static final VoxelShape WEST_AABB = Shapes.or(box(15,5,5,16,11,11), box(14,6,6,15,10,10),
			Shapes.join(box(13,4,4,16,12,12), Shapes.or(box(13,4,7,16,12,9), box(13,7,4,16,9,12)), BooleanOp.ONLY_FIRST));
	protected static final VoxelShape EAST_AABB = Shapes.or(box(0,5,5,1,11,11), box(1,6,6,2,10,10),
			Shapes.join(box(0,4,4,3,12,12), Shapes.or(box(0,4,7,3,12,9), box(0,7,4,3,9,12)), BooleanOp.ONLY_FIRST));

	public MirrorRelayBlock(Properties properties) {
		super(properties);
	}

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
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
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
		return RegistryManager.MIRROR_RELAY_ENTITY.get().create(pPos, pState);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
		return null;
	}
}