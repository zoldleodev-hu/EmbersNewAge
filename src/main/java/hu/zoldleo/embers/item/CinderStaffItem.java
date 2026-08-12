package hu.zoldleo.embers.item;

import java.util.Random;
import java.util.function.Function;

import hu.zoldleo.embers.RegistryManager;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import hu.zoldleo.embers.ConfigManager;
import hu.zoldleo.embers.api.event.EmberProjectileEvent;
import hu.zoldleo.embers.api.event.ItemVisualEvent;
import hu.zoldleo.embers.api.item.IProjectileWeapon;
import hu.zoldleo.embers.api.projectile.EffectArea;
import hu.zoldleo.embers.api.projectile.EffectDamage;
import hu.zoldleo.embers.api.projectile.IProjectilePreset;
import hu.zoldleo.embers.api.projectile.ProjectileFireball;
import hu.zoldleo.embers.damage.DamageEmber;
import hu.zoldleo.embers.datagen.EmbersDamageTypes;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.particle.GlowParticleOptions;
import hu.zoldleo.embers.util.EmberInventoryUtil;
import hu.zoldleo.embers.util.EmbersColors;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CinderStaffItem extends Item implements IProjectileWeapon {
	public static boolean soundPlaying = false; //Clientside anyway so whatever
	public static Random rand = new Random();

	public CinderStaffItem(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public void releaseUsing(@NotNull ItemStack stack, Level level, @NotNull LivingEntity entity, int timeLeft) {
		if (!level.isClientSide) {
			double charge = (Math.min(ConfigManager.CINDER_STAFF_MAX_CHARGE.get(), getUseDuration(stack, entity) - timeLeft)) / (double) ConfigManager.CINDER_STAFF_MAX_CHARGE.get();
			Vec3 launchPos = getLaunchPos(entity);
			float damage = (float) Math.max(charge * ConfigManager.CINDER_STAFF_DAMAGE.get(), 0.5f);
			float size = (float) Math.max(charge * ConfigManager.CINDER_STAFF_SIZE.get(), 0.5f);
			float aoeSize = (float) (charge * ConfigManager.CINDER_STAFF_AOE_SIZE.get());
			int lifetime = charge >= 0.06 ? ConfigManager.CINDER_STAFF_LIFETIME.get() : 5;

			Function<Entity, DamageSource> damageSource = e -> new DamageEmber(level.registryAccess().registry(Registries.DAMAGE_TYPE).get().getHolderOrThrow(EmbersDamageTypes.EMBER_KEY), e, entity);
			EffectArea effect = new EffectArea(new EffectDamage(damage, damageSource, 1, 1.0), aoeSize, false);
			ProjectileFireball fireball = new ProjectileFireball(entity, launchPos, entity.getLookAngle().scale(0.85), size, lifetime, effect);
			EmberProjectileEvent event = new EmberProjectileEvent(entity, stack, charge, fireball);
			NeoForge.EVENT_BUS.post(event);
			if (!event.isCanceled())
				for (IProjectilePreset projectile : event.getProjectiles())
					projectile.shoot(level);

			SoundEvent sound;
			if (charge * ConfigManager.CINDER_STAFF_DAMAGE.get() >= 10.0)
				sound = EmbersSounds.FIREBALL_BIG.get();
			else if (charge * ConfigManager.CINDER_STAFF_DAMAGE.get() >= 1.0)
				sound = EmbersSounds.FIREBALL.get();
			else
				sound = EmbersSounds.CINDER_STAFF_FAIL.get();
			level.playSound(null, launchPos.x, launchPos.y, launchPos.z, sound, SoundSource.PLAYERS, 1.0f, 1.0f);
		}
        stack.set(RegistryManager.COOLDOWN_COMPONENT, level.getGameTime());
		//stack.getOrCreateTag().putLong("lastUse", level.getGameTime());
		entity.swing(entity.getUsedItemHand());
		entity.stopUsingItem();
	}

	@Override
	public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity, ItemStack stack, int count) {
        Long cooldownTime = stack.get(RegistryManager.COOLDOWN_COMPONENT);
		if (cooldownTime != null && cooldownTime + ConfigManager.CINDER_STAFF_COOLDOWN.get() > level.getGameTime() && !(entity instanceof Player player && player.hasInfiniteMaterials()))
			entity.stopUsingItem();
		double charge = (Math.min(ConfigManager.CINDER_STAFF_MAX_CHARGE.get(), getUseDuration(stack, entity) - count)) / (double) ConfigManager.CINDER_STAFF_MAX_CHARGE.get();
		boolean fullCharge = charge >= 1.0;
		ItemVisualEvent event = new ItemVisualEvent(entity, Misc.handToSlot(entity.getUsedItemHand()),stack,EmbersColors.EMBER,fullCharge ? EmbersSounds.CINDER_STAFF_LOOP.get() : null, 1.0f, 1.0f, "charge");

		NeoForge.EVENT_BUS.post(event);

		if (event.hasSound()) {
			if (!soundPlaying) {
				if (level.isClientSide())
					EmbersSounds.playItemSoundClient(entity, this, event.getSound(), SoundSource.PLAYERS, true, event.getVolume(), event.getPitch());
				soundPlaying = true;
			}
		} else {
			soundPlaying = false;
		}
		if (event.hasParticles()) {
			Vector3f color = event.getColor();
			Vec3 launchPos = getLaunchPos(entity);
			GlowParticleOptions options = new GlowParticleOptions(new Vector3f(color.x, color.y, color.z), (float) (charge * ConfigManager.CINDER_STAFF_SIZE.get() / 2.0f), 24);
			for (int i = 0; i < 4; i++)
				level.addParticle(options, (float) launchPos.x + (rand.nextFloat() * 0.1f - 0.05f), (float) launchPos.y + (rand.nextFloat() * 0.1f - 0.05f), (float) launchPos.z + (rand.nextFloat() * 0.1f - 0.05f), 0, 0.000001, 0);
		}
	}

	public static Vec3 getLaunchPos(LivingEntity entity) {
		float spawnDistance = 2.0f;//Math.max(1.0f, (float)charge/5.0f);
		Vec3 eyesPos = entity.getEyePosition();
		if (entity instanceof Player player) {
			HitResult traceResult = getPlayerPOVHitResult(entity.level(), player, ClipContext.Fluid.NONE);
			if (traceResult.getType() == HitResult.Type.BLOCK)
				spawnDistance = (float) Math.min(spawnDistance, traceResult.getLocation().distanceTo(eyesPos));
		}

		Vec3 look = entity.getLookAngle().add(entity.getUpVector(1.0f).scale(0.2));
		double handmod = entity.getUsedItemHand() == InteractionHand.MAIN_HAND ? 1.0 : -1.0;
		handmod *= entity.getMainArm() == HumanoidArm.RIGHT ? 1.0 : -1.0;
		return new Vec3(
				entity.getX() + look.x * spawnDistance + handmod * (entity.getBbWidth() / 1.5) * Math.sin(Math.toRadians(-entity.getYHeadRot() - 90)),
				entity.getY() + entity.getEyeHeight() + look.y * spawnDistance,
				entity.getZ() + look.z * spawnDistance + handmod * (entity.getBbWidth() / 1.5) * Math.cos(Math.toRadians(-entity.getYHeadRot() - 90)));
	}

	@Override
	public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
		return slotChanged || !ItemStack.matches(oldStack, newStack);
	}

	@Override
	public int getUseDuration(@NotNull ItemStack pStack, @NotNull LivingEntity entity) {
		return 72000;
	}

	@Override
	public @NotNull UseAnim getUseAnimation(@NotNull ItemStack pStack) {
		return UseAnim.BOW;
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
        Long cooldownTime = stack.get(RegistryManager.COOLDOWN_COMPONENT);
		if (cooldownTime == null || cooldownTime + ConfigManager.CINDER_STAFF_COOLDOWN.get() <= level.getGameTime() || player.hasInfiniteMaterials()) {
			if (EmberInventoryUtil.getEmberTotal(player) >= ConfigManager.CINDER_STAFF_COST.get() || player.hasInfiniteMaterials()) {
				EmberInventoryUtil.removeEmber(player, ConfigManager.CINDER_STAFF_COST.get());
				player.startUsingItem(hand);
				if (level.isClientSide())
					EmbersSounds.playItemSoundClient(player, this, EmbersSounds.CINDER_STAFF_CHARGE.get(), SoundSource.PLAYERS, false, 1.0f, 1.0f);
				else
					EmbersSounds.playItemSound(player, this, EmbersSounds.CINDER_STAFF_CHARGE.get(), SoundSource.PLAYERS, false, 1.0f, 1.0f);
				return InteractionResultHolder.consume(stack);
			} else {
				return InteractionResultHolder.fail(stack);
			}
		}
		return InteractionResultHolder.pass(stack); //OFFHAND FIRE ENABLED BOYS
	}
}