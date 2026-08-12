package hu.zoldleo.embers.blockentity.render;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import hu.zoldleo.embers.blockentity.MixerCentrifugeBottomBlockEntity;
import hu.zoldleo.embers.blockentity.MixerCentrifugeBottomBlockEntity.MixerFluidTank;
import hu.zoldleo.embers.render.FluidCuboid;
import hu.zoldleo.embers.render.FluidRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class MixerCentrifugeBottomBlockEntityRenderer implements BlockEntityRenderer<MixerCentrifugeBottomBlockEntity> {
	FluidCuboid cubeNorth = new FluidCuboid(new Vector3f(7, 12, 3), new Vector3f(9, 15, 4), FluidCuboid.DEFAULT_FACES);
	FluidCuboid cubeSouth = new FluidCuboid(new Vector3f(7, 12, 12), new Vector3f(9, 15, 13), FluidCuboid.DEFAULT_FACES);
	FluidCuboid cubeEast = new FluidCuboid(new Vector3f(12, 12, 7), new Vector3f(13, 15, 9), FluidCuboid.DEFAULT_FACES);
	FluidCuboid cubeWest = new FluidCuboid(new Vector3f(3, 12, 7), new Vector3f(4, 15, 9), FluidCuboid.DEFAULT_FACES);
	FluidCuboid[] cubes = new FluidCuboid[] {cubeNorth, cubeSouth, cubeEast, cubeWest};

	public MixerCentrifugeBottomBlockEntityRenderer(BlockEntityRendererProvider.Context ignored) {

	}

	@Override
	public void render(@NotNull MixerCentrifugeBottomBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        //render fluid
        for (int i = 0; i < blockEntity.getTanks().length; i++) {
            MixerFluidTank fluidStack = blockEntity.getTanks()[i];
            int capacity = fluidStack.getCapacity();
            if (!fluidStack.isEmpty() && capacity > 0) {
                float offset = fluidStack.renderOffset;
                if (offset > 1.2f || offset < -1.2f) {
                    offset = offset - ((offset / 12f + 0.1f) * partialTick);
                    fluidStack.renderOffset = offset;
                } else {
                    fluidStack.renderOffset = 0;
                }
                FluidRenderer.renderScaledCuboid(poseStack, bufferSource, cubes[i], fluidStack.getFluid(), offset, capacity, packedLight, packedOverlay, false);
            } else {
                fluidStack.renderOffset = 0;
            }
        }
    }
}