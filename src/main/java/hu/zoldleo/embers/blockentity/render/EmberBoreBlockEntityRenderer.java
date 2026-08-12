package hu.zoldleo.embers.blockentity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.event.EmberBoreBladeRenderEvent;
import hu.zoldleo.embers.api.upgrades.UpgradeUtil;
import hu.zoldleo.embers.blockentity.EmberBoreBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

public class EmberBoreBlockEntityRenderer implements BlockEntityRenderer<EmberBoreBlockEntity> {
	public static BakedModel blades;

	public EmberBoreBlockEntityRenderer(BlockEntityRendererProvider.Context ignored) {

	}

	@Override
	public void render(EmberBoreBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		BlockRenderDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRenderer();
		float angle = blockEntity.angle;
		float lastAngle = blockEntity.lastAngle;
		BlockPos pos = blockEntity.getBlockPos().below();
		Level level = blockEntity.getLevel();

		if (level.getBlockState(pos).isAir())
			packedLight = LightTexture.pack(level.getBrightness(LightLayer.BLOCK, pos), level.getBrightness(LightLayer.SKY, pos));

		BlockState blockState = level.getBlockState(blockEntity.getBlockPos());
		if (blockState.getBlock() == RegistryManager.EMBER_BORE.get()) {
			poseStack.pushPose();
			poseStack.translate(0.5D, -0.5D, 0.5D);
			if (blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS) == Direction.Axis.X) {
				poseStack.mulPose(Axis.ZP.rotationDegrees(partialTick * angle + (1 - partialTick) * lastAngle));
			} else {
				poseStack.mulPose(Axis.XP.rotationDegrees(partialTick * angle + (1 - partialTick) * lastAngle));
				poseStack.mulPose(Axis.YP.rotationDegrees(90));
			}
			poseStack.translate(-0.5D, -0.5D, -0.5D);
			if (blades != null)
				blockrendererdispatcher.getModelRenderer().renderModel(poseStack.last(), bufferSource.getBuffer(Sheets.solidBlockSheet()), blockState, blades, 0.0f, 0.0f, 0.0f, packedLight, packedOverlay, ModelData.EMPTY, Sheets.solidBlockSheet());

			UpgradeUtil.throwEvent(blockEntity, new EmberBoreBladeRenderEvent(blockEntity, poseStack, blockState, bufferSource, packedLight, packedOverlay), blockEntity.upgrades);

			poseStack.popPose();
		}
	}

    @Override
    public @NotNull AABB getRenderBoundingBox(EmberBoreBlockEntity tile) {
        return new AABB(Vec3.atLowerCornerWithOffset(tile.getBlockPos(), -1, -2, -1), Vec3.atLowerCornerWithOffset(tile.getBlockPos(), 2, 0, 2));
    }
}