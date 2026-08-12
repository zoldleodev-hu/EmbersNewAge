package hu.zoldleo.embers.block.machine;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.blockentity.AlchemyPedestalBlockEntity;
import hu.zoldleo.embers.blockentity.AlchemyPedestalTopBlockEntity;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.util.Misc;

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

public class AlchemyPedestalBlock extends DoubleTallMachineBlock {
    public static final MapCodec<AlchemyPedestalBlock> CODEC = simpleCodec(properties -> new AlchemyPedestalBlock(properties, EmbersSounds.MULTIBLOCK_EXTRA));
	protected static final VoxelShape BASE_AABB = Shapes.or(Block.box(1,0,1,6,4,6), Block.box(10,0,10,15,4,15), Block.box(10,0,1,15,4,6), Block.box(1,0,10,6,4,15), Block.box(3,0,3,13,16,13));
	protected static final VoxelShape TOP_AABB = Shapes.join(Shapes.or(Block.box(1,0,1,6,4,6), Block.box(10,0,10,15,4,15), Block.box(10,0,1,15,4,6), Block.box(1,0,10,6,4,15),
			Block.box(3,0,3,13,4,13), Block.box(4,4,4,12,6,12), Block.box(2,6,2,14,8,14)),
			Block.box(6,4,6,10,16,10), BooleanOp.ONLY_FIRST);
	protected static final VoxelShape TOP_INTERACTION = Block.box(1,0,1,15,8,15);

	public AlchemyPedestalBlock(Properties properties, SoundType topSound) {
		super(properties, topSound);
	}

	@Override
	public @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof AlchemyPedestalBlockEntity pedestalEntity)
			return Misc.useItemOnInventory(pedestalEntity.inventory, level, player, hand);
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? BASE_AABB : TOP_AABB;
	}

	@Override
	public @NotNull VoxelShape getInteractionShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
		return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? Shapes.empty() : TOP_INTERACTION;
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, BlockState pState) {
		if (pState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER)
			return RegistryManager.ALCHEMY_PEDESTAL_ENTITY.get().create(pPos, pState);
		return RegistryManager.ALCHEMY_PEDESTAL_TOP_ENTITY.get().create(pPos, pState);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
		return pLevel.isClientSide && pState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER ? createTickerHelper(pBlockEntityType, RegistryManager.ALCHEMY_PEDESTAL_TOP_ENTITY.get(), AlchemyPedestalTopBlockEntity::clientTick) : null;
	}

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}