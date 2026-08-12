package hu.zoldleo.embers.blockentity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.event.DialInformationEvent;
import hu.zoldleo.embers.api.event.EmberEvent;
import hu.zoldleo.embers.api.tile.IExtraCapabilityInformation;
import hu.zoldleo.embers.api.tile.IExtraDialInformation;
import hu.zoldleo.embers.api.tile.IUpgradeable;
import hu.zoldleo.embers.api.upgrades.UpgradeContext;
import hu.zoldleo.embers.api.upgrades.UpgradeUtil;
import hu.zoldleo.embers.block.EmberDialBlock;
import hu.zoldleo.embers.blockentity.capability_helper.IFluidBlock;
import hu.zoldleo.embers.blockentity.capability_helper.IInventoryBlock;
import hu.zoldleo.embers.datagen.EmbersBlockTags;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.particle.GlowParticleOptions;
import hu.zoldleo.embers.particle.SmokeParticleOptions;
import hu.zoldleo.embers.recipe.context.BlockStateContext;
import hu.zoldleo.embers.recipe.base.IEmberActivationRecipe;
import hu.zoldleo.embers.recipe.base.IMetalCoefficientRecipe;
import hu.zoldleo.embers.util.DecimalFormats;
import hu.zoldleo.embers.util.EmbersColors;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;

public class PressureRefineryBottomBlockEntity extends BlockEntity implements IExtraDialInformation, IExtraCapabilityInformation, IUpgradeable, IFluidBlock, IInventoryBlock {
	public static final float BASE_MULTIPLIER = 1.25f;
	public static final int FLUID_CONSUMED = 25;
	public static final float PER_BLOCK_MULTIPLIER = 0.25f;
	public static final int PROCESS_TIME = 20;
	public static int capacity = FluidType.BUCKET_VOLUME * 8;
    protected FluidTank tank;
	int progress = -1;
	public ItemStackHandler inventory = new ItemStackHandler(1) {
		@Override
		protected void onContentsChanged(int slot) {
			PressureRefineryBottomBlockEntity.this.setChanged();
		}

		@Override
		public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
			if (Misc.getRecipe(cachedRecipe, RegistryManager.EMBER_ACTIVATION.get(), new SingleRecipeInput(stack), level) != null) {
				return super.insertItem(slot, stack, simulate);
			}
			return stack;
		}
	};
	protected List<UpgradeContext> upgrades = new ArrayList<>();
	public RecipeHolder<IEmberActivationRecipe> cachedRecipe = null;
	public RecipeHolder<IMetalCoefficientRecipe> cachedCoefficient = null;

	public PressureRefineryBottomBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.PRESSURE_REFINERY_BOTTOM_ENTITY.get(), pPos, pBlockState);
		tank = new FluidTank(capacity) {
			@Override
			protected void onContentsChanged() {
				PressureRefineryBottomBlockEntity.this.setChanged();
			}
		};
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
        tank.readFromNBT(provider, nbt);
		inventory.deserializeNBT(provider, nbt.getCompound("Inventory"));
		progress = nbt.getInt("progress");
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
        tank.writeToNBT(provider, nbt);
		nbt.put("Inventory", inventory.serializeNBT(provider));
		nbt.putInt("progress", progress);
	}

	public double getMultiplier() {
		BlockState metalState = level.getBlockState(worldPosition.below());
		BlockStateContext context = new BlockStateContext(metalState);
		cachedCoefficient = Misc.getRecipe(cachedCoefficient, RegistryManager.METAL_COEFFICIENT.get(), context, level);

		double metalMultiplier = cachedCoefficient == null ? BASE_MULTIPLIER : cachedCoefficient.value().getCoefficient(context) - BASE_MULTIPLIER;
		double totalMult = BASE_MULTIPLIER;
		if (cachedCoefficient == null) {
			totalMult = metalMultiplier;
		} else {
			for (Direction facing : Misc.horizontals) {
				if (level.getBlockState(worldPosition.below().relative(facing)).is(EmbersBlockTags.HEAT_SOURCES)) {
					totalMult += PER_BLOCK_MULTIPLIER * metalMultiplier;
				}
			}
		}
		return totalMult;
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, PressureRefineryBottomBlockEntity blockEntity) {
		blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos, Misc.horizontals);
		UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, PressureRefineryBottomBlockEntity blockEntity) {
		blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos, Misc.horizontals);
		UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
		if (UpgradeUtil.doTick(blockEntity, blockEntity.upgrades))
			return;

		if (!blockEntity.inventory.getStackInSlot(0).isEmpty()) {
			BlockEntity tile = level.getBlockEntity(pos.above());
			if (blockEntity.tank.getFluid().is(FluidTags.WATER) && blockEntity.tank.getFluidAmount() >= FLUID_CONSUMED) {
				boolean cancel = UpgradeUtil.doWork(blockEntity, blockEntity.upgrades);

				if (!cancel && tile instanceof PressureRefineryTopBlockEntity top) {
                    blockEntity.progress++;
					if (blockEntity.progress > UpgradeUtil.getWorkTime(blockEntity, PROCESS_TIME, blockEntity.upgrades)) {
						blockEntity.progress = 0;
						if (blockEntity.inventory != null) {
							RecipeWrapper wrapper = new RecipeWrapper(blockEntity.inventory);
							blockEntity.cachedRecipe = Misc.getRecipe(blockEntity.cachedRecipe, RegistryManager.EMBER_ACTIVATION.get(), wrapper, level);
							if (blockEntity.cachedRecipe != null) {
								double emberValue = blockEntity.cachedRecipe.value().getOutput(wrapper) * blockEntity.getMultiplier();
								double ember = UpgradeUtil.getTotalEmberProduction(blockEntity, emberValue, blockEntity.upgrades);
								if ((ember > 0 || emberValue == 0) && top.capability.getEmber() + ember <= top.capability.getEmberCapacity()) {
									level.playSound(null, pos, EmbersSounds.PRESSURE_REFINERY.get(), SoundSource.BLOCKS, 1.0f, 1.0f);

									if (level instanceof ServerLevel serverLevel) {
										serverLevel.sendParticles(new GlowParticleOptions(EmbersColors.EMBER_ID, new Vec3(0, 0.65f, 0), 4.7f), pos.getX() + 0.5f, pos.getY() + 1.5f, pos.getZ() + 0.5f, 80, 0.1, 0.1, 0.1, 1.0);
										serverLevel.sendParticles(new SmokeParticleOptions(EmbersColors.SMOKE_ID, 5.0f), pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 20, 0.1, 0.1, 0.1, 1.0);
									}
									UpgradeUtil.throwEvent(blockEntity, new EmberEvent(blockEntity, EmberEvent.EnumType.PRODUCE, ember), blockEntity.upgrades);
									top.capability.addAmount(ember, true);

									//the recipe is responsible for taking items from the getCapability
									blockEntity.cachedRecipe.value().process(wrapper);
									blockEntity.tank.drain(FLUID_CONSUMED, IFluidHandler.FluidAction.EXECUTE);
								}
							}
						}
					}
					blockEntity.setChanged();
				}
			}
		}
	}

	@Override
	public void addDialInformation(Direction facing, List<Component> information, String dialType) {
		if(EmberDialBlock.DIAL_TYPE.equals(dialType)) {
			DecimalFormat multiplierFormat = DecimalFormats.getDecimalFormat(Embers.MODID + ".decimal_format.ember_multiplier");
			double multiplier = getMultiplier();
			information.add(Component.translatable(Embers.MODID + ".tooltip.dial.ember_multiplier", multiplierFormat.format(multiplier)));
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
			strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.INPUT, Embers.MODID + ".tooltip.goggles.item", Component.translatable(Embers.MODID + ".tooltip.goggles.item.ember")));
		if(capability == Capabilities.FluidHandler.BLOCK)
			strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.INPUT, Embers.MODID + ".tooltip.goggles.fluid", Component.translatable(Embers.MODID + ".tooltip.goggles.fluid.water")));
	}

	@Override
	public boolean isSideUpgradeSlot(Direction face) {
		return face.getAxis() != Axis.Y;
	}

    @Override
    public IFluidHandler getFluidCapability(Direction side) {
        return this.remove ? null: tank;
    }

    @Override
    public IItemHandler getInventoryCapability(Direction side) {
        if (!this.remove && side != Direction.DOWN)
            return inventory;
        return null;
    }
}
