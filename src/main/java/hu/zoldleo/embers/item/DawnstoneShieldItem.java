package hu.zoldleo.embers.item;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.item.IInflictorGem;
import hu.zoldleo.embers.api.item.IInflictorGemHolder;
import hu.zoldleo.embers.datacomponents.GemSocketComponent;
import hu.zoldleo.embers.render.DawnstoneShieldRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class DawnstoneShieldItem extends ShieldItem implements IInflictorGemHolder {
    public Supplier<Integer> gemSlots;

    public DawnstoneShieldItem(Properties properties, Supplier<Integer> gemSlots) {
        super(properties);
        this.gemSlots = gemSlots;
    }

    @Override
    public int getGemSlots(ItemStack holder) {
        GemSocketComponent gems = holder.get(RegistryManager.GEM_SOCKET_COMPONENT);
        return gems == null ? gemSlots.get() : gems.sockets();
    }

    @Override
    public void attachGem(ItemStack holder, ItemStack gem) {
        if (!canAttachGem(holder, gem))
            return;
        GemSocketComponent gems = holder.get(RegistryManager.GEM_SOCKET_COMPONENT);
        gems = gems == null ? new GemSocketComponent(gemSlots.get()) : gems.copy();
        if (gems.socketGem(gem.copy()))
            holder.set(RegistryManager.GEM_SOCKET_COMPONENT, gems);
    }

    @Override
    public ItemStack detachGem(ItemStack holder) {
        GemSocketComponent gems = holder.get(RegistryManager.GEM_SOCKET_COMPONENT);
        if (gems == null)
            return ItemStack.EMPTY;
        gems = gems.copy();
        ItemStack gem = gems.unsocketGem();
        if (!gem.isEmpty())
            holder.set(RegistryManager.GEM_SOCKET_COMPONENT, gems);
        return gem;
    }

    @Override
    public ItemStack getLastGem(ItemStack holder)  {
        GemSocketComponent gems = holder.get(RegistryManager.GEM_SOCKET_COMPONENT);
        return gems == null ? ItemStack.EMPTY : gems.getLast();
    }

    @Override
    public void clearGems(ItemStack holder) {
        holder.remove(RegistryManager.GEM_SOCKET_COMPONENT);
    }

    @Override
    public int getAttachedGemCount(ItemStack holder) {
        GemSocketComponent gems = holder.get(RegistryManager.GEM_SOCKET_COMPONENT);
        return gems == null ? 0 : gems.socketedGems();
    }

    @Override
    public ItemStack[] getAttachedGems(ItemStack holder) {
        GemSocketComponent component = holder.get(RegistryManager.GEM_SOCKET_COMPONENT);
        return  component != null ? component.getAttachedGems() : new ItemStack[0];
    }

    @Override
    public float getTotalDamageResistance(LivingEntity entity, DamageSource source, ItemStack holder) {
        if (!entity.isUsingItem() || entity.getUseItem() != holder)
            return 0;
        if (AshenArmorItem.isBroken(holder))
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

    @OnlyIn(Dist.CLIENT)
    public static IClientItemExtensions getExtensions() {
        return DawnstoneShieldItemExtensions.instance;
    }

    @OnlyIn(Dist.CLIENT)
    private static class DawnstoneShieldItemExtensions implements IClientItemExtensions {
        public static DawnstoneShieldItemExtensions instance = new DawnstoneShieldItemExtensions();
        private DawnstoneShieldRenderer renderer;

        @Override
        public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (renderer == null) {
                Minecraft minecraft = Minecraft.getInstance();
                renderer = new DawnstoneShieldRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
            }
            return renderer;
        }
    }
}