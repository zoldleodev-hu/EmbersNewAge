package hu.zoldleo.embers.entity.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class RandomFlightGoal extends Goal {
    protected final Mob mob;
    protected final double speedModifier;
    protected BlockPos pos;

    public RandomFlightGoal(Mob mob, double speedModifier) {
        setFlags(EnumSet.of(Goal.Flag.MOVE));
        this.mob = mob;
        this.speedModifier = speedModifier;
    }

    @Override
    public boolean canUse() {
        return !mob.hasControllingPassenger();
    }

    protected BlockPos getPosition() {
        return BlockPos.containing(mob.getX() + (double)mob.getRandom().nextInt(7) - (double)mob.getRandom().nextInt(7), mob.getY() + (double)mob.getRandom().nextInt(6) - (double)2.0F, mob.getZ() + (double)mob.getRandom().nextInt(7) - (double)mob.getRandom().nextInt(7));
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (pos != null && (!mob.level().isEmptyBlock(pos) || pos.getY() <= mob.level().getMinBuildHeight())) {
            pos = null;
        }

        if (pos == null || mob.getRandom().nextInt(30) == 0 || pos.closerToCenterThan(mob.position(), 2)) {
            pos = getPosition();
        }

        double d2 = (double)pos.getX() + 0.5 - mob.getX();
        double d0 = (double)pos.getY() + 0.1 - mob.getY();
        double d1 = (double)pos.getZ() + 0.5 - mob.getZ();
        Vec3 vec3 = mob.getDeltaMovement();
        Vec3 vec31 = vec3.add((Math.signum(d2) * 0.5 - vec3.x) * 0.1, (Math.signum(d0) * 0.7 - vec3.y) * 0.1, (Math.signum(d1) * 0.5 - vec3.z) * 0.1);
        mob.setDeltaMovement(vec31.scale(speedModifier));
    }
}