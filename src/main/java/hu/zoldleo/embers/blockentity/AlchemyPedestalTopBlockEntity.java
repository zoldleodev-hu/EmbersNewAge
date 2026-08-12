package hu.zoldleo.embers.blockentity;

import java.util.HashSet;
import java.util.List;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.tile.IExtraCapabilityInformation;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.recipe.base.IAlchemyRecipe.PedestalContents;
import hu.zoldleo.embers.util.sound.ISoundController;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.items.ItemStackHandler;

public class AlchemyPedestalTopBlockEntity extends AlchemyPedestalBlockEntity implements ISoundController {
	public int active = 0;

	public static final int SOUND_PROCESS = 1;
	public static final int[] SOUND_IDS = new int[]{SOUND_PROCESS};
	HashSet<Integer> soundsPlaying = new HashSet<>();

	public AlchemyPedestalTopBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.ALCHEMY_PEDESTAL_TOP_ENTITY.get(), pPos, pBlockState);
        inventory = new ItemStackHandler(1) {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            protected void onContentsChanged(int slot) {
                AlchemyPedestalTopBlockEntity.this.setChanged();
            }
        };
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, AlchemyPedestalTopBlockEntity blockEntity) {
		blockEntity.handleSound();
		blockEntity.active--;
	}

	public PedestalContents getContents() {
		ItemStack aspectus = ItemStack.EMPTY;
		if (level != null && level.getBlockEntity(worldPosition.below()) instanceof AlchemyPedestalBlockEntity bottom)
            aspectus = bottom.inventory.getStackInSlot(0);
		return new PedestalContents(aspectus, inventory.getStackInSlot(0));
	}

	public boolean isValid() {
		if (level == null || inventory.getStackInSlot(0).isEmpty())
			return false;
		if (level.getBlockEntity(worldPosition.below()) instanceof AlchemyPedestalBlockEntity bottom)
			return !bottom.inventory.getStackInSlot(0).isEmpty();
		return false;
	}

	public boolean isActive() {
		return active > 0;
	}

	public void setActive(int time) {
		active = time;
	}

	@Override
	public void playSound(int id) {
        if (id == SOUND_PROCESS)
            EmbersSounds.playMachineSound(this, SOUND_PROCESS, EmbersSounds.PEDESTAL_LOOP.get(), SoundSource.BLOCKS, true, 0.1f, 1.0f, (float) worldPosition.getX() + 0.5f, (float) worldPosition.getY() + 1.0f, (float) worldPosition.getZ() + 0.5f);
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
		return id == SOUND_PROCESS && isActive();
	}

    @Override
	public void addCapabilityDescription(List<Component> strings, BlockCapability<?, ?> capability, Direction facing) {
		strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.INPUT, Embers.MODID + ".tooltip.goggles.item", null));
	}
}