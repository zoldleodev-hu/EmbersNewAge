package hu.zoldleo.embers.fluidtypes;

import java.awt.Color;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import hu.zoldleo.embers.Embers;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceLocation;

public class EmbersFluidType extends FluidType {
	public final ResourceLocation RENDER_OVERLAY;
	public final ResourceLocation TEXTURE_STILL;
	public final ResourceLocation TEXTURE_FLOW;
	public final ResourceLocation TEXTURE_OVERLAY;
	public final Vector3f FOG_COLOR;
	public final float fogStart;
	public final float fogEnd;

	public EmbersFluidType(Properties properties, FluidInfo info) {
		super(properties);
		RENDER_OVERLAY = Embers.res("textures/overlay/" + info.name + ".png");
		TEXTURE_STILL = Embers.res("block/fluid/" + info.name + "_still");
		TEXTURE_FLOW = Embers.res("block/fluid/" + info.name + "_flow");
		TEXTURE_OVERLAY = Embers.res("block/fluid/" + info.name + "_overlay");
		Color colorObject = new Color(info.color);
		FOG_COLOR = new Vector3f(colorObject.getRed()/255F, colorObject.getGreen()/255F, colorObject.getBlue()/255F);
		fogStart = info.fogStart;
		fogEnd = info.fogEnd;
	}

    @OnlyIn(Dist.CLIENT)
	public IClientFluidTypeExtensions getFluidTypeExtension() {
		return new IClientFluidTypeExtensions() {
			@Override
			public @NotNull ResourceLocation getStillTexture() {
				return TEXTURE_STILL;
			}

			@Override
			public @NotNull ResourceLocation getFlowingTexture() {
				return TEXTURE_FLOW;
			}

			@Override
			public ResourceLocation getOverlayTexture() {
				return TEXTURE_OVERLAY;
			}

			@Override
			public ResourceLocation getRenderOverlayTexture(@NotNull Minecraft mc) {
				return RENDER_OVERLAY;
			}

			@Override
			public @NotNull Vector3f modifyFogColor(@NotNull Camera camera, float partialTick, @NotNull ClientLevel level, int renderDistance, float darkenWorldAmount, @NotNull Vector3f fluidFogColor) {
				return FOG_COLOR;
			}

			@Override
			public void modifyFogRender(@NotNull Camera camera, FogRenderer.@NotNull FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, @NotNull FogShape shape) {
				RenderSystem.setShaderFogStart(fogStart);
				RenderSystem.setShaderFogEnd(fogEnd);
			}
		};
	}

	public static class FluidInfo {
		public String name;
		public int color;
		public float fogStart;
		public float fogEnd;

		public FluidInfo(String name, int color, float fogStart, float fogEnd) {
			this.name = name;
			this.color = color;
			this.fogStart = fogStart;
			this.fogEnd = fogEnd;
		}
	}
}