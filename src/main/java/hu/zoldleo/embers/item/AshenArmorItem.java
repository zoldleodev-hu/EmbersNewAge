package hu.zoldleo.embers.item;

import hu.zoldleo.embers.Embers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

@EventBusSubscriber
public class AshenArmorItem extends ArmorItem {
	public AshenArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
		super(material, type, properties);
	}

	/*/@Override TODO
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		return Embers.MODID + ":textures/models/armor/robe.png";
	}*/

	/*/@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
		Multimap<Attribute, AttributeModifier> modifiers = super.getAttributeModifiers(slot, stack);
		super.getDefaultAttributeModifiers(slot);
		if (isBroken(stack))
			return ImmutableMultimap.of();
		return modifiers;
	}*/

    @SubscribeEvent
    public static void modifyAttributes(ItemAttributeModifierEvent event) {
        if (isBroken(event.getItemStack()))
            event.clearModifiers();
    }

	@Override
	public void setDamage(@NotNull ItemStack stack, int damage) {
		super.setDamage(stack, Math.min(damage, getMaxDamage(stack) - 1));
	}

	@Override
	public <T extends LivingEntity> int damageItem(@NotNull ItemStack stack, int amount, T entity, @NotNull Consumer<Item> onBroken) {
		return isBroken(stack) ? 0 : Math.min(amount, getMaxDamage(stack) - getDamage(stack) - 1);
	}

	public static boolean isBroken(ItemStack armor) {
		return armor.getDamageValue() >= armor.getMaxDamage() - 1;
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag isAdvanced) {
		super.appendHoverText(stack, context, tooltip, isAdvanced);
		if (isBroken(stack))
			tooltip.add(Component.translatable(Embers.MODID + ".tooltip.broken").withStyle(ChatFormatting.GRAY));
	}

	/*/@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(AshenArmorModel.ARMOR_MODEL_GETTER);
	}*/
}