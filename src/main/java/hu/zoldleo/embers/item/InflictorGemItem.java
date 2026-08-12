package hu.zoldleo.embers.item;

import java.util.List;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.item.IInflictorGem;
import hu.zoldleo.embers.datagen.EmbersDamageTypeTags;
import hu.zoldleo.embers.datagen.EmbersSounds;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class InflictorGemItem extends Item implements IInflictorGem {
	public InflictorGemItem(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide && player.isSecondaryUseActive() && stack.has(RegistryManager.GEM_COMPONENT)) {
			stack.remove(RegistryManager.GEM_COMPONENT);
			if (player.getHealth() > 1f)
				player.setHealth(Math.max(player.getHealth() - 10.0f, 1f));
		}
		return player.isSecondaryUseActive() ? InteractionResultHolder.consume(stack) : InteractionResultHolder.pass(stack);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag isAdvanced) {
		super.appendHoverText(stack, context, tooltip, isAdvanced);
        Holder<DamageType> type = stack.get(RegistryManager.GEM_COMPONENT);
		if (type != null) {
			tooltip.add(Component.translatable(Embers.MODID + ".tooltip.inflictor", type.value().msgId()).withStyle(ChatFormatting.GRAY));
		} else {
			tooltip.add(Component.translatable(Embers.MODID + ".tooltip.inflictor.none").withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	public void attuneSource(ItemStack stack, LivingEntity entity, DamageSource source) {
		if (!source.is(EmbersDamageTypeTags.INFLICTOR_GEM_BLACKLIST)) {
            stack.set(RegistryManager.GEM_COMPONENT, source.typeHolder());
			if (entity != null)
				entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), EmbersSounds.INFLICTOR_GEM.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
		}
	}

	@Override
	public Holder<DamageType> getAttunedSource(ItemStack stack) {
		if (!stack.isEmpty())
			return stack.get(RegistryManager.GEM_COMPONENT);
		return null;
	}

	@Override
	public float getDamageResistance(ItemStack stack, float modifier) {
		return 0.35f;
	}
}
