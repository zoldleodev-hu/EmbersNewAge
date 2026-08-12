package hu.zoldleo.embers.block.storage;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.block.MechEdgeBlockBase;
import hu.zoldleo.embers.blockentity.CaminiteValveBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public class CaminiteValveEdgeBlock extends MechEdgeBlockBase implements EntityBlock {

	public static final VoxelShape NORTH_AABB = Shapes.or(Block.box(0,0,4,16,16,12), Block.box(1,0,3,7,16,13), Block.box(9,0,3,15,16,13), Block.box(4,4,1,12,12,15), Block.box(6,6,0,10,10,1));
	public static final VoxelShape SOUTH_AABB = Shapes.or(Block.box(0,0,4,16,16,12), Block.box(1,0,3,7,16,13), Block.box(9,0,3,15,16,13), Block.box(4,4,1,12,12,15), Block.box(6,6,15,10,10,16));
	public static final VoxelShape WEST_AABB = Shapes.or(Block.box(4,0,0,12,16,16), Block.box(3,0,1,13,16,7), Block.box(3,0,9,13,16,15), Block.box(1,4,4,15,12,12), Block.box(0,6,6,1,10,10));
	public static final VoxelShape EAST_AABB = Shapes.or(Block.box(4,0,0,12,16,16), Block.box(3,0,1,13,16,7), Block.box(3,0,9,13,16,15), Block.box(1,4,4,15,12,12), Block.box(15,6,6,16,10,10));
	public static final VoxelShape NORTHEAST_AABB = Shapes.or(Block.box(0,0,4,12,16,12), Block.box(4,0,4,12,16,16));
	public static final VoxelShape SOUTHEAST_AABB = Shapes.or(Block.box(0,0,4,12,16,12), Block.box(4,0,0,12,16,12));
	public static final VoxelShape SOUTHWEST_AABB = Shapes.or(Block.box(4,0,4,16,16,12), Block.box(4,0,0,12,16,12));
	public static final VoxelShape NORTHWEST_AABB = Shapes.or(Block.box(4,0,4,16,16,12), Block.box(4,0,4,12,16,16));
	public static final VoxelShape[] SHAPES = new VoxelShape[] { NORTH_AABB, NORTHEAST_AABB, EAST_AABB, SOUTHEAST_AABB, SOUTH_AABB, SOUTHWEST_AABB, WEST_AABB, NORTHWEST_AABB };
	public static final VoxelShape X_INTERACTION = Block.box(0,1,1,16,15,15);
	public static final VoxelShape Z_INTERACTION = Block.box(1,1,0,15,15,16);
	public static final VoxelShape[] INTERACTION_SHAPES = new VoxelShape[] { Z_INTERACTION, Shapes.empty(), X_INTERACTION, Shapes.empty(), Z_INTERACTION, Shapes.empty(), X_INTERACTION, Shapes.empty() };

	public CaminiteValveEdgeBlock(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof CaminiteValveBlockEntity valveEntity) {
			if (!stack.isEmpty() && valveEntity.getReservoir() != null) {
				IFluidHandler cap = valveEntity.getReservoir().getTank();
				if (cap != null && FluidUtil.interactWithFluidHandler(player, hand, cap))
                    return ItemInteractionResult.SUCCESS;
				//prevent buckets from placing their fluid in the world when clicking on the vessel
				if (stack.getCapability(Capabilities.FluidHandler.ITEM) != null)
					return ItemInteractionResult.CONSUME_PARTIAL;
			}
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return SHAPES[state.getValue(EDGE).index];
	}

	@Override
	public @NotNull VoxelShape getInteractionShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
		return INTERACTION_SHAPES[state.getValue(EDGE).index];
	}

	@Override
	public Block getCenterBlock() {
		return RegistryManager.CAMINITE_VALVE.get();
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, BlockState pState) {
		return pState.getValue(EDGE).corner ? null : RegistryManager.CAMINITE_VALVE_ENTITY.get().create(pPos, pState);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
		return pState.getValue(EDGE).corner ? null : BaseEntityBlock.createTickerHelper(pBlockEntityType, RegistryManager.CAMINITE_VALVE_ENTITY.get(), CaminiteValveBlockEntity::commonTick);
	}
}