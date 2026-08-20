package hu.zoldleo.embers.entity.render;

import hu.zoldleo.embers.entity.EmberWispEntity;
import hu.zoldleo.embers.model.EmberWispModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class EmberWispRenderer extends MobRenderer<EmberWispEntity, EmberWispModel> {
    public EmberWispRenderer(EntityRendererProvider.Context context) {
        super(context, new EmberWispModel(), 0);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull EmberWispEntity emberWispEntity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}