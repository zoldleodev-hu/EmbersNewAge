package hu.zoldleo.embers.blockentity;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.capabilities.EmbersCapabilities;
import hu.zoldleo.embers.api.event.EmberEvent;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.api.power.IEmberPacketProducer;
import hu.zoldleo.embers.api.power.IEmberPacketReceiver;
import hu.zoldleo.embers.api.tile.IExtraCapabilityInformation;
import hu.zoldleo.embers.api.tile.ISparkable;
import hu.zoldleo.embers.api.tile.IUpgradeable;
import hu.zoldleo.embers.api.upgrades.UpgradeContext;
import hu.zoldleo.embers.api.upgrades.UpgradeUtil;
import hu.zoldleo.embers.blockentity.capability_helper.IEmberBlock;
import hu.zoldleo.embers.damage.DamageEmber;
import hu.zoldleo.embers.datagen.EmbersDamageTypes;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.network.message.MessageBeamCannonFX;
import hu.zoldleo.embers.power.DefaultEmberCapability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class BeamCannonBlockEntity extends BlockEntity implements IUpgradeable, IEmberPacketProducer, IExtraCapabilityInformation, IEmberBlock {
	public IEmberCapability capability = new DefaultEmberCapability() {
		@Override
		public void onContentsChanged() {
			super.onContentsChanged();
			BeamCannonBlockEntity.this.setChanged();
		}
	};
	public static final double PULL_RATE = 2000.0;
	public static final int FIRE_THRESHOLD = AlchemyTabletBlockEntity.SPARK_THRESHOLD;
	public static final float DAMAGE = 25.0f;
	public static final int MAX_DISTANCE = 64;

	public long ticksExisted = 0;
	public boolean lastPowered = false;
	public Random random = new Random();
	public int offset = random.nextInt(40);
	protected List<UpgradeContext> upgrades = new LinkedList<>();

	public BeamCannonBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.BEAM_CANNON_ENTITY.get(), pPos, pBlockState);
		capability.setEmberCapacity(2000);
	}

	public BeamCannonBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
		super(pType, pPos, pBlockState);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		lastPowered = nbt.getBoolean("lastPowered");
		capability.readFromNBT(provider, nbt);
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
		nbt.putBoolean("lastPowered", lastPowered);
        capability.writeToNBT(provider, nbt);
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, BeamCannonBlockEntity blockEntity) {
		blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos, Direction.values());
		UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, BeamCannonBlockEntity blockEntity) {
		blockEntity.ticksExisted ++;
		Direction facing = state.getValue(BlockStateProperties.FACING);
		BlockEntity attachedTile = level.getBlockEntity(pos.relative(facing, -1));
		if (blockEntity.ticksExisted % 5 == 0 && attachedTile != null) {
			IEmberCapability cap = level.getCapability(EmbersCapabilities.EMBER_CAPABILITY_BLOCK, attachedTile.getBlockPos(), facing);
			if (cap != null) {
				if (cap.getEmber() > 0 && blockEntity.capability.getEmber() < blockEntity.capability.getEmberCapacity()){
					double removed = cap.removeAmount(PULL_RATE, true);
					blockEntity.capability.addAmount(removed, true);
				}
			}
		}
		blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos, Direction.values());
		UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
		boolean cancel = UpgradeUtil.doWork(blockEntity, blockEntity.upgrades);
		boolean isPowered = level.hasNeighborSignal(pos);
		boolean redstoneEnabled = UpgradeUtil.getOtherParameter(blockEntity, "redstone_enabled", true, blockEntity.upgrades);
		int threshold = UpgradeUtil.getOtherParameter(blockEntity, "fire_threshold", FIRE_THRESHOLD, blockEntity.upgrades);
		if (!cancel && blockEntity.capability.getEmber() >= threshold && (!redstoneEnabled || (isPowered && !blockEntity.lastPowered))){
			blockEntity.fire(facing);
		}
		blockEntity.lastPowered = isPowered;
	}

	public void fire(Direction facing) {
		Vec3 ray = new Vec3(facing.getNormal().getX(), facing.getNormal().getY(), facing.getNormal().getZ());
		double damage = UpgradeUtil.getOtherParameter(this, "damage", DAMAGE, upgrades);
		boolean doContinue = true;
		int maxDist = UpgradeUtil.getOtherParameter(this, "distance", MAX_DISTANCE, upgrades);
		double impactDist = maxDist;
		BlockPos hitPos = worldPosition;
		for (int i = 0; i < maxDist && doContinue; i++) {
			hitPos = hitPos.relative(facing);
			BlockState state = level.getBlockState(hitPos);
			BlockEntity tile = level.getBlockEntity(hitPos);
			if (sparkTarget(tile)) {
				doContinue = false;
				impactDist = i + 1;
			} else if (tile instanceof IEmberPacketReceiver) {
				IEmberCapability cap = level.getCapability(EmbersCapabilities.EMBER_CAPABILITY_BLOCK, tile.getBlockPos(), null);
				if (cap != null) {
					cap.addAmount(capability.getEmber(), true);
				}
				doContinue = false;
				impactDist = i + 1;
			} else if (!state.getCollisionShape(level, hitPos).isEmpty()) {
				doContinue = false;
				impactDist = i + 0.5;
			}
			if (!doContinue) {
				level.playSound(null, hitPos, EmbersSounds.BEAM_CANNON_HIT.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
			}
		}
		List<Entity> entities = level.getEntities((Entity) null, new AABB(worldPosition.getCenter(), hitPos.getCenter()), EntitySelector.NO_SPECTATORS);
		for (Entity entity : entities) {
			DamageSource damageSource = new DamageEmber(level.registryAccess().registry(Registries.DAMAGE_TYPE).get().getHolderOrThrow(EmbersDamageTypes.EMBER_KEY), worldPosition.getCenter());
			entity.hurt(damageSource, (float)damage);
		}

        if (level instanceof ServerLevel serverLevel)
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, serverLevel.getChunkAt(worldPosition).getPos(), new MessageBeamCannonFX(worldPosition.getX()+0.5,worldPosition.getY()+0.5,worldPosition.getZ()+0.5,ray.x*impactDist,ray.y*impactDist,ray.z*impactDist));

		UpgradeUtil.throwEvent(this, new EmberEvent(this, EmberEvent.EnumType.TRANSFER, this.capability.getEmber()), upgrades);
		this.capability.setEmber(0);
		this.setChanged();

		level.playSound(null, worldPosition, EmbersSounds.BEAM_CANNON_FIRE.get(), SoundSource.BLOCKS, 0.7f, 1.0f);
	}

	public boolean sparkTarget(BlockEntity target) {
		if (target instanceof ISparkable sparkable) {
			sparkable.sparkProgress(this, capability.getEmber());
			return true;
		}
		return false;
	}

	@Override
	public boolean isSideUpgradeSlot(Direction face) {
		return true;
	}

	@Override
	public Vec3 getEmittingDirection(Direction side) {
		if (getBlockState().hasProperty(BlockStateProperties.FACING)) {
			Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
			return new Vec3(facing.getNormal().getX(), facing.getNormal().getY(), facing.getNormal().getZ());
		}
		return null;
	}

	@Override
	public BlockPos getTarget(Direction side) {
		if (getBlockState().hasProperty(BlockStateProperties.FACING)) {
			Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
			if (side != facing)
				return null;
			int maxDist = UpgradeUtil.getOtherParameter(this, "distance", MAX_DISTANCE, upgrades);
			BlockPos hitPos = worldPosition;
			for (int i = 0; i < maxDist; i++) {
				hitPos = hitPos.relative(facing);
				BlockState hitState = level.getBlockState(hitPos);
				BlockEntity tile = level.getBlockEntity(hitPos);
				if (tile instanceof ISparkable || tile instanceof IEmberPacketReceiver || !hitState.getCollisionShape(level, hitPos).isEmpty()) {
					return hitPos;
				}
			}
			return hitPos;
		}
		return null;
	}

	@Override
	public void addOtherDescription(List<Component> strings, Direction facing) {
		strings.add(Component.translatable(Embers.MODID + ".tooltip.goggles.redstone_signal"));
	}

    @Override
    public IEmberCapability getEmberCapability(Direction side) {
        if (!this.remove && getBlockState().getValue(BlockStateProperties.FACING) != side)
            return capability;
        return null;
    }
}