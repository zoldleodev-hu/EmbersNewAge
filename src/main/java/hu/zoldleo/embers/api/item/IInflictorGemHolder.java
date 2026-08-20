package hu.zoldleo.embers.api.item;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IInflictorGemHolder {
	int getGemSlots(ItemStack holder);

	default boolean canAttachGem(ItemStack holder, ItemStack gem) {
        return gem.getItem() instanceof IInflictorGem && !isFull(holder);
    }

	void attachGem(ItemStack holder, ItemStack gem);

	ItemStack detachGem(ItemStack holder);

    default ItemStack getLastGem(ItemStack holder) {
        return getAttachedGems(holder)[getAttachedGemCount(holder) - 1];
    }

	void clearGems(ItemStack holder);

	default int getAttachedGemCount(ItemStack holder) {
		int amt = 0;
		for (ItemStack stack : getAttachedGems(holder))
			if(!stack.isEmpty())
				amt++;
		return amt;
	}

    default boolean isFull(ItemStack holder) {
        return getAttachedGemCount(holder) >= getGemSlots(holder);
    }

    default boolean isEmpty(ItemStack holder) {
        return getAttachedGemCount(holder) <= 0;
    }

	ItemStack[] getAttachedGems(ItemStack holder);

	float getTotalDamageResistance(LivingEntity entity, DamageSource source, ItemStack holder);
}