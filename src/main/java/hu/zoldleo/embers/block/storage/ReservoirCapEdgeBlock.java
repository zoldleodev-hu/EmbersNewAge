package hu.zoldleo.embers.block.storage;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.block.MechEdgeBlockBase;
import net.minecraft.world.level.block.Block;

public class ReservoirCapEdgeBlock extends MechEdgeBlockBase {
    public ReservoirCapEdgeBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Block getCenterBlock() {
        return RegistryManager.RESERVOIR_CAP.get();
    }
}