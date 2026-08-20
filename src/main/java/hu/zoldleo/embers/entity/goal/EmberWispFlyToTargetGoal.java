package hu.zoldleo.embers.entity.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class EmberWispFlyToTargetGoal extends Goal {
    protected final Mob mob;
    protected final double speedModifier;

    public EmberWispFlyToTargetGoal(Mob mob, double speedModifier) {
        setFlags(EnumSet.of(Goal.Flag.MOVE));
        this.mob = mob;
        this.speedModifier = speedModifier;
    }

    @Override
    public boolean canUse() {
        return mob.getTarget() != null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null)
            return;
        Vec3 curPos = mob.position();
        Vec3 targetPos = mob.getTarget().position();
        mob.addDeltaMovement(targetPos.subtract(curPos).normalize().scale(0.05 * speedModifier));
    }
}