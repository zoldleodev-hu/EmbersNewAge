package hu.zoldleo.embers.api.event;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EmberBoreBladeRenderEvent extends UpgradeEvent {
	PoseStack pose;
	BlockState state;
	MultiBufferSource bufferSource;
	int packedLight;
	int packedOverlay;

	public EmberBoreBladeRenderEvent(BlockEntity tile, PoseStack pose, BlockState state, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		super(tile);
		this.pose = pose;
		this.state = state;
		this.bufferSource = bufferSource;
		this.packedLight = packedLight;
		this.packedOverlay = packedOverlay;
	}

	public PoseStack getPose() {
		return pose;
	}

	public BlockState getBlockState() {
		return state;
	}

	public MultiBufferSource getBuffer() {
		return bufferSource;
	}

	public int getLight() {
		return packedLight;
	}

	public int getOverlay() {
		return packedOverlay;
	}
}