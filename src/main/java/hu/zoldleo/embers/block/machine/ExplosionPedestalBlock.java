package hu.zoldleo.embers.block.machine;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.compat.curios.CuriosCompat;

import hu.zoldleo.embers.datagen.EmbersSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class ExplosionPedestalBlock extends AlchemyPedestalBlock {
    public static final MapCodec<ExplosionPedestalBlock> CODEC = simpleCodec(properties -> new ExplosionPedestalBlock(properties, EmbersSounds.MULTIBLOCK_EXTRA));

	protected static final VoxelShape TOP_AABB = Shapes.joinUnoptimized(Shapes.or(Block.box(1,0,1,6,4,6), Block.box(10,0,10,15,4,15), Block.box(10,0,1,15,4,6), Block.box(1,0,10,6,4,15),
			Block.box(3,0,3,13,4,13), Block.box(4,4,4,12,6,12),
			Block.box(7,8,4,9,10,12), Block.box(4,8,7,12,10,9), Block.box(4,7.5,4,7,10.5,7), Block.box(9,7.5,4,12,10.5,7), Block.box(9,7.5,9,12,10.5,12), Block.box(4,7.5,9,7,10.5,12), Block.box(2,7.5,6,4,10.5,10), Block.box(6,7.5,2,10,10.5,4), Block.box(12,7.5,6,14,10.5,10), Block.box(6,7.5,12,10,10.5,14)),
			Block.box(6,4,6,10,7,10), BooleanOp.ONLY_FIRST);

	public ExplosionPedestalBlock(Properties properties, SoundType topSound) {
		super(properties, topSound);
	}

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

	@Override
	public @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? BASE_AABB : TOP_AABB;
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, BlockState pState) {
		if (pState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER)
			return null;
		return CuriosCompat.EXPLOSION_PEDESTAL_ENTITY.get().create(pPos, pState);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
		return null;
	}
}