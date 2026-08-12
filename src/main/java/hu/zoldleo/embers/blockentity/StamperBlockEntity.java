package hu.zoldleo.embers.blockentity;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.event.DialInformationEvent;
import hu.zoldleo.embers.api.event.EmberEvent;
import hu.zoldleo.embers.api.event.MachineRecipeEvent;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.api.tile.IBin;
import hu.zoldleo.embers.api.tile.IExtraCapabilityInformation;
import hu.zoldleo.embers.api.tile.IExtraDialInformation;
import hu.zoldleo.embers.api.tile.IMechanicallyPowered;
import hu.zoldleo.embers.api.tile.IUpgradeable;
import hu.zoldleo.embers.api.upgrades.UpgradeContext;
import hu.zoldleo.embers.api.upgrades.UpgradeUtil;
import hu.zoldleo.embers.blockentity.capability_helper.IEmberBlock;
import hu.zoldleo.embers.blockentity.capability_helper.IInventoryBlock;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.particle.SmokeParticleOptions;
import hu.zoldleo.embers.particle.SparkParticleOptions;
import hu.zoldleo.embers.power.DefaultEmberCapability;
import hu.zoldleo.embers.recipe.base.IStampingRecipe;
import hu.zoldleo.embers.recipe.context.StampingContext;
import hu.zoldleo.embers.util.EmbersColors;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class StamperBlockEntity extends BlockEntity implements IMechanicallyPowered, IExtraDialInformation, IExtraCapabilityInformation, IUpgradeable, IEmberBlock, IInventoryBlock {
	public static final double EMBER_COST = 80.0;
	public static final int STAMP_TIME = 70;
	public static final int RETRACT_TIME = 10;
	public IEmberCapability capability = new DefaultEmberCapability() {
		@Override
		public void onContentsChanged() {
			super.onContentsChanged();
			StamperBlockEntity.this.setChanged();
		}
	};
	public boolean prevPowered = false;
	public boolean powered = false;
	public long ticksExisted = 0;
	public ItemStackHandler stamp = new ItemStackHandler(1) {
		@Override
		protected void onContentsChanged(int slot) {
			StamperBlockEntity.this.setChanged();
		}

		@Override
		public int getSlotLimit(int slot) {
			return 1;
		}
	};
	protected List<UpgradeContext> upgrades = new ArrayList<>();
	public RecipeHolder<IStampingRecipe> cachedRecipe = null;

	public StamperBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.STAMPER_ENTITY.get(), pPos, pBlockState);
		capability.setEmberCapacity(8000);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		capability.readFromNBT(provider, nbt);
		powered = nbt.getBoolean("powered");
		stamp.deserializeNBT(provider, nbt.getCompound("stamp"));
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
		capability.writeToNBT(provider, nbt);
		nbt.putBoolean("powered", powered);
		nbt.put("stamp", stamp.serializeNBT(provider));
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		nbt.putBoolean("powered", powered);
		nbt.put("stamp", stamp.serializeNBT(provider));
		return nbt;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, StamperBlockEntity blockEntity) {
		blockEntity.prevPowered = blockEntity.powered;
		if (level.getBlockState(pos.below(2)).getBlock() == RegistryManager.STAMP_BASE.get()) {
			blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos, Misc.horizontals);
			UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
		}
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, StamperBlockEntity blockEntity) {
		blockEntity.ticksExisted++;
		blockEntity.prevPowered = blockEntity.powered;
		if (level.getBlockState(pos.below(2)).getBlock() == RegistryManager.STAMP_BASE.get()) {
			blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos, Misc.horizontals);
			UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
			if (UpgradeUtil.doTick(blockEntity, blockEntity.upgrades))
				return;

			StampBaseBlockEntity stamp = (StampBaseBlockEntity) level.getBlockEntity(pos.below(2));
			IFluidHandler handler = stamp.getTank();

			StampingContext context = new StampingContext(stamp.inventory, handler, blockEntity.stamp.getStackInSlot(0));
			blockEntity.cachedRecipe = Misc.getRecipe(blockEntity.cachedRecipe, RegistryManager.STAMPING.get(), context, level);

			if (blockEntity.cachedRecipe != null || blockEntity.powered) {
				boolean cancel = UpgradeUtil.doWork(blockEntity, blockEntity.upgrades);
				int stampTime = UpgradeUtil.getWorkTime(blockEntity, STAMP_TIME, blockEntity.upgrades);
				int retractTime = UpgradeUtil.getWorkTime(blockEntity, RETRACT_TIME, blockEntity.upgrades);
				if (!cancel && !blockEntity.powered && blockEntity.ticksExisted >= stampTime) {
					double emberCost = UpgradeUtil.getTotalEmberConsumption(blockEntity, EMBER_COST, blockEntity.upgrades);
					if (blockEntity.capability.getEmber() >= emberCost) {
						List<ItemStack> results = Lists.newArrayList(blockEntity.cachedRecipe.value().getOutput(context).copy());
						UpgradeUtil.transformOutput(blockEntity, results, blockEntity.upgrades);

						BlockEntity outputTile = level.getBlockEntity(pos.below(3));
						if (outputTile instanceof IBin) {
							for (ItemStack remainder : results) {
								remainder = ((IBin) outputTile).getInventory().insertItem(0, remainder, true);
								if (!remainder.isEmpty())
									return;
							}
						}

						UpgradeUtil.throwEvent(blockEntity, new EmberEvent(blockEntity, EmberEvent.EnumType.CONSUME, emberCost), blockEntity.upgrades);
						blockEntity.capability.removeAmount(emberCost, true);
						if (level instanceof ServerLevel serverLevel) {
							serverLevel.sendParticles(new SparkParticleOptions(EmbersColors.EMBER_ID, 1.0f), pos.getX() + 0.5f, pos.getY() - 1.1f, pos.getZ() + 0.5f, 10, 0.25, 0.0, 0.25, 1.0);
							serverLevel.sendParticles(new SmokeParticleOptions(EmbersColors.SMOKE_ID, 3.0f), pos.getX() + 0.5f, pos.getY() - 1.1f, pos.getZ() + 0.5f, 10, 0.25, 0.0, 0.25, 1.0);
						}

						level.playSound(null, pos.below(), EmbersSounds.STAMPER_DOWN.get(), SoundSource.BLOCKS, 1.0f, 1.0f);

						blockEntity.powered = true;
						blockEntity.ticksExisted = 0;

						UpgradeUtil.throwEvent(blockEntity, new MachineRecipeEvent.Success<>(blockEntity, blockEntity.cachedRecipe), blockEntity.upgrades);

						//the recipe is responsible for taking items and fluid from the getCapability
						blockEntity.cachedRecipe.value().assemble(context, level.registryAccess());

						BlockPos middlePos = pos.below();
						for (ItemStack remainder : results) {
							if (outputTile instanceof IBin) {
								((IBin) outputTile).getInventory().insertItem(0, remainder, false);
							} else {
								level.addFreshEntity(new ItemEntity(level, middlePos.getX() + 0.5, middlePos.getY() + 0.5, middlePos.getZ() + 0.5, remainder));
							}
						}
						stamp.setChanged();
					}

					blockEntity.setChanged();
				} else if (!cancel && blockEntity.powered && blockEntity.ticksExisted >= retractTime) {
					blockEntity.retract();
				}
			}
		} else if (blockEntity.powered) {
			blockEntity.retract();
		}
	}

	private void retract() {
		level.playSound(null, worldPosition.below(), EmbersSounds.STAMPER_UP.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
		powered = false;
		ticksExisted = 0;
		setChanged();
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (level instanceof ServerLevel)
			((ServerLevel) level).getChunkSource().blockChanged(worldPosition);
	}

	@Override
	public double getMechanicalSpeed(double power) {
		return Misc.getDiminishedPower(power,20,1.5/20);
	}

	@Override
	public double getNominalSpeed() {
		return 1;
	}

	@Override
	public double getMinimumPower() {
		return 10;
	}

	@Override
	public void addDialInformation(Direction facing, List<Component> information, String dialType) {
		UpgradeUtil.throwEvent(this, new DialInformationEvent(this, information, dialType), upgrades);
	}

	@Override
	public boolean hasCapabilityDescription(BlockCapability<?, ?> capability) {
		return capability == Capabilities.ItemHandler.BLOCK;
	}

	@Override
	public void addCapabilityDescription(List<Component> strings, BlockCapability<?, ?> capability, Direction facing) {
		if (capability == Capabilities.ItemHandler.BLOCK)
			strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.BOTH, Embers.MODID + ".tooltip.goggles.item", Component.translatable(Embers.MODID + ".tooltip.goggles.item.stamp")));
	}

	@Override
	public boolean isSideUpgradeSlot(Direction face) {
		return face.getAxis() != Axis.Y;
	}

    @Override
    public IEmberCapability getEmberCapability(Direction side) {
        return this.remove ? null : capability;
    }

    @Override
    public IItemHandler getInventoryCapability(Direction side) {
        return this.remove ? null : stamp;
    }
}