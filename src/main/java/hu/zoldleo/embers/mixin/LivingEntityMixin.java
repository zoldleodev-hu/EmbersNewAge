package hu.zoldleo.embers.mixin;

import hu.zoldleo.embers.RegistryManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow
    public abstract ItemStack getUseItem();

    @ModifyArg(method = "handleEntityEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V", ordinal = 1))
    private SoundEvent playDawnstoneShieldBlockSound(SoundEvent par1) {
        if (getUseItem().is(RegistryManager.DAWNSTONE_SHIELD)) // TODO: Is the item still considered used when this happens?
            return par1; // TODO: Sound for blocking an attack with a dawnstone shield
        return par1;
    }
}