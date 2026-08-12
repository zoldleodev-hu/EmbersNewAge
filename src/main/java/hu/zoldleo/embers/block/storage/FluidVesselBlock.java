package hu.zoldleo.embers.block.storage;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.blockentity.FluidVesselBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public class FluidVesselBlock extends AbstractCauldronBlock implements EntityBlock, SimpleWaterloggedBlock {
    public static final MapCodec<FluidVesselBlock> CODEC = simpleCodec(FluidVesselBlock::new);
    public static final CauldronInteraction.InteractionMap INTERACTION = CauldronInteraction.newInteractionMap("embers_fluid_vessel");

	protected static final VoxelShape VESSEL_AABB = Shapes.or(Block.box(0,0,0,4,16,4),Block.box(12,0,0,16,16,4),Block.box(12,0,12,16,16,16),Block.box(0,0,12,4,16,16),Block.box(4,0,12,12,16,14),Block.box(4,0,2,12,16,4),Block.box(2,0,4,4,16,12),Block.box(12,0,4,14,16,12),Block.box(4,0,4,12,2,12),Block.box(6,6,0,10,10,2),Block.box(6,6,14,10,10,16),Block.box(14,6,6,16,10,10),Block.box(0,6,6,2,10,10));

	public FluidVesselBlock(Properties properties) {
		super(properties, INTERACTION); // TODO;
		this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.WATERLOGGED, false));
	}

	@Override
	public @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!stack.isEmpty()) {
            IFluidHandler cap = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, hit.getDirection());
            if (cap != null && FluidUtil.interactWithFluidHandler(player, hand, cap))
                return ItemInteractionResult.SUCCESS;
            //prevent buckets from placing their fluid in the world when clicking on the vessel
            if (stack.getCapability(Capabilities.FluidHandler.ITEM) != null)
                return ItemInteractionResult.CONSUME_PARTIAL;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
		return RenderShape.MODEL;
	}

	@Override
	public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return VESSEL_AABB;
	}

	@Override
	public @NotNull VoxelShape getInteractionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
		return Shapes.block();
	}

    @Override
    protected @NotNull MapCodec<? extends AbstractCauldronBlock> codec() {
        return CODEC;
    }

    @Override
	public boolean isEntityInsideContent(@NotNull BlockState state, @NotNull BlockPos pos, @NotNull Entity entity) {
		return false;
	}

	@Override
	public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
		return false;
	}

	@Override
	public boolean isFull(@NotNull BlockState state) {
		return false;
	}

	@Override
	public boolean canReceiveStalactiteDrip(@NotNull Fluid fluid) {
		return true;
	}

	@Override
	public void receiveStalactiteDrip(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Fluid fluid) {
        IFluidHandler cap = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, Direction.UP);
        if (cap == null)
            return;

        int amount = 333;
        if (fluid == Fluids.LAVA)
            amount = FluidType.BUCKET_VOLUME;

        cap.fill(new FluidStack(fluid, amount), IFluidHandler.FluidAction.EXECUTE);

        if (fluid.getFluidType().getTemperature() > 500) {
            level.levelEvent(LevelEvent.SOUND_DRIP_LAVA_INTO_CAULDRON, pos, 0);
        } else {
            level.levelEvent(LevelEvent.SOUND_DRIP_WATER_INTO_CAULDRON, pos, 0);
        }
    }

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
		return RegistryManager.FLUID_VESSEL_ENTITY.get().create(pPos, pState);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
		return pLevel.isClientSide ? BaseEntityBlock.createTickerHelper(pBlockEntityType, RegistryManager.FLUID_VESSEL_ENTITY.get(), FluidVesselBlockEntity::clientTick) : null;
	}

	@Override
	public @NotNull List<ItemStack> getDrops(@NotNull BlockState pState, LootParams.@NotNull Builder pBuilder) {
		List<ItemStack> items = super.getDrops(pState, pBuilder);
		BlockEntity blockentity = pBuilder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		if (blockentity instanceof FluidVesselBlockEntity vesselTile)
            for (ItemStack stack : items)
                if (stack.getItem() == RegistryManager.FLUID_VESSEL_ITEM.get()) {
                    IFluidHandler cap = stack.getCapability(Capabilities.FluidHandler.ITEM);
                    if (cap != null)
                        cap.fill(vesselTile.getFluidStack(), IFluidHandler.FluidAction.EXECUTE);
                }
		return items;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(@NotNull BlockPlaceContext pContext) {
		return super.getStateForPlacement(pContext).setValue(BlockStateProperties.WATERLOGGED, pContext.getLevel().getFluidState(pContext.getClickedPos()).is(Fluids.WATER));
	}

	@Override
	public @NotNull BlockState updateShape(BlockState pState, @NotNull Direction pFacing, @NotNull BlockState pFacingState, @NotNull LevelAccessor pLevel, @NotNull BlockPos pCurrentPos, @NotNull BlockPos pFacingPos) {
		if (pState.getValue(BlockStateProperties.WATERLOGGED))
			pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
		return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
		pBuilder.add(BlockStateProperties.WATERLOGGED);
	}

	@Override
	public @NotNull FluidState getFluidState(BlockState pState) {
		return pState.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
	}
}