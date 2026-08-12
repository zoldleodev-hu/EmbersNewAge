package hu.zoldleo.embers.block.transport.item;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.block.IPipeConnection;
import hu.zoldleo.embers.blockentity.ItemTransferBlockEntity;
import hu.zoldleo.embers.blockentity.PipeBlockEntityBase.PipeConnection;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class ItemTransferBlock extends BaseEntityBlock implements SimpleWaterloggedBlock, IPipeConnection {
    public static final MapCodec<ItemTransferBlock> CODEC = simpleCodec(ItemTransferBlock::new);

	protected static final VoxelShape UP_AABB = Shapes.or(Block.box(0,0,0,4,16,4), Block.box(0,0,12,4,16,16), Block.box(12,0,0,16,16,4), Block.box(12,0,12,16,16,16),
			box(4,0,4,12,4,12), box(2,4,2,14,12,14), box(0,12,0,16,16,16));
	protected static final VoxelShape DOWN_AABB = Shapes.or(Block.box(0,0,0,4,16,4), Block.box(0,0,12,4,16,16), Block.box(12,0,0,16,16,4), Block.box(12,0,12,16,16,16),
			box(4,4,4,12,16,12), box(2,4,2,14,12,14), box(0,0,0,16,4,16));
	protected static final VoxelShape NORTH_AABB = Shapes.or(Block.box(0,0,0,4,4,16), Block.box(0,12,0,4,16,16), Block.box(12,0,0,16,4,16), Block.box(12,12,0,16,16,16),
			box(4,4,4,12,12,16), box(2,2,4,14,14,12), box(0,0,0,16,16,4));
	protected static final VoxelShape SOUTH_AABB = Shapes.or(Block.box(0,0,0,4,4,16), Block.box(0,12,0,4,16,16), Block.box(12,0,0,16,4,16), Block.box(12,12,0,16,16,16),
			box(4,4,0,12,12,4), box(2,2,4,14,14,12), box(0,0,12,16,16,16));
	protected static final VoxelShape WEST_AABB = Shapes.or(Block.box(0,0,0,16,4,4), Block.box(0,0,12,16,4,16), Block.box(0,12,0,16,16,4), Block.box(0,12,12,16,16,16),
			box(4,4,4,16,12,12), box(4,2,2,12,14,14), box(0,0,0,4,16,16));
	protected static final VoxelShape EAST_AABB = Shapes.or(Block.box(0,0,0,16,4,4), Block.box(0,0,12,16,4,16), Block.box(0,12,0,16,16,4), Block.box(0,12,12,16,16,16),
			box(0,4,4,4,12,12), box(4,2,2,12,14,14), box(12,0,0,16,16,16));

	public static final BooleanProperty FILTER = BooleanProperty.create("filter");

	public ItemTransferBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.FACING, Direction.UP).setValue(FILTER, false).setValue(BlockStateProperties.WATERLOGGED, false));
	}

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
	public @NotNull VoxelShape getShape(BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return switch (pState.getValue(BlockStateProperties.FACING)) {
            case UP -> UP_AABB;
            case DOWN -> DOWN_AABB;
            case EAST -> EAST_AABB;
            case WEST -> WEST_AABB;
            case SOUTH -> SOUTH_AABB;
            default -> NORTH_AABB;
        };
	}

	@Override
	public @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof ItemTransferBlockEntity transfer) {
			ItemStack heldItem = player.getItemInHand(hand);
			if (!heldItem.isEmpty()) {
				transfer.filterItem = heldItem.copy();
				level.setBlock(pos, state.setValue(FILTER, true), 10);
			} else {
				transfer.filterItem = ItemStack.EMPTY;
				level.setBlock(pos, state.setValue(FILTER, false), 10);
			}
			transfer.setupFilter();

			transfer.syncFilter = true;
			transfer.setChanged();
			return ItemInteractionResult.SUCCESS;
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public void onRemove(BlockState pState, @NotNull Level level, @NotNull BlockPos pos, BlockState pNewState, boolean pIsMoving) {
		if (!pState.is(pNewState.getBlock())) {
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
            if (handler != null) {
                Misc.spawnInventoryInWorld(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, handler);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(pState, level, pos, pNewState, pIsMoving);
		}
	}

	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction facing = context.getPlayer().isSecondaryUseActive() ? context.getNearestLookingDirection().getOpposite() : context.getNearestLookingDirection();
		return this.defaultBlockState().setValue(BlockStateProperties.FACING, facing).setValue(BlockStateProperties.WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
	}

	@Override
	public @NotNull BlockState updateShape(BlockState pState, @NotNull Direction pFacing, @NotNull BlockState pFacingState, @NotNull LevelAccessor pLevel, @NotNull BlockPos pCurrentPos, @NotNull BlockPos pFacingPos) {
		if (pState.getValue(BlockStateProperties.WATERLOGGED))
			pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
		return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
		pBuilder.add(BlockStateProperties.FACING).add(FILTER).add(BlockStateProperties.WATERLOGGED);
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
		return RegistryManager.ITEM_TRANSFER_ENTITY.get().create(pPos, pState);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
		return pLevel.isClientSide ? null : createTickerHelper(pBlockEntityType, RegistryManager.ITEM_TRANSFER_ENTITY.get(), ItemTransferBlockEntity::serverTick);
	}

	@Override
	public @NotNull FluidState getFluidState(BlockState pState) {
		return pState.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
	}

	@Override
	public PipeConnection getPipeConnection(BlockState state, Direction direction) {
		return PipeConnection.PIPE;
	}

	@Override
	public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(BlockStateProperties.FACING, rotation.rotate(state.getValue(BlockStateProperties.FACING)));
	}

	@Override
	public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
		return state.setValue(BlockStateProperties.FACING, mirror.mirror(state.getValue(BlockStateProperties.FACING)));
	}
}