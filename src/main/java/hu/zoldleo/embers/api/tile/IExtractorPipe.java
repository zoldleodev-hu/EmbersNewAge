package hu.zoldleo.embers.api.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public interface IExtractorPipe {
    void addConnection(BlockPos pos, Direction side, int priority);
    void clearConnections();
    boolean active();
    default boolean acceptsPipes() { return false; }
}