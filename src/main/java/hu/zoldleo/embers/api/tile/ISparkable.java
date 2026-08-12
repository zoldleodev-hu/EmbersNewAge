package hu.zoldleo.embers.api.tile;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface ISparkable {
	void sparkProgress(BlockEntity tile, double ember);
}