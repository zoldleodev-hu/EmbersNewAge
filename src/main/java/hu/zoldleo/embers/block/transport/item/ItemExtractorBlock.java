package hu.zoldleo.embers.block.transport.item;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.block.transport.ExtractorBlockBase;
import hu.zoldleo.embers.blockentity.ItemExtractorBlockEntity;
import hu.zoldleo.embers.blockentity.ItemPipeBlockEntityBase;
import hu.zoldleo.embers.datagen.EmbersBlockTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.NotNull;

public class ItemExtractorBlock extends ExtractorBlockBase {
    public static final MapCodec<ItemExtractorBlock> CODEC = simpleCodec(ItemExtractorBlock::new);

	public ItemExtractorBlock(Properties pProperties) {
		super(pProperties);
	}

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
		return RegistryManager.ITEM_EXTRACTOR_ENTITY.get().create(pPos, pState);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
		return pLevel.isClientSide ? null : createTickerHelper(pBlockEntityType, RegistryManager.ITEM_EXTRACTOR_ENTITY.get(), ItemExtractorBlockEntity::serverTick);
	}

	@Override
	public TagKey<Block> getConnectionTag() {
		return EmbersBlockTags.ITEM_PIPE_CONNECTION;
	}

	@Override
	public TagKey<Block> getToggleConnectionTag() {
		return EmbersBlockTags.ITEM_PIPE_CONNECTION_TOGGLEABLE;
	}

	@Override
	public boolean connectToBlock(Level level, BlockPos pos, Direction direction) {
		return level.getBlockEntity(pos) instanceof ItemPipeBlockEntityBase || level.getCapability(Capabilities.ItemHandler.BLOCK, pos, direction.getOpposite()) != null;
	}
}