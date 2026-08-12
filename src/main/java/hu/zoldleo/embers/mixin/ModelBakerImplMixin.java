package hu.zoldleo.embers.mixin;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.throwables.MixinApplyError;

@Mixin(ModelBakery.ModelBakerImpl.class)
public interface ModelBakerImplMixin { // Because accesstransformers refuse to work on inner classes
    @Invoker("<init>")
    static ModelBakery.ModelBakerImpl ctor(ModelBakery bakery, ModelBakery.TextureGetter textureGetter, ModelResourceLocation modelLocation) {
        throw new MixinApplyError("ModelBakerImplMixin failed to apply");
    }
}