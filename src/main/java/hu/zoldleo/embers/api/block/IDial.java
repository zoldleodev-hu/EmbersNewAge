package hu.zoldleo.embers.api.block;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IDial {
    List<Component> getDisplayInfo(Level world, BlockPos pos, BlockState state, int maxLines);

    void updateBEData(BlockPos pos, int maxLines);

    String getDialType();
}