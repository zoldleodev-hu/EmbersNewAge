package hu.zoldleo.embers.item;

import java.util.List;
import java.util.function.Supplier;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.item.IInflictorGem;
import hu.zoldleo.embers.api.item.IInflictorGemHolder;

import hu.zoldleo.embers.datacomponents.GemSocketComponent;
import hu.zoldleo.embers.util.ItemStackNonNullList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

public class AshenArmorGemItem extends AshenArmorItem implements IInflictorGemHolder {
	public Supplier<Integer> gemSlots;

	public AshenArmorGemItem(Holder<ArmorMaterial> material, Type type, Properties properties, Supplier<Integer> gemSlots) {
		super(material, type, properties);
		this.gemSlots = gemSlots;
	}

	@Override
	public int getGemSlots(ItemStack holder) {
        NonNullList<ItemStack> gems = holder.get(RegistryManager.GEM_SOCKET_COMPONENT);
		return gems == null ? gemSlots.get() : gems.size();
	}

	@Override
	public boolean canAttachGem(ItemStack holder, ItemStack gem) {
		return gem.getItem() instanceof IInflictorGem;
	}

	@Override
	public void attachGem(ItemStack holder, ItemStack gem, int slot) {
        if (!canAttachGem(holder, gem))
            return;
        GemSocketComponent gems = holder.get(RegistryManager.GEM_SOCKET_COMPONENT);
        gems = gems == null ? GemSocketComponent.withSize(gemSlots.get()) : GemSocketComponent.copy(gems);
        if (gems.size() > slot) {
            gems.set(slot, gem.copy());
            holder.set(RegistryManager.GEM_SOCKET_COMPONENT, gems);
        }
	}

	@Override
	public ItemStack detachGem(ItemStack holder, int slot) {
        GemSocketComponent gems = holder.get(RegistryManager.GEM_SOCKET_COMPONENT);
        if (gems == null)
            return ItemStack.EMPTY;
        gems = GemSocketComponent.copy(gems);
        ItemStack gem = ItemStack.EMPTY;
        if (gems.size() > slot)
            gem = gems.get(slot);
        if (!gem.isEmpty()) {
            gems.set(slot, ItemStack.EMPTY);
            holder.set(RegistryManager.GEM_SOCKET_COMPONENT, gems);
        }
        return gem;
	}

	@Override
	public void clearGems(ItemStack holder) {
        holder.remove(RegistryManager.GEM_SOCKET_COMPONENT);
        /*/NonNullList<ItemStack> gems = holder.get(RegistryManager.GEM_SOCKET_COMPONENT);
        if (gems != null)
            gems.clear(); // Sets all slots to ItemStack.EMPTY*/
	}

	@Override
	public ItemStack[] getAttachedGems(ItemStack holder) {
        return holder.getOrDefault(RegistryManager.GEM_SOCKET_COMPONENT, ItemStackNonNullList.of()).toArray(new ItemStack[0]);
	}

	@Override
	public float getTotalDamageResistance(LivingEntity entity, DamageSource source, ItemStack holder) {
        if (isBroken(holder))
            return 0;
		float reduction = 0;
        for (ItemStack stack : getAttachedGems(holder))
            if (stack.getItem() instanceof IInflictorGem gem && source.typeHolder().equals(gem.getAttunedSource(stack)))
                reduction += gem.getDamageResistance(stack, reduction);
		return reduction;
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag isAdvanced) {
		super.appendHoverText(stack, context, tooltip, isAdvanced);
		ItemStack[] attached = getAttachedGems(stack);
		int filledSlots = 0;

		for (ItemStack stacks : attached)
			if (!stacks.isEmpty())
				filledSlots++;

		if (getGemSlots(stack) > filledSlots)
			tooltip.add(Component.translatable(Embers.MODID + ".tooltip.inflictor.slots", getGemSlots(stack) - filledSlots).withStyle(ChatFormatting.GRAY));

		for (ItemStack stacks : attached) {
			if (!stacks.isEmpty()) {
                Holder<DamageType> resistance = stacks.get(RegistryManager.GEM_COMPONENT);
				if (resistance != null) {
					tooltip.add(Component.translatable(Embers.MODID + ".tooltip.inflictor", resistance.value().msgId()).withStyle(ChatFormatting.GRAY));
				} else {
					tooltip.add(Component.translatable(Embers.MODID + ".tooltip.inflictor.none").withStyle(ChatFormatting.GRAY));
				}
			}
		}
	}
}