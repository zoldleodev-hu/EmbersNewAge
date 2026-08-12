package hu.zoldleo.embers.blockentity;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.tile.IExtraCapabilityInformation;
import hu.zoldleo.embers.api.upgrades.IUpgradeProvider;
import hu.zoldleo.embers.blockentity.capability_helper.IFluidBlock;
import hu.zoldleo.embers.blockentity.capability_helper.IUpgradeBlock;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.particle.VaporParticleOptions;
import hu.zoldleo.embers.recipe.base.IGaseousFuelRecipe;
import hu.zoldleo.embers.upgrade.WildfireStirlingUpgrade;
import hu.zoldleo.embers.util.EmbersColors;
import hu.zoldleo.embers.util.sound.ISoundController;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

public class WildfireStirlingBlockEntity extends BlockEntity implements ISoundController, IExtraCapabilityInformation, IUpgradeBlock, IFluidBlock {
	public static final int SOUND_OFF = 1;
	public static final int SOUND_ON = 2;
	public static final int[] SOUND_IDS = new int[]{SOUND_OFF,SOUND_ON};

	public int activeTicks = 0;
	public int burnTime = 0;
	public WildfireStirlingUpgrade upgrade;
	public FluidTank tank = new FluidTank(FluidType.BUCKET_VOLUME * 4) {
		@Override
		public void onContentsChanged() {
			WildfireStirlingBlockEntity.this.setChanged();
		}
	};
	private static final Random random = new Random();
	public RecipeHolder<IGaseousFuelRecipe> cachedRecipe = null;

	HashSet<Integer> soundsPlaying = new HashSet<>();

	public WildfireStirlingBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.WILDFIRE_STIRLING_ENTITY.get(), pPos, pBlockState);
		upgrade = new WildfireStirlingUpgrade(this);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		tank.readFromNBT(provider, nbt.getCompound("tank"));
		activeTicks = nbt.getInt("active");
		burnTime = nbt.getInt("burnTime");
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
		nbt.put("tank", tank.writeToNBT(provider, new CompoundTag()));
		nbt.putInt("active", activeTicks);
		nbt.putInt("burnTime", burnTime);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		nbt.put("tank", tank.writeToNBT(provider, new CompoundTag()));
		nbt.putInt("active", activeTicks);
		nbt.putInt("burnTime", burnTime);
		return nbt;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public void setActive(int ticks) {
		activeTicks = Math.max(ticks, activeTicks);
		setChanged();
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, WildfireStirlingBlockEntity blockEntity) {
		blockEntity.activeTicks--;
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, WildfireStirlingBlockEntity blockEntity) {
		blockEntity.handleSound();
		blockEntity.activeTicks--;

		if (blockEntity.activeTicks > 0 && state.hasProperty(BlockStateProperties.FACING)) {
			Direction facing = state.getValue(BlockStateProperties.FACING).getOpposite();
			float frontoffset = -0.6f;
			float yoffset = 0.2f;
			float wideoffset = 0.5f;
			float breadthoffset = 0.4f;
			Vec3 frontOffset = new Vec3(0.5 - facing.getNormal().getX() * frontoffset, 0.5 - facing.getNormal().getY() * frontoffset, 0.5 - facing.getNormal().getZ() * frontoffset);
			Vec3 baseOffset = new Vec3(0.5 - facing.getNormal().getX() * yoffset, 0.5 - facing.getNormal().getY() * yoffset, 0.5 - facing.getNormal().getZ() * yoffset);
			Direction[] planars = switch (facing.getAxis()) {
                case X -> new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH};
                case Y -> new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH};
                case Z -> new Direction[]{Direction.DOWN, Direction.UP, Direction.EAST, Direction.WEST};
            };
            for (Direction planar : planars) {
				BlockState sideState = level.getBlockState(pos.relative(planar));
				if (!sideState.getFaceOcclusionShape(level, pos.relative(planar), planar.getOpposite()).isEmpty())
					continue;
				Direction cross = facing.getClockWise(planar.getAxis());
				float x1 = pos.getX() + (float) baseOffset.x + planar.getNormal().getX() * wideoffset;
				float y1 = pos.getY() + (float) baseOffset.y + planar.getNormal().getY() * wideoffset;
				float z1 = pos.getZ() + (float) baseOffset.z + planar.getNormal().getZ() * wideoffset;
				float x2 = pos.getX() + (float) frontOffset.x + planar.getNormal().getX() * wideoffset + cross.getNormal().getX() * (random.nextFloat()-0.5f) * 2 * breadthoffset;
				float y2 = pos.getY() + (float) frontOffset.y + planar.getNormal().getY() * wideoffset + cross.getNormal().getY() * (random.nextFloat()-0.5f) * 2 * breadthoffset;
				float z2 = pos.getZ() + (float) frontOffset.z + planar.getNormal().getZ() * wideoffset + cross.getNormal().getZ() * (random.nextFloat()-0.5f) * 2 * breadthoffset;
				int lifetime = 24 + random.nextInt(8);
				//float motionx = facing.getNormal().getX() * (1.0f/lifetime) - 0.01f + random.nextFloat() * 0.02f;
				//float motiony = facing.getNormal().getY() * (1.0f/lifetime) - 0.01f + random.nextFloat() * 0.02f;
				//float motionz = facing.getNormal().getZ() * (1.0f/lifetime) - 0.01f + random.nextFloat() * 0.02f;
				float motionx = (x2 - x1) / lifetime;
				float motiony = (y2 - y1) / lifetime;
				float motionz = (z2 - z1) / lifetime;

				level.addParticle(new VaporParticleOptions(EmbersColors.EMBER_ID, new Vec3(motionx, motiony, motionz), lifetime / 16.0f), x1, y1, z1, 0, 0, 0);
			}
			float x = pos.getX() + (float) frontOffset.x;
			float y = pos.getY() + (float) frontOffset.y;
			float z = pos.getZ() + (float) frontOffset.z;
			int lifetime = 16 + random.nextInt(16);
			float motionx = (Math.abs(facing.getNormal().getX()) - 1) * (random.nextFloat()-0.5f) * 2 * wideoffset / lifetime;
			float motiony = (Math.abs(facing.getNormal().getY()) - 1) * (random.nextFloat()-0.5f) * 2 * wideoffset / lifetime;
			float motionz = (Math.abs(facing.getNormal().getZ()) - 1) * (random.nextFloat()-0.5f) * 2 * wideoffset / lifetime;

			level.addParticle(new VaporParticleOptions(EmbersColors.EMBER_ID, new Vec3(motionx, motiony, motionz), lifetime / 16.0f), x, y, z, 0, 0, 0);
		}
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (level instanceof ServerLevel serverLevel)
			serverLevel.getChunkSource().blockChanged(worldPosition);
	}

	@Override
	public void playSound(int id) {
		float soundX = (float) worldPosition.getX() + 0.5f;
		float soundY = (float) worldPosition.getY() + 0.5f;
		float soundZ = (float) worldPosition.getZ() + 0.5f;
		switch (id) {
		case SOUND_ON:
			EmbersSounds.playMachineSound(this, SOUND_ON, EmbersSounds.WILDFIRE_STIRLING_LOOP.get(), SoundSource.BLOCKS, true, 1.0f, 1.0f, soundX, soundY, soundZ);
			level.playLocalSound(soundX, soundY, soundZ, EmbersSounds.WILDFIRE_STIRLING_START.get(), SoundSource.BLOCKS, 1.0f, 1.0f, false);
			break;
		case SOUND_OFF:
			EmbersSounds.playMachineSound(this, SOUND_OFF, EmbersSounds.WILDFIRE_STIRLING_LOOP_READY.get(), SoundSource.BLOCKS, true, 1.0f, 1.0f, soundX, soundY, soundZ);
			break;
		}
		soundsPlaying.add(id);
	}

	@Override
	public void stopSound(int id) {
		if (id == SOUND_ON) {
			level.playLocalSound(worldPosition, EmbersSounds.WILDFIRE_STIRLING_STOP.get(), SoundSource.BLOCKS, 1.0f, 1.0f, false);
		}
		soundsPlaying.remove(id);
	}

	@Override
	public boolean isSoundPlaying(int id) {
		return soundsPlaying.contains(id);
	}

	@Override
	public int[] getSoundIDs() {
		return SOUND_IDS;
	}

	@Override
	public boolean shouldPlaySound(int id) {
		boolean isWorking = activeTicks > 0;
        return switch (id) {
            case SOUND_OFF -> !isWorking && tank.getFluidAmount() > 0;
            case SOUND_ON -> isWorking;
            default -> false;
        };
	}

	@Override
	public float getCurrentVolume(int id, float volume) {
		boolean isWorking = activeTicks > 0;
        return switch (id) {
            case SOUND_OFF -> !isWorking ? 1.0f : 0.0f;
            case SOUND_ON -> isWorking ? 1.0f : 0.0f;
            default -> 0f;
        };
	}

	@Override
	public boolean hasCapabilityDescription(BlockCapability<?, ?> capability) {
		return capability == Capabilities.FluidHandler.BLOCK;
	}

	@Override
	public void addCapabilityDescription(List<Component> strings, BlockCapability<?, ?> capability, Direction facing) {
		if (capability == Capabilities.FluidHandler.BLOCK)
			strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.INPUT, Embers.MODID + ".tooltip.goggles.fluid", Component.translatable(Embers.MODID + ".tooltip.goggles.fluid.steam")));
	}

    @Override
    public IFluidHandler getFluidCapability(Direction side) {
        if (!this.remove && getBlockState().hasProperty(BlockStateProperties.FACING))
            if (side == null || side == getBlockState().getValue(BlockStateProperties.FACING))
                return tank;
        return null;
    }

    @Override
    public IUpgradeProvider getUpgradeCapability(Direction side) {
        if (!this.remove && getBlockState().hasProperty(BlockStateProperties.FACING))
            if (side == null || side.getOpposite() == getBlockState().getValue(BlockStateProperties.FACING))
                return upgrade;
        return null;
    }
}