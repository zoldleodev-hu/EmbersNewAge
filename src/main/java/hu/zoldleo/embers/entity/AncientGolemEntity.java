package hu.zoldleo.embers.entity;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.projectile.EffectDamage;
import hu.zoldleo.embers.damage.DamageEmber;
import hu.zoldleo.embers.datagen.EmbersDamageTypes;
import hu.zoldleo.embers.datagen.EmbersSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class AncientGolemEntity extends Monster {
	public AncientGolemEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
		xpReward = 10;
	}

	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		addBehaviourGoals();
	}

	protected void addBehaviourGoals() {
		goalSelector.addGoal(2, new MeleeAttackGoal(this, 0.46D, false));
		goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.46D));
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.FOLLOW_RANGE, 32.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.5D)
				.add(Attributes.ATTACK_DAMAGE, 6.0D)
				.add(Attributes.MAX_HEALTH, 30.0D)
				.add(Attributes.ARMOR, 6.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
	}

	public void tick() {
		super.tick();
		//this.yBodyRot = this.yHeadRot;
		if (!isRemoved() && getHealth() > 0 && tickCount % 100 == 0 && getTarget() != null) {
			if (!level().isClientSide()) {
				playSound(EmbersSounds.FIREBALL.get(), 1.0f, 1.0f);
				EmberProjectileEntity proj = RegistryManager.EMBER_PROJECTILE.get().create(level());
                if (proj == null)
                    return;

				DamageSource damage = new DamageEmber(level().registryAccess().registry(Registries.DAMAGE_TYPE).orElseThrow().getHolderOrThrow(EmbersDamageTypes.EMBER_KEY), proj, this);
				EffectDamage effect = new EffectDamage(4.0f, e -> damage, 1, 1.0f);

				Vec3 lookVec = getLookAngle();
				proj.shoot(lookVec.x, lookVec.y, lookVec.z, 0.5f, 0.0f, 4.0f);
				proj.setPos(getEyePosition());
				proj.setEffect(effect);

				level().addFreshEntity(proj);
			}
		}
	}

	public boolean doHurtTarget(@NotNull Entity pEntity) {
		if (super.doHurtTarget(pEntity)) {
			playSound(EmbersSounds.ANCIENT_GOLEM_PUNCH.get());
			return true;
		}
		return false;
	}

	protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
		return EmbersSounds.ANCIENT_GOLEM_HURT.get();
	}

	protected @NotNull SoundEvent getDeathSound() {
		return EmbersSounds.ANCIENT_GOLEM_DEATH.get();
	}

	protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState state) {
		super.playStepSound(pos, state);
		playSound(EmbersSounds.ANCIENT_GOLEM_STEP.get());
	}
}