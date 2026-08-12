package hu.zoldleo.embers.blockentity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import hu.zoldleo.embers.EmbersClientEvents;
import hu.zoldleo.embers.blockentity.ItemTransferBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemTransferBlockEntityRenderer implements BlockEntityRenderer<ItemTransferBlockEntity> {
	private final ItemRenderer itemRenderer;

	public ItemTransferBlockEntityRenderer(BlockEntityRendererProvider.Context pContext) {
		this.itemRenderer = pContext.getItemRenderer();
	}

	@Override
	public void render(@NotNull ItemTransferBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.filterItem.isEmpty()) {
            poseStack.pushPose();
            ItemStack stack = blockEntity.filterItem;
            int seed = stack.isEmpty() ? 187 : Item.getId(stack.getItem()) + stack.getDamageValue();
            BakedModel bakedmodel = this.itemRenderer.getModel(stack, blockEntity.getLevel(), null, seed);
            float f2 = bakedmodel.getTransforms().getTransform(ItemDisplayContext.GROUND).scale.y(); // TODO: deprecation
            poseStack.translate(0.5D, 0.4F * f2 + 0.21D, 0.5D);
            if (f2 > 0.4f)
                poseStack.scale(0.75f, 0.75f, 0.75f);
            poseStack.mulPose(Axis.YP.rotation(((float) EmbersClientEvents.ticks + partialTick) / 20.0F));
            this.itemRenderer.render(stack, ItemDisplayContext.GROUND, false, poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, bakedmodel);
            poseStack.popPose();
        }
    }
}