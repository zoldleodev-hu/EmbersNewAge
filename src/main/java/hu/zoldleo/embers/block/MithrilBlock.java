package hu.zoldleo.embers.block;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.RegistryManager;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MithrilBlock extends BaseEntityBlock {
    public static final MapCodec<MithrilBlock> CODEC = simpleCodec(MithrilBlock::new);

	public MithrilBlock(Properties properties) {
		super(properties);
	}

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
		return RegistryManager.MITHRIL_BLOCK_ENTITY.get().create(pPos, pState);
	}
}