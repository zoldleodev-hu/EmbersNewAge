package hu.zoldleo.embers.block.machine;

import javax.annotation.Nullable;

import hu.zoldleo.embers.datagen.EmbersBlockTags;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public abstract class ChamberBlockBase extends BaseEntityBlock implements SimpleWaterloggedBlock {
	public SoundType topSound;
	public static final EnumProperty<ChamberConnection> CONNECTION = EnumProperty.create("connection", ChamberConnection.class);

	protected static final VoxelShape TOP_AABB = Shapes.or(Block.box(3,0,3,13,4,13), Block.box(4,4,4,12,6,12), Block.box(5,6,5,11,11,11));

	public ChamberBlockBase(Properties properties, SoundType topSound) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(CONNECTION, ChamberConnection.BOTTOM).setValue(BlockStateProperties.WATERLOGGED, false));
		this.topSound = topSound;
	}

	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
		return RenderShape.MODEL;
	}

	@Override
	public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			if (state.getValue(CONNECTION) == ChamberConnection.BOTTOM) {
				BlockState above = level.getBlockState(pos.above());
				if (above.getBlock() == this && above.getValue(CONNECTION) != ChamberConnection.BOTTOM)
					level.destroyBlock(pos.above(), false);
			} else {
				BlockState below = level.getBlockState(pos.below());
				if (below.getBlock() == this && below.getValue(CONNECTION) == ChamberConnection.BOTTOM)
					level.destroyBlock(pos.below(), false);
			}
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
            if (handler != null) {
                Misc.spawnInventoryInWorld(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, handler);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, isMoving);
		}
	}

	/*/@Override
	public @NotNull SoundType getSoundType(BlockState state) {
		return state.getValue(CONNECTION) == ChamberConnection.BOTTOM ? super.getSoundType(state) : topSound;
	}*/

    @Override
    public @NotNull SoundType getSoundType(BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos, Entity entity) {
        return state.getValue(CONNECTION) == ChamberConnection.BOTTOM ? super.getSoundType(state, level, pos, entity) : topSound;
    }

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		if (context.getLevel().getBlockState(context.getClickedPos().above()).canBeReplaced(context))
			return super.getStateForPlacement(context).setValue(BlockStateProperties.WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
		return null;
	}

	@Override
	public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, BlockState state, LivingEntity placer, @NotNull ItemStack stack) {
		if (state.getValue(CONNECTION) == ChamberConnection.BOTTOM) {
			BlockState topState = this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, level.getFluidState(pos.above()).is(Fluids.WATER));
			ChamberConnection connection = ChamberConnection.TOP;
			for (Direction direction : Misc.horizontals) {
				if (level.getBlockState(pos.above().relative(direction)).is(EmbersBlockTags.CHAMBER_CONNECTION)) {
					connection = ChamberConnection.getConnection(direction);
					break;
				}
			}
			level.setBlock(pos.above(), topState.setValue(CONNECTION, connection), UPDATE_ALL);
		}
	}

	@Override
	public @NotNull BlockState updateShape(BlockState state, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull LevelAccessor level, @NotNull BlockPos currentPos, @NotNull BlockPos facingPos) {
		if (state.getValue(BlockStateProperties.WATERLOGGED)) {
			level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		}
		if (state.getValue(CONNECTION) != ChamberConnection.BOTTOM && facing.getAxis() != Direction.Axis.Y) {
			if (state.getValue(CONNECTION).direction == facing && !facingState.is(EmbersBlockTags.CHAMBER_CONNECTION))
				return state.setValue(CONNECTION, ChamberConnection.TOP);
			if (facingState.is(EmbersBlockTags.CHAMBER_CONNECTION))
				return state.setValue(CONNECTION, ChamberConnection.getConnection(facing));
		}
		return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
		pBuilder.add(BlockStateProperties.WATERLOGGED).add(CONNECTION);
	}

	@Override
	public @NotNull FluidState getFluidState(BlockState pState) {
		return pState.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
	}

	public enum ChamberConnection implements StringRepresentable {
		BOTTOM("bottom", Direction.DOWN),
		TOP("top_none", Direction.UP),
		NORTH("top_north", Direction.NORTH),
		SOUTH("top_south", Direction.SOUTH),
		WEST("top_west", Direction.WEST),
		EAST("top_east", Direction.EAST);

		private final String name;
		public final Direction direction;

		ChamberConnection(String name, Direction direction) {
			this.name = name;
			this.direction = direction;
		}

		public static ChamberConnection getConnection(Direction direction) {
			return ChamberConnection.values()[direction.get3DDataValue()];
		}

		public String toString() {
			return this.name;
		}

		public @NotNull String getSerializedName() {
			return this.name;
		}
	}
}