package hu.zoldleo.embers.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import hu.zoldleo.embers.entity.EmberPacketEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class EmberPacketRenderer extends EntityRenderer<EmberPacketEntity> {
	public EmberPacketRenderer(EntityRendererProvider.Context pContext) {
		super(pContext);
	}

	@Override
	public void render(@NotNull EmberPacketEntity pEntity, float pEntityYaw, float pPartialTicks, @NotNull PoseStack pMatrixStack, @NotNull MultiBufferSource pBuffer, int pPackedLight) {}

	@Override
	public @NotNull ResourceLocation getTextureLocation(@NotNull EmberPacketEntity pEntity) {
		return InventoryMenu.BLOCK_ATLAS;
	}
}