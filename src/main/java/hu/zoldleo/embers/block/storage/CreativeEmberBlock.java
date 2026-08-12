package hu.zoldleo.embers.block.storage;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.RegistryManager;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class CreativeEmberBlock extends BaseEntityBlock {
    public static final MapCodec<CreativeEmberBlock> CODEC = simpleCodec(CreativeEmberBlock::new);

	public CreativeEmberBlock(Properties properties) {
		super(properties);
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
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
		return RegistryManager.CREATIVE_EMBER_ENTITY.get().create(pPos, pState);
	}
}