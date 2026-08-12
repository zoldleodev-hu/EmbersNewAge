package hu.zoldleo.embers.blockentity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import hu.zoldleo.embers.blockentity.MithrilBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MithrilBlockEntityRenderer implements BlockEntityRenderer<MithrilBlockEntity> {
	private final ItemRenderer itemRenderer;

	public MithrilBlockEntityRenderer(BlockEntityRendererProvider.Context pContext) {
		this.itemRenderer = pContext.getItemRenderer();
	}

	@Override // TODO: why??
	public void render(@NotNull MithrilBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack block = new ItemStack(blockEntity.getBlockState().getBlock());
        int seed = block.isEmpty() ? 187 : Item.getId(block.getItem()) + block.getDamageValue();
        BakedModel bakedmodel = this.itemRenderer.getModel(block, blockEntity.getLevel(), null, seed);
        poseStack.translate(0.5, 0.5, 0.5);
        this.itemRenderer.render(block, ItemDisplayContext.NONE, false, poseStack, bufferSource, packedLight, packedOverlay, bakedmodel);
    }
}