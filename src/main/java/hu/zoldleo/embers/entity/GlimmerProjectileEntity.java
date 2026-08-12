package hu.zoldleo.embers.entity;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.block.GlimmerBlock;
import hu.zoldleo.embers.particle.GlowParticleOptions;
import hu.zoldleo.embers.particle.SmokeParticleOptions;
import hu.zoldleo.embers.particle.SparkParticleOptions;
import hu.zoldleo.embers.util.EmbersColors;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;

public class GlimmerProjectileEntity extends Projectile {
	public static final GlowParticleOptions EMBER = new GlowParticleOptions(EmbersColors.EMBER_ID, new Vec3(0.0, 0.000001, 0.0), 3.0F, 120);
	public static final SparkParticleOptions GLIMMER = new SparkParticleOptions(EmbersColors.GLIMMER_PROJECTILE_ID, 1.5F);
	public static final SmokeParticleOptions SMOKE = new SmokeParticleOptions(EmbersColors.SMOKE_ID, 6.0F);
	public static final EntityDataAccessor<Integer> lifetime = SynchedEntityData.defineId(GlimmerProjectileEntity.class, EntityDataSerializers.INT);

	public GlimmerProjectileEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
		this.noPhysics = true;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(lifetime, 160);
	}

	public void shootFromRotation(@NotNull Entity shooter, float x, float y, float z, float velocity, float inaccuracy) {
		this.setOwner(shooter);
		super.shoot(x, y, z, velocity, inaccuracy);
	}

	public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
		super.shoot(x, y, z, velocity, inaccuracy);
	}

	public void tick() {
		super.tick();
		int lifetime = getEntityData().get(GlimmerProjectileEntity.lifetime);
		getEntityData().set(GlimmerProjectileEntity.lifetime, lifetime - 1);
		if (lifetime <= 0) {
			this.remove(RemovalReason.DISCARDED);
		}

		Vec3 oldPosition = new Vec3(getX(), getY(), getZ());
		Vec3 newPosVector = oldPosition.add(getDeltaMovement());
		BlockHitResult raytraceresult = level().clip(new ClipContext(oldPosition, newPosVector.add(getDeltaMovement().normalize().scale(1.5)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

		if (raytraceresult.getType() != HitResult.Type.MISS)
			newPosVector = raytraceresult.getLocation();

		move(MoverType.SELF, newPosVector.subtract(oldPosition));

		setDeltaMovement(getDeltaMovement().add(0, -0.05f, 0));

		if (!level().isClientSide() && raytraceresult.getType() == HitResult.Type.BLOCK && !EventHooks.onProjectileImpact(this, raytraceresult)) {
			this.onHit(raytraceresult);
		}

		if (level().isClientSide()) {
			double deltaX = getX() - oldPosition.x;
			double deltaY = getY() - oldPosition.y;
			double deltaZ = getZ() - oldPosition.z;
			for (double i = 0; i < 9; i ++) {
				double coeff = i / 9.0;
				level().addParticle(GLIMMER, oldPosition.x + deltaX * coeff, oldPosition.y + deltaY * coeff, oldPosition.z + deltaZ * coeff, 01.1f*(Misc.random.nextFloat()-0.5f), 01.1f*(Misc.random.nextFloat()-0.5f), 01.1f*(Misc.random.nextFloat()-0.5f));
			}
		}
	}

	public void onHitBlock(@NotNull BlockHitResult raytraceresult) {
		super.onHitBlock(raytraceresult);
		Direction side = raytraceresult.getDirection();
		BlockPos hitPos = raytraceresult.getBlockPos().relative(side);
		BlockPlaceContext context = new BlockPlaceContext(level(), (getOwner() instanceof Player) ? (Player) getOwner() : null, InteractionHand.MAIN_HAND, ItemStack.EMPTY, raytraceresult);

		if (level().getBlockState(hitPos).canBeReplaced(context)) {
			level().setBlock(hitPos, RegistryManager.GLIMMER.get().getStateForPlacement(context), 11);

			if (level() instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(GlimmerBlock.GLIMMER, hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5, 1, 0, 0, 0, 0.0);
				serverLevel.sendParticles(EMBER, hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5, 1, 0, 0.001, 0, 0.0);
			}
		} else {
			if (level() instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(SMOKE, getX(), getY(), getZ(), 6, 0, 0, 0, 0.0);
			}
		}
		this.remove(RemovalReason.DISCARDED);
	}

	/*/@Override // same as super
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this);
	}*/
}