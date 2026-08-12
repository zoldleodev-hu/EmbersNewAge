package hu.zoldleo.embers.util;

import java.util.Iterator;

import com.google.common.collect.HashMultimap;
import hu.zoldleo.embers.blockentity.ExplosionPedestalBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ExplosionCharmWorldInfo { //basicly a k-d tree
	HashMultimap<ChunkPos, BlockPos> data = HashMultimap.create();

	public ExplosionCharmWorldInfo() {}

	public void put(BlockPos pos) {
		ChunkPos chunkPos = new ChunkPos(pos);
		data.put(chunkPos, pos);
	}

	public BlockPos getClosestExplosionCharm(Level world, BlockPos pos, int radius) {
		BlockPos chosen = null;
		double minDistance = Double.POSITIVE_INFINITY;
		ChunkPos chunkPosA = new ChunkPos(pos.offset(-radius, 0, -radius));
		ChunkPos chunkPosB = new ChunkPos(pos.offset(radius, 0, radius));
		for (int x = chunkPosA.x; x <= chunkPosB.x; x++)
			for (int z = chunkPosA.z; z <= chunkPosB.z; z++) {
				ChunkPos chunkPos = new ChunkPos(x, z);
				Iterator<BlockPos> iterator = data.get(chunkPos).iterator();
				while (iterator.hasNext()) {
					BlockPos testpos = iterator.next();
					double testdist = testpos.distToCenterSqr(pos.getX(), pos.getY(), pos.getZ());
					if (testdist >= minDistance || testdist > radius * radius)
						continue;
					BlockEntity tile = world.getBlockEntity(testpos);
					if (tile instanceof ExplosionPedestalBlockEntity && !tile.isRemoved()) {
						chosen = testpos;
						minDistance = testdist;
					} else {
						iterator.remove();
					}
				}
			}
		return chosen;
	}
}
