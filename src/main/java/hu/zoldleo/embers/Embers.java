package hu.zoldleo.embers;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import hu.zoldleo.embers.api.capabilities.EmbersCapabilities;
import hu.zoldleo.embers.apiimpl.EmbersAPIImpl;
import hu.zoldleo.embers.augment.ShiftingScalesAugment;
import hu.zoldleo.embers.augment.WindingGearsAugment;
import hu.zoldleo.embers.blockentity.capability_helper.IEmberBlock;
import hu.zoldleo.embers.blockentity.capability_helper.IFluidBlock;
import hu.zoldleo.embers.blockentity.capability_helper.IInventoryBlock;
import hu.zoldleo.embers.blockentity.capability_helper.IUpgradeBlock;
import hu.zoldleo.embers.blockentity.render.*;
import hu.zoldleo.embers.compat.curios.CuriosCompat;
import hu.zoldleo.embers.datagen.*;
import hu.zoldleo.embers.entity.AncientGolemEntity;
import hu.zoldleo.embers.entity.EmberWispEntity;
import hu.zoldleo.embers.entity.render.*;
import hu.zoldleo.embers.fluidtypes.EmbersFluidType;
import hu.zoldleo.embers.gui.SlateScreen;
import hu.zoldleo.embers.item.AlchemicalNoteItem;
import hu.zoldleo.embers.item.DawnstoneShieldItem;
import hu.zoldleo.embers.item.EmberStorageItem;
import hu.zoldleo.embers.item.TyrfingItem;
import hu.zoldleo.embers.model.AncientGolemModel;
import hu.zoldleo.embers.model.AshenArmorModel;
import hu.zoldleo.embers.model.DawnstoneShieldModel;
import hu.zoldleo.embers.network.PacketHandler;
import hu.zoldleo.embers.particle.*;
import hu.zoldleo.embers.power.DefaultEmberItemCapability;
import hu.zoldleo.embers.render.EmbersRenderTypes;
import hu.zoldleo.embers.render.PipeModel;
import hu.zoldleo.embers.research.ResearchManager;
import hu.zoldleo.embers.util.*;
import hu.zoldleo.embers.util.GlowingTextTooltip.GlowingTextClientTooltip;
import hu.zoldleo.embers.util.HeatBarTooltip.HeatBarClientTooltip;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import net.neoforged.neoforge.items.ComponentItemHandler;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Mod(Embers.MODID)
public class Embers {
	public static final String MODID_OLD = "embersrekindled";
	public static final String MODID = "embers";

	public static final Logger LOGGER = LogUtils.getLogger();

	public Embers(IEventBus modEventBus, ModContainer modContainer) {
		modEventBus.addListener(this::commonSetup);
		modEventBus.addListener(this::gatherData);
		modEventBus.addListener(this::registerCaps);
		modEventBus.addListener(this::entityAttributes);
		modEventBus.addListener(this::spawnPlacements);
		modEventBus.addListener(this::registerRecipeSerializers);
        modEventBus.addListener(RegistryManager::addRegistries);
        modEventBus.addListener(PacketHandler::registerPayloads);

		EmbersAPIImpl.init();
		RegistryManager.BLOCKS.register(modEventBus);
		RegistryManager.ITEMS.register(modEventBus);
		RegistryManager.FLUIDTYPES.register(modEventBus);
		RegistryManager.FLUIDS.register(modEventBus);
		RegistryManager.ENTITY_TYPES.register(modEventBus);
		RegistryManager.BLOCK_ENTITY_TYPES_NEW.register(modEventBus);
		RegistryManager.BLOCK_ENTITY_TYPES_OLD.register(modEventBus);
		RegistryManager.CREATIVE_TABS.register(modEventBus);
		RegistryManager.PARTICLE_TYPES.register(modEventBus);
		RegistryManager.SOUND_EVENTS.register(modEventBus);
		RegistryManager.RECIPE_TYPES.register(modEventBus);
		RegistryManager.RECIPE_SERIALIZERS.register(modEventBus);
		RegistryManager.LOOT_MODIFIERS.register(modEventBus);
		RegistryManager.MENU_TYPES.register(modEventBus);
		RegistryManager.STRUCTURE_TYPES.register(modEventBus);
		RegistryManager.STRUCTURE_PROCESSOR_TYPES.register(modEventBus);
        RegistryManager.ATTACHMENT_TYPES.register(modEventBus);
        RegistryManager.INGREDIENT_TYPES.register(modEventBus);
        RegistryManager.DATA_COMPONENTS.register(modEventBus);
        RegistryManager.ARMOR_MATERIALS.register(modEventBus);
		EmbersSounds.init();

		ConfigManager.register(modContainer);

		if (ModList.get().isLoaded("curios"))
			CuriosCompat.init();
	}

	public void commonSetup(final FMLCommonSetupEvent event) {
		RegistryManager.init(event);
		ResearchManager.initResearches();
		NeoForge.EVENT_BUS.addListener(EmbersEvents::onJoin);
		NeoForge.EVENT_BUS.addListener(EventPriority.LOW, EmbersEvents::onEntityIncomingDamage);
		NeoForge.EVENT_BUS.addListener(EventPriority.LOW, EmbersEvents::onEntityDamaged);
		NeoForge.EVENT_BUS.addListener(EmbersEvents::onBlockBreak);
		NeoForge.EVENT_BUS.addListener(EventPriority.LOW, EmbersEvents::onProjectileFired);
		NeoForge.EVENT_BUS.addListener(EventPriority.LOW, EmbersEvents::onArrowLoose);
		NeoForge.EVENT_BUS.addListener(EmbersEvents::onAnvilUpdate);
		NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, TagsUpdatedEvent.class, e -> Misc.tagItems.clear());
		NeoForge.EVENT_BUS.addListener(EmbersEvents::onLevelLoad);
		NeoForge.EVENT_BUS.addListener(EmbersEvents::onServerTick);
		NeoForge.EVENT_BUS.addListener(EmbersEvents::onExplosion);
		NeoForge.EVENT_BUS.addListener(EmbersEvents::onTagsReload);
	}

	public void registerCaps(RegisterCapabilitiesEvent event) {
        for (DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<? extends IEmberBlock>> tile : List.of(
                RegistryManager.AUTOMATIC_HAMMER_ENTITY, RegistryManager.BEAM_CANNON_ENTITY, RegistryManager.COPPER_CELL_ENTITY,
                RegistryManager.COPPER_CHARGER_ENTITY, RegistryManager.CREATIVE_EMBER_ENTITY, RegistryManager.CRYSTAL_CELL_ENTITY,
                RegistryManager.EMBER_ACTIVATOR_TOP_ENTITY, RegistryManager.EMBER_EJECTOR_ENTITY,
                RegistryManager.EMBER_EMITTER_ENTITY, RegistryManager.EMBER_FUNNEL_ENTITY, RegistryManager.EMBER_INJECTOR_ENTITY,
                RegistryManager.EMBER_RECEIVER_ENTITY, RegistryManager.EMBER_SIPHON_ENTITY, RegistryManager.HEARTH_COIL_ENTITY,
                RegistryManager.IGNEM_REACTOR_ENTITY, RegistryManager.INFERNO_FORGE_BOTTOM_ENTITY,
                RegistryManager.MECHANICAL_CORE_ENTITY, RegistryManager.MECHANICAL_PUMP_BOTTOM_ENTITY,
                RegistryManager.MELTER_BOTTOM_ENTITY, RegistryManager.MIXER_CENTRIFUGE_TOP_ENTITY,
                RegistryManager.PRESSURE_REFINERY_TOP_ENTITY, RegistryManager.STAMPER_ENTITY))
            event.registerBlockEntity(EmbersCapabilities.EMBER_CAPABILITY_BLOCK, tile.get(), IEmberBlock::getEmberCapability);

        for (DeferredHolder<Item, ? extends EmberStorageItem> item : List.of(
                RegistryManager.EMBER_CARTRIDGE, RegistryManager.EMBER_JAR))
            event.registerItem(EmbersCapabilities.EMBER_CAPABILITY_ITEM, (stack, v) -> ((EmberStorageItem)stack.getItem()).getEmberCapability(stack), item.get());
        event.registerItem(EmbersCapabilities.EMBER_CAPABILITY_ITEM, (stack, v) -> new DefaultEmberItemCapability(stack), RegistryManager.COPPER_CELL_ITEM);

        for (DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<? extends IUpgradeBlock>> tile : List.of(
                RegistryManager.ATMOSPHERIC_BELLOWS_ENTITY, RegistryManager.CATALYTIC_PLUG_ENTITY,
                RegistryManager.CHAR_INSTILLER_ENTITY, RegistryManager.CLOCKWORK_ATTENUATOR_ENTITY,
                RegistryManager.EMBER_SIPHON_ENTITY, RegistryManager.ENTROPIC_ENUMERATOR_ENTITY,
                RegistryManager.EXCAVATION_BUCKETS_ENTITY, RegistryManager.GEOLOGIC_SEPARATOR_ENTITY,
                RegistryManager.HEAT_EXCHANGER_ENTITY, RegistryManager.HEAT_INSULATION_ENTITY, RegistryManager.MINI_BOILER_ENTITY,
                RegistryManager.MNEMONIC_INSCRIBER_ENTITY, RegistryManager.WILDFIRE_STIRLING_ENTITY))
            event.registerBlockEntity(EmbersCapabilities.UPGRADE_PROVIDER_CAPABILITY, tile.get(), IUpgradeBlock::getUpgradeCapability);

        for (DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<? extends IInventoryBlock>> tile : List.of(
                RegistryManager.ALCHEMY_PEDESTAL_ENTITY, RegistryManager.ALCHEMY_PEDESTAL_TOP_ENTITY,
                RegistryManager.ALCHEMY_TABLET_ENTITY, RegistryManager.BIN_ENTITY, RegistryManager.CATALYSIS_CHAMBER_ENTITY,
                RegistryManager.COMBUSTION_CHAMBER_ENTITY, RegistryManager.COPPER_CHARGER_ENTITY,
                RegistryManager.CRYSTAL_CELL_ENTITY, RegistryManager.DAWNSTONE_ANVIL_ENTITY,
                RegistryManager.EMBER_ACTIVATOR_BOTTOM_ENTITY, RegistryManager.EMBER_BORE_ENTITY,
                RegistryManager.HEARTH_COIL_ENTITY, RegistryManager.IGNEM_REACTOR_ENTITY, RegistryManager.ITEM_DROPPER_ENTITY,
                RegistryManager.ITEM_EXTRACTOR_ENTITY, RegistryManager.ITEM_PIPE_ENTITY, RegistryManager.ITEM_TRANSFER_ENTITY,
                RegistryManager.ITEM_VACUUM_ENTITY, RegistryManager.MECHANICAL_CORE_ENTITY, RegistryManager.MELTER_TOP_ENTITY,
                RegistryManager.MNEMONIC_INSCRIBER_ENTITY, RegistryManager.PRESSURE_REFINERY_BOTTOM_ENTITY,
                RegistryManager.STAMP_BASE_ENTITY, RegistryManager.STAMPER_ENTITY))
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, tile.get(), IInventoryBlock::getInventoryCapability);

        event.registerItem(Capabilities.ItemHandler.ITEM, (stack, v) -> new ComponentItemHandler(stack, DataComponents.CONTAINER, 7), RegistryManager.CODEBREAKING_SLATE);

        for (DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<? extends IFluidBlock>> tile : List.of(
                RegistryManager.CAMINITE_VALVE_ENTITY, RegistryManager.CATALYTIC_PLUG_ENTITY,
                RegistryManager.FLUID_EXTRACTOR_ENTITY, RegistryManager.FLUID_PIPE_ENTITY, RegistryManager.FLUID_TRANSFER_ENTITY,
                RegistryManager.FLUID_VESSEL_ENTITY, RegistryManager.GEOLOGIC_SEPARATOR_ENTITY,
                RegistryManager.MECHANICAL_CORE_ENTITY, RegistryManager.MECHANICAL_PUMP_TOP_ENTITY,
                RegistryManager.MELTER_TOP_ENTITY, RegistryManager.MINI_BOILER_ENTITY,
                RegistryManager.MIXER_CENTRIFUGE_BOTTOM_ENTITY, RegistryManager.MIXER_CENTRIFUGE_TOP_ENTITY,
                RegistryManager.PRESSURE_REFINERY_BOTTOM_ENTITY, RegistryManager.RESERVOIR_ENTITY,
                RegistryManager.STAMP_BASE_ENTITY, RegistryManager.WILDFIRE_STIRLING_ENTITY))
            event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, tile.get(), IFluidBlock::getFluidCapability);

        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, v) -> new FluidHandlerItemStack(RegistryManager.FLUID_COMPONENT, stack, ConfigManager.FLUID_VESSEL_CAPACITY.get()), RegistryManager.FLUID_VESSEL_ITEM);

        if (ModList.get().isLoaded("curios"))
            event.registerItem(EmbersCapabilities.EMBER_CAPABILITY_ITEM, (stack, v) -> ((EmberStorageItem)stack.getItem()).getEmberCapability(stack), CuriosCompat.EMBER_BULB);
	}

	public void entityAttributes(EntityAttributeCreationEvent event) {
		event.put(RegistryManager.ANCIENT_GOLEM.get(), AncientGolemEntity.createAttributes().build());
        event.put(RegistryManager.EMBER_WISP.get(), EmberWispEntity.createAttributes().build());
	}

	public void spawnPlacements(RegisterSpawnPlacementsEvent event) {
		event.register(RegistryManager.ANCIENT_GOLEM.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
		event.register(RegistryManager.EMBER_WISP.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EmberWispEntity::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
	}

	public void gatherData(GatherDataEvent event) {
		DataGenerator gen = event.getGenerator();
		PackOutput output = gen.getPackOutput();
		ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

		if (event.includeClient()) {
			gen.addProvider(true, new EmbersLang(output));
			ItemModelProvider itemModels = new EmbersItemModels(output, existingFileHelper);
			gen.addProvider(true, itemModels);
			gen.addProvider(true, new EmbersBlockStates(output, existingFileHelper));
			gen.addProvider(true, new EmbersSounds(output, existingFileHelper));
		} if (event.includeServer()) {
			gen.addProvider(true, new EmbersLootTables(output, lookupProvider));
			gen.addProvider(true, new EmbersRecipes(output, lookupProvider));
			BlockTagsProvider blockTags = new EmbersBlockTags(output, lookupProvider, existingFileHelper);
			gen.addProvider(true, blockTags);
			gen.addProvider(true, new EmbersItemTags(output, lookupProvider, blockTags.contentsGetter(), existingFileHelper));
			gen.addProvider(true, new EmbersFluidTags(output, lookupProvider, existingFileHelper));
			gen.addProvider(true, new DatapackBuiltinEntriesProvider(output, lookupProvider, new RegistrySetBuilder()
					.add(Registries.CONFIGURED_FEATURE, EmbersConfiguredFeatures::generate) //it doesn't like this one for some reason
					.add(Registries.PLACED_FEATURE, EmbersPlacedFeatures::generate)
					.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, EmbersBiomeModifiers::generate)
					.add(Registries.DAMAGE_TYPE, EmbersDamageTypes::generate)
					.add(Registries.PROCESSOR_LIST, EmbersStructures::generateProcessors)
					.add(Registries.TEMPLATE_POOL, EmbersStructures::generatePools)
					.add(Registries.STRUCTURE, EmbersStructures::generateStructures)
					.add(Registries.STRUCTURE_SET, EmbersStructures::generateSets),
					Set.of(MODID)));
			gen.addProvider(true, new EmbersDamageTypeTags(output, lookupProvider, existingFileHelper));
			gen.addProvider(true, new EmbersLootModifiers(output, lookupProvider));
		}
	}

	public void registerRecipeSerializers(RegisterEvent event) {
        event.register(Registries.ITEM_SUB_PREDICATE_TYPE, AugmentPredicate.ID, () -> AugmentPredicate.TYPE);
        event.register(Registries.LOOT_CONDITION_TYPE, res("match_curio"), () -> MatchCurioLootCondition.LOOT_CONDITION_TYPE);
	}

    public static ResourceLocation res(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

	@EventBusSubscriber(modid = Embers.MODID)
	public static class ClientModEvents {
		@OnlyIn(Dist.CLIENT)
		@SubscribeEvent
        @SuppressWarnings("AssertStatement")
		public static void clientSetup(FMLClientSetupEvent event) {
			IEventBus modEventBus = ModLoadingContext.get().getActiveContainer().getEventBus();
            if (modEventBus != null)
			    modEventBus.addListener(EmbersClientEvents::afterModelBake);
            NeoForge.EVENT_BUS.addListener(EmbersClientEvents::onLevelLoad);
            NeoForge.EVENT_BUS.addListener(EmbersClientEvents::onClientTick);
            NeoForge.EVENT_BUS.addListener(EmbersClientEvents::onMovementInput);
            NeoForge.EVENT_BUS.addListener(EmbersClientEvents::onBlockHighlight);
            NeoForge.EVENT_BUS.addListener(EmbersClientEvents::onLevelRender);
            NeoForge.EVENT_BUS.addListener(EmbersClientEvents::onTooltip);
            NeoForge.EVENT_BUS.addListener(EmbersClientEvents::onWorldRender);
            NeoForge.EVENT_BUS.addListener(EmbersClientEvents::registerReloadListeners);
			ItemBlockRenderTypes.setRenderLayer(RegistryManager.STEAM.FLUID.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(RegistryManager.STEAM.FLUID_FLOW.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(RegistryManager.DWARVEN_OIL.FLUID.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(RegistryManager.DWARVEN_OIL.FLUID_FLOW.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(RegistryManager.DWARVEN_GAS.FLUID.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(RegistryManager.DWARVEN_GAS.FLUID_FLOW.get(), RenderType.translucent());
			event.enqueueWork(() -> {
                ItemProperties.register(RegistryManager.INFLICTOR_GEM.get(), res("charged"), (stack, level, entity, seed) -> Boolean.TRUE.equals(stack.get(RegistryManager.INFLICTOR_CHARGE_COMPONENT)) ? 1 : 0);
                ItemProperties.register(RegistryManager.DAWNSTONE_SHIELD.get(), ResourceLocation.withDefaultNamespace("blocking"), (ClampedItemPropertyFunction)((stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1 : 0));
            });
		}

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(RegistryManager.SLATE_MENU.get(), SlateScreen::new);
        }

		@OnlyIn(Dist.CLIENT)
		@SubscribeEvent
		public static void overlayRegister(RegisterGuiLayersEvent event) {
			event.registerAboveAll(res("embers_ingame_overlay"), new EmbersClientEvents.Overlay());
			event.registerAboveAll(res("shifting_scales_particles"), new ShiftingScalesAugment.Overlay());
			event.registerAbove(VanillaGuiLayers.PLAYER_HEALTH, res("shifting_scales_hearts"), new ShiftingScalesAugment.HeartsOverlay());
			event.registerBelow(VanillaGuiLayers.JUMP_METER, res("winding_gears_spring_bottom"), new  WindingGearsAugment.Underlay());
			event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, res("winding_gears_spring_top"), new  WindingGearsAugment.Overlay());
		}

		@OnlyIn(Dist.CLIENT)
		@SubscribeEvent
		static void addResourceListener(RegisterClientReloadListenersEvent event) {
			event.registerReloadListener(new DecimalFormats());
			event.registerReloadListener(new EmbersColors());
		}

		@OnlyIn(Dist.CLIENT)
		@SubscribeEvent
		static void addParticleProvider(RegisterParticleProvidersEvent event) {
			event.registerSprite(RegistryManager.GLOW_PARTICLE.get(), new GlowParticle.Provider());
			event.registerSprite(RegistryManager.STAR_PARTICLE.get(), new StarParticle.Provider());
			event.registerSprite(RegistryManager.SPARK_PARTICLE.get(), new SparkParticle.Provider());
			event.registerSprite(RegistryManager.SMOKE_PARTICLE.get(), new SmokeParticle.Provider());
			event.registerSprite(RegistryManager.VAPOR_PARTICLE.get(), new VaporParticle.Provider());
			event.registerSprite(RegistryManager.ALCHEMY_CIRCLE_PARTICLE.get(), new AlchemyCircleParticle.Provider());
			event.registerSprite(RegistryManager.TYRFING_PARTICLE.get(), new TyrfingParticle.Provider());
			event.registerSprite(RegistryManager.XRAY_GLOW_PARTICLE.get(), new XRayGlowParticle.Provider());
		}

		@OnlyIn(Dist.CLIENT)
		@SubscribeEvent
		static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
			event.registerEntityRenderer(RegistryManager.EMBER_PACKET.get(), EmberPacketRenderer::new);
			event.registerEntityRenderer(RegistryManager.EMBER_PROJECTILE.get(), EmberProjectileRenderer::new);
			event.registerEntityRenderer(RegistryManager.GLIMMER_PROJECTILE.get(), GlimmerProjectileRenderer::new);
			event.registerEntityRenderer(RegistryManager.ANCIENT_GOLEM.get(), AncientGolemRenderer::new);
			event.registerEntityRenderer(RegistryManager.EMBER_WISP.get(), EmberWispRenderer::new);

			event.registerBlockEntityRenderer(RegistryManager.EMBER_BORE_ENTITY.get(), EmberBoreBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.MELTER_TOP_ENTITY.get(), MelterTopBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.FLUID_VESSEL_ENTITY.get(), FluidVesselBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.STAMPER_ENTITY.get(), StamperBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.STAMP_BASE_ENTITY.get(), StampBaseBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.BIN_ENTITY.get(), BinBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.MIXER_CENTRIFUGE_BOTTOM_ENTITY.get(), MixerCentrifugeBottomBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.MIXER_CENTRIFUGE_TOP_ENTITY.get(), MixerCentrifugeTopBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.RESERVOIR_ENTITY.get(), ReservoirBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.CRYSTAL_CELL_ENTITY.get(), CrystalCellBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.GEOLOGIC_SEPARATOR_ENTITY.get(), GeologicSeparatorBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.COPPER_CHARGER_ENTITY.get(), CopperChargerBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.ITEM_TRANSFER_ENTITY.get(), ItemTransferBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.FLUID_TRANSFER_ENTITY.get(), FluidTransferBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.ALCHEMY_PEDESTAL_TOP_ENTITY.get(), AlchemyPedestalTopBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.ALCHEMY_PEDESTAL_ENTITY.get(), AlchemyPedestalBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.ALCHEMY_TABLET_ENTITY.get(), AlchemyTabletBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.MECHANICAL_PUMP_BOTTOM_ENTITY.get(), MechanicalPumpBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.CATALYTIC_PLUG_ENTITY.get(), CatalyticPlugBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.COPPER_CRYSTAL_SEED.BLOCKENTITY.get(), CrystalSeedBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.IRON_CRYSTAL_SEED.BLOCKENTITY.get(), CrystalSeedBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.GOLD_CRYSTAL_SEED.BLOCKENTITY.get(), CrystalSeedBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.LEAD_CRYSTAL_SEED.BLOCKENTITY.get(), CrystalSeedBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.SILVER_CRYSTAL_SEED.BLOCKENTITY.get(), CrystalSeedBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.NICKEL_CRYSTAL_SEED.BLOCKENTITY.get(), CrystalSeedBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.TIN_CRYSTAL_SEED.BLOCKENTITY.get(), CrystalSeedBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.ALUMINUM_CRYSTAL_SEED.BLOCKENTITY.get(), CrystalSeedBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.ZINC_CRYSTAL_SEED.BLOCKENTITY.get(), CrystalSeedBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.PLATINUM_CRYSTAL_SEED.BLOCKENTITY.get(), CrystalSeedBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.URANIUM_CRYSTAL_SEED.BLOCKENTITY.get(), CrystalSeedBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.DAWNSTONE_CRYSTAL_SEED.BLOCKENTITY.get(), CrystalSeedBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(RegistryManager.MITHRIL_CRYSTAL_SEED.BLOCKENTITY.get(), CrystalSeedBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.FIELD_CHART_ENTITY.get(), FieldChartBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.CINDER_PLINTH_ENTITY.get(), CinderPlinthBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.DAWNSTONE_ANVIL_ENTITY.get(), DawnstoneAnvilBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.AUTOMATIC_HAMMER_ENTITY.get(), AutomaticHammerBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.INFERNO_FORGE_TOP_ENTITY.get(), InfernoForgeTopBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.MNEMONIC_INSCRIBER_ENTITY.get(), MnemonicInscriberBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.ATMOSPHERIC_BELLOWS_ENTITY.get(), AtmosphericBellowsBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.ENTROPIC_ENUMERATOR_ENTITY.get(), EntropicEnumeratorBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.MITHRIL_BLOCK_ENTITY.get(), MithrilBlockEntityRenderer::new);
			event.registerBlockEntityRenderer(RegistryManager.EXCAVATION_BUCKETS_ENTITY.get(), ExcavationBucketsBlockEntityRenderer::new);
		}

		@OnlyIn(Dist.CLIENT)
		@SubscribeEvent
		static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
			event.registerLayerDefinition(AncientGolemRenderer.LAYER_LOCATION, AncientGolemModel::createLayer);
			event.registerLayerDefinition(AshenArmorModel.ASHEN_ARMOR_HEAD, () -> LayerDefinition.create(AshenArmorModel.createHeadMesh(), 64, 64));
			event.registerLayerDefinition(AshenArmorModel.ASHEN_ARMOR_CHEST, () -> LayerDefinition.create(AshenArmorModel.createChestMesh(), 64, 64));
			event.registerLayerDefinition(AshenArmorModel.ASHEN_ARMOR_LEGS, () -> LayerDefinition.create(AshenArmorModel.createLegsMesh(), 64, 64));
			event.registerLayerDefinition(AshenArmorModel.ASHEN_ARMOR_FEET, () -> LayerDefinition.create(AshenArmorModel.createFeetMesh(), 64, 64));
            event.registerLayerDefinition(DawnstoneShieldModel.LAYER_LOCATION, DawnstoneShieldModel::createLayer);
		}

		@OnlyIn(Dist.CLIENT)
		@SubscribeEvent
		static void registerItemColorHandlers(RegisterColorHandlersEvent.Item event) {
			ItemColor emberContainerColor = new EmberStorageItem.ColorHandler();
			if (ModList.get().isLoaded("curios"))
				CuriosCompat.registerColorHandler(event, emberContainerColor);
			event.register(emberContainerColor, RegistryManager.EMBER_JAR.get(), RegistryManager.EMBER_CARTRIDGE.get());
			event.register(new TyrfingItem.ColorHandler(), RegistryManager.TYRFING.get());
		}

		@OnlyIn(Dist.CLIENT)
		@SubscribeEvent
		static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
			event.register(res("pipe"), PipeModel.Loader.INSTANCE);
		}

		@OnlyIn(Dist.CLIENT)
		@SubscribeEvent
		static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
			event.register(GlowingTextTooltip.class, GlowingTextClientTooltip::new);
			event.register(HeatBarTooltip.class, HeatBarClientTooltip::new);
		}

		@OnlyIn(Dist.CLIENT)
		@SubscribeEvent
		public static void shaderRegistry(RegisterShadersEvent event) throws IOException {
			event.registerShader(new ShaderInstance(event.getResourceProvider(), res("position_tex_color_additive"), DefaultVertexFormat.POSITION_TEX_COLOR), shaderInstance -> EmbersRenderTypes.additiveShader = shaderInstance);
			event.registerShader(new ShaderInstance(event.getResourceProvider(), res("particle_ember"), DefaultVertexFormat.PARTICLE), shaderInstance -> EmbersRenderTypes.emberParticleShader = shaderInstance);
			event.registerShader(new ShaderInstance(event.getResourceProvider(), res("particle_ember_fab"), DefaultVertexFormat.PARTICLE), shaderInstance -> EmbersRenderTypes.emberParticleFabShader = shaderInstance);
			event.registerShader(new ShaderInstance(event.getResourceProvider(), res("particle_translucent"), DefaultVertexFormat.PARTICLE), shaderInstance -> EmbersRenderTypes.translucentParticleShader = shaderInstance);
			event.registerShader(new ShaderInstance(event.getResourceProvider(), res("rendertype_entity_solid_mithril"), DefaultVertexFormat.NEW_ENTITY), shaderInstance -> EmbersRenderTypes.mithrilShader = shaderInstance);
		}

		@OnlyIn(Dist.CLIENT)
		@SubscribeEvent
		public static void renderTypeRegistry(RegisterNamedRenderTypesEvent event) {
			event.register(res("mithril"), RenderType.solid(), EmbersRenderTypes.MITHRIL);
		}

        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public static void registerExtensions(RegisterClientExtensionsEvent event) {
            event.registerItem(AshenArmorModel.ARMOR_MODEL_GETTER,
                    RegistryManager.ASHEN_GOGGLES,
                    RegistryManager.ASHEN_CLOAK,
                    RegistryManager.ASHEN_LEGGINGS,
                    RegistryManager.ASHEN_BOOTS
            );
            event.registerItem(DawnstoneShieldItem.getExtensions(), RegistryManager.DAWNSTONE_SHIELD);
            event.registerItem(AlchemicalNoteItem.getExtensions(), RegistryManager.ALCHEMICAL_NOTE);
            for (RegistryManager.FluidStuff fluid : RegistryManager.fluidList)
                if (fluid.TYPE.value() instanceof EmbersFluidType type)
                    event.registerFluidType(type.getFluidTypeExtension(), type);
        }
	}
}
