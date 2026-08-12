package hu.zoldleo.embers.datagen;

import java.util.List;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.holdersets.AndHolderSet;
import net.neoforged.neoforge.registries.holdersets.NotHolderSet;
import net.neoforged.neoforge.registries.holdersets.OrHolderSet;
import org.jetbrains.annotations.NotNull;

public class EmbersBiomeModifiers {
	public static final ResourceKey<BiomeModifier> ORE_LEAD_KEY = ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, Embers.res("add_lead_ore"));
	public static final ResourceKey<BiomeModifier> ORE_SILVER_KEY = ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, Embers.res("add_silver_ore"));

	public static final ResourceKey<BiomeModifier> GOLEM_SPAWN = ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, Embers.res("add_golem_spawn"));

	public static void generate(BootstrapContext<BiomeModifier> bootstrap) {
		HolderGetter<PlacedFeature> placed = bootstrap.lookup(Registries.PLACED_FEATURE);
		HolderGetter<Biome> biome = bootstrap.lookup(Registries.BIOME);
		HolderSet<Biome> overworldBiomes = biome.getOrThrow(BiomeTags.IS_OVERWORLD);
		List<HolderSet<Biome>> biomeBlackList = List.of(biome.getOrThrow(Tags.Biomes.IS_MUSHROOM), HolderSet.direct(biome.getOrThrow(Biomes.DEEP_DARK)), biome.getOrThrow(Tags.Biomes.NO_DEFAULT_MONSTERS));
		HolderSet<Biome> hostileSpawns = new AndHolderSet<>(List.of(overworldBiomes, new NotHolderSetWrapper<>(new OrHolderSet<>(biomeBlackList))));

		bootstrap.register(ORE_LEAD_KEY, new BiomeModifiers.AddFeaturesBiomeModifier(overworldBiomes, HolderSet.direct(placed.getOrThrow(EmbersPlacedFeatures.ORE_LEAD_KEY)), Decoration.UNDERGROUND_ORES));
		bootstrap.register(ORE_SILVER_KEY, new BiomeModifiers.AddFeaturesBiomeModifier(overworldBiomes, HolderSet.direct(placed.getOrThrow(EmbersPlacedFeatures.ORE_SILVER_KEY)), Decoration.UNDERGROUND_ORES));

		bootstrap.register(GOLEM_SPAWN, new BiomeModifiers.AddSpawnsBiomeModifier(hostileSpawns, List.of(new MobSpawnSettings.SpawnerData(RegistryManager.ANCIENT_GOLEM.get(), 15, 1, 1))));
	}

	//wow this is stupid
	public static class NotHolderSetWrapper<T> extends NotHolderSet<T> {
		public NotHolderSetWrapper(HolderSet<T> value) {
			super(null, value);
		}

		@Override
		public boolean canSerializeIn(@NotNull HolderOwner<T> holderOwner) {
			return true;
		}
	}
}