package hu.zoldleo.embers.api.tile;

import hu.zoldleo.embers.api.filter.IFilter;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface IOrderDestination {
    void order(BlockEntity source, IFilter filter, int orderSize);

    void resetOrder(BlockEntity source);
}