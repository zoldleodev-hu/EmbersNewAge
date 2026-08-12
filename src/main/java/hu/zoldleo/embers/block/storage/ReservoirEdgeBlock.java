package hu.zoldleo.embers.block.storage;

import hu.zoldleo.embers.RegistryManager;

import hu.zoldleo.embers.block.MechEdgeBlockBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class ReservoirEdgeBlock extends MechEdgeBlockBase {
	public ReservoirEdgeBlock(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public Block getCenterBlock() {
		return RegistryManager.RESERVOIR.get();
	}

	@Override
	public @NotNull VoxelShape getInteractionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
		return Shapes.block();
	}
}