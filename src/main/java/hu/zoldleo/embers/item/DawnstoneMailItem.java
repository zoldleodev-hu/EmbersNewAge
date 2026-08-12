package hu.zoldleo.embers.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class DawnstoneMailItem extends Item implements IEmbersCurioItem {
	public DawnstoneMailItem(Properties properties) {
		super(properties);
	}

	public SoundEvent equipSound() {
		return SoundEvents.ARMOR_EQUIP_CHAIN.value();
	}

	public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
		ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
		builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(id, 1, AttributeModifier.Operation.ADD_VALUE));
		return builder.build();
	}
}