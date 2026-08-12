package hu.zoldleo.embers.item;

import java.util.List;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.EmbersClientEvents;
import hu.zoldleo.embers.api.item.ITyrfingWeapon;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.particle.TyrfingParticleOptions;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.ChatFormatting;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.jetbrains.annotations.NotNull;

public class TyrfingItem extends SwordItem implements ITyrfingWeapon {
	public TyrfingItem(Tier tier, int damage, float speed, Properties prop) {
		super(tier, prop.attributes(SwordItem.createAttributes(tier, damage, speed)));
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, List<Component> tooltip, @NotNull TooltipFlag isAdvanced) {
		tooltip.add(Component.translatable(Embers.MODID + ".tooltip.tyrfing").withStyle(ChatFormatting.GRAY));
	}

	@Override
	public void attack(LivingIncomingDamageEvent event, double armor) {
		if (armor > 0) {
			event.getEntity().playSound(EmbersSounds.TYRFING_HIT.get(),1.0f,1.0f);
			if (event.getEntity().level() instanceof ServerLevel serverLevel)
				serverLevel.sendParticles(TyrfingParticleOptions.TYRFING, event.getEntity().position().x, event.getEntity().position().y + event.getEntity().getBbHeight() / 2.0f, event.getEntity().position().z, 80, 0.1, 0.1, 0.1, 0.0);
			event.setAmount((event.getAmount() / 4.0f) * (4.0f + (float) armor));
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static class ColorHandler implements ItemColor {
		@Override
		public int getColor(@NotNull ItemStack stack, int tintIndex) {
			if (tintIndex == 1) {
				float timerSine = ((float)Math.sin(8.0*Math.toRadians(EmbersClientEvents.ticks % 360))+1.0f)/2.0f;
				int r = (int)(64.0f*timerSine);
				int g = (int)(16.0f);
				int b = (int)(32.0f+32.0f*timerSine);
				return Misc.intColor(0xFF, r, g, b);
			}
			return 0xFFFFFFFF;
		}		
	}
}