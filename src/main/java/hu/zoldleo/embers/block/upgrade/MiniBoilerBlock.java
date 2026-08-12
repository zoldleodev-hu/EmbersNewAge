package hu.zoldleo.embers.block.upgrade;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.block.IPipeConnection;
import hu.zoldleo.embers.blockentity.MiniBoilerBlockEntity;
import hu.zoldleo.embers.blockentity.PipeBlockEntityBase;
import hu.zoldleo.embers.blockentity.PipeBlockEntityBase.PipeConnection;
import hu.zoldleo.embers.datagen.EmbersBlockTags;
import hu.zoldleo.embers.util.Misc;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class MiniBoilerBlock extends BaseEntityBlock implements SimpleWaterloggedBlock, IPipeConnection {
    public static final MapCodec<MiniBoilerBlock> CODEC = simpleCodec(MiniBoilerBlock::new);

	protected static final VoxelShape NORTH_AABB = Shapes.or(Block.box(2,2,2,14,14,14),Block.box(3,0,3,13,16,13),Block.box(4,4,-2,12,12,2));
	protected static final VoxelShape SOUTH_AABB = Misc.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, NORTH_AABB);
	protected static final VoxelShape WEST_AABB = Misc.rotateVoxelShape(Direction.NORTH, Direction.WEST, NORTH_AABB);
	protected static final VoxelShape EAST_AABB = Misc.rotateVoxelShape(Direction.NORTH, Direction.EAST, NORTH_AABB);

	public MiniBoilerBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.WATERLOGGED, false).setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
	}

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
	public @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof MiniBoilerBlockEntity boilerEntity) {
			if (!stack.isEmpty()) {
				IFluidHandler cap = Misc.makeRestrictedFluidHandler(boilerEntity.getGasTank(), false, true);
                boolean didFill = FluidUtil.interactWithFluidHandler(player, hand, cap);

                if (didFill)
                    return ItemInteractionResult.SUCCESS;
                //prevent buckets from placing their fluid in the world when clicking on the boiler
				if (stack.getCapability(Capabilities.FluidHandler.ITEM) != null)
					return ItemInteractionResult.CONSUME_PARTIAL;
			}
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
		return RenderShape.MODEL;
	}

	@Override
	public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case EAST -> EAST_AABB;
            case WEST -> WEST_AABB;
            case SOUTH -> SOUTH_AABB;
            default -> NORTH_AABB;
        };
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
		return RegistryManager.MINI_BOILER_ENTITY.get().create(pPos, pState);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
		return pLevel.isClientSide ? createTickerHelper(pBlockEntityType, RegistryManager.MINI_BOILER_ENTITY.get(), MiniBoilerBlockEntity::clientTick) : createTickerHelper(pBlockEntityType, RegistryManager.MINI_BOILER_ENTITY.get(), MiniBoilerBlockEntity::serverTick);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		Direction direction = pContext.getClickedFace().getAxis() != Axis.Y ?
                pContext.getClickedFace().getOpposite() : pContext.getHorizontalDirection();
		return super.getStateForPlacement(pContext).setValue(BlockStateProperties.HORIZONTAL_FACING, direction).setValue(BlockStateProperties.WATERLOGGED, pContext.getLevel().getFluidState(pContext.getClickedPos()).is(Fluids.WATER));
	}

	@Override
	public @NotNull BlockState updateShape(BlockState pState, @NotNull Direction pFacing, @NotNull BlockState pFacingState, @NotNull LevelAccessor pLevel, @NotNull BlockPos pCurrentPos, @NotNull BlockPos pFacingPos) {
		if (pState.getValue(BlockStateProperties.WATERLOGGED))
			pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));

		if (pFacing.getAxis() != Axis.Y && pLevel.getBlockEntity(pCurrentPos) instanceof PipeBlockEntityBase pipe) {
			BlockEntity facingBE = pLevel.getBlockEntity(pFacingPos);
			if (pFacingState.is(EmbersBlockTags.FLUID_PIPE_CONNECTION)) {
				if (facingBE instanceof PipeBlockEntityBase base && base.getConnection(pFacing.getOpposite()) == PipeConnection.DISABLED) {
					pipe.setConnection(pFacing, PipeConnection.NONE);
				} else {
					pipe.setConnection(pFacing, PipeConnection.PIPE);
				}
			} else {
				pipe.setConnection(pFacing, PipeConnection.NONE);
			}
		}
		return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
		pBuilder.add(BlockStateProperties.WATERLOGGED, BlockStateProperties.HORIZONTAL_FACING);
	}

	@Override
	public @NotNull FluidState getFluidState(BlockState pState) {
		return pState.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
	}

	@Override
	public PipeConnection getPipeConnection(BlockState state, Direction direction) {
		return direction.getAxis() == Axis.Y ? PipeConnection.END : PipeConnection.PIPE;
	}

	@Override
	public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
	}

	@Override
	public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
		return state.setValue(BlockStateProperties.HORIZONTAL_FACING, mirror.mirror(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
	}
}