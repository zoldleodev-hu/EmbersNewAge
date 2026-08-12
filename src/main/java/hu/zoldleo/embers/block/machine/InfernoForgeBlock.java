package hu.zoldleo.embers.block.machine;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.block.MechEdgeBlockBase;
import hu.zoldleo.embers.block.MechEdgeBlockBase.MechEdge;
import hu.zoldleo.embers.blockentity.InfernoForgeBottomBlockEntity;
import hu.zoldleo.embers.blockentity.InfernoForgeTopBlockEntity;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class InfernoForgeBlock extends DoubleTallMachineBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<InfernoForgeBlock> CODEC = simpleCodec(properties -> new InfernoForgeBlock(properties, EmbersSounds.MULTIBLOCK_EXTRA));

	public static final VoxelShape BOTTOM_AABB = Shapes.or(Block.box(0,0,0,16,6,16), Block.box(12,6,0,16,16,4), Block.box(0,6,0,4,16,4), Block.box(12,6,12,16,16,16), Block.box(0,6,12,4,16,16));
	public static final VoxelShape TOP_AABB = Block.box(0,10,0,16,12,16);

	public InfernoForgeBlock(Properties pProperties, SoundType topSound) {
		super(pProperties, topSound);
		this.registerDefaultState(this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false).setValue(BlockStateProperties.HORIZONTAL_AXIS, Axis.Z));
	}

	@Override
	public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER)
			return BOTTOM_AABB;
		return TOP_AABB;
	}

	@Override
	public @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
		if (!player.isSecondaryUseActive() && level.getBlockEntity(pos) instanceof InfernoForgeTopBlockEntity hatch && level.getBlockEntity(pos.below()) instanceof InfernoForgeBottomBlockEntity forge) {
			if (forge.progress == 0) {
				if (hatch.open && forge.capability.getEmber() <= 0) { //Syke bitch, not enough ember
					if (level.isClientSide())
						player.sendSystemMessage(Component.translatable(Embers.MODID + ".tooltip.forge.cannot_start"));
					return ItemInteractionResult.CONSUME;
				}
				if (!level.isClientSide()) {
					hatch.open = !hatch.open;
					hatch.lastToggle = level.getGameTime();
					if (hatch.open)
						level.playSound(null, pos, EmbersSounds.INFERNO_FORGE_OPEN.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
					else
						level.playSound(null, pos, EmbersSounds.INFERNO_FORGE_CLOSE.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
					hatch.setChanged();
				}
				return ItemInteractionResult.SUCCESS;
			}
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public @NotNull VoxelShape getCollisionShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
			if (level.getBlockEntity(pos) instanceof InfernoForgeTopBlockEntity hatch && hatch.open)
				return Shapes.empty();
			return TOP_AABB;
		}
		return super.getCollisionShape(state, level, pos, context);
	}

	@Override
	public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			for (MechEdge edge : MechEdge.values()) {
				BlockPos cornerPos = pos.subtract(edge.centerPos);
				if (level.getBlockState(cornerPos).getBlock() instanceof MechEdgeBlockBase)
					level.destroyBlock(cornerPos, false);
			}
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
            if (handler != null) {
                Misc.spawnInventoryInWorld(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, handler);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, isMoving);
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		BlockState state = super.getStateForPlacement(pContext);
		if (state == null)
			return null;
		boolean lower = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER;
		for (MechEdge edge : MechEdge.values()) {
			if (!pContext.getLevel().getBlockState(pContext.getClickedPos().subtract(edge.centerPos)).canBeReplaced(pContext))
				return null;
			if (lower && !pContext.getLevel().getBlockState(pContext.getClickedPos().subtract(edge.centerPos.below())).canBeReplaced(pContext))
				return null;
		}
		return state.setValue(BlockStateProperties.HORIZONTAL_AXIS, pContext.getHorizontalDirection().getAxis()).setValue(BlockStateProperties.WATERLOGGED, pContext.getLevel().getFluidState(pContext.getClickedPos()).is(Fluids.WATER));
	}

	@Override
	public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, BlockState state, LivingEntity placer, @NotNull ItemStack stack) {
		if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
			for (MechEdge edge : MechEdge.values()) {
				BlockPos edgePos = pos.subtract(edge.centerPos);
				BlockState edgeState = RegistryManager.INFERNO_FORGE_EDGE.get().defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, level.getFluidState(edgePos).is(Fluids.WATER));
				level.setBlock(edgePos, edgeState.setValue(MechEdgeBlockBase.EDGE, edge).setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER), UPDATE_ALL);
				BlockPos edgePosUpper = pos.above().subtract(edge.centerPos);
				BlockState edgeStateUpper = RegistryManager.INFERNO_FORGE_EDGE.get().defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, level.getFluidState(edgePosUpper).is(Fluids.WATER));
				level.setBlock(edgePosUpper, edgeStateUpper.setValue(MechEdgeBlockBase.EDGE, edge).setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), UPDATE_ALL);
			}
			BlockState topState = this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_AXIS, state.getValue(BlockStateProperties.HORIZONTAL_AXIS)).setValue(BlockStateProperties.WATERLOGGED, level.getFluidState(pos.above()).is(Fluids.WATER));
			level.setBlock(pos.above(), topState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), UPDATE_ALL);
		}
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, BlockState pState) {
		if (pState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER)
			return RegistryManager.INFERNO_FORGE_BOTTOM_ENTITY.get().create(pPos, pState);
		return RegistryManager.INFERNO_FORGE_TOP_ENTITY.get().create(pPos, pState);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
		if (pState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER)
			return pLevel.isClientSide ? createTickerHelper(pBlockEntityType, RegistryManager.INFERNO_FORGE_BOTTOM_ENTITY.get(), InfernoForgeBottomBlockEntity::clientTick) : createTickerHelper(pBlockEntityType, RegistryManager.INFERNO_FORGE_BOTTOM_ENTITY.get(), InfernoForgeBottomBlockEntity::serverTick);
		return null;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder);
		pBuilder.add(BlockStateProperties.HORIZONTAL_AXIS);
	}

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}