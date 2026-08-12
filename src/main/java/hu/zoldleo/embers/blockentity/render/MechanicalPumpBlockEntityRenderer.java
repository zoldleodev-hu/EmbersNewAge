package hu.zoldleo.embers.blockentity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.blockentity.MechanicalPumpBottomBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

public class MechanicalPumpBlockEntityRenderer implements BlockEntityRenderer<MechanicalPumpBottomBlockEntity> {
	public static BakedModel pistonBottom;
	public static BakedModel pistonTop;

	public MechanicalPumpBlockEntityRenderer(BlockEntityRendererProvider.Context pContext) {

	}

	@Override
	public void render(MechanicalPumpBottomBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		BlockRenderDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRenderer();
		BlockPos pos = blockEntity.getBlockPos().below();
		Level level = blockEntity.getLevel();

		double progress = blockEntity.totalProgress * partialTick + blockEntity.lastProgress * (1-partialTick);
		double amountUp = Math.abs(Math.sin((Math.PI * progress)/400.0));

		if (level.getBlockState(pos).isAir())
			packedLight = LightTexture.pack(level.getBrightness(LightLayer.BLOCK, pos), level.getBrightness(LightLayer.SKY, pos));

		BlockState blockState = level.getBlockState(blockEntity.getBlockPos());
		if (blockState.getBlock() == RegistryManager.MECHANICAL_PUMP.get()) {
			poseStack.pushPose();
			poseStack.translate(0.0D, 1.0D + amountUp * 0.25D, 0.0D);

			if (pistonBottom != null)
				blockrendererdispatcher.getModelRenderer().renderModel(poseStack.last(), bufferSource.getBuffer(Sheets.solidBlockSheet()), blockState, pistonBottom, 0.0f, 0.0f, 0.0f, packedLight, packedOverlay, ModelData.EMPTY, Sheets.solidBlockSheet());

			poseStack.translate(0.0D, amountUp * 0.25D, 0.0D);
			if (pistonTop != null)
				blockrendererdispatcher.getModelRenderer().renderModel(poseStack.last(), bufferSource.getBuffer(Sheets.solidBlockSheet()), blockState, pistonTop, 0.0f, 0.0f, 0.0f, packedLight, packedOverlay, ModelData.EMPTY, Sheets.solidBlockSheet());
			poseStack.popPose();
		}
	}

    @Override
    public @NotNull AABB getRenderBoundingBox(MechanicalPumpBottomBlockEntity tile) {
        return new AABB(Vec3.atLowerCornerWithOffset(tile.getBlockPos(), 0, 1, 0), Vec3.atLowerCornerWithOffset(tile.getBlockPos(), 1, 3, 1));
    }
}