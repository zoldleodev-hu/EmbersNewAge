package hu.zoldleo.embers.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ReconstructedGolemEntity extends AbstractGolem implements OwnableEntity {
    protected ReconstructedGolemEntity(EntityType<? extends AbstractGolem> entityType, Level level) {
        super(entityType, level);
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
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1)
                .add(Attributes.ATTACK_DAMAGE, 6)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 6.0D);
    }

    @Override
    public @Nullable UUID getOwnerUUID() {
        return null;
    }
}