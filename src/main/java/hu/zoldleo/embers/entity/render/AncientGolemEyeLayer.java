package hu.zoldleo.embers.entity.render;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.model.AncientGolemModel;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class AncientGolemEyeLayer<T extends Entity, M extends AncientGolemModel<T>> extends EyesLayer<T, M> {
	private static final RenderType GOLEM_EYE = RenderType.eyes(Embers.res("textures/entity/golem_overlay.png"));

	public AncientGolemEyeLayer(RenderLayerParent<T, M> p_117507_) {
		super(p_117507_);
	}

	public @NotNull RenderType renderType() {
		return GOLEM_EYE;
	}
}