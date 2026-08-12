package hu.zoldleo.embers.blockentity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import hu.zoldleo.embers.blockentity.AlchemyTabletBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class AlchemyTabletBlockEntityRenderer implements BlockEntityRenderer<AlchemyTabletBlockEntity> {
	private final ItemRenderer itemRenderer;

	public AlchemyTabletBlockEntityRenderer(BlockEntityRendererProvider.Context pContext) {
		this.itemRenderer = pContext.getItemRenderer();
	}

	@Override
	public void render(@NotNull AlchemyTabletBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.inventory.getStackInSlot(0).isEmpty()) {
            poseStack.pushPose();
            ItemStack stack = blockEntity.inventory.getStackInSlot(0);
            int seed = stack.isEmpty() ? 187 : Item.getId(stack.getItem()) + stack.getDamageValue();
            BakedModel bakedmodel = this.itemRenderer.getModel(stack, blockEntity.getLevel(), null, seed);
            float f2 = bakedmodel.getTransforms().getTransform(ItemDisplayContext.GROUND).scale.y(); // TODO: deprecation
            poseStack.translate(0.5D, (double)(0.25F * f2) + 0.8D, 0.5D);
            this.itemRenderer.render(stack, ItemDisplayContext.GROUND, false, poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, bakedmodel);
            poseStack.popPose();
        }
    }

	@Override
	public @NotNull AABB getRenderBoundingBox(@NotNull AlchemyTabletBlockEntity tile) {
		return new AABB(Vec3.atLowerCornerOf(tile.getBlockPos()), Vec3.atLowerCornerWithOffset(tile.getBlockPos(), 1, 2, 1));
	}
}