package hu.zoldleo.embers.blockentity.render;

import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import hu.zoldleo.embers.blockentity.StampBaseBlockEntity;
import hu.zoldleo.embers.render.FluidCuboid;
import hu.zoldleo.embers.render.FluidRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class StampBaseBlockEntityRenderer implements BlockEntityRenderer<StampBaseBlockEntity> {

	private final ItemRenderer itemRenderer;

	FluidCuboid cube = new FluidCuboid(new Vector3f(4, 12, 4), new Vector3f(12, 15, 12), FluidCuboid.DEFAULT_FACES);

	public StampBaseBlockEntityRenderer(BlockEntityRendererProvider.Context pContext) {
		this.itemRenderer = pContext.getItemRenderer();
	}

	@Override
	public void render(@NotNull StampBaseBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        //render item
        if (!blockEntity.inventory.getStackInSlot(0).isEmpty()) {
            poseStack.pushPose();
            ItemStack stack = blockEntity.inventory.getStackInSlot(0);
            int seed = stack.isEmpty() ? 187 : Item.getId(stack.getItem()) + stack.getDamageValue();
            BakedModel bakedmodel = this.itemRenderer.getModel(stack, blockEntity.getLevel(), null, seed);
            float f2 = bakedmodel.getTransforms().getTransform(ItemDisplayContext.GROUND).scale.y(); // TODO: deprecation
            poseStack.translate(0.5D, (double)(0.25F * f2) + 0.75D, 0.5D);
            this.itemRenderer.render(stack, ItemDisplayContext.GROUND, false, poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, bakedmodel);
            poseStack.popPose();
        }

        //render fluid
        FluidStack fluidStack = blockEntity.getFluidStack();
        int capacity = blockEntity.getCapacity();
        if (!fluidStack.isEmpty() && capacity > 0) {
            float offset = blockEntity.renderOffset;
            if (offset > 1.2f || offset < -1.2f) {
                offset = offset - ((offset / 12f + 0.1f) * partialTick);
                blockEntity.renderOffset = offset;
            } else {
                blockEntity.renderOffset = 0;
            }
            FluidRenderer.renderScaledCuboid(poseStack, bufferSource, cube, fluidStack, offset, capacity, packedLight, packedOverlay, false);
        } else {
            blockEntity.renderOffset = 0;
        }
    }
}