package hu.zoldleo.embers.blockentity;

import java.util.List;
import java.util.Random;

import hu.zoldleo.embers.ConfigManager;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.tile.IBin;
import hu.zoldleo.embers.api.tile.IHammerable;
import hu.zoldleo.embers.blockentity.capability_helper.IInventoryBlock;
import hu.zoldleo.embers.particle.SmokeParticleOptions;
import hu.zoldleo.embers.particle.SparkParticleOptions;
import hu.zoldleo.embers.recipe.base.IDawnstoneAnvilRecipe;
import hu.zoldleo.embers.util.EmbersColors;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;

public class DawnstoneAnvilBlockEntity extends BlockEntity implements IHammerable, IInventoryBlock {
	int progress = 0;
	public ItemStackHandler inventory = new ItemStackHandler(2) {
		@Override
		protected void onContentsChanged(int slot) {
			DawnstoneAnvilBlockEntity.this.setChanged();
		}

		@Override
		protected int getStackLimit(int slot, @NotNull ItemStack stack) {
			return 1;
		}
	};
	static Random random = new Random();
	public RecipeHolder<IDawnstoneAnvilRecipe> cachedRecipe = null;

	public DawnstoneAnvilBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.DAWNSTONE_ANVIL_ENTITY.get(), pPos, pBlockState);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		progress = nbt.getInt("progress");
		inventory.deserializeNBT(provider, nbt.getCompound("Inventory"));
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
		nbt.putInt("progress", progress);
		nbt.put("Inventory", inventory.serializeNBT(provider));
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		nbt.putInt("progress", progress);
		nbt.put("Inventory", inventory.serializeNBT(provider));
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

	public boolean onHit() {
		RecipeWrapper context = new RecipeWrapper(inventory);
		cachedRecipe = Misc.getRecipe(cachedRecipe, RegistryManager.DAWNSTONE_ANVIL_RECIPE.get(), context, level);
		if (cachedRecipe != null && level != null) {
			progress += 1;
			level.playSound(null, worldPosition, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.25f, 2.0f+random.nextFloat());
			if (progress > ConfigManager.DAWNSTONE_ANVIL_MAX_HITS.get()) {
				progress = 0;
				List<ItemStack> results = cachedRecipe.value().getOutput(context);
				for (ItemStack result : results) {
					BlockEntity bin = level.getBlockEntity(worldPosition.below());
					if (bin instanceof IBin) {
						ItemStack remainder = ((IBin) bin).getInventory().insertItem(0, result.copy(), false);
						if (!remainder.isEmpty() && !level.isClientSide()) {
							level.addFreshEntity(new ItemEntity(level, worldPosition.getX()+0.5,worldPosition.getY()+1.0625f,worldPosition.getZ()+0.5, remainder));
						}
					} else if (!level.isClientSide()) {
						level.addFreshEntity(new ItemEntity(level, worldPosition.getX()+0.5,worldPosition.getY()+1.0625f,worldPosition.getZ()+0.5, result));
					}
				}
				//the recipe is not responsible for removing items from the getCapability
				inventory.setStackInSlot(0, ItemStack.EMPTY);
				inventory.setStackInSlot(1, ItemStack.EMPTY);

				if (level instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(new SparkParticleOptions(EmbersColors.EMBER_ID, 1.0f), worldPosition.getX() + 0.5f, worldPosition.getY() + 1.0625f, worldPosition.getZ() + 0.5f, 10, 0.1, 0.0, 0.1, 1.0);
					serverLevel.sendParticles(new SmokeParticleOptions(EmbersColors.SMOKE_ID, 3.0f), worldPosition.getX() + 0.5f, worldPosition.getY() + 1.0625f, worldPosition.getZ() + 0.5f, 10, 0.1, 0.0, 0.1, 1.0);
				}
				level.playSound(null, worldPosition, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 1.0f, 0.95f+random.nextFloat()*0.1f);
			} else if (level instanceof ServerLevel serverLevel) {
				setChanged();
				serverLevel.sendParticles(new SparkParticleOptions(EmbersColors.EMBER_ID, 1.0f), worldPosition.getX() + 0.5f, worldPosition.getY() + 1.0625f, worldPosition.getZ() + 0.5f, 1, 0.02, 0.0, 0.02, 1.0);
			}
			return true;
		}
		return false;
	}

	@Override
	public void onHit(BlockEntity hammer) {
		progress = ConfigManager.DAWNSTONE_ANVIL_MAX_HITS.get();
		onHit();
	}

	@Override
	public boolean isValid() {
		cachedRecipe = Misc.getRecipe(cachedRecipe, RegistryManager.DAWNSTONE_ANVIL_RECIPE.get(), new RecipeWrapper(inventory), level);
		return cachedRecipe != null;
	}

    @Override
    public IItemHandler getInventoryCapability(Direction side) {
        return this.remove ? null : inventory;
    }
}