package hu.zoldleo.embers.entity;

import hu.zoldleo.embers.datagen.EmbersDamageTypes;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.entity.goal.EmberWispFindTargetGoal;
import hu.zoldleo.embers.entity.goal.EmberWispFlyToTargetGoal;
import hu.zoldleo.embers.entity.goal.RandomFlightGoal;
import hu.zoldleo.embers.particle.GlowParticleOptions;
import hu.zoldleo.embers.util.EmberInventoryUtil;
import hu.zoldleo.embers.util.EmbersColors;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class EmberWispEntity extends Mob implements Enemy {
    public static final EntityDataAccessor<Float> value = SynchedEntityData.defineId(EmberWispEntity.class, EntityDataSerializers.FLOAT);

    public EmberWispEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(7, new RandomFlightGoal(this, 1));
        goalSelector.addGoal(2, new EmberWispFlyToTargetGoal(this, 1));
        goalSelector.addGoal(2, new EmberWispFindTargetGoal<>(this, LivingEntity.class, true, this::canAttack));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(value, 800 + getRandom().nextFloat() * 200);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.FOLLOW_RANGE, 32)
                .add(Attributes.MAX_HEALTH, 1)
                .add(Attributes.GRAVITY, 0);
    }

    public void tick() {
        super.tick();
        float ember = getEntityData().get(value);
        getEntityData().set(value, ember - 0.2f); // TODO: tweak / config

        if (ember <= 0)
            discard();
        if (isRemoved())
            return;

        Vec3 oldPosition = new Vec3(getX(), getY(), getZ());

        move(MoverType.SELF, getDeltaMovement());

        if (level().isClientSide()) {
            double deltaX = getX() - oldPosition.x;
            double deltaY = getY() - oldPosition.y;
            double deltaZ = getZ() - oldPosition.z;
            double dist = Math.ceil(Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) * 20);
            for (double i = 0; i < dist; i++) {
                double coeff = i / dist;
                level().addAlwaysVisibleParticle(new GlowParticleOptions(EmbersColors.EMBER_ID, 2f * (ember / 500f)), oldPosition.x + deltaX * coeff, oldPosition.y + deltaY * coeff, oldPosition.z + deltaZ * coeff, 0.125f*(random.nextFloat()-0.5f), 0.125f*(random.nextFloat()-0.5f), 0.125f*(random.nextFloat()-0.5f));
            }
        }
    }

    public boolean canAttack(@NotNull LivingEntity target) {
        return super.canAttack(target) &&
                isEffectiveAi() &&
                target.canBeHitByProjectile() &&
                EmberInventoryUtil.getEmberCapacityTotal(target) - EmberInventoryUtil.getEmberTotal(target) > getEntityData().get(value);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, @NotNull BlockState state, @NotNull BlockPos pos) {

    }

    public static boolean checkMonsterSpawnRules(EntityType<? extends Mob> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && (MobSpawnType.ignoresLightRequirements(spawnType) || Monster.isDarkEnoughToSpawn(level, pos, random)) && checkMobSpawnRules(type, level, spawnType, pos, random);
    }

    public void push(@NotNull Entity entity) {
        super.push(entity);
        if (entity instanceof LivingEntity target && canAttack(target))
            dealDamage(target);
    }

    protected void dealDamage(LivingEntity livingEntity) {
        if (isAlive() && isWithinMeleeAttackRange(livingEntity) && hasLineOfSight(livingEntity)) {
            DamageSource damagesource = damageSources().source(EmbersDamageTypes.EMBER_KEY, this);
            float ember = getEmber();
            if (livingEntity.hurt(damagesource, getAttackDamage(ember))) {
                playSound(EmbersSounds.EMBER_RECEIVE_BIG.get(), 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                if (level() instanceof ServerLevel serverlevel)
                    EnchantmentHelper.doPostAttackEffects(serverlevel, livingEntity, damagesource);
                EmberInventoryUtil.addEmber(livingEntity, ember * 0.001); // TODO: tweak / config
                discard();
            }
        }
    }

    protected float getAttackDamage(float ember) {
        return ember * 0.01f; // TODO: tweak / config
    }

    protected float getEmber() {
        return getEntityData().get(value);
    }

    protected void setEmber(float ember) {
        getEntityData().set(value, ember);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        setEmber(nbt.getFloat("value"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putFloat("value", getEmber());
    }
}