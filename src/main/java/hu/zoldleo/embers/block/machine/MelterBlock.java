package hu.zoldleo.embers.block.machine;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.blockentity.MelterBottomBlockEntity;
import hu.zoldleo.embers.blockentity.MelterTopBlockEntity;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public class MelterBlock extends DoubleTallMachineBlock {
    public static final MapCodec<MelterBlock> CODEC = simpleCodec(properties ->new MelterBlock(properties, EmbersSounds.MULTIBLOCK_EXTRA));

	protected static final VoxelShape BASE_AABB = Shapes.or(Block.box(2,0,2,14,16,14), Block.box(0,8,0,4,16,4), Block.box(0,8,12,4,16,16), Block.box(12,8,0,16,16,4), Block.box(12,8,12,16,16,16), Block.box(1,0,1,4,8,4), Block.box(1,0,12,4,8,15), Block.box(12,0,1,15,8,4), Block.box(12,0,12,15,8,15));
	protected static final VoxelShape BASE_INTERACTION = Shapes.or(Block.box(1,0,1,15,8,15),Block.box(0,8,0,16,16,16));
	protected static final VoxelShape TOP_AABB = Shapes.or(Block.box(0,0,0,4,16,4),Block.box(6,0,0,10,16,4),Block.box(12,0,0,16,16,4),Block.box(12,0,6,16,16,10),Block.box(12,0,12,16,16,16),Block.box(6,0,12,10,16,16),Block.box(0,0,12,4,16,16),Block.box(0,0,6,4,16,10),Block.box(4,1,2,6,15,4),Block.box(10,1,2,12,15,4),Block.box(12,1,4,14,15,6),Block.box(12,1,10,14,15,12),Block.box(10,1,12,12,15,14),Block.box(4,1,12,6,15,14),Block.box(2,1,10,4,15,12),Block.box(2,1,4,4,15,6),Block.box(3,0,13,13,2,15),Block.box(3,0,1,13,2,3),Block.box(13,0,3,15,2,13),Block.box(1,0,3,3,2,13));

	public MelterBlock(Properties properties, SoundType topSound) {
		super(properties, topSound);
	}

	@Override
	public @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
		if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != DoubleBlockHalf.LOWER && level.getBlockEntity(pos) instanceof MelterTopBlockEntity melterEntity) {
			if (!stack.isEmpty()) {
				IFluidHandler cap = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, hit.getDirection());
				if (cap != null && FluidUtil.interactWithFluidHandler(player, hand, cap))
					return ItemInteractionResult.SUCCESS;
			}
			return Misc.useItemOnInventory(melterEntity.inventory, level, player, hand);
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? BASE_AABB : TOP_AABB;
	}

	@Override
	public @NotNull VoxelShape getInteractionShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
		return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? BASE_INTERACTION : Shapes.block();
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, BlockState pState) {
		if (pState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER)
			return RegistryManager.MELTER_BOTTOM_ENTITY.get().create(pPos, pState);
		return RegistryManager.MELTER_TOP_ENTITY.get().create(pPos, pState);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
		if (pState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER)
			return pLevel.isClientSide ? createTickerHelper(pBlockEntityType, RegistryManager.MELTER_BOTTOM_ENTITY.get(), MelterBottomBlockEntity::clientTick) : createTickerHelper(pBlockEntityType, RegistryManager.MELTER_BOTTOM_ENTITY.get(), MelterBottomBlockEntity::serverTick);
		return pLevel.isClientSide ? createTickerHelper(pBlockEntityType, RegistryManager.MELTER_TOP_ENTITY.get(), MelterTopBlockEntity::clientTick) : createTickerHelper(pBlockEntityType, RegistryManager.MELTER_TOP_ENTITY.get(), MelterTopBlockEntity::serverTick);
	}

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}