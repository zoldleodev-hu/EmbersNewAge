package hu.zoldleo.embers.blockentity.render;

import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import hu.zoldleo.embers.blockentity.CatalyticPlugBlockEntity;
import hu.zoldleo.embers.render.FluidCuboid;
import hu.zoldleo.embers.render.FluidRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class CatalyticPlugBlockEntityRenderer implements BlockEntityRenderer<CatalyticPlugBlockEntity> {

	FluidCuboid cube = new FluidCuboid(new Vector3f(0, 0.499f, 0), new Vector3f(3, 6, 3), FluidCuboid.FLOWING_DOWN_FACES);

	public CatalyticPlugBlockEntityRenderer(BlockEntityRendererProvider.Context pContext) {

	}

	@Override
	public void render(@NotNull CatalyticPlugBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		if (blockEntity.getBlockState().hasProperty(BlockStateProperties.FACING)) {
			//render fluid
			FluidStack fluidStack = blockEntity.tank.getFluid();
			int capacity = blockEntity.tank.getCapacity();
			if (!fluidStack.isEmpty() && capacity > 0) {
				float offset = blockEntity.renderOffset;
				if (offset > 1.2f || offset < -1.2f) {
					offset = offset - ((offset / 12f + 0.1f) * partialTick);
					blockEntity.renderOffset = offset;
				} else {
					blockEntity.renderOffset = 0;
				}
				Direction facing = blockEntity.getBlockState().getValue(BlockStateProperties.FACING);
				RenderSystem.enableCull();
				poseStack.pushPose();
				poseStack.translate(0.5D, 0.5D, 0.5D);
				poseStack.mulPose(facing.getRotation());
				poseStack.translate(-0.5D, -0.5D, -0.5D);

				poseStack.pushPose();
				poseStack.translate(0.5D, 0.25D, 0.1875D);
				poseStack.mulPose(Axis.XP.rotationDegrees(-22.5f));
				poseStack.translate(-0.09375D, 0.09375D, -0.09375D);
				FluidRenderer.renderScaledCuboid(poseStack, bufferSource, cube, fluidStack, offset, capacity, packedLight, packedOverlay, false);
				poseStack.popPose();

				poseStack.pushPose();
				poseStack.translate(0.1875D, 0.25D, 0.5D);
				poseStack.mulPose(Axis.ZP.rotationDegrees(22.5f));
				poseStack.translate(-0.09375D, 0.09375D, -0.09375D);
				FluidRenderer.renderScaledCuboid(poseStack, bufferSource, cube, fluidStack, offset, capacity, packedLight, packedOverlay, false);
				poseStack.popPose();

				poseStack.pushPose();
				poseStack.translate(0.5D, 0.25D, 0.8125D);
				poseStack.mulPose(Axis.XP.rotationDegrees(22.5f));
				poseStack.translate(-0.09375D, 0.09375D, -0.09375D);
				FluidRenderer.renderScaledCuboid(poseStack, bufferSource, cube, fluidStack, offset, capacity, packedLight, packedOverlay, false);
				poseStack.popPose();

				poseStack.pushPose();
				poseStack.translate(0.8125D, 0.25D, 0.5D);
				poseStack.mulPose(Axis.ZP.rotationDegrees(-22.5f));
				poseStack.translate(-0.09375D, 0.09375D, -0.09375D);
				FluidRenderer.renderScaledCuboid(poseStack, bufferSource, cube, fluidStack, offset, capacity, packedLight, packedOverlay, false);
				poseStack.popPose();

				poseStack.popPose();
			} else {
				blockEntity.renderOffset = 0;
			}
		}
	}
}