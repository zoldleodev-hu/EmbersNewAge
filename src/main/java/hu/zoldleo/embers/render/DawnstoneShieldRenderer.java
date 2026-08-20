package hu.zoldleo.embers.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hu.zoldleo.embers.model.DawnstoneShieldModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class DawnstoneShieldRenderer extends BlockEntityWithoutLevelRenderer {
    private DawnstoneShieldModel shieldModel;
    private final EntityModelSet entityModelSet;

    public DawnstoneShieldRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {
        super(blockEntityRenderDispatcher, entityModelSet);
        this.entityModelSet = entityModelSet;
    }

    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        this.shieldModel = new DawnstoneShieldModel(entityModelSet.bakeLayer(DawnstoneShieldModel.LAYER_LOCATION));
    }

    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.scale(1.0F, -1.0F, -1.0F);
        Material material = DawnstoneShieldModel.MATERIAL;
        VertexConsumer vertexconsumer = material.sprite().wrap(ItemRenderer.getFoilBufferDirect(buffer, shieldModel.renderType(material.atlasLocation()), true, stack.hasFoil()));
        shieldModel.setup(stack);
        shieldModel.handle().render(poseStack, vertexconsumer, packedLight, packedOverlay);
        shieldModel.plate().render(poseStack, vertexconsumer, packedLight, packedOverlay);
        poseStack.popPose();
    }
}