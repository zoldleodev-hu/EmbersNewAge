package hu.zoldleo.embers.blockentity;

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
import hu.zoldleo.embers.blockentity.capability_helper.IInventoryBlock;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.particle.GlowParticleOptions;
import hu.zoldleo.embers.particle.SmokeParticleOptions;
import hu.zoldleo.embers.recipe.base.IEmberActivationRecipe;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;

public class EmberActivatorBottomBlockEntity extends BlockEntity implements IExtraDialInformation, IExtraCapabilityInformation, IUpgradeable, IInventoryBlock {
	public static final int PROCESS_TIME = 40;
	int progress = -1;
	public ItemStackHandler inventory = new ItemStackHandler(1) {
		@Override
		protected void onContentsChanged(int slot) {
			EmberActivatorBottomBlockEntity.this.setChanged();
		}

		@Override
		public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
			if (Misc.getRecipe(cachedRecipe, RegistryManager.EMBER_ACTIVATION.get(), new SingleRecipeInput(stack), level) != null)
				return super.insertItem(slot, stack, simulate);
			return stack;
		}
	};
	protected List<UpgradeContext> upgrades = new ArrayList<>();
	public RecipeHolder<IEmberActivationRecipe> cachedRecipe = null;

	public EmberActivatorBottomBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.EMBER_ACTIVATOR_BOTTOM_ENTITY.get(), pPos, pBlockState);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		inventory.deserializeNBT(provider, nbt.getCompound("Inventory"));
		progress = nbt.getInt("progress");
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
		nbt.put("Inventory", inventory.serializeNBT(provider));
		nbt.putInt("progress", progress);
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, EmberActivatorBottomBlockEntity blockEntity) {
		blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos, Misc.horizontals);
		UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, EmberActivatorBottomBlockEntity blockEntity) {
		blockEntity.upgrades = UpgradeUtil.getUpgrades(level, pos, Misc.horizontals);
		UpgradeUtil.verifyUpgrades(blockEntity, blockEntity.upgrades);
		if(UpgradeUtil.doTick(blockEntity, blockEntity.upgrades))
			return;

		if (!blockEntity.inventory.getStackInSlot(0).isEmpty()) {
			BlockEntity tile = level.getBlockEntity(pos.above());
			boolean cancel = UpgradeUtil.doWork(blockEntity, blockEntity.upgrades);

			if (!cancel && tile instanceof EmberActivatorTopBlockEntity top) {
                blockEntity.progress++;
				if (blockEntity.progress > UpgradeUtil.getWorkTime(blockEntity, PROCESS_TIME, blockEntity.upgrades)) {
					blockEntity.progress = 0;
					if (blockEntity.inventory != null) {
						RecipeWrapper wrapper = new RecipeWrapper(blockEntity.inventory);
						blockEntity.cachedRecipe = Misc.getRecipe(blockEntity.cachedRecipe, RegistryManager.EMBER_ACTIVATION.get(), wrapper, level);
						if (blockEntity.cachedRecipe != null) {
							double emberValue = blockEntity.cachedRecipe.value().getOutput(wrapper);
							double ember = UpgradeUtil.getTotalEmberProduction(blockEntity, emberValue, blockEntity.upgrades);
							if ((ember > 0 || emberValue == 0) && top.capability.getEmber() + ember <= top.capability.getEmberCapacity()) {
								level.playSound(null, pos, EmbersSounds.ACTIVATOR.get(), SoundSource.BLOCKS, 1.0f, 1.0f);

								if (level instanceof ServerLevel serverLevel) {
									serverLevel.sendParticles(new GlowParticleOptions(EmbersColors.EMBER_ID, new Vec3(0, 0.65f, 0), 4.7f), pos.getX() + 0.5f, pos.getY() + 1.5f, pos.getZ() + 0.5f, 80, 0.1, 0.1, 0.1, 1.0);
									serverLevel.sendParticles(SmokeParticleOptions.BIG_SMOKE, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 20, 0.1, 0.1, 0.1, 1.0);
								}
								UpgradeUtil.throwEvent(blockEntity, new EmberEvent(blockEntity, EmberEvent.EnumType.PRODUCE, ember), blockEntity.upgrades);
								top.capability.addAmount(ember, true);

								//the recipe is responsible for taking items from the getCapability
								blockEntity.cachedRecipe.value().process(wrapper);
							}
						}
					}
				}
				blockEntity.setChanged();
			}
		}
	}

	@Override
	public boolean hasCapabilityDescription(BlockCapability<?, ?> capability) {
		return capability == Capabilities.ItemHandler.BLOCK;
	}

	@Override
	public void addCapabilityDescription(List<Component> strings, BlockCapability<?, ?> capability, Direction facing) {
		if (capability == Capabilities.ItemHandler.BLOCK)
			strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.INPUT, Embers.MODID + ".tooltip.goggles.item", Component.translatable(Embers.MODID + ".tooltip.goggles.item.ember")));
	}

	@Override
	public void addDialInformation(Direction facing, List<Component> information, String dialType) {
		UpgradeUtil.throwEvent(this, new DialInformationEvent(this, information, dialType), upgrades);
	}

	@Override
	public boolean isSideUpgradeSlot(Direction face) {
		return face.getAxis() != Axis.Y;
	}

    @Override
    public IItemHandler getInventoryCapability(Direction side) {
        if (!this.remove && side != Direction.DOWN)
            return inventory;
        return null;
    }
}