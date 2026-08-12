package hu.zoldleo.embers.block.storage;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.capabilities.EmbersCapabilities;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.blockentity.CopperCellBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class CopperCellBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<CopperCellBlock> CODEC = simpleCodec(CopperCellBlock::new);

	protected static final VoxelShape CELL_AABB = Shapes.or(Block.box(2,2,2,14,14,14),Block.box(0,0,0,4,6,4),Block.box(12,0,0,16,6,4),Block.box(12,0,12,16,6,16),Block.box(0,0,12,4,6,16),Block.box(0,10,12,4,16,16),Block.box(12,10,12,16,16,16),Block.box(12,10,0,16,16,4),Block.box(0,10,0,4,16,4),Block.box(0,12,4,4,16,6),Block.box(0,12,10,4,16,12),Block.box(0,0,10,4,4,12),Block.box(0,0,4,4,4,6),Block.box(12,0,4,16,4,6),Block.box(12,0,10,16,4,12),Block.box(12,12,10,16,16,12),Block.box(12,12,4,16,16,6),Block.box(10,12,12,12,16,16),Block.box(4,12,12,6,16,16),Block.box(4,0,12,6,4,16),Block.box(10,0,12,12,4,16),Block.box(10,0,0,12,4,4),Block.box(4,0,0,6,4,4),Block.box(4,12,0,6,16,4),Block.box(10,12,0,12,16,4),Block.box(1,6,1,3,10,3),Block.box(13,6,1,15,10,3),Block.box(13,6,13,15,10,15),Block.box(1,6,13,3,10,15),Block.box(1,1,6,3,3,10),Block.box(1,13,6,3,15,10),Block.box(13,13,6,15,15,10),Block.box(13,1,6,15,3,10),Block.box(6,1,13,10,3,15),Block.box(6,13,13,10,15,15),Block.box(6,13,1,10,15,3),Block.box(6,1,1,10,3,3),Block.box(5,0,5,11,16,11));

	public CopperCellBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.WATERLOGGED, false));
	}

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
		return RenderShape.MODEL;
	}

	@Override
	public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return CELL_AABB;
	}

	@Override
	public @NotNull VoxelShape getBlockSupportShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos) {
		return Shapes.block();
	}

	@Override
	public @NotNull VoxelShape getInteractionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
		return Shapes.block();
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
		return RegistryManager.COPPER_CELL_ENTITY.get().create(pPos, pState);
	}

	@Override
	public @NotNull List<ItemStack> getDrops(@NotNull BlockState pState, LootParams.@NotNull Builder pBuilder) {
		List<ItemStack> items = super.getDrops(pState, pBuilder);
		BlockEntity blockentity = pBuilder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		if (blockentity instanceof CopperCellBlockEntity cellTile)
            for (ItemStack stack : items)
                if (stack.getItem() == RegistryManager.COPPER_CELL_ITEM.get()) {
                    IEmberCapability cap = stack.getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM);
                    if (cap != null) {
                        cap.setEmber(cellTile.capability.getEmber());
                        cap.setEmberCapacity(cellTile.capability.getEmberCapacity());
                    }
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