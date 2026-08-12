package hu.zoldleo.embers.blockentity.render;

import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import hu.zoldleo.embers.blockentity.MixerCentrifugeTopBlockEntity;
import hu.zoldleo.embers.render.FluidCuboid;
import hu.zoldleo.embers.render.FluidRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class MixerCentrifugeTopBlockEntityRenderer implements BlockEntityRenderer<MixerCentrifugeTopBlockEntity> {
	FluidCuboid cube = new FluidCuboid(new Vector3f(3, 2, 3), new Vector3f(13, 5, 13), FluidCuboid.DEFAULT_FACES);

	public MixerCentrifugeTopBlockEntityRenderer(BlockEntityRendererProvider.Context ignored) {

	}

	@Override
	public void render(@NotNull MixerCentrifugeTopBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
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