package hu.zoldleo.embers.blockentity;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import hu.zoldleo.embers.api.upgrades.IUpgradeProvider;
import hu.zoldleo.embers.blockentity.capability_helper.IFluidBlock;
import hu.zoldleo.embers.blockentity.capability_helper.IUpgradeBlock;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.tile.IExtraCapabilityInformation;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.particle.VaporParticleOptions;
import hu.zoldleo.embers.recipe.base.IGaseousFuelRecipe;
import hu.zoldleo.embers.upgrade.CatalyticPlugUpgrade;
import hu.zoldleo.embers.util.sound.ISoundController;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class CatalyticPlugBlockEntity extends BlockEntity implements ISoundController, IExtraCapabilityInformation, IUpgradeBlock, IFluidBlock {
	public static final int SOUND_OFF = 1;
	public static final int SOUND_ON = 2;
	public static final int[] SOUND_IDS = new int[]{SOUND_OFF,SOUND_ON};

	int ticksExisted = 0;
	public float renderOffset;
	int previousFluid;
	public int activeTicks = 0;
	public int burnTime = 0;
	public CatalyticPlugUpgrade upgrade;
	public FluidTank tank = new FluidTank(FluidType.BUCKET_VOLUME * 4) {
		@Override
		public void onContentsChanged() {
			CatalyticPlugBlockEntity.this.setChanged();
		}
	};
	private static final Random random = new Random();
	public RecipeHolder<IGaseousFuelRecipe> cachedRecipe = null;

	HashSet<Integer> soundsPlaying = new HashSet<>();

	public CatalyticPlugBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.CATALYTIC_PLUG_ENTITY.get(), pPos, pBlockState);
		upgrade = new CatalyticPlugUpgrade(this);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		tank.readFromNBT(provider, nbt);
		activeTicks = nbt.getInt("active");
		burnTime = nbt.getInt("burnTime");
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
		tank.writeToNBT(provider, nbt);
		nbt.putInt("active", activeTicks);
		nbt.putInt("burnTime", burnTime);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		tank.writeToNBT(provider, nbt);
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

	public static void serverTick(Level level, BlockPos pos, BlockState state, CatalyticPlugBlockEntity blockEntity) {
		blockEntity.activeTicks--;
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, CatalyticPlugBlockEntity blockEntity) {
		blockEntity.ticksExisted++;

		//I know I'm supposed to use onLoad for stuff on the first tick but the tank isn't synced to the client yet when that happens
		if (blockEntity.ticksExisted == 1)
			blockEntity.previousFluid = blockEntity.tank.getFluidAmount();
		if (blockEntity.tank.getFluidAmount() != blockEntity.previousFluid) {
			blockEntity.renderOffset = blockEntity.renderOffset + blockEntity.tank.getFluidAmount() - blockEntity.previousFluid;
			blockEntity.previousFluid = blockEntity.tank.getFluidAmount();
		}
		blockEntity.handleSound();
		blockEntity.activeTicks--;

		if (blockEntity.activeTicks > 0 && state.hasProperty(BlockStateProperties.FACING)) {
			Direction facing = state.getValue(BlockStateProperties.FACING);
			float yoffset = 0.38f;
			float wideoffset = 0.45f;
			Vec3 baseOffset = new Vec3(0.5 - facing.getNormal().getX() * yoffset, 0.5 - facing.getNormal().getY() * yoffset, 0.5 - facing.getNormal().getZ() * yoffset);
			Direction[] planars = switch (facing.getAxis()) {
                case X -> new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH};
                case Y -> new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH};
                case Z -> new Direction[]{Direction.DOWN, Direction.UP, Direction.EAST, Direction.WEST};
            };
            Vector3f color = IClientFluidTypeExtensions.of(blockEntity.tank.getFluid().getFluid().getFluidType()).modifyFogColor(Minecraft.getInstance().gameRenderer.getMainCamera(), 0, (ClientLevel) level, 6, 0, new Vector3f(1, 1, 1));
			for (Direction planar : planars) {
				BlockState sideState = level.getBlockState(pos.relative(planar));
				if (!sideState.getFaceOcclusionShape(level, pos.relative(planar), planar.getOpposite()).isEmpty())
					continue;
				float x = pos.getX() + (float) baseOffset.x + planar.getNormal().getX() * wideoffset;
				float y = pos.getY() + (float) baseOffset.y + planar.getNormal().getY() * wideoffset;
				float z = pos.getZ() + (float) baseOffset.z + planar.getNormal().getZ() * wideoffset;
				float motionx = planar.getNormal().getX() * 0.053f - facing.getNormal().getX() * 0.015f - 0.01f + random.nextFloat() * 0.02f;
				float motiony = planar.getNormal().getY() * 0.053f - facing.getNormal().getY() * 0.015f - 0.01f + random.nextFloat() * 0.02f;
				float motionz = planar.getNormal().getZ() * 0.053f - facing.getNormal().getZ() * 0.015f - 0.01f + random.nextFloat() * 0.02f;

				level.addParticle(new VaporParticleOptions(color, new Vec3(motionx, motiony, motionz), 1.25f), x, y, z, 0, 0, 0);
			}
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
			EmbersSounds.playMachineSound(this, SOUND_ON, EmbersSounds.CATALYTIC_PLUG_LOOP.get(), SoundSource.BLOCKS, true, 1.0f, 1.0f, soundX, soundY, soundZ);
			level.playLocalSound(soundX, soundY, soundZ, EmbersSounds.CATALYTIC_PLUG_START.get(), SoundSource.BLOCKS, 1.0f, 1.0f, false);
			break;
		case SOUND_OFF:
			EmbersSounds.playMachineSound(this, SOUND_OFF, EmbersSounds.CATALYTIC_PLUG_LOOP_READY.get(), SoundSource.BLOCKS, true, 1.0f, 1.0f, soundX, soundY, soundZ);
			break;
		}
		soundsPlaying.add(id);
	}

	@Override
	public void stopSound(int id) {
		if (id == SOUND_ON)
			level.playLocalSound(worldPosition, EmbersSounds.CATALYTIC_PLUG_STOP.get(), SoundSource.BLOCKS, 1.0f, 1.0f, false);
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
    public IUpgradeProvider getUpgradeCapability(Direction side) {
        if (!this.remove && getBlockState().hasProperty(BlockStateProperties.FACING))
            if (side == null || side.getOpposite() == getBlockState().getValue(BlockStateProperties.FACING))
                return upgrade;
        return null;
    }

    @Override
    public IFluidHandler getFluidCapability(Direction side) {
        if (!this.remove && getBlockState().hasProperty(BlockStateProperties.FACING))
            if (side == null || side == getBlockState().getValue(BlockStateProperties.FACING))
                return tank;
        return null;
    }
}
