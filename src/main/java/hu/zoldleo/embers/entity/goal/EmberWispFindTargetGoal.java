package hu.zoldleo.embers.entity.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class EmberWispFindTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    public EmberWispFindTargetGoal(Mob mob, Class<T> targetType, boolean mustSee) {
        super(mob, targetType, mustSee);
    }

    public EmberWispFindTargetGoal(Mob mob, Class<T> targetType, boolean mustSee, Predicate<LivingEntity> targetPredicate) {
        super(mob, targetType, mustSee, targetPredicate);
    }

    public EmberWispFindTargetGoal(Mob mob, Class<T> targetType, boolean mustSee, boolean mustReach) {
        super(mob, targetType, mustSee, mustReach);
    }

    public EmberWispFindTargetGoal(Mob mob, Class<T> targetType, int randomInterval, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(mob, targetType, randomInterval, mustSee, mustReach, targetPredicate);
    }

    @Override
    public boolean canUse() {
        if (randomInterval > 0 && mob.getRandom().nextInt(randomInterval) != 0) {
            return false;
        } else {
            findTarget();
            return true;
        }
    }

    @Override
    protected @NotNull AABB getTargetSearchArea(double targetDistance) {
        return mob.getBoundingBox().inflate(targetDistance, targetDistance, targetDistance);
    }
}