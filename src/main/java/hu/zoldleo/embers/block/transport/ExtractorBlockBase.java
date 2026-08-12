package hu.zoldleo.embers.block.transport;

import hu.zoldleo.embers.block.transport.ember.EmberEmitterBlock;
import hu.zoldleo.embers.blockentity.PipeBlockEntityBase;
import hu.zoldleo.embers.datagen.EmbersBlockTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public abstract class ExtractorBlockBase extends PipeBlockBase {
	public static final VoxelShape EXTRACTOR_AABB = Block.box(5,5,5,11,11,11);
	public static final VoxelShape[] EXTRACTOR_SHAPES = new VoxelShape[729];

	static {
		makeShapes(EXTRACTOR_AABB, EXTRACTOR_SHAPES);
	}

	public ExtractorBlockBase(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public VoxelShape getCenterShape() {
		return EXTRACTOR_AABB;
	}

	@Override
	public @NotNull VoxelShape getShape(@NotNull BlockState state, BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		BlockEntity BE = level.getBlockEntity(pos);
		if (BE instanceof PipeBlockEntityBase pipe)
			return EXTRACTOR_SHAPES[getShapeIndex(pipe.connections[0], pipe.connections[1], pipe.connections[2], pipe.connections[3], pipe.connections[4], pipe.connections[5])];
		return CENTER_AABB;
	}

	@Override
	public boolean connected(Direction direction, BlockState state) {
		if (!state.is(EmbersBlockTags.EMITTER_CONNECTION))
			return false;
		//always connect to floor or ceiling blocks but only on the top and bottom
		if (state.is(EmbersBlockTags.EMITTER_CONNECTION_FLOOR)) { // TODO: why nested
			if (direction == Direction.DOWN && state.is(EmbersBlockTags.EMITTER_CONNECTION_CEILING))
				return true;
			return direction == Direction.UP;
		}
		//if the block has a side property, use that
		BooleanProperty sideProp = EmberEmitterBlock.ALL_DIRECTIONS[direction.getOpposite().get3DDataValue()];
		if (state.hasProperty(sideProp) && state.getValue(sideProp))
			return true;
		//only support ceiling bells
		if (state.hasProperty(BlockStateProperties.BELL_ATTACHMENT) && state.getValue(BlockStateProperties.BELL_ATTACHMENT) == BellAttachType.CEILING && direction == Direction.DOWN)
			return true;
		//lantern hanging property
		if (state.hasProperty(BlockStateProperties.HANGING)) {
			if (direction == Direction.DOWN && state.getValue(BlockStateProperties.HANGING))
				return true;
            return direction == Direction.UP && !state.getValue(BlockStateProperties.HANGING);
        }
		//connect to blocks on the same axis
		if (state.hasProperty(BlockStateProperties.AXIS)) {
			return state.getValue(BlockStateProperties.AXIS) == direction.getAxis();
		}
		//if there is a face property and it is not wall, only check floor and ceiling
		if (state.hasProperty(BlockStateProperties.ATTACH_FACE) && state.getValue(BlockStateProperties.ATTACH_FACE) != AttachFace.WALL) {
			if (direction == Direction.DOWN && state.getValue(BlockStateProperties.ATTACH_FACE) == AttachFace.CEILING)
				return true;
            return direction == Direction.UP && state.getValue(BlockStateProperties.ATTACH_FACE) == AttachFace.FLOOR;
        }
		//try relevant facing properties, if any are present must be facing this
		return facingConnected(direction, state, BlockStateProperties.HORIZONTAL_FACING)
				&& facingConnected(direction, state, BlockStateProperties.FACING)
				&& facingConnected(direction, state, BlockStateProperties.FACING_HOPPER);
	}

	@Override
	public @NotNull VoxelShape getBlockSupportShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos) {
		return Shapes.block();
	}
}