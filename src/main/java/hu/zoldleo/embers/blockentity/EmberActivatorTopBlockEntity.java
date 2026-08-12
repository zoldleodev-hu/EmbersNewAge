package hu.zoldleo.embers.blockentity;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.capabilities.EmbersCapabilities;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.api.tile.IExtraCapabilityInformation;
import hu.zoldleo.embers.api.tile.IExtraDialInformation;
import hu.zoldleo.embers.blockentity.capability_helper.IEmberBlock;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.particle.GlowParticleOptions;
import hu.zoldleo.embers.power.DefaultEmberCapability;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.NotNull;

public class EmberActivatorTopBlockEntity extends BlockEntity implements ISoundController, IExtraDialInformation, IExtraCapabilityInformation, IEmberBlock {
	public IEmberCapability capability = new DefaultEmberCapability() {
		@Override
		public void onContentsChanged() {
			super.onContentsChanged();
			EmberActivatorTopBlockEntity.this.setChanged();
		}
	};
	static Random random = new Random();

	public static final int SOUND_HAS_EMBER = 1;
	public static final int[] SOUND_IDS = new int[]{SOUND_HAS_EMBER};

	HashSet<Integer> soundsPlaying = new HashSet<>();

	public EmberActivatorTopBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.EMBER_ACTIVATOR_TOP_ENTITY.get(), pPos, pBlockState);
		capability.setEmberCapacity(16000);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		capability.readFromNBT(provider, nbt);
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
		capability.writeToNBT(provider, nbt);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		capability.writeToNBT(provider, nbt);
		return nbt;
	}

    // Sync on block update
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, EmberActivatorTopBlockEntity blockEntity) {
		blockEntity.handleSound();
		if (blockEntity.capability.getEmber() > 0) {
			for (int i = 0; i < Math.ceil(blockEntity.capability.getEmber() / 500.0); i ++) {
				level.addParticle(GlowParticleOptions.EMBER, pos.getX()+0.25f+random.nextFloat()*0.5f, pos.getY()+0.25f+random.nextFloat()*0.5f, pos.getZ()+0.25f+random.nextFloat()*0.5f,
						(Math.random() * 2.0D - 1.0D) * 0.2D, (Math.random() * 2.0D - 1.0D) * 0.2D, (Math.random() * 2.0D - 1.0D) * 0.2D);
			}
		}
	}

	@Override
	public void playSound(int id) {
        if (id == SOUND_HAS_EMBER)
            EmbersSounds.playMachineSound(this, SOUND_HAS_EMBER, EmbersSounds.GENERATOR_LOOP.get(), SoundSource.BLOCKS, true, 1.0f, 1.0f, (float) worldPosition.getX() + 0.5f, (float) worldPosition.getY() + 0.5f, (float) worldPosition.getZ() + 0.5f);
		soundsPlaying.add(id);
	}

	@Override
	public void stopSound(int id) {
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
		return id == SOUND_HAS_EMBER && capability.getEmber() > 0;
	}

	public float getCurrentVolume(int id, float volume) {
		return (float) ((capability.getEmber() + 5000.0f) / (capability.getEmberCapacity() + 5000.0f));
	}

	@Override
	public void addDialInformation(Direction facing, List<Component> information, String dialType) {
		BlockEntity bottom = level.getBlockEntity(worldPosition.below());
		if(bottom instanceof EmberActivatorBottomBlockEntity tile)
			tile.addDialInformation(facing, information, dialType);
	}

	@Override
	public boolean hasCapabilityDescription(BlockCapability<?, ?> capability) {
		return true; // TODO: ???
	}

	@Override
	public void addCapabilityDescription(List<Component> strings, BlockCapability<?, ?> capability, Direction facing) {
		if(capability == EmbersCapabilities.EMBER_CAPABILITY_BLOCK)
			strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.OUTPUT, Embers.MODID + ".tooltip.goggles.ember", null));
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (level instanceof ServerLevel serverLevel)
			serverLevel.getChunkSource().blockChanged(worldPosition);
	}

    @Override
    public IEmberCapability getEmberCapability(Direction side) {
        return this.remove ? null : capability;
    }
}