package hu.zoldleo.embers.upgrade;

import java.util.List;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.event.EmberBoreBladeRenderEvent;
import hu.zoldleo.embers.api.event.MachineRecipeEvent;
import hu.zoldleo.embers.api.event.UpgradeEvent;
import hu.zoldleo.embers.api.upgrades.UpgradeContext;
import hu.zoldleo.embers.blockentity.EmberBoreBlockEntity;
import hu.zoldleo.embers.blockentity.ExcavationBucketsBlockEntity;
import hu.zoldleo.embers.recipe.context.BoringContext;
import hu.zoldleo.embers.recipe.base.IBoringRecipe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;

public class ExcavationBucketsUpgrade extends DefaultUpgradeProvider {
	@OnlyIn(Dist.CLIENT)
	public static BakedModel buckets;

	public ExcavationBucketsUpgrade(BlockEntity tile) {
		super(Embers.res("excavation_buckets"), tile);
	}

	@Override
	public int getPriority() {
		return -90; //after the clockwork attenuator
	}

	@Override
	public int getLimit(BlockEntity tile) {
		if (tile instanceof EmberBoreBlockEntity)
			return Integer.MAX_VALUE;
		return 0;
	}

	@Override
	public boolean doTick(BlockEntity tile, List<UpgradeContext> upgrades, int distance, int count) {
		((ExcavationBucketsBlockEntity) this.tile).lastAngle = ((ExcavationBucketsBlockEntity) this.tile).angle;
		if (tile instanceof EmberBoreBlockEntity bore && bore.isRunning)
			((ExcavationBucketsBlockEntity) this.tile).angle += (float) (14.0f * bore.speedMod);
		return false;
	}

	@Override
	public void throwEvent(BlockEntity tile, List<UpgradeContext> upgrades, UpgradeEvent event, int distance, int count) {
        if (tile.getLevel() == null)
            return;
		if (tile instanceof EmberBoreBlockEntity bore && event instanceof MachineRecipeEvent<?> recipeEvent && recipeEvent.getRecipe() instanceof List<?>) {
			@SuppressWarnings("unchecked")
			List<IBoringRecipe> recipes = (List<IBoringRecipe>) recipeEvent.getRecipe();

			ResourceKey<Biome> biome = tile.getLevel().getBiome(tile.getBlockPos()).unwrapKey().orElse(Biomes.PLAINS);
			BoringContext context = new BoringContext(tile.getLevel().dimension().location(), biome.location(), tile.getBlockPos().getY(), tile.getLevel().getBlockStatesIfLoaded(bore.getBladeBoundingBox()).toArray(BlockState[]::new));
			List<IBoringRecipe> newRecipes = tile.getLevel().getRecipeManager().getRecipesFor(RegistryManager.EXCAVATION.get(), context, tile.getLevel()).stream().map(RecipeHolder::value).toList();

			recipes.addAll(newRecipes);
		}
		if (tile.getLevel().isClientSide())
			CLientStuff.throwEvent(tile, upgrades, event, distance, count);
	}

	public static class CLientStuff {
		public static void throwEvent(BlockEntity tile, List<UpgradeContext> upgrades, UpgradeEvent event, int distance, int count) {
			if (buckets != null && event instanceof EmberBoreBladeRenderEvent renderEvent) {
				Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(renderEvent.getPose().last(), renderEvent.getBuffer().getBuffer(Sheets.solidBlockSheet()), renderEvent.getBlockState(), buckets, 0.0f, 0.0f, 0.0f, renderEvent.getLight(), renderEvent.getOverlay(), ModelData.EMPTY, Sheets.solidBlockSheet());
			}
		}
	}
}