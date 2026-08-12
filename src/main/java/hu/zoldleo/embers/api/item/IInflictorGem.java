package hu.zoldleo.embers.api.item;

import javax.annotation.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IInflictorGem {
	void attuneSource(ItemStack stack, @Nullable LivingEntity entity, DamageSource source);

	@Nullable
    Holder<DamageType> getAttunedSource(ItemStack stack);

	float getDamageResistance(ItemStack stack, float modifier);
}