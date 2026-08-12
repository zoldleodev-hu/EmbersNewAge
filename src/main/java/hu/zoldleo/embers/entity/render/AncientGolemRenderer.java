package hu.zoldleo.embers.entity.render;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.entity.AncientGolemEntity;
import hu.zoldleo.embers.model.AncientGolemModel;
import hu.zoldleo.embers.model.AshenArmorModel;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class AncientGolemRenderer extends MobRenderer<AncientGolemEntity, AncientGolemModel<AncientGolemEntity>> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Embers.res("ancient_golem"), "main");
	public static final ResourceLocation TEXTURE = Embers.res("textures/entity/golem.png");

	public AncientGolemRenderer(EntityRendererProvider.Context context) {
		super(context, new AncientGolemModel<>(context.bakeLayer(LAYER_LOCATION)), 0.5f);
		this.addLayer(new AncientGolemEyeLayer<>(this));
		//I just need to get this context from somewhere
		AshenArmorModel.init(context);
	}

	@Override
	public @NotNull ResourceLocation getTextureLocation(@NotNull AncientGolemEntity pEntity) {
		return TEXTURE;
	}
}