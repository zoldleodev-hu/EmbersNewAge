package hu.zoldleo.embers.blockentity.render;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import hu.zoldleo.embers.blockentity.FluidTransferBlockEntity;
import hu.zoldleo.embers.render.FluidCuboid;
import hu.zoldleo.embers.render.FluidRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class FluidTransferBlockEntityRenderer implements BlockEntityRenderer<FluidTransferBlockEntity> {
	FluidCuboid cube = new FluidCuboid(new Vector3f(4, 4, 4), new Vector3f(12, 12, 12), FluidCuboid.DEFAULT_FACES);

	public FluidTransferBlockEntityRenderer(BlockEntityRendererProvider.Context ignored) {

	}

	@Override
	public void render(@NotNull FluidTransferBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        //render fluid
        if (!blockEntity.filterFluid.isEmpty())
            FluidRenderer.renderScaledCuboid(poseStack, bufferSource, cube, blockEntity.filterFluid, 0, blockEntity.filterFluid.getAmount(), packedLight, packedOverlay, false);
    }
}