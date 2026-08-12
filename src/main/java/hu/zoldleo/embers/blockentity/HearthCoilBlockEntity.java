package hu.zoldleo.embers.blockentity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import hu.zoldleo.embers.blockentity.capability_helper.IEmberBlock;
import hu.zoldleo.embers.blockentity.capability_helper.IInventoryBlock;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import com.google.common.collect.Lists;
import hu.zoldleo.embers.ConfigManager;
import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.event.DialInformationEvent;
import hu.zoldleo.embers.api.event.EmberEvent;
import hu.zoldleo.embers.api.event.HeatCoilVisualEvent;
import hu.zoldleo.embers.api.event.MachineRecipeEvent;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.api.tile.IExtraCapabilityInformation;
import hu.zoldleo.embers.api.tile.IExtraDialInformation;
import hu.zoldleo.embers.api.tile.IUpgradeable;
import hu.zoldleo.embers.api.upgrades.UpgradeContext;
import hu.zoldleo.embers.api.upgrades.UpgradeUtil;
import hu.zoldleo.embers.block.EmberDialBlock;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.particle.GlowParticleOptions;
import hu.zoldleo.embers.particle.SmokeParticleOptions;
import hu.zoldleo.embers.power.DefaultEmberCapability;
import hu.zoldleo.embers.util.DecimalFormats;
import hu.zoldleo.embers.util.EmbersColors;
import hu.zoldleo.embers.util.Misc;
import hu.zoldleo.embers.util.sound.ISoundController;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class HearthCoilBlockEntity extends BlockEntity implements ISoundController, IExtraDialInformation, IExtraCapabilityInformation, IUpgradeable, IEmberBlock, IInventoryBlock {
	public IEmberCapability capability = new DefaultEmberCapability() {
		@Override
		public void onContentsChanged() {
			super.onContentsChanged();
			HearthCoilBlockEntity.this.setChanged();
		}
	};
	public ItemStackHandler inventory = new ItemStackHandler(1) {
		@Override
		public void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			HearthCoilBlockEntity.this.setChanged();
		}
	};
	protected static Random random = new Random();
	protected int progress = 0;
	public double heat = 0;
	protected int ticksExisted = 0;

	public static final int SOUND_LOW_LOOP = 1;
	public static final int SOUND_MID_LOOP = 2;
	public static final int SOUND_HIGH_LOOP = 3;
	public static final int SOUND_PROCESS = 4;
	public static final int[] SOUND_IDS = new int[]{SOUND_LOW_LOOP, SOUND_MID_LOOP, SOUND_HIGH_LOOP, SOUND_PROCESS};

	HashSet<Integer> soundsPlaying = new HashSet<>();
	boolean isWorking;
	protected List<UpgradeContext> upgrades;
	public HashMap<RecipeType<?>, RecipeHolder<AbstractCookingRecipe>> cachedRecipes = new HashMap<>();

	public HearthCoilBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.HEARTH_COIL_ENTITY.get(), pPos, pBlockState);
		capability.setEmberCapacity(8000);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		capability.readFromNBT(provider, nbt);
		if (nbt.contains("Inventory"))
			inventory.deserializeNBT(provider, nbt.getCompound("Inventory"));
		if (nbt.contains("progress"))
			progress = nbt.getInt("progress");
		heat = nbt.getDouble("heat");
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
		capability.writeToNBT(provider, nbt);
		nbt.put("Inventory", inventory.serializeNBT(provider));
		nbt.putInt("progress", progress);
		nbt.putDouble("heat", heat);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		nbt.putDouble("heat", heat);
		return nbt;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (level instanceof ServerLevel serverLevel)
			serverLevel.getChunkSource().blockChanged(worldPosition);
	}

	public static <T extends AbstractCookingRecipe> void serverTick(Level level, BlockPos pos, BlockState state, HearthCoilBlockEntity blockEntity) {
		blockEntity.ticksExisted++;

		blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos, new Direction[]{Direction.DOWN});
		UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
		if (UpgradeUtil.doTick(blockEntity, blockEntity.upgrades))
			return;

		double emberCost = UpgradeUtil.getTotalEmberConsumption(blockEntity, ConfigManager.HEARTH_COIL_EMBER_COST.get(), blockEntity.upgrades);
		double prevHeat = blockEntity.heat;
		Boolean cancel = null;
		if (blockEntity.capability.getEmber() >= emberCost) {
			cancel = UpgradeUtil.doWork(blockEntity, blockEntity.upgrades);
			if (!cancel) {
				UpgradeUtil.throwEvent(blockEntity, new EmberEvent(blockEntity, EmberEvent.EnumType.CONSUME, emberCost), blockEntity.upgrades);
				blockEntity.capability.removeAmount(emberCost, true);
				if (blockEntity.ticksExisted % 20 == 0) {
					blockEntity.heat += UpgradeUtil.getOtherParameter(blockEntity, "heating_speed", (double) ConfigManager.HEARTH_COIL_HEATING_SPEED.get(), blockEntity.upgrades);
				}
			} else {
				if (blockEntity.ticksExisted % 20 == 0) {
					blockEntity.heat -= UpgradeUtil.getOtherParameter(blockEntity, "cooling_speed", (double) ConfigManager.HEARTH_COIL_COOLING_SPEED.get(), blockEntity.upgrades);
				}
			}
		} else {
			if (blockEntity.ticksExisted % 20 == 0) {
				blockEntity.heat -= UpgradeUtil.getOtherParameter(blockEntity, "cooling_speed", (double) ConfigManager.HEARTH_COIL_COOLING_SPEED.get(), blockEntity.upgrades);
			}
		}
		double maxHeat = UpgradeUtil.getOtherParameter(blockEntity, "max_heat", (double) ConfigManager.HEARTH_COIL_MAX_HEAT.get(), blockEntity.upgrades);
		blockEntity.heat = Mth.clamp(blockEntity.heat, 0, maxHeat);
		blockEntity.isWorking = false;
		if (blockEntity.heat != prevHeat)
			blockEntity.setChanged();

		int cookTime = UpgradeUtil.getWorkTime(blockEntity, (int)Math.ceil(Mth.lerp(ConfigManager.HEARTH_COIL_MIN_COOK_TIME.get(), ConfigManager.HEARTH_COIL_MAX_COOK_TIME.get(), 1.0 - (blockEntity.heat / ConfigManager.HEARTH_COIL_MAX_HEAT.get()))), blockEntity.upgrades);
		if (cookTime < 1)
			cookTime = 1;
		if (blockEntity.heat > 0 && blockEntity.ticksExisted % cookTime == 0) {
			if (cancel == null)
				cancel = UpgradeUtil.doWork(blockEntity, blockEntity.upgrades);
			if (!cancel) {
				List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos.getX()-1, pos.getY(), pos.getZ()-1, pos.getX()+2, pos.getY()+2, pos.getZ()+2));
				for (ItemEntity item : items) {
					item.setUnlimitedLifetime();
					item.lifespan = 10800;
				}
				if (!items.isEmpty()) {
					int i = random.nextInt(items.size());
					ItemEntity entityItem = items.get(i);
					SingleRecipeInput wrapper = new SingleRecipeInput(entityItem.getItem());
					RecipeType<?> type = UpgradeUtil.getOtherParameter(blockEntity, "recipe_type", (RecipeType<?>) RecipeType.SMELTING, blockEntity.upgrades);

                    blockEntity.cachedRecipes.put(type, Misc.getRecipe(blockEntity.cachedRecipes.getOrDefault(type, null), type, wrapper, level));

					if (blockEntity.cachedRecipes.get(type) != null) {
						ArrayList<ItemStack> returns = Lists.newArrayList(blockEntity.cachedRecipes.get(type).value().assemble(wrapper, level.registryAccess()));
						//int inputCount = recipe.getInputConsumed();
						UpgradeUtil.throwEvent(blockEntity, new MachineRecipeEvent.Success<>(blockEntity, blockEntity.cachedRecipes.get(type)), blockEntity.upgrades);
						UpgradeUtil.transformOutput(blockEntity, returns, blockEntity.upgrades);
						depleteItem(entityItem, 1);
						for(ItemStack stack : returns) {
							ItemStack remainder = blockEntity.inventory.insertItem(0, stack, false);
							if (!remainder.isEmpty())
								level.addFreshEntity(new ItemEntity(level, entityItem.getX(), entityItem.getY(), entityItem.getZ(), remainder));
						}
					}
				}
			}
		}
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, HearthCoilBlockEntity blockEntity) {
		blockEntity.handleSound();

		blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos, new Direction[]{Direction.DOWN});
		UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
		if (UpgradeUtil.doTick(blockEntity, blockEntity.upgrades))
			return;

		if (blockEntity.heat > 0) {
			int particleCount = (int) ((1 + random.nextInt(2)) * (1 + (float) Math.sqrt(blockEntity.heat)));
			HeatCoilVisualEvent event = new HeatCoilVisualEvent(blockEntity, EmbersColors.EMBER, particleCount, 0);
			UpgradeUtil.throwEvent(blockEntity, event, blockEntity.upgrades);
			Vector3f color = event.getColor();
			GlowParticleOptions options = new GlowParticleOptions(color, 2.0F);
			for (int i = 0; i < event.getParticles(); i ++) {
				level.addParticle(options, pos.getX()-0.2f+random.nextFloat()*1.4f, pos.getY()+1.275f, pos.getZ()-0.2f+random.nextFloat()*1.4f,
						(Math.random() * 2.0D - 1.0D) * 0.2D, random.nextFloat() * event.getVerticalSpeed(), (Math.random() * 2.0D - 1.0D) * 0.2D);
			}
		}
	}

	/*private HeatCoilRecipe getRecipe(ItemEntity entityItem) {
		HeatCoilRecipe recipe = RecipeRegistry.getHeatCoilRecipe(entityItem.getItem());
		MachineRecipeEvent<HeatCoilRecipe> event = new MachineRecipeEvent<>(this, recipe);
		UpgradeUtil.throwEvent(this, event,upgrades);
		return event.getRecipe();
	}*/

	public static void depleteItem(ItemEntity entityItem, int inputCount) {
		ItemStack stack = entityItem.getItem();
		stack.shrink(inputCount);
		entityItem.setItem(stack);
		((ServerLevel) entityItem.level()).sendParticles(new SmokeParticleOptions(EmbersColors.SMOKE_ID, 5.0f), entityItem.getX(), entityItem.getY(), entityItem.getZ(), 2, 0.07, 0.07, 0.07, 1.0);
		((ServerLevel) entityItem.level()).sendParticles(new SmokeParticleOptions(EmbersColors.SMOKE_ID, 2.0f), entityItem.getX(), entityItem.getY(), entityItem.getZ(), 3, 0.07, 0.07, 0.07, 1.0);
		if (stack.isEmpty()) {
			entityItem.discard();
		}
	}

	@Override
	public void playSound(int id) {
		float soundX = (float) worldPosition.getX() + 0.5f;
		float soundY = (float) worldPosition.getY() - 0.5f;
		float soundZ = (float) worldPosition.getZ() + 0.5f;
		switch (id) {
		case SOUND_LOW_LOOP:
			EmbersSounds.playMachineSound(this, SOUND_LOW_LOOP, EmbersSounds.HEATCOIL_LOW.get(), SoundSource.BLOCKS, true, 1.0f, 1.0f, soundX, soundY, soundZ);
			break;
		case SOUND_MID_LOOP:
			EmbersSounds.playMachineSound(this, SOUND_MID_LOOP, EmbersSounds.HEATCOIL_MID.get(), SoundSource.BLOCKS, true, 1.0f, 1.0f, soundX, soundY, soundZ);
			break;
		case SOUND_HIGH_LOOP:
			EmbersSounds.playMachineSound(this, SOUND_HIGH_LOOP, EmbersSounds.HEATCOIL_HIGH.get(), SoundSource.BLOCKS, true, 1.0f, 1.0f, soundX, soundY, soundZ);
			break;
		case SOUND_PROCESS:
			EmbersSounds.playMachineSound(this, SOUND_PROCESS, EmbersSounds.HEATCOIL_COOK.get(), SoundSource.BLOCKS, true, 1.0f, 1.0f, soundX, soundY, soundZ);
			break;
		}
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
		double heatRatio = heat / ConfigManager.HEARTH_COIL_MAX_HEAT.get();
		float highVolume = (float)Mth.clampedLerp(0,1,(heatRatio -0.75) * 4);
		float midVolume = (float)Mth.clampedLerp(0,1,(heatRatio -0.25) * 4) - highVolume;
		float lowVolume = (float)Mth.clampedLerp(0,1, heatRatio * 10) - midVolume;

        return switch (id) {
            case SOUND_LOW_LOOP -> lowVolume > 0;
            case SOUND_MID_LOOP -> midVolume > 0;
            case SOUND_HIGH_LOOP -> highVolume > 0;
            default -> false;
        };
	}

	@Override
	public float getCurrentVolume(int id, float volume) {
		double heatRatio = heat / ConfigManager.HEARTH_COIL_MAX_HEAT.get();
		float highVolume = (float)Mth.clampedLerp(0,1,(heatRatio -0.75) * 4);
		float midVolume = (float)Mth.clampedLerp(0,1,(heatRatio -0.25) * 4) - highVolume;
		float lowVolume = (float)Mth.clampedLerp(0,1, heatRatio * 10) - midVolume;

        return switch (id) {
            case SOUND_LOW_LOOP -> lowVolume;
            case SOUND_MID_LOOP -> midVolume;
            case SOUND_HIGH_LOOP -> highVolume;
            default -> 0.0f;
        };
	}

	@Override
	public void addDialInformation(Direction facing, List<Component> information, String dialType) {
		if (EmberDialBlock.DIAL_TYPE.equals(dialType)) {
			DecimalFormat heatFormat = DecimalFormats.getDecimalFormat(Embers.MODID + ".decimal_format.heat");
			double maxHeat = UpgradeUtil.getOtherParameter(this, "max_heat", (double) ConfigManager.HEARTH_COIL_MAX_HEAT.get(), upgrades);
			double heat = Mth.clamp(this.heat, 0, maxHeat);
			information.add(Component.translatable(Embers.MODID + ".tooltip.dial.heat", heatFormat.format(heat), heatFormat.format(maxHeat)));
		}
		UpgradeUtil.throwEvent(this, new DialInformationEvent(this, information, dialType), upgrades);
	}

	@Override
	public boolean hasCapabilityDescription(BlockCapability<?, ?> capability) {
		return capability == Capabilities.ItemHandler.BLOCK;
	}

	@Override
	public void addCapabilityDescription(List<Component> strings, BlockCapability<?, ?> capability, Direction facing) {
		if (capability == Capabilities.ItemHandler.BLOCK)
			strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.OUTPUT, Embers.MODID + ".tooltip.goggles.item", null));
	}

	@Override
	public boolean isSideUpgradeSlot(Direction face) {
		return face == Direction.DOWN;
	}

    @Override
    public IEmberCapability getEmberCapability(Direction side) {
        if (!this.remove && (side == null || side == Direction.DOWN))
            return capability;
        return null;
    }

    @Override
    public IItemHandler getInventoryCapability(Direction side) {
        if (!this.remove && (side == null || side == Direction.DOWN))
            return inventory;
        return null;
    }
}
