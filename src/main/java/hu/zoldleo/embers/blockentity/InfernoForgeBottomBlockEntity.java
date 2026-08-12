package hu.zoldleo.embers.blockentity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.augment.AugmentUtil;
import hu.zoldleo.embers.api.event.DialInformationEvent;
import hu.zoldleo.embers.api.event.EmberEvent;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.api.tile.IExtraDialInformation;
import hu.zoldleo.embers.api.tile.IUpgradeable;
import hu.zoldleo.embers.api.upgrades.UpgradeContext;
import hu.zoldleo.embers.api.upgrades.UpgradeUtil;
import hu.zoldleo.embers.blockentity.capability_helper.IEmberBlock;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.particle.GlowParticleOptions;
import hu.zoldleo.embers.particle.SmokeParticleOptions;
import hu.zoldleo.embers.particle.SparkParticleOptions;
import hu.zoldleo.embers.power.DefaultEmberCapability;
import hu.zoldleo.embers.recipe.base.IEmberActivationRecipe;
import hu.zoldleo.embers.util.EmbersColors;
import hu.zoldleo.embers.util.Misc;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

public class InfernoForgeBottomBlockEntity extends BlockEntity implements IExtraDialInformation, ISoundController, IUpgradeable, IEmberBlock {
	public static double EMBER_COST = 16.0;
	public static int MAX_LEVEL = 5; // TODO: config
	public static double MAX_CRYSTAL_VALUE = 3600 * 32.0;
	public static double CHANCE_MIDPOINT = 3600 * 4.0;
	public static int PROCESS_TIME = 200;
	public IEmberCapability capability = new DefaultEmberCapability() {
		@Override
		public void onContentsChanged() {
			super.onContentsChanged();
			InfernoForgeBottomBlockEntity.this.setChanged();
		}
	};
	static Random random = new Random();
	public int progress = 0;
	public double emberValue = 0;
	public RecipeHolder<IEmberActivationRecipe> cachedEmberRecipe = null;

	public static final int SOUND_PROCESS = 1;
	public static final int[] SOUND_IDS = new int[]{SOUND_PROCESS};

	HashSet<Integer> soundsPlaying = new HashSet<>();
	protected List<UpgradeContext> upgrades = new ArrayList<>();

	public InfernoForgeBottomBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.INFERNO_FORGE_BOTTOM_ENTITY.get(), pPos, pBlockState);
		capability.setEmberCapacity(32000);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		capability.readFromNBT(provider, nbt);
		progress = nbt.getInt("progress");
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
		capability.writeToNBT(provider, nbt);
		nbt.putInt("progress", progress);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		capability.writeToNBT(provider, nbt);
		nbt.putInt("progress", progress);
		return nbt;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, InfernoForgeBottomBlockEntity blockEntity) {
		blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos, new Direction[]{Direction.DOWN});
		UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
		blockEntity.handleSound();

		if (blockEntity.progress > 0) {
			if (random.nextInt(10) == 0) {
				if (random.nextInt(3) == 0)
					level.addParticle(SparkParticleOptions.EMBER, pos.getX() - 0.5f + 0.125f * (random.nextFloat() - 0.5f), pos.getY() + 1.75f, pos.getZ() - 0.5f + 0.125f * (random.nextFloat() - 0.5f), 0.125f * (random.nextFloat() - 0.5f), 0.125f * (random.nextFloat()), 0.125f * (random.nextFloat() - 0.5f));
				if (random.nextInt(3) == 0)
					level.addParticle(SparkParticleOptions.EMBER, pos.getX() + 1.5f + 0.125f * (random.nextFloat() - 0.5f), pos.getY() + 1.75f, pos.getZ() - 0.5f + 0.125f * (random.nextFloat() - 0.5f), 0.125f * (random.nextFloat() - 0.5f), 0.125f * (random.nextFloat()), 0.125f * (random.nextFloat() - 0.5f));
				if (random.nextInt(3) == 0)
					level.addParticle(SparkParticleOptions.EMBER, pos.getX() + 1.5f + 0.125f * (random.nextFloat() - 0.5f), pos.getY() + 1.75f, pos.getZ() + 1.5f + 0.125f * (random.nextFloat() - 0.5f), 0.125f * (random.nextFloat() - 0.5f), 0.125f * (random.nextFloat()), 0.125f * (random.nextFloat() - 0.5f));
				if (random.nextInt(3) == 0)
					level.addParticle(SparkParticleOptions.EMBER, pos.getX() - 0.5f + 0.125f * (random.nextFloat() - 0.5f), pos.getY() + 1.75f, pos.getZ() + 1.5f + 0.125f * (random.nextFloat() - 0.5f), 0.125f * (random.nextFloat() - 0.5f), 0.125f * (random.nextFloat()), 0.125f * (random.nextFloat() - 0.5f));
			}
			level.addParticle(SmokeParticleOptions.BIG_SMOKE, pos.getX() - 0.3f, pos.getY() + 1.85f, pos.getZ() - 0.3f, 0.025f * (random.nextFloat() - 0.5f), 0.05f * (random.nextFloat() + 1.0f), 0.025f * (random.nextFloat() - 0.5f));
			level.addParticle(SmokeParticleOptions.BIG_SMOKE, pos.getX() + 1.3f, pos.getY() + 1.85f, pos.getZ() - 0.3f, 0.025f * (random.nextFloat() - 0.5f), 0.05f * (random.nextFloat() + 1.0f), 0.025f * (random.nextFloat() - 0.5f));
			level.addParticle(SmokeParticleOptions.BIG_SMOKE, pos.getX() + 1.3f, pos.getY() + 1.85f, pos.getZ() + 1.3f, 0.025f * (random.nextFloat() - 0.5f), 0.05f * (random.nextFloat() + 1.0f), 0.025f * (random.nextFloat() - 0.5f));
			level.addParticle(SmokeParticleOptions.BIG_SMOKE, pos.getX() - 0.3f, pos.getY() + 1.85f, pos.getZ() + 1.3f, 0.025f * (random.nextFloat() - 0.5f), 0.05f * (random.nextFloat() + 1.0f), 0.025f * (random.nextFloat() - 0.5f));
		}
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, InfernoForgeBottomBlockEntity blockEntity) {
		blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos, new Direction[]{Direction.DOWN});
		UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
		if (UpgradeUtil.doTick(blockEntity, blockEntity.upgrades))
			return;

		if (level.getBlockEntity(pos.above()) instanceof InfernoForgeTopBlockEntity hatch && !hatch.open) {
			long openTicks = level.getGameTime() - hatch.lastToggle;
			if (openTicks > 7) {
				blockEntity.updateProgress();
			}
		}

		if (blockEntity.progress <= 0) {
			if (level.getGameTime() % 20 == 1) {
				List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos.getX(), pos.getY() + 0.25, pos.getZ(), pos.getX() + 1.0, pos.getY() + 1.5, pos.getZ() + 1.0));
				for (ItemEntity e : items) {
					e.setExtendedLifetime();
				}
			}
			return;
		}
		boolean cancel = UpgradeUtil.doWork(blockEntity, blockEntity.upgrades);
		double emberCost = UpgradeUtil.getTotalEmberConsumption(blockEntity, EMBER_COST, blockEntity.upgrades);
		if (cancel || blockEntity.capability.getEmber() < emberCost) {
			blockEntity.progress = 0;
			blockEntity.setChanged();
			if (level.getBlockEntity(pos.above()) instanceof InfernoForgeTopBlockEntity hatch) {
				hatch.open = true;
				hatch.lastToggle = level.getGameTime();
				hatch.setChanged();
				level.playSound(null, pos, EmbersSounds.INFERNO_FORGE_OPEN.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
			}
			return;
		}
		UpgradeUtil.throwEvent(blockEntity, new EmberEvent(blockEntity, EmberEvent.EnumType.CONSUME, emberCost), blockEntity.upgrades);
		blockEntity.progress -= (int) UpgradeUtil.getTotalSpeedModifier(blockEntity, blockEntity.upgrades);
		blockEntity.capability.removeAmount(emberCost, true);
		List<ItemEntity> items = blockEntity.getValidItems();
		for (ItemEntity e : items) {
			e.setExtendedLifetime();
			e.setPickUpDelay(20);
		}
		if (blockEntity.progress != 0) {
			return;
		}
		if (items.isEmpty()) {
            blockEntity.setChanged();
			return;
		}
		boolean forgeSuccess = false;
		if (level.getBlockEntity(pos.above()) instanceof InfernoForgeTopBlockEntity hatch) {
			hatch.open = true;
			hatch.lastToggle = level.getGameTime();
			hatch.setChanged();
			level.playSound(null, pos, EmbersSounds.INFERNO_FORGE_OPEN.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
		}
		if (blockEntity.emberValue > 0) {
			for (ItemEntity item : items)
				if (!AugmentUtil.hasHeat(item.getItem())) {
					if (item.getItem().is(Tags.Items.MUSIC_DISCS)) {
						item.setItem(new ItemStack(RegistryManager.MUSIC_DISC_7F_PATTERNS.get()));
						forgeSuccess = true;
					} else
						item.discard();
				} else if (/*blockEntity.emberValue <= MAX_CRYSTAL_VALUE && */Misc.random.nextDouble() < UpgradeUtil.getOtherParameter(blockEntity, "reforge_chance",
						Math.atan(blockEntity.emberValue / (CHANCE_MIDPOINT + 14400 * AugmentUtil.getLevel(item.getItem()))) / (Math.PI / 2.0), blockEntity.upgrades)  //clockwork arcane business
						) {
					ItemStack stack = item.getItem();
					AugmentUtil.setHeat(stack, 0);
					AugmentUtil.setLevel(stack, AugmentUtil.getLevel(stack) + 1);
					item.setItem(stack);
					blockEntity.progress = 0;
					forgeSuccess = true;
				}
		}
		level.playSound(null, pos.above(), forgeSuccess ? EmbersSounds.INFERNO_FORGE_SUCCESS.get() : EmbersSounds.INFERNO_FORGE_FAIL.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
		/*Color flameColor = new Color(255, 64, 16);
		if (!forgeSuccess)
			flameColor = new Color(0, 0, 0);
		if (blockEntity.emberValue > MAX_CRYSTAL_VALUE)
			flameColor = new Color(16, 64, 255);
		Color sparkColor = new Color(255, 64, 16);

		PacketHandler.INSTANCE.sendToAll(new MessageEmberActivationFX(getPos().getX() + 0.5, getPos().getY() + 1.5, getPos().getZ() + 0.5, flameColor, sparkColor));*/
		if (level instanceof ServerLevel serverLevel) {
			if (forgeSuccess)
				serverLevel.sendParticles(new GlowParticleOptions(EmbersColors.EMBER_ID, new Vec3(0, 0.65f, 0), 4.7f), pos.getX() + 0.5f, pos.getY() + 1.5f, pos.getZ() + 0.5f, 80, 0.1, 0.1, 0.1, 1.0);
			serverLevel.sendParticles(SmokeParticleOptions.BIG_SMOKE, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 20, 0.1, 0.1, 0.1, 1.0);
		}
		blockEntity.setChanged();
	}

	public void updateProgress() {
		if (progress != 0) return;
		List<ItemEntity> items = getValidItems();
		if (!items.isEmpty()) {
			progress = PROCESS_TIME;
			level.playSound(null, worldPosition, EmbersSounds.INFERNO_FORGE_START.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
			setChanged();
		}
	}

	private List<ItemEntity> getValidItems() {
		List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(worldPosition.getX(), worldPosition.getY() + 0.25, worldPosition.getZ(), worldPosition.getX() + 1.0, worldPosition.getY() + 1.5, worldPosition.getZ() + 1.0));
		ItemStack pickedItem = ItemStack.EMPTY;
		emberValue = 0;
		for (ItemEntity item : items) {
			final ItemStack stack = item.getItem();
			if (AugmentUtil.hasHeat(stack) || stack.is(Tags.Items.MUSIC_DISCS)) {
				if (pickedItem.isEmpty() && ((AugmentUtil.getLevel(stack) < MAX_LEVEL && AugmentUtil.getHeat(stack) >= AugmentUtil.getMaxHeat(stack)) || stack.is(Tags.Items.MUSIC_DISCS)))
					pickedItem = stack;
				else return Lists.newArrayList();
			} else {
				RecipeInput context = new SingleRecipeInput(stack);
				cachedEmberRecipe = Misc.getRecipe(cachedEmberRecipe, RegistryManager.EMBER_ACTIVATION.get(), context, level);
				if (cachedEmberRecipe != null) {
					emberValue += cachedEmberRecipe.value().getOutput(context) * stack.getCount();
				} else return Lists.newArrayList();
			}
		}
		if (!pickedItem.isEmpty() && emberValue > 0)
			return items;
		return Lists.newArrayList();
	}

	@Override
	public void playSound(int id) {
		if (id == SOUND_PROCESS)
			EmbersSounds.playMachineSound(this, SOUND_PROCESS, EmbersSounds.INFERNO_FORGE_LOOP.get(), SoundSource.BLOCKS, true, 1.0f, 1.0f, (float) worldPosition.getX() + 0.5f, (float) worldPosition.getY() + 0.5f, (float) worldPosition.getZ() + 0.5f);
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
		return id == SOUND_PROCESS && progress > 0;
	}

	@Override
	public void addDialInformation(Direction facing, List<Component> information, String dialType) {
		UpgradeUtil.throwEvent(this, new DialInformationEvent(this, information, dialType), upgrades);
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (level instanceof ServerLevel)
			((ServerLevel) level).getChunkSource().blockChanged(worldPosition);
	}

	@Override
	public boolean isSideUpgradeSlot(Direction face) {
		return face == Direction.DOWN;
	}

    @Override
    public IEmberCapability getEmberCapability(Direction side) {
        return this.remove ? null : capability;
    }
}