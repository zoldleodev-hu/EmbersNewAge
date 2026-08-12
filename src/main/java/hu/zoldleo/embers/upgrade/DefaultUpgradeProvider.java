package hu.zoldleo.embers.upgrade;

import hu.zoldleo.embers.api.upgrades.IUpgradeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DefaultUpgradeProvider implements IUpgradeProvider {
    protected final ResourceLocation id;
    protected final BlockEntity tile;

    public DefaultUpgradeProvider(ResourceLocation id, BlockEntity tile) {
        this.id = id;
        this.tile = tile;
    }

    @Override
    public ResourceLocation getUpgradeId() {
        return id;
    }
}