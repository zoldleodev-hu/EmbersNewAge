package hu.zoldleo.embers;

import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.api.EmbersAPI;
import hu.zoldleo.embers.api.augment.IAugment;
import hu.zoldleo.embers.augment.*;
import hu.zoldleo.embers.block.*;
import hu.zoldleo.embers.block.machine.*;
import hu.zoldleo.embers.block.storage.*;
import hu.zoldleo.embers.block.transport.ember.*;
import hu.zoldleo.embers.block.transport.fluid.FluidExtractorBlock;
import hu.zoldleo.embers.block.transport.fluid.FluidPipeBlock;
import hu.zoldleo.embers.block.transport.fluid.FluidTransferBlock;
import hu.zoldleo.embers.block.transport.item.*;
import hu.zoldleo.embers.block.upgrade.*;
import hu.zoldleo.embers.blockentity.*;
import hu.zoldleo.embers.datacomponents.*;
import hu.zoldleo.embers.datagen.EmbersFluidTags;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.entity.*;
import hu.zoldleo.embers.fluidtypes.EmbersFluidType.FluidInfo;
import hu.zoldleo.embers.fluidtypes.MoltenMetalFluidType;
import hu.zoldleo.embers.fluidtypes.SteamFluidType;
import hu.zoldleo.embers.fluidtypes.ViscousFluidType;
import hu.zoldleo.embers.gui.SlateMenu;
import hu.zoldleo.embers.item.*;
import hu.zoldleo.embers.particle.*;
import hu.zoldleo.embers.recipe.*;
import hu.zoldleo.embers.recipe.base.*;
import hu.zoldleo.embers.recipe.ingredient.AugmentIngredient;
import hu.zoldleo.embers.recipe.ingredient.HeatIngredient;
import hu.zoldleo.embers.util.*;
import hu.zoldleo.embers.worldgen.CaveStructure;
import hu.zoldleo.embers.worldgen.CrystalSeedStructureProcessor;
import hu.zoldleo.embers.worldgen.EntityMobilizerStructureProcessor;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class RegistryManager {
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Embers.MODID);
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Embers.MODID);
	public static final DeferredRegister<FluidType> FLUIDTYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Embers.MODID);
	public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, Embers.MODID);
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Embers.MODID);
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES_NEW = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Embers.MODID);
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES_OLD = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Embers.MODID_OLD);
	public static final LegacyDeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = new LegacyDeferredRegister<>(BLOCK_ENTITY_TYPES_NEW, BLOCK_ENTITY_TYPES_OLD);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Embers.MODID);
	public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, Embers.MODID);
	public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Embers.MODID);
	public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, Embers.MODID);
	public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Embers.MODID);
	public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Embers.MODID);
	public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, Embers.MODID);
	public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, Embers.MODID);
	public static final DeferredRegister<StructureProcessorType<?>> STRUCTURE_PROCESSOR_TYPES = DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, Embers.MODID);
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Embers.MODID);
    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, Embers.MODID);
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Embers.MODID);
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(BuiltInRegistries.ARMOR_MATERIAL, Embers.MODID);
    //public static final DeferredRegister<?> STRUCTURES = DeferredRegister.create(Registries.STRUCTURE, Embers.MODID);

    public static final ResourceKey<Registry<IAugment>> AUGMENT_REGISTRY_KEY = ResourceKey.createRegistryKey(Embers.res("augments"));
    public static final Registry<IAugment> AUGMENT_REGISTRY = new RegistryBuilder<>(AUGMENT_REGISTRY_KEY).sync(true).create();

	public static List<FluidStuff> fluidList = new ArrayList<>();

	public static FluidStuff addFluid(String localizedName, FluidInfo info, BiFunction<FluidType.Properties, FluidInfo, FluidType> type, BiFunction<FlowingFluid, BlockBehaviour.Properties, LiquidBlock> block, Function<BaseFlowingFluid.Properties, BaseFlowingFluid.Source> source, Function<BaseFlowingFluid.Properties, BaseFlowingFluid.Flowing> flowing, @Nullable Consumer<BaseFlowingFluid.Properties> fluidProperties, FluidType.Properties prop) {
		FluidStuff fluid = new FluidStuff(info.name, localizedName, info.color, type.apply(prop, info), block, fluidProperties, source, flowing);
		fluidList.add(fluid);
		return fluid;
	}

	public static FluidStuff addFluid(String localizedName, FluidInfo info, BiFunction<FluidType.Properties, FluidInfo, FluidType> type, BiFunction<FlowingFluid, BlockBehaviour.Properties, LiquidBlock> block, @Nullable Consumer<BaseFlowingFluid.Properties> fluidProperties, FluidType.Properties prop) {
		return addFluid(localizedName, info, type, block, BaseFlowingFluid.Source::new, BaseFlowingFluid.Flowing::new, fluidProperties, prop);
	}

	public static FluidType.Properties moltenMetalProps() {
		return FluidType.Properties.create()
				.canSwim(false)
				.canDrown(false)
				.pathType(PathType.LAVA)
				.adjacentPathType(null)
				.motionScale(0.0023333333333333335D)
				.sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
				.sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
				.lightLevel(12)
				.density(3000)
				.viscosity(6000)
				.temperature(1100);
	}

	public static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> registerEntity(String name, EntityType.Builder<T> builder) {
		return ENTITY_TYPES.register(name, () -> builder.build(Embers.MODID + ":" + name));
	}

	public static <T extends ParticleOptions> DeferredHolder<ParticleType<?>, ParticleType<T>> registerParticle(String name, boolean overrideLimiter, MapCodec<T> codec, StreamCodec<ByteBuf, T> streamCodec) {
		return PARTICLE_TYPES.register(name, () -> new ParticleType<>(overrideLimiter) {
			public @NotNull MapCodec<T> codec() { return codec; }
            public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() { return streamCodec; }
        });
	}

	public static <T extends Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> registerRecipeType(final String identifier) {
		return RECIPE_TYPES.register(identifier, () -> new RecipeType<>() {
            public String toString() {
                return Embers.MODID + ":" + identifier;
            }
        });
	}

    public static final BlockSetType CAMINITE_BLOCK_SET = new BlockSetType("caminite", true, true, false, BlockSetType.PressurePlateSensitivity.EVERYTHING, EmbersSounds.CAMINITE, SoundEvents.IRON_DOOR_CLOSE, SoundEvents.IRON_DOOR_OPEN, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN, SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON);

	//blocks
	public static final DeferredBlock<DropExperienceBlock> LEAD_ORE = BLOCKS.register("lead_ore", () -> new DropExperienceBlock(ConstantInt.ZERO, Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).requiresCorrectToolForDrops().strength(3.0f)));
	public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_LEAD_ORE = BLOCKS.register("deepslate_lead_ore", () -> new DropExperienceBlock(ConstantInt.ZERO, Properties.ofFullCopy(LEAD_ORE.get()).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)));
	public static final DeferredBlock<Block> RAW_LEAD_BLOCK = BLOCKS.register("raw_lead_block", () -> new Block(Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).sound(SoundType.METAL).requiresCorrectToolForDrops().strength(3.0F, 6.0F)));
	public static final DeferredBlock<Block> LEAD_BLOCK = BLOCKS.register("lead_block", () -> new Block(Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).sound(SoundType.METAL).requiresCorrectToolForDrops().strength(5.0F, 6.0F)));

	public static final DeferredBlock<DropExperienceBlock> SILVER_ORE = BLOCKS.register("silver_ore", () -> new DropExperienceBlock(ConstantInt.ZERO, Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).requiresCorrectToolForDrops().strength(3.0f)));
	public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_SILVER_ORE = BLOCKS.register("deepslate_silver_ore", () -> new DropExperienceBlock(ConstantInt.ZERO, Properties.ofFullCopy(SILVER_ORE.get()).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)));
	public static final DeferredBlock<Block> RAW_SILVER_BLOCK = BLOCKS.register("raw_silver_block", () -> new Block(Properties.of().mapColor(MapColor.ICE).sound(SoundType.METAL).requiresCorrectToolForDrops().strength(3.0F, 6.0F)));
	public static final DeferredBlock<Block> SILVER_BLOCK = BLOCKS.register("silver_block", () -> new Block(Properties.of().mapColor(MapColor.ICE).sound(SoundType.METAL).requiresCorrectToolForDrops().strength(5.0F, 6.0F)));

	public static final DeferredBlock<Block> DAWNSTONE_BLOCK = BLOCKS.register("dawnstone_block", () -> new Block(Properties.of().mapColor(MapColor.TERRACOTTA_YELLOW).sound(SoundType.METAL).requiresCorrectToolForDrops().strength(5.0F, 6.0F)));
	public static final DeferredBlock<MithrilBlock> MITHRIL_BLOCK = BLOCKS.register("mithril_block", () -> new MithrilBlock(Properties.of().mapColor(MapColor.GLOW_LICHEN).sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops().strength(5.0F, 6.0F).noOcclusion()));

	public static final DeferredBlock<Block> CAMINITE_BRICKS = BLOCKS.register("caminite_bricks", () -> new Block(Properties.of().mapColor(MapColor.WOOD).sound(EmbersSounds.CAMINITE).requiresCorrectToolForDrops().strength(1.6f)));
	public static final StoneDecoBlocks CAMINITE_BRICKS_DECO = new StoneDecoBlocks("caminite_bricks", CAMINITE_BRICKS, Properties.of().mapColor(MapColor.WOOD).sound(EmbersSounds.CAMINITE).requiresCorrectToolForDrops().strength(1.6f));
	public static final DeferredBlock<Block> CAMINITE_LARGE_BRICKS = BLOCKS.register("caminite_large_bricks", () -> new Block(Properties.of().mapColor(MapColor.WOOD).sound(EmbersSounds.CAMINITE).requiresCorrectToolForDrops().strength(1.6f)));
	public static final StoneDecoBlocks CAMINITE_LARGE_BRICKS_DECO = new StoneDecoBlocks("caminite_large_bricks", CAMINITE_LARGE_BRICKS, Properties.of().mapColor(MapColor.WOOD).sound(EmbersSounds.CAMINITE).requiresCorrectToolForDrops().strength(1.6f), true, true, false);
	public static final DeferredBlock<Block> RAW_CAMINITE_BLOCK = BLOCKS.register("raw_caminite_block", () -> new Block(Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).sound(SoundType.GRAVEL).requiresCorrectToolForDrops().strength(1.6f)));
	public static final DeferredBlock<Block> CAMINITE_LARGE_TILE = BLOCKS.register("caminite_large_tile", () -> new Block(Properties.of().mapColor(MapColor.WOOD).sound(EmbersSounds.CAMINITE).requiresCorrectToolForDrops().strength(1.6f)));
	public static final StoneDecoBlocks CAMINITE_LARGE_TILE_DECO = new StoneDecoBlocks("caminite_large_tile", CAMINITE_LARGE_TILE, Properties.of().mapColor(MapColor.WOOD).sound(EmbersSounds.CAMINITE).requiresCorrectToolForDrops().strength(1.6f), true, true, false);
	public static final DeferredBlock<Block> CAMINITE_TILES = BLOCKS.register("caminite_tiles", () -> new Block(Properties.of().mapColor(MapColor.WOOD).sound(EmbersSounds.CAMINITE).requiresCorrectToolForDrops().strength(1.6f)));
	public static final StoneDecoBlocks CAMINITE_TILES_DECO = new StoneDecoBlocks("caminite_tiles", CAMINITE_TILES, Properties.of().mapColor(MapColor.WOOD).sound(EmbersSounds.CAMINITE).requiresCorrectToolForDrops().strength(1.6f), true, true, false);
	public static final DeferredBlock<Block> ARCHAIC_BRICKS = BLOCKS.register("archaic_bricks", () -> new Block(Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).sound(SoundType.NETHER_BRICKS).requiresCorrectToolForDrops().strength(1.6f)));
	public static final StoneDecoBlocks ARCHAIC_BRICKS_DECO = new StoneDecoBlocks("archaic_bricks", ARCHAIC_BRICKS, Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).sound(SoundType.NETHER_BRICKS).requiresCorrectToolForDrops().strength(1.6f));
	public static final DeferredBlock<Block> ARCHAIC_EDGE = BLOCKS.register("archaic_edge", () -> new Block(Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).sound(SoundType.NETHER_BRICKS).requiresCorrectToolForDrops().strength(1.6f)));
	public static final DeferredBlock<Block> ARCHAIC_TILE = BLOCKS.register("archaic_tile", () -> new Block(Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).sound(SoundType.NETHER_BRICKS).requiresCorrectToolForDrops().strength(1.6f)));
	public static final StoneDecoBlocks ARCHAIC_TILE_DECO = new StoneDecoBlocks("archaic_tile", ARCHAIC_TILE, Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).sound(SoundType.NETHER_BRICKS).requiresCorrectToolForDrops().strength(1.6f), true, true, false);
	public static final DeferredBlock<Block> ARCHAIC_LARGE_BRICKS = BLOCKS.register("archaic_large_bricks", () -> new Block(Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).sound(SoundType.NETHER_BRICKS).requiresCorrectToolForDrops().strength(1.6f)));
	public static final StoneDecoBlocks ARCHAIC_LARGE_BRICKS_DECO = new StoneDecoBlocks("archaic_large_bricks", ARCHAIC_LARGE_BRICKS, Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).sound(SoundType.NETHER_BRICKS).requiresCorrectToolForDrops().strength(1.6f), true, true, false);
	public static final DeferredBlock<ArchaicLightBlock> ARCHAIC_LIGHT = BLOCKS.register("archaic_light", () -> new ArchaicLightBlock(Properties.of().mapColor(MapColor.COLOR_ORANGE).sound(SoundType.NETHER_BRICKS).requiresCorrectToolForDrops().strength(1.6f).lightLevel(state -> 15)));
	public static final DeferredBlock<Block> ASHEN_STONE = BLOCKS.register("ashen_stone", () -> new Block(Properties.of().mapColor(MapColor.COLOR_BLACK).sound(EmbersSounds.ASHEN_STONE).requiresCorrectToolForDrops().strength(1.6f)));
	public static final StoneDecoBlocks ASHEN_STONE_DECO = new StoneDecoBlocks("ashen_stone", ASHEN_STONE, Properties.of().mapColor(MapColor.COLOR_BLACK).sound(EmbersSounds.ASHEN_STONE).requiresCorrectToolForDrops().strength(1.6f));
	public static final DeferredBlock<Block> ASHEN_BRICK = BLOCKS.register("ashen_brick", () -> new Block(Properties.of().mapColor(MapColor.COLOR_BLACK).sound(EmbersSounds.ASHEN_STONE).requiresCorrectToolForDrops().strength(1.6f)));
	public static final StoneDecoBlocks ASHEN_BRICK_DECO = new StoneDecoBlocks("ashen_brick", ASHEN_BRICK, Properties.of().mapColor(MapColor.COLOR_BLACK).sound(EmbersSounds.ASHEN_STONE).requiresCorrectToolForDrops().strength(1.6f));
	public static final DeferredBlock<Block> ASHEN_TILE = BLOCKS.register("ashen_tile", () -> new Block(Properties.of().mapColor(MapColor.COLOR_BLACK).sound(EmbersSounds.ASHEN_STONE).requiresCorrectToolForDrops().strength(1.6f)));
	public static final StoneDecoBlocks ASHEN_TILE_DECO = new StoneDecoBlocks("ashen_tile", ASHEN_TILE, Properties.of().mapColor(MapColor.COLOR_BLACK).sound(EmbersSounds.ASHEN_STONE).requiresCorrectToolForDrops().strength(1.6f));
	public static final DeferredBlock<Block> SEALED_PLANKS = BLOCKS.register("sealed_planks", () -> new Block(Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).sound(SoundType.WOOD).strength(1.6f)));
	public static final StoneDecoBlocks SEALED_PLANKS_DECO = new StoneDecoBlocks("sealed_planks", SEALED_PLANKS, Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).sound(SoundType.WOOD).strength(1.6f), true, true, false);
	public static final DeferredBlock<Block> REINFORCED_SEALED_PLANKS = BLOCKS.register("reinforced_sealed_planks", () -> new Block(Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).sound(SoundType.WOOD).strength(2.6f)));
	public static final DeferredBlock<Block> SEALED_WOOD_TILE = BLOCKS.register("sealed_wood_tile", () -> new Block(Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).sound(SoundType.WOOD).strength(1.6f)));
	public static final StoneDecoBlocks SEALED_WOOD_TILE_DECO = new StoneDecoBlocks("sealed_wood_tile", SEALED_WOOD_TILE, Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).sound(SoundType.WOOD).strength(1.6f), true, true, false);
	public static final DeferredBlock<RotatedPillarBlock> SEALED_WOOD_PILLAR = BLOCKS.register("sealed_wood_pillar", () -> new RotatedPillarBlock(Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).sound(SoundType.WOOD).strength(1.6f)));
	public static final DeferredBlock<RotatedPillarBlock> SEALED_WOOD_KEG = BLOCKS.register("sealed_wood_keg", () -> new RotatedPillarBlock(Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).sound(SoundType.WOOD).strength(1.6f)));
	public static final DeferredBlock<Block> SOLIDIFIED_METAL = BLOCKS.register("solidified_metal", () -> new Block(Properties.of().mapColor(MapColor.COLOR_GRAY).sound(EmbersSounds.SOLID_METAL).requiresCorrectToolForDrops().strength(1.6f)));
	public static final DeferredBlock<Block> METAL_PLATFORM = BLOCKS.register("metal_platform", () -> new Block(Properties.of().mapColor(MapColor.COLOR_GRAY).sound(EmbersSounds.SOLID_METAL).requiresCorrectToolForDrops().strength(1.6f)));
	public static final StoneDecoBlocks METAL_PLATFORM_DECO = new StoneDecoBlocks("metal_platform", METAL_PLATFORM, Properties.of().mapColor(MapColor.COLOR_GRAY).sound(EmbersSounds.SOLID_METAL).requiresCorrectToolForDrops().strength(1.6f), false, true, false);
	public static final DeferredBlock<EmberLanternBlock> EMBER_LANTERN = BLOCKS.register("ember_lantern", () -> new EmberLanternBlock(Properties.of().mapColor(MapColor.NONE).sound(SoundType.LANTERN).requiresCorrectToolForDrops().strength(1.6f).lightLevel(state -> 15)));

	public static final DeferredBlock<CopperCellBlock> COPPER_CELL = BLOCKS.register("copper_cell", () -> new CopperCellBlock(Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).sound(EmbersSounds.MACHINE).requiresCorrectToolForDrops().strength(1.4f).noOcclusion().isRedstoneConductor((a, b, c) -> true)));
	public static final DeferredBlock<CreativeEmberBlock> CREATIVE_EMBER = BLOCKS.register("creative_ember_source", () -> new CreativeEmberBlock(Properties.of().mapColor(MapColor.TERRACOTTA_YELLOW).sound(EmbersSounds.SOLID_METAL).requiresCorrectToolForDrops().strength(1.6f)));
	public static final DeferredBlock<EmberDialBlock> EMBER_DIAL = BLOCKS.register("ember_dial", () -> new EmberDialBlock(Properties.of().mapColor(MapColor.NONE).sound(EmbersSounds.LIGHT_METAL).requiresCorrectToolForDrops().strength(1.6f).noOcclusion()));
	public static final DeferredBlock<ItemDialBlock> ITEM_DIAL = BLOCKS.register("item_dial", () -> new ItemDialBlock(Properties.of().mapColor(MapColor.NONE).sound(EmbersSounds.LIGHT_METAL).requiresCorrectToolForDrops().strength(1.6f).noOcclusion()));
	public static final DeferredBlock<FluidDialBlock> FLUID_DIAL = BLOCKS.register("fluid_dial", () -> new FluidDialBlock(Properties.of().mapColor(MapColor.NONE).sound(EmbersSounds.LIGHT_METAL).requiresCorrectToolForDrops().strength(1.6f).noOcclusion()));
	public static final DeferredBlock<AtmosphericGaugeBlock> ATMOSPHERIC_GAUGE = BLOCKS.register("atmospheric_gauge", () -> new AtmosphericGaugeBlock(Properties.of().mapColor(MapColor.NONE).sound(EmbersSounds.LIGHT_METAL).requiresCorrectToolForDrops().strength(1.6f).noOcclusion()));
	public static final DeferredBlock<EmberEmitterBlock> EMBER_EMITTER = BLOCKS.register("ember_emitter", () -> new EmberEmitterBlock(Properties.of().mapColor(MapColor.NONE).sound(EmbersSounds.LIGHT_MACHINE).requiresCorrectToolForDrops().strength(0.6f).noOcclusion().forceSolidOn()));
	public static final DeferredBlock<EmberReceiverBlock> EMBER_RECEIVER = BLOCKS.register("ember_receiver", () -> new EmberReceiverBlock(Properties.of().mapColor(MapColor.NONE).sound(EmbersSounds.LIGHT_MACHINE).requiresCorrectToolForDrops().strength(0.6f).noOcclusion().forceSolidOn()));
	public static final DeferredBlock<WaterloggableLeverBlock> CAMINITE_LEVER = BLOCKS.register("caminite_lever", () -> new WaterloggableLeverBlock(Properties.of().mapColor(MapColor.NONE).noCollission().sound(EmbersSounds.CAMINITE).strength(0.75f)));
	public static final DeferredBlock<WaterloggableButtonBlock> CAMINITE_BUTTON = BLOCKS.register("caminite_button", () -> new WaterloggableButtonBlock(Properties.of().mapColor(MapColor.NONE).noCollission().strength(0.5f), CAMINITE_BLOCK_SET, 20));
	public static final DeferredBlock<ItemPipeBlock> ITEM_PIPE = BLOCKS.register("item_pipe", () -> new ItemPipeBlock(Properties.of().mapColor(MapColor.NONE).sound(EmbersSounds.LIGHT_METAL).requiresCorrectToolForDrops().strength(1.6f).dynamicShape().noOcclusion().forceSolidOn()));
	public static final DeferredBlock<ItemExtractorBlock> ITEM_EXTRACTOR = BLOCKS.register("item_extractor", () -> new ItemExtractorBlock(Properties.of().mapColor(MapColor.NONE).sound(EmbersSounds.LIGHT_METAL).requiresCorrectToolForDrops().strength(1.6f).dynamicShape().noOcclusion().forceSolidOn()));
	public static final DeferredBlock<EmberBoreBlock> EMBER_BORE = BLOCKS.register("ember_bore", () -> new EmberBoreBlock(Properties.of().mapColor(MapColor.COLOR_GRAY).sound(EmbersSounds.MULTIBLOCK_CENTER).requiresCorrectToolForDrops().strength(1.6f).noOcclusion()));
	public static final DeferredBlock<EmberBoreEdgeBlock> EMBER_BORE_EDGE = BLOCKS.register("ember_bore_edge", () -> new EmberBoreEdgeBlock(Properties.of().mapColor(MapColor.WOOD).sound(EmbersSounds.MULTIBLOCK_EXTRA).requiresCorrectToolForDrops().strength(1.6f)));
	public static final DeferredBlock<MechanicalCoreBlock> MECHANICAL_CORE = BLOCKS.register("mechanical_core", () -> new MechanicalCoreBlock(Properties.of().mapColor(MapColor.COLOR_GRAY).sound(EmbersSounds.MACHINE).requiresCorrectToolForDrops().strength(1.6f).noOcclusion().isRedstoneConductor((a, b, c) -> true)));
	public static final DeferredBlock<EmberActivatorBlock> EMBER_ACTIVATOR = BLOCKS.register("ember_activator", () -> new EmberActivatorBlock(Properties.of().mapColor(MapColor.COLOR_ORANGE).sound(EmbersSounds.MACHINE).requiresCorrectToolForDrops().strength(1.6f).noOcclusion(), EmbersSounds.MULTIBLOCK_EXTRA));
	public static final DeferredBlock<MelterBlock> MELTER = BLOCKS.register("melter", () -> new MelterBlock(Properties.of().mapColor(MapColor.COLOR_GRAY).sound(EmbersSounds.MACHINE).requiresCorrectToolForDrops().strength(1.6f).noOcclusion(), EmbersSounds.MULTIBLOCK_EXTRA));
	public static final DeferredBlock<FluidPipeBlock> FLUID_PIPE = BLOCKS.register("fluid_pipe", () -> new FluidPipeBlock(Properties.of().mapColor(MapColor.NONE).sound(EmbersSounds.LIGHT_METAL).requiresCorrectToolForDrops().strength(1.6f).dynamicShape().noOcclusion().forceSolidOn()));
	public static final DeferredBlock<FluidExtractorBlock> FLUID_EXTRACTOR = BLOCKS.register("fluid_extractor", () -> new FluidExtractorBlock(Properties.of().mapColor(MapColor.NONE).sound(EmbersSounds.LIGHT_METAL).requiresCorrectToolForDrops().strength(1.6f).dynamicShape().noOcclusion().forceSolidOn()));
	public static final DeferredBlock<FluidVesselBlock> FLUID_VESSEL = BLOCKS.register("fluid_vessel", () -> new FluidVesselBlock(Properties.of().mapColor(MapColor.COLOR_GRAY).sound(EmbersSounds.MACHINE).requiresCorrectToolForDrops().strength(1.6f).noOcclusion()));
	public static final DeferredBlock<StamperBlock> STAMPER = BLOCKS.register("stamper", () -> new StamperBlock(Properties.of().mapColor(MapColor.WOOD).sound(EmbersSounds.MACHINE).requiresCorrectToolForDrops().strength(1.6f).noOcclusion()));
	public static final DeferredBlock<StampBaseBlock> STAMP_BASE = BLOCKS.register("stamp_base", () -> new StampBaseBlock(Properties.of().mapColor(MapColor.COLOR_GRAY).sound(EmbersSounds.MACHINE).requiresCorrectToolForDrops().strength(1.6f).noOcclusion()));
	public static final DeferredBlock<BinBlock> BIN = BLOCKS.register("bin", () -> new BinBlock(Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops().strength(1.6f)));
	public static final DeferredBlock<MixerCentrifugeBlock> MIXER_CENTRIFUGE = BLOCKS.register("mixer_centrifuge", () -> new MixerCentrifugeBlock(Properties.of().mapColor(MapColor.COLOR_GRAY).sound(EmbersSounds.MACHINE).requiresCorrectToolForDrops().strength(1.6f).noOcclusion(), EmbersSounds.MULTIBLOCK_EXTRA));
	public static final DeferredBlock<ItemDropperBlock> ITEM_DROPPER = BLOCKS.register("item_dropper", () -> new ItemDropperBlock(Properties.of().mapColor(MapColor.NONE).sound(EmbersSounds.LIGHT_METAL).requiresCorrectToolForDrops().strength(1.6f).forceSolidOn()));
	public static final DeferredBlock<PressureRefineryBlock> PRESSURE_REFINERY = BLOCKS.register("pressure_refinery", () -> new PressureRefineryBlock(Properties.of().mapColor(MapColor.COLOR_ORANGE).sound(EmbersSounds.MACHINE).requiresCorrectToolForDrops().strength(1.6f).noOcclusion(), EmbersSounds.MULTIBLOCK_EXTRA));
	public static final DeferredBlock<EmberEjectorBlock> EMBER_EJECTOR = BLOCKS.register("ember_ejector", () -> new EmberEjectorBlock(Properties.of().mapColor(MapColor.NONE).sound(EmbersSounds.LIGHT_MACHINE).requiresCorrectToolForDrops().strength(0.6f).noOcclusion().forceSolidOn()));
	public static final DeferredBlock<EmberFunnelBlock> EMBER_FUNNEL = BLOCKS.register("ember_funnel", () -> new EmberFunnelBlock(Properties.of().mapColor(MapColor.NONE).sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops().strength(1.6f).noOcclusion()));
	public static final DeferredBlock<EmberRelayBlock> EMBER_RELAY = BLOCKS.register("ember_relay", () -> new EmberRelayBlock(Properties.of().mapColor(MapColor.NONE).sound(EmbersSounds.LIGHT_METAL).requiresCorrectToolForDrops().strength(0.6f).noOcclusion().forceSolidOn()));
	public static final DeferredBlock<MirrorRelayBlock> MIRROR_RELAY = BLOCKS.register("mirror_relay", () -> new MirrorRelayBlock(Properties.of().mapColor(MapColor.NONE).sound(EmbersSounds.LIGHT_METAL).requiresCorrectToolForDrops().strength(0.6f).noOcclusion().forceSolidOn()));
	public static final DeferredBlock<BeamSplitterBlock> BEAM_SPLITTER = BLOCKS.register("beam_splitter", () -> new BeamSplitterBlock(Properties.of().mapColor(MapColor.NONE).sound(EmbersSounds.SOLID_METAL).requiresCorrectToolForDrops().strength(0.6f).noOcclusion().forceSolidOn()));
	public static final DeferredBlock<ItemVacuumBlock> ITEM_VACUUM = BLOCKS.register("item_vacuum", () -> new ItemVacuumBlock(Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops().strength(1.6f).noOcclusion()));
	public static final DeferredBlock<HearthCoilBlock> HEARTH_COIL = BLOCKS.register("hearth_coil", () -> new HearthCoilBlock(Properties.of().mapColor(MapColor.COLOR_ORANGE).sound(EmbersSounds.MULTIBLOCK_CENTER).requiresCorrectToolForDrops().strength(1.6f).noOcclusion()));
	public static final DeferredBlock<HearthCoilEdgeBlock> HEARTH_COIL_EDGE = BLOCKS.register("hearth_coil_edge", () -> new HearthCoilEdgeBlock(Properties.of().mapColor(MapColor.WOOD).pushReaction(PushReaction.BLOCK).sound(EmbersSounds.MULTIBLOCK_EXTRA).requiresCorrectToolForDrops().strength(1.6f)));
	public static final DeferredBlock<ReservoirBlock> RESERVOIR = BLOCKS.register("reservoir", () -> new ReservoirBlock(Properties.of().mapColor(MapColor.WOOD).sound(EmbersSounds.CAMINITE_MULTIBLOCK_CENTER).requiresCorrectToolForDrops().strength(1.6f).noOcclusion()));
	public static final DeferredBlock<ReservoirEdgeBlock> RESERVOIR_EDGE = BLOCKS.register("reservoir_edge", () -> new ReservoirEdgeBlock(Properties.of().mapColor(MapColor.WOOD).pushReaction(PushReaction.BLOCK).sound(EmbersSounds.MULTIBLOCK_EXTRA).requiresCorrectToolForDrops().strength(1.6f)));
	public static final DeferredBlock<CaminiteRingBlock> CAMINITE_RING = BLOCKS.register("caminite_ring", () -> new CaminiteRingBlock(Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.BLOCK).noCollission().sound(EmbersSounds.CAMINITE).requiresCorrectToolForDrops().strength(1.6f).noOcclusion()));
	public static final DeferredBlock<CaminiteRingEdgeBlock> CAMINITE_RING_EDGE = BLOCKS.register("caminite_ring_edge", () -> new CaminiteRingEdgeBlock(Properties.of().mapColor(MapColor.WOOD).pushReaction(PushReaction.BLOCK).sound(EmbersSounds.CAMINITE_MULTIBLOCK_EXTRA).requiresCorrectToolForDrops().strength(1.6f)));
	public static final DeferredBlock<CaminiteGaugeBlock> CAMINITE_GAUGE = BLOCKS.register("caminite_gauge", () -> new CaminiteGaugeBlock(Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.BLOCK).noCollission().sound(EmbersSounds.CAMINITE).requiresCorrectToolForDrops().strength(1.6f).noOcclusion()));
	public static final DeferredBlock<CaminiteGaugeEdgeBlock> CAMINITE_GAUGE_EDGE = BLOCKS.register("caminite_gauge_edge", () -> new CaminiteGaugeEdgeBlock(Properties.of().mapColor(MapColor.WOOD).pushReaction(PushReaction.BLOCK).sound(EmbersSounds.CAMINITE_MULTIBLOCK_EXTRA).requiresCorrectToolForDrops().strength(1.6f)));
	public static final DeferredBlock<CaminiteValveBlock> CAMINITE_VALVE = BLOCKS.register("caminite_valve", () -> new CaminiteValveBlock(Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.BLOCK).noCollission().sound(EmbersSounds.CAMINITE).requiresCorrectToolForDrops().strength(1.6f).noOcclusion()));
	public static final DeferredBlock<CaminiteValveEdgeBlock> CAMINITE_VALVE_EDGE = BLOCKS.register("caminite_valve_edge", () -> new CaminiteValveEdgeBlock(Properties.of().mapColor(MapColor.WOOD).pushReaction(PushReaction.BLOCK).sound(EmbersSounds.CAMINITE_MULTIBLOCK_EXTRA).requiresCorrectToolForDrops().strength(1.6f)));
	public static final DeferredBlock<CrystalCellBlock> CRYSTAL_CELL = BLOCKS.register("crystal_cell", () -> new CrystalCellBlock(Properties.of().mapColor(MapColor.WOOD).pushReaction(PushReaction.BLOCK).sound(EmbersSounds.MULTIBLOCK_CENTER).requiresCorrectToolForDrops().strength(1.6f).noOcclusion()));
	public static final DeferredBlock<CrystalCellEdgeBlock> CRYSTAL_CELL_EDGE = BLOCKS.register("crystal_cell_edge", () -> new CrystalCellEdgeBlock(Properties.of().mapColor(MapColor.WOOD).sound(EmbersSounds.MULTIBLOCK_EXTRA).requiresCorrectToolForDrops().strength(1.6f)));
	public static final DeferredBlock<ClockworkAttenuatorBlock> CLOCKWORK_ATTENUATOR = BLOCKS.register("clockwork_attenuator", () -> new ClockworkAttenuatorBlock(Properties.of().mapColor(MapColor.NONE).sound(EmbersSounds.LIGHT_METAL).requiresCorrectToolForDrops().strength(1.6f).noOcclusion()));
	public static final DeferredBlock<GeologicSeparatorBlock> GEOLOGIC_SEPARATOR = BLOCKS.register("geologic_separator", () -> new GeologicSeparatorBlock(Properties.of().mapColor(MapColor.COLOR_GRAY).sound(EmbersSounds.MACHINE).requiresCorrectToolForDrops().strength(1.6f).noOcclusion().forceSolidOn()));
	public static final DeferredBlock<CopperChargerBlock> COPPER_CHARGER = BLOCKS.register("copper_charger", () -> new CopperChargerBlock(Properties.of().mapColor(MapColor.COLOR_GRAY).sound(EmbersSounds.SOLID_METAL).requiresCorrectToolForDrops().strength(1.6f).noOcclusion().forceSolidOn()));
	public static final DeferredBlock<EmberSiphonBlock> EMBER_SIPHON = BLOCKS.register("ember_siphon", () -> new EmberSiphonBlock(Properties.of().mapColor(MapColor.COLOR_GRAY).sound(EmbersSounds.MACHINE).requiresCorrectToolForDrops().strength(1.6f).noOcclusion().forceSolidOn()));
	public static final DeferredBlock<ItemTransferBlock> ITEM_TRANSFER = BLOCKS.register("item_transfer", () -> new ItemTransferBlock(Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops().strength(1.6f).noOcclusion().forceSolidOn()));
	public static final DeferredBlock<FluidTransferBlock> FLUID_TRANSFER = BLOCKS.register("fluid_transfer", () -> new FluidTransferBlock(Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops().strength(1.6f).noOcclusion().forceSolidOn()));
	public static final DeferredBlock<AlchemyPedestalBlock> ALCHEMY_PEDESTAL = BLOCKS.register("alchemy_pedestal", () -> new AlchemyPedestalBlock(Properties.of().mapColor(MapColor.TERRACOTTA_YELLOW).sound(EmbersSounds.CAMINITE).requiresCorrectToolForDrops().strength(1.6f).noOcclusion().forceSolidOn(), EmbersSounds.MULTIBLOCK_EXTRA));
	public static final DeferredBlock<AlchemyTabletBlock> ALCHEMY_TABLET = BLOCKS.register("alchemy_tablet", () -> new AlchemyTabletBlock(Properties.of().mapColor(MapColor.TERRACOTTA_YELLOW).sound(EmbersSounds.MACHINE).requiresCorrectToolForDrops().strength(1.6f).noOcclusion().forceSolidOn()));
	public static final DeferredBlock<BeamCannonBlock> BEAM_CANNON = BLOCKS.register("beam_cannon", () -> new BeamCannonBlock(Properties.of().mapColor(MapColor.NONE).sound(EmbersSounds.SOLID_METAL).requiresCorrectToolForDrops().strength(1.6f).noOcclusion().forceSolidOn()));
	public static final DeferredBlock<MechanicalPumpBlock> MECHANICAL_PUMP = BLOCKS.register("mechanical_pump", () -> new MechanicalPumpBlock(Properties.of().mapColor(MapColor.COLOR_GRAY).sound(EmbersSounds.MACHINE).requiresCorrectToolForDrops().strength(1.6f).noOcclusion(), EmbersSounds.MULTIBLOCK_EXTRA));
	public static final DeferredBlock<MiniBoilerBlock> MINI_BOILER = BLOCKS.register("mini_boiler", () -> new MiniBoilerBlock(Properties.of().mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops().strength(1.6f).noOcclusion().forceSolidOn()));
	public static final DeferredBlock<CatalyticPlugBlock> CATALYTIC_PLUG = BLOCKS.register("catalytic_plug", () -> new CatalyticPlugBlock(Properties.of().mapColor(MapColor.NONE).sound(EmbersSounds.SOLID_METAL).requiresCorrectToolForDrops().strength(1.6f).noOcclusion().forceSolidOn()));
	public static final DeferredBlock<WildfireStirlingBlock> WILDFIRE_STIRLING = BLOCKS.register("wildfire_stirling", () -> new WildfireStirlingBlock(Properties.of().mapColor(MapColor.TERRACOTTA_YELLOW).sound(EmbersSounds.SOLID_METAL).requiresCorrectToolForDrops().strength(1.6f).noOcclusion().forceSolidOn()));
	public static final DeferredBlock<EmberInjectorBlock> EMBER_INJECTOR = BLOCKS.register("ember_injector", () -> new EmberInjectorBlock(Properties.of().mapColor(MapColor.COLOR_ORANGE).sound(EmbersSounds.MACHINE).requiresCorrectToolForDrops().strength(1.6f).noOcclusion().forceSolidOn()));
	public static final MetalCrystalSeed COPPER_CRYSTAL_SEED = new MetalCrystalSeed("copper");
	public static final MetalCrystalSeed IRON_CRYSTAL_SEED = new MetalCrystalSeed("iron");
	public static final MetalCrystalSeed GOLD_CRYSTAL_SEED = new MetalCrystalSeed("gold");
	public static final MetalCrystalSeed LEAD_CRYSTAL_SEED = new MetalCrystalSeed("lead");
	public static final MetalCrystalSeed SILVER_CRYSTAL_SEED = new MetalCrystalSeed("silver");
	public static final MetalCrystalSeed NICKEL_CRYSTAL_SEED = new MetalCrystalSeed("nickel");
	public static final MetalCrystalSeed TIN_CRYSTAL_SEED = new MetalCrystalSeed("tin");
	public static final MetalCrystalSeed ALUMINUM_CRYSTAL_SEED = new MetalCrystalSeed("aluminum");
	public static final MetalCrystalSeed ZINC_CRYSTAL_SEED = new MetalCrystalSeed("zinc");
	public static final MetalCrystalSeed PLATINUM_CRYSTAL_SEED = new MetalCrystalSeed("platinum");
	public static final MetalCrystalSeed URANIUM_CRYSTAL_SEED = new MetalCrystalSeed("uranium");
	public static final MetalCrystalSeed DAWNSTONE_CRYSTAL_SEED = new MetalCrystalSeed("dawnstone");
	public static final MetalCrystalSeed MITHRIL_CRYSTAL_SEED = new MetalCrystalSeed("dwarven_mithril");
	public static final DeferredBlock<Block> FIELD_CHART = BLOCKS.register("field_chart", () -> new FieldChartBlock(Properties.of().mapColor(MapColor.COLOR_ORANGE).sound(SoundType.NETHER_BRICKS).requiresCorrectToolForDrops().strength(1.6f).noOcclusion()));
	public static final DeferredBlock<Block> FIELD_CHART_EDGE = BLOCKS.register("field_chart_edge", () -> new FieldChartEdgeBlock(Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).pushReaction(PushReaction.BLOCK).sound(EmbersSounds.ARCHAIC_MULTIBLOCK_EXTRA).requiresCorrectToolForDrops().strength(1.6f)));
	public static final DeferredBlock<Block> IGNEM_REACTOR = BLOCKS.register("ignem_reactor", () -> new IgnemReactorBlock(Properties.of().mapColor(MapColor.COLOR_ORANGE).sound(EmbersSounds.MACHINE).requiresCorrectToolForDrops().strength(1.6f).noOcclusion().forceSolidOn()));
	public static final DeferredBlock<Block> CATALYSIS_CHAMBER = BLOCKS.register("catalysis_chamber", () -> new CatalysisChamberBlock(Properties.of().mapColor(MapColor.COLOR_GRAY).sound(EmbersSounds.MACHINE).requiresCorrectToolForDrops().strength(1.6f).noOcclusion().forceSolidOn(), EmbersSounds.MULTIBLOCK_EXTRA));
	public static final DeferredBlock<Block> COMBUSTION_CHAMBER = BLOCKS.register("combustion_chamber", () -> new CombustionChamberBlock(Properties.of().mapColor(MapColor.COLOR_GRAY).sound(EmbersSounds.MACHINE).requiresCorrectToolForDrops().strength(1.6f).noOcclusion().forceSolidOn(), EmbersSounds.MULTIBLOCK_EXTRA));
	public static final DeferredBlock<Block> GLIMMER = BLOCKS.register("glimmer", () -> new GlimmerBlock(Properties.of().mapColor(MapColor.COLOR_YELLOW).sound(SoundType.STONE).strength(0f).lightLevel(state -> 15).noTerrainParticles().noCollission().noOcclusion()));
	public static final DeferredBlock<Block> CINDER_PLINTH = BLOCKS.register("cinder_plinth", () -> new CinderPlinthBlock(Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).sound(EmbersSounds.MACHINE).requiresCorrectToolForDrops().strength(1.6f).noOcclusion()));
	public static final DeferredBlock<Block> DAWNSTONE_ANVIL = BLOCKS.register("dawnstone_anvil", () -> new DawnstoneAnvilBlock(Properties.of().mapColor(MapColor.TERRACOTTA_YELLOW).sound(SoundType.ANVIL).requiresCorrectToolForDrops().strength(1.6f).noOcclusion()));
	public static final DeferredBlock<Block> AUTOMATIC_HAMMER = BLOCKS.register("automatic_hammer", () -> new AutomaticHammerBlock(Properties.of().mapColor(MapColor.WOOD).sound(EmbersSounds.MACHINE).requiresCorrectToolForDrops().strength(1.6f).noOcclusion()));
	public static final DeferredBlock<Block> INFERNO_FORGE = BLOCKS.register("inferno_forge", () -> new InfernoForgeBlock(Properties.of().mapColor(MapColor.COLOR_GRAY).sound(EmbersSounds.MULTIBLOCK_CENTER).requiresCorrectToolForDrops().strength(1.6f).noOcclusion(), EmbersSounds.MULTIBLOCK_EXTRA));
	public static final DeferredBlock<Block> INFERNO_FORGE_EDGE = BLOCKS.register("inferno_forge_edge", () -> new InfernoForgeEdgeBlock(Properties.of().mapColor(MapColor.TERRACOTTA_YELLOW).sound(EmbersSounds.MULTIBLOCK_EXTRA).requiresCorrectToolForDrops().strength(1.6f)));
	public static final DeferredBlock<Block> MNEMONIC_INSCRIBER = BLOCKS.register("mnemonic_inscriber", () -> new MnemonicInscriberBlock(Properties.of().mapColor(MapColor.NONE).sound(EmbersSounds.MACHINE).requiresCorrectToolForDrops().strength(1.6f).noOcclusion().forceSolidOn()));
	public static final DeferredBlock<Block> CHAR_INSTILLER = BLOCKS.register("char_instiller", () -> new CharInstillerBlock(Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).sound(SoundType.WOOD).requiresCorrectToolForDrops().strength(1.6f).forceSolidOn()));
	public static final DeferredBlock<Block> ATMOSPHERIC_BELLOWS = BLOCKS.register("atmospheric_bellows", () -> new AtmosphericBellowsBlock(Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).sound(SoundType.WOOD).requiresCorrectToolForDrops().strength(1.6f).forceSolidOn()));
	public static final DeferredBlock<Block> ENTROPIC_ENUMERATOR = BLOCKS.register("entropic_enumerator", () -> new EntropicEnumeratorBlock(Properties.of().mapColor(MapColor.COLOR_BLACK).sound(EmbersSounds.ASHEN_STONE).requiresCorrectToolForDrops().strength(1.6f).forceSolidOn()));
	public static final DeferredBlock<Block> HEAT_EXCHANGER = BLOCKS.register("heat_exchanger", () -> new HeatExchangerBlock(Properties.of().mapColor(MapColor.COLOR_ORANGE).sound(EmbersSounds.SOLID_METAL).requiresCorrectToolForDrops().strength(1.6f).forceSolidOn()));
	public static final DeferredBlock<Block> HEAT_INSULATION = BLOCKS.register("heat_insulation", () -> new HeatInsulationBlock(Properties.of().mapColor(MapColor.COLOR_BLACK).sound(EmbersSounds.MACHINE).requiresCorrectToolForDrops().strength(1.6f).forceSolidOn()));
	public static final DeferredBlock<Block> EXCAVATION_BUCKETS = BLOCKS.register("excavation_buckets", () -> new ExcavationBucketsBlock(Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).sound(EmbersSounds.SOLID_METAL).requiresCorrectToolForDrops().strength(1.6f).forceSolidOn()));

    //armor materials
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ASHEN_ARMOR_MATERIAL = ARMOR_MATERIALS.register("ashen", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 3);
                map.put(ArmorItem.Type.LEGGINGS, 5);
                map.put(ArmorItem.Type.CHESTPLATE, 7);
                map.put(ArmorItem.Type.HELMET, 3);
                map.put(ArmorItem.Type.BODY, 3);
            }),
            18,
            SoundEvents.ARMOR_EQUIP_GENERIC,
            () -> Ingredient.of(RegistryManager.ASHEN_FABRIC),
            List.of(new ArmorMaterial.Layer(Embers.res("ashen"))),
            1,
            0
    ));

    //data components
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<HintComponent>> HINT_COMPONENT = DATA_COMPONENTS.registerComponentType("hints", builder -> builder.persistent(HintComponent.CODEC).networkSynchronized(HintComponent.STREAM_CODEC).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> INFLICTOR_CHARGE_COMPONENT = DATA_COMPONENTS.registerComponentType("inflictor_charge", builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GearsChargeComponent>> GEARS_CHARGE_COMPONENT = DATA_COMPONENTS.registerComponentType("gears_charge", builder -> builder.persistent(GearsChargeComponent.CODEC).networkSynchronized(GearsChargeComponent.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EmberComponent>> EMBER_COMPONENT = DATA_COMPONENTS.registerComponentType("ember_storage", builder -> builder.persistent(EmberComponent.CODEC).networkSynchronized(EmberComponent.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GemSocketComponent>> GEM_SOCKET_COMPONENT = DATA_COMPONENTS.registerComponentType("inflictor_gem_socket", builder -> builder.persistent(GemSocketComponent.CODEC).networkSynchronized(GemSocketComponent.STREAM_CODEC).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockTargetComponent>> BLOCK_TARGET_COMPONENT = DATA_COMPONENTS.registerComponentType("block_target", builder -> builder.persistent(BlockTargetComponent.CODEC).networkSynchronized(BlockTargetComponent.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Holder<DamageType>>> GEM_COMPONENT = DATA_COMPONENTS.registerComponentType("inflictor_gem_type", builder -> builder.persistent(DamageType.CODEC).networkSynchronized(DamageType.STREAM_CODEC).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleFluidContent>> FLUID_COMPONENT = DATA_COMPONENTS.registerComponentType("fluid", builder -> builder.persistent(SimpleFluidContent.CODEC).networkSynchronized(SimpleFluidContent.STREAM_CODEC).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> COOLDOWN_COMPONENT = DATA_COMPONENTS.registerComponentType("cooldown_time", builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EmberToolComponent>> EMBER_TOOL_COMPONENT = DATA_COMPONENTS.registerComponentType("ember_tool", builder -> builder.persistent(EmberToolComponent.CODEC).networkSynchronized(EmberToolComponent.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<HeatComponent>> HEAT_COMPONENT = DATA_COMPONENTS.registerComponentType("heat_tag", builder -> builder.persistent(HeatComponent.CODEC).networkSynchronized(HeatComponent.STREAM_CODEC));

	//items
	public static final DeferredItem<TinkerHammerItem> TINKER_HAMMER = ITEMS.register("tinker_hammer", () -> new TinkerHammerItem(new Item.Properties().stacksTo(1)));
	public static final DeferredItem<Item> TINKER_LENS = ITEMS.register("tinker_lens", () -> new Item(new Item.Properties().stacksTo(1)));
	public static final DeferredItem<Item> SMOKY_TINKER_LENS = ITEMS.register("smoky_tinker_lens", () -> new Item(new Item.Properties().stacksTo(1)));
	public static final DeferredItem<AncientCodexItem> ANCIENT_CODEX = ITEMS.register("ancient_codex", () -> new AncientCodexItem(new Item.Properties().stacksTo(1)));
	public static final DeferredItem<BlockItem> ATMOSPHERIC_GAUGE_ITEM = ITEMS.register("atmospheric_gauge", () -> new BlockItem(ATMOSPHERIC_GAUGE.get(), new Item.Properties().stacksTo(1)));
	public static final DeferredItem<EmberJarItem> EMBER_JAR = ITEMS.register("ember_jar", () -> new EmberJarItem(new Item.Properties().stacksTo(1).component(RegistryManager.EMBER_COMPONENT, new EmberComponent(0, EmberJarItem.CAPACITY))));
	public static final DeferredItem<EmberCartridgeItem> EMBER_CARTRIDGE = ITEMS.register("ember_cartridge", () -> new EmberCartridgeItem(new Item.Properties().stacksTo(1).component(RegistryManager.EMBER_COMPONENT, new EmberComponent(0, EmberCartridgeItem.CAPACITY))));
	public static final DeferredItem<ClockworkPickaxeItem> CLOCKWORK_PICKAXE = ITEMS.register("clockwork_pickaxe", () -> new ClockworkPickaxeItem(new Item.Properties().stacksTo(1)));
	public static final DeferredItem<ClockworkAxeItem> CLOCKWORK_AXE = ITEMS.register("clockwork_axe", () -> new ClockworkAxeItem(new Item.Properties().stacksTo(1)));
	public static final DeferredItem<ClockworkHammerItem> GRANDHAMMER = ITEMS.register("grandhammer", () -> new ClockworkHammerItem(new Item.Properties().stacksTo(1)));
	public static final DeferredItem<BlazingRayItem> BLAZING_RAY = ITEMS.register("blazing_ray", () -> new BlazingRayItem(new Item.Properties().stacksTo(1)));
	public static final DeferredItem<CinderStaffItem> CINDER_STAFF = ITEMS.register("cinder_staff", () -> new CinderStaffItem(new Item.Properties().stacksTo(1)));
	public static final DeferredItem<AlchemyHintItem> ALCHEMICAL_WASTE = ITEMS.register("alchemical_waste", () -> new AlchemyHintItem(new Item.Properties().stacksTo(1)));
	public static final DeferredItem<AlchemicalNoteItem> ALCHEMICAL_NOTE = ITEMS.register("alchemical_note", () -> new AlchemicalNoteItem(new Item.Properties().stacksTo(1)));
	public static final DeferredItem<CodebreakingSlateItem> CODEBREAKING_SLATE = ITEMS.register("codebreaking_slate", () -> new CodebreakingSlateItem(new Item.Properties().stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)));
	public static final DeferredItem<TyrfingItem> TYRFING = ITEMS.register("tyrfing", () -> new TyrfingItem(EmbersTiers.TYRFING, 3, -2.4f, new Item.Properties()));
	public static final DeferredItem<InflictorGemItem> INFLICTOR_GEM = ITEMS.register("inflictor_gem", () -> new InflictorGemItem(new Item.Properties().stacksTo(1)));
	public static final DeferredItem<AshenArmorGemItem> ASHEN_GOGGLES = ITEMS.register("ashen_goggles", () -> new AshenArmorGemItem(ASHEN_ARMOR_MATERIAL, ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(19)), ConfigManager.ASHEN_GOGGLES_SLOTS));
	public static final DeferredItem<AshenArmorGemItem> ASHEN_CLOAK = ITEMS.register("ashen_cloak", () -> new AshenArmorGemItem(ASHEN_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(19)), ConfigManager.ASHEN_CLOAK_SLOTS));
	public static final DeferredItem<AshenArmorGemItem> ASHEN_LEGGINGS = ITEMS.register("ashen_leggings", () -> new AshenArmorGemItem(ASHEN_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(19)), ConfigManager.ASHEN_LEGGINGS_SLOTS));
	public static final DeferredItem<AshenArmorGemItem> ASHEN_BOOTS = ITEMS.register("ashen_boots", () -> new AshenArmorGemItem(ASHEN_ARMOR_MATERIAL, ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(19)), ConfigManager.ASHEN_BOOTS_SLOTS));
	public static final DeferredItem<GlimmerCrystalItem> GLIMMER_CRYSTAL = ITEMS.register("glimmer_crystal", () -> new GlimmerCrystalItem(new Item.Properties().durability(800)));
	public static final DeferredItem<GlimmerLampItem> GLIMMER_LAMP = ITEMS.register("glimmer_lamp", () -> new GlimmerLampItem(new Item.Properties().durability(1200)));
    public static final DeferredItem<DawnstoneShieldItem> DAWNSTONE_SHIELD = ITEMS.register("dawnstone_shield", () -> new DawnstoneShieldItem(new Item.Properties().durability(336), ConfigManager.DAWNSTONE_SHIELD_SLOTS)); // TODO: tweak durability (default shield durability: 336)

	public static final DeferredItem<CopperCellBlockItem> COPPER_CELL_ITEM = ITEMS.register("copper_cell", () -> new CopperCellBlockItem(COPPER_CELL.get(), new Item.Properties().stacksTo(1).component(RegistryManager.EMBER_COMPONENT, new EmberComponent(0, CopperCellBlockEntity.CAPACITY))));
	public static final DeferredItem<BlockItem> CREATIVE_EMBER_ITEM = ITEMS.register("creative_ember_source", () -> new BlockItem(CREATIVE_EMBER.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> EMBER_DIAL_ITEM = ITEMS.register("ember_dial", () -> new BlockItem(EMBER_DIAL.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> ITEM_DIAL_ITEM = ITEMS.register("item_dial", () -> new BlockItem(ITEM_DIAL.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> FLUID_DIAL_ITEM = ITEMS.register("fluid_dial", () -> new BlockItem(FLUID_DIAL.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> EMBER_EMITTER_ITEM = ITEMS.register("ember_emitter", () -> new BlockItem(EMBER_EMITTER.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> EMBER_RECEIVER_ITEM = ITEMS.register("ember_receiver", () -> new BlockItem(EMBER_RECEIVER.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> CAMINITE_LEVER_ITEM = ITEMS.register("caminite_lever", () -> new BlockItem(CAMINITE_LEVER.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> CAMINITE_BUTTON_ITEM = ITEMS.register("caminite_button", () -> new BlockItem(CAMINITE_BUTTON.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> ITEM_PIPE_ITEM = ITEMS.register("item_pipe", () -> new BlockItem(ITEM_PIPE.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> ITEM_EXTRACTOR_ITEM = ITEMS.register("item_extractor", () -> new BlockItem(ITEM_EXTRACTOR.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> EMBER_BORE_ITEM = ITEMS.register("ember_bore", () -> new BlockItem(EMBER_BORE.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> MECHANICAL_CORE_ITEM = ITEMS.register("mechanical_core", () -> new BlockItem(MECHANICAL_CORE.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> EMBER_ACTIVATOR_ITEM = ITEMS.register("ember_activator", () -> new BlockItem(EMBER_ACTIVATOR.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> MELTER_ITEM = ITEMS.register("melter", () -> new BlockItem(MELTER.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> FLUID_PIPE_ITEM = ITEMS.register("fluid_pipe", () -> new BlockItem(FLUID_PIPE.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> FLUID_EXTRACTOR_ITEM = ITEMS.register("fluid_extractor", () -> new BlockItem(FLUID_EXTRACTOR.get(), new Item.Properties()));
	public static final DeferredItem<FluidVesselBlockItem> FLUID_VESSEL_ITEM = ITEMS.register("fluid_vessel", () -> new FluidVesselBlockItem(FLUID_VESSEL.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> STAMPER_ITEM = ITEMS.register("stamper", () -> new BlockItem(STAMPER.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> STAMP_BASE_ITEM = ITEMS.register("stamp_base", () -> new BlockItem(STAMP_BASE.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> BIN_ITEM = ITEMS.register("bin", () -> new BlockItem(BIN.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> MIXER_CENTRIFUGE_ITEM = ITEMS.register("mixer_centrifuge", () -> new BlockItem(MIXER_CENTRIFUGE.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> ITEM_DROPPER_ITEM = ITEMS.register("item_dropper", () -> new BlockItem(ITEM_DROPPER.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> PRESSURE_REFINERY_ITEM = ITEMS.register("pressure_refinery", () -> new BlockItem(PRESSURE_REFINERY.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> EMBER_EJECTOR_ITEM = ITEMS.register("ember_ejector", () -> new BlockItem(EMBER_EJECTOR.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> EMBER_FUNNEL_ITEM = ITEMS.register("ember_funnel", () -> new BlockItem(EMBER_FUNNEL.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> EMBER_RELAY_ITEM = ITEMS.register("ember_relay", () -> new BlockItem(EMBER_RELAY.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> MIRROR_RELAY_ITEM = ITEMS.register("mirror_relay", () -> new BlockItem(MIRROR_RELAY.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> BEAM_SPLITTER_ITEM = ITEMS.register("beam_splitter", () -> new BlockItem(BEAM_SPLITTER.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> ITEM_VACUUM_ITEM = ITEMS.register("item_vacuum", () -> new BlockItem(ITEM_VACUUM.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> HEARTH_COIL_ITEM = ITEMS.register("hearth_coil", () -> new BlockItem(HEARTH_COIL.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> RESERVOIR_ITEM = ITEMS.register("reservoir", () -> new BlockItem(RESERVOIR.get(), new Item.Properties()));
	public static final DeferredItem<CaminiteRingBlockItem> CAMINITE_RING_ITEM = ITEMS.register("caminite_ring", () -> new CaminiteRingBlockItem(CAMINITE_RING.get(), new Item.Properties()));
	public static final DeferredItem<CaminiteRingBlockItem> CAMINITE_GAUGE_ITEM = ITEMS.register("caminite_gauge", () -> new CaminiteRingBlockItem(CAMINITE_GAUGE.get(), new Item.Properties()));
	public static final DeferredItem<CaminiteRingBlockItem> CAMINITE_VALVE_ITEM = ITEMS.register("caminite_valve", () -> new CaminiteRingBlockItem(CAMINITE_VALVE.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> CRYSTAL_CELL_ITEM = ITEMS.register("crystal_cell", () -> new BlockItem(CRYSTAL_CELL.get(), new Item.Properties()));
	public static final DeferredItem<UpgradeItem> CLOCKWORK_ATTENUATOR_ITEM = ITEMS.register("clockwork_attenuator", () -> new UpgradeItem(CLOCKWORK_ATTENUATOR.get(), new Item.Properties()));
	public static final DeferredItem<UpgradeItem> GEOLOGIC_SEPARATOR_ITEM = ITEMS.register("geologic_separator", () -> new UpgradeItem(GEOLOGIC_SEPARATOR.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> COPPER_CHARGER_ITEM = ITEMS.register("copper_charger", () -> new BlockItem(COPPER_CHARGER.get(), new Item.Properties()));
	public static final DeferredItem<UpgradeItem> EMBER_SIPHON_ITEM = ITEMS.register("ember_siphon", () -> new UpgradeItem(EMBER_SIPHON.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> ITEM_TRANSFER_ITEM = ITEMS.register("item_transfer", () -> new BlockItem(ITEM_TRANSFER.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> FLUID_TRANSFER_ITEM = ITEMS.register("fluid_transfer", () -> new BlockItem(FLUID_TRANSFER.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> ALCHEMY_PEDESTAL_ITEM = ITEMS.register("alchemy_pedestal", () -> new BlockItem(ALCHEMY_PEDESTAL.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> ALCHEMY_TABLET_ITEM = ITEMS.register("alchemy_tablet", () -> new BlockItem(ALCHEMY_TABLET.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> BEAM_CANNON_ITEM = ITEMS.register("beam_cannon", () -> new BlockItem(BEAM_CANNON.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> MECHANICAL_PUMP_ITEM = ITEMS.register("mechanical_pump", () -> new BlockItem(MECHANICAL_PUMP.get(), new Item.Properties()));
	public static final DeferredItem<UpgradeItem> MINI_BOILER_ITEM = ITEMS.register("mini_boiler", () -> new UpgradeItem(MINI_BOILER.get(), new Item.Properties()));
	public static final DeferredItem<UpgradeItem> CATALYTIC_PLUG_ITEM = ITEMS.register("catalytic_plug", () -> new UpgradeItem(CATALYTIC_PLUG.get(), new Item.Properties()));
	public static final DeferredItem<UpgradeItem> WILDFIRE_STIRLING_ITEM = ITEMS.register("wildfire_stirling", () -> new UpgradeItem(WILDFIRE_STIRLING.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> EMBER_INJECTOR_ITEM = ITEMS.register("ember_injector", () -> new BlockItem(EMBER_INJECTOR.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> FIELD_CHART_ITEM = ITEMS.register("field_chart", () -> new BlockItem(FIELD_CHART.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> IGNEM_REACTOR_ITEM = ITEMS.register("ignem_reactor", () -> new BlockItem(IGNEM_REACTOR.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> CATALYSIS_CHAMBER_ITEM = ITEMS.register("catalysis_chamber", () -> new BlockItem(CATALYSIS_CHAMBER.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> COMBUSTION_CHAMBER_ITEM = ITEMS.register("combustion_chamber", () -> new BlockItem(COMBUSTION_CHAMBER.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> CINDER_PLINTH_ITEM = ITEMS.register("cinder_plinth", () -> new BlockItem(CINDER_PLINTH.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> DAWNSTONE_ANVIL_ITEM = ITEMS.register("dawnstone_anvil", () -> new BlockItem(DAWNSTONE_ANVIL.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> AUTOMATIC_HAMMER_ITEM = ITEMS.register("automatic_hammer", () -> new BlockItem(AUTOMATIC_HAMMER.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> INFERNO_FORGE_ITEM = ITEMS.register("inferno_forge", () -> new BlockItem(INFERNO_FORGE.get(), new Item.Properties()));
	public static final DeferredItem<UpgradeItem> MNEMONIC_INSCRIBER_ITEM = ITEMS.register("mnemonic_inscriber", () -> new UpgradeItem(MNEMONIC_INSCRIBER.get(), new Item.Properties()));
	public static final DeferredItem<UpgradeItem> CHAR_INSTILLER_ITEM = ITEMS.register("char_instiller", () -> new UpgradeItem(CHAR_INSTILLER.get(), new Item.Properties()));
	public static final DeferredItem<UpgradeItem> ATMOSPHERIC_BELLOWS_ITEM = ITEMS.register("atmospheric_bellows", () -> new UpgradeItem(ATMOSPHERIC_BELLOWS.get(), new Item.Properties()));
	public static final DeferredItem<UpgradeItem> ENTROPIC_ENUMERATOR_ITEM = ITEMS.register("entropic_enumerator", () -> new UpgradeItem(ENTROPIC_ENUMERATOR.get(), new Item.Properties()));
	public static final DeferredItem<UpgradeItem> HEAT_EXCHANGER_ITEM = ITEMS.register("heat_exchanger", () -> new UpgradeItem(HEAT_EXCHANGER.get(), new Item.Properties()));
	public static final DeferredItem<UpgradeItem> HEAT_INSULATION_ITEM = ITEMS.register("heat_insulation", () -> new UpgradeItem(HEAT_INSULATION.get(), new Item.Properties()));
	public static final DeferredItem<UpgradeItem> EXCAVATION_BUCKETS_ITEM = ITEMS.register("excavation_buckets", () -> new UpgradeItem(EXCAVATION_BUCKETS.get(), new Item.Properties()));

	public static final DeferredItem<Item> EMBER_CRYSTAL = ITEMS.register("ember_crystal", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> EMBER_SHARD = ITEMS.register("ember_shard", () -> new Item(new Item.Properties()));
	public static final DeferredItem<FuelItem> EMBER_GRIT = ITEMS.register("ember_grit", () -> new FuelItem(new Item.Properties(), 1600));
	public static final DeferredItem<Item> CAMINITE_BLEND = ITEMS.register("caminite_blend", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> CAMINITE_BRICK = ITEMS.register("caminite_brick", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> ARCHAIC_BRICK = ITEMS.register("archaic_brick", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> ANCIENT_MOTIVE_CORE = ITEMS.register("ancient_motive_core", () -> new Item(new Item.Properties()));
	public static final DeferredItem<FuelItem> ASH = ITEMS.register("ash", () -> new FuelItem(new Item.Properties(), 200));
	public static final DeferredItem<Item> ASHEN_FABRIC = ITEMS.register("ashen_fabric", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> EMBER_CRYSTAL_CLUSTER = ITEMS.register("ember_crystal_cluster", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> WILDFIRE_CORE = ITEMS.register("wildfire_core", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> ISOLATED_MATERIA = ITEMS.register("isolated_materia", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> ADHESIVE = ITEMS.register("adhesive", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> ARCHAIC_CIRCUIT = ITEMS.register("archaic_circuit", () -> new Item(new Item.Properties()));

	public static final DeferredItem<Item> SUPERHEATER = ITEMS.register("superheater", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> CINDER_JET = ITEMS.register("cinder_jet", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> MUSIC_DISC_7F_PATTERNS = ITEMS.register("music_disc_7f_patterns", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(EmbersSounds.ULTRASYD_7F_PATTERNS_SONG)));
	public static final DeferredItem<Item> BLASTING_CORE = ITEMS.register("blasting_core", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> CASTER_ORB = ITEMS.register("caster_orb", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> RESONATING_BELL = ITEMS.register("resonating_bell", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> FLAME_BARRIER = ITEMS.register("flame_barrier", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> ELDRITCH_INSIGNIA = ITEMS.register("eldritch_insignia", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> INTELLIGENT_APPARATUS = ITEMS.register("intelligent_apparatus", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> DIFFRACTION_BARREL = ITEMS.register("diffraction_barrel", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> FOCAL_LENS = ITEMS.register("focal_lens", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> SHIFTING_SCALES = ITEMS.register("shifting_scales", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> WINDING_GEARS = ITEMS.register("winding_gears", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> EMBER_REDIRECTION_MODULE = ITEMS.register("ember_redirection_module", () -> new Item(new Item.Properties()));

	static { COPPER_CRYSTAL_SEED.makeItem(); }
	static { IRON_CRYSTAL_SEED.makeItem(); }
	static { GOLD_CRYSTAL_SEED.makeItem(); }
	static { LEAD_CRYSTAL_SEED.makeItem(); }
	static { SILVER_CRYSTAL_SEED.makeItem(); }
	static { NICKEL_CRYSTAL_SEED.makeItem(); }
	static { TIN_CRYSTAL_SEED.makeItem(); }
	static { ALUMINUM_CRYSTAL_SEED.makeItem(); }
	static { ZINC_CRYSTAL_SEED.makeItem(); }
	static { PLATINUM_CRYSTAL_SEED.makeItem(); }
	static { URANIUM_CRYSTAL_SEED.makeItem(); }
	static { DAWNSTONE_CRYSTAL_SEED.makeItem(); }
	static { MITHRIL_CRYSTAL_SEED.makeItem(); }

	public static final DeferredItem<Item> RAW_CAMINITE_PLATE = ITEMS.register("raw_caminite_plate", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> RAW_FLAT_STAMP = ITEMS.register("raw_flat_stamp", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> RAW_INGOT_STAMP = ITEMS.register("raw_ingot_stamp", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> RAW_NUGGET_STAMP = ITEMS.register("raw_nugget_stamp", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> RAW_PLATE_STAMP = ITEMS.register("raw_plate_stamp", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> RAW_GEAR_STAMP = ITEMS.register("raw_gear_stamp", () -> new Item(new Item.Properties()));

	public static final DeferredItem<Item> CAMINITE_PLATE = ITEMS.register("caminite_plate", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> FLAT_STAMP = ITEMS.register("flat_stamp", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> INGOT_STAMP = ITEMS.register("ingot_stamp", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> NUGGET_STAMP = ITEMS.register("nugget_stamp", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> PLATE_STAMP = ITEMS.register("plate_stamp", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> GEAR_STAMP = ITEMS.register("gear_stamp", () -> new Item(new Item.Properties()));

	public static final DeferredItem<Item> IRON_ASPECTUS = ITEMS.register("iron_aspectus", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> COPPER_ASPECTUS = ITEMS.register("copper_aspectus", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> LEAD_ASPECTUS = ITEMS.register("lead_aspectus", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> SILVER_ASPECTUS = ITEMS.register("silver_aspectus", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> DAWNSTONE_ASPECTUS = ITEMS.register("dawnstone_aspectus", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> MITHRIL_ASPECTUS = ITEMS.register("mithril_aspectus", () -> new Item(new Item.Properties()));

	public static final DeferredItem<Item> IRON_PLATE = ITEMS.register("iron_plate", () -> new Item(new Item.Properties()));
	//public static final RegistryObject<Item> GOLD_PLATE = ITEMS.register("gold_plate", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> COPPER_PLATE = ITEMS.register("copper_plate", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> COPPER_NUGGET = ITEMS.register("copper_nugget", () -> new Item(new Item.Properties()));

	public static final DeferredItem<Item> RAW_LEAD = ITEMS.register("raw_lead", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> LEAD_INGOT = ITEMS.register("lead_ingot", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> LEAD_NUGGET = ITEMS.register("lead_nugget", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> LEAD_PLATE = ITEMS.register("lead_plate", () -> new Item(new Item.Properties()));

	public static final DeferredItem<BlockItem> LEAD_ORE_ITEM = ITEMS.register("lead_ore", () -> new BlockItem(LEAD_ORE.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> DEEPSLATE_LEAD_ORE_ITEM = ITEMS.register("deepslate_lead_ore", () -> new BlockItem(DEEPSLATE_LEAD_ORE.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> RAW_LEAD_BLOCK_ITEM = ITEMS.register("raw_lead_block", () -> new BlockItem(RAW_LEAD_BLOCK.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> LEAD_BLOCK_ITEM = ITEMS.register("lead_block", () -> new BlockItem(LEAD_BLOCK.get(), new Item.Properties()));

	public static final DeferredItem<Item> RAW_SILVER = ITEMS.register("raw_silver", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> SILVER_INGOT = ITEMS.register("silver_ingot", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> SILVER_NUGGET = ITEMS.register("silver_nugget", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> SILVER_PLATE = ITEMS.register("silver_plate", () -> new Item(new Item.Properties()));

	public static final DeferredItem<BlockItem> SILVER_ORE_ITEM = ITEMS.register("silver_ore", () -> new BlockItem(SILVER_ORE.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> DEEPSLATE_SILVER_ORE_ITEM = ITEMS.register("deepslate_silver_ore", () -> new BlockItem(DEEPSLATE_SILVER_ORE.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> RAW_SILVER_BLOCK_ITEM = ITEMS.register("raw_silver_block", () -> new BlockItem(RAW_SILVER_BLOCK.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> SILVER_BLOCK_ITEM = ITEMS.register("silver_block", () -> new BlockItem(SILVER_BLOCK.get(), new Item.Properties()));

	public static final DeferredItem<Item> DAWNSTONE_INGOT = ITEMS.register("dawnstone_ingot", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> DAWNSTONE_NUGGET = ITEMS.register("dawnstone_nugget", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> DAWNSTONE_PLATE = ITEMS.register("dawnstone_plate", () -> new Item(new Item.Properties()));

	public static final DeferredItem<Item> MITHRIL_INGOT = ITEMS.register("mithril_ingot", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> MITHRIL_NUGGET = ITEMS.register("mithril_nugget", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> MITHRIL_PLATE = ITEMS.register("mithril_plate", () -> new Item(new Item.Properties()));

	public static final DeferredItem<BlockItem> DAWNSTONE_BLOCK_ITEM = ITEMS.register("dawnstone_block", () -> new BlockItem(DAWNSTONE_BLOCK.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> MITHRIL_BLOCK_ITEM = ITEMS.register("mithril_block", () -> new BlockItem(MITHRIL_BLOCK.get(), new Item.Properties()));

	public static final ToolSet LEAD_TOOLS = new ToolSet("lead", EmbersTiers.LEAD);
	public static final ToolSet SILVER_TOOLS = new ToolSet("silver", EmbersTiers.SILVER);
	public static final ToolSet DAWNSTONE_TOOLS = new ToolSet("dawnstone", EmbersTiers.DAWNSTONE);

	public static final DeferredItem<BlockItem> CAMINITE_BRICKS_ITEM = ITEMS.register("caminite_bricks", () -> new BlockItem(CAMINITE_BRICKS.get(), new Item.Properties()));
	static { CAMINITE_BRICKS_DECO.makeItems(); }
	public static final DeferredItem<BlockItem> CAMINITE_LARGE_BRICKS_ITEM = ITEMS.register("caminite_large_bricks", () -> new BlockItem(CAMINITE_LARGE_BRICKS.get(), new Item.Properties()));
	static { CAMINITE_LARGE_BRICKS_DECO.makeItems(); }
	public static final DeferredItem<BlockItem> RAW_CAMINITE_BLOCK_ITEM = ITEMS.register("raw_caminite_block", () -> new BlockItem(RAW_CAMINITE_BLOCK.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> CAMINITE_LARGE_TILE_ITEM = ITEMS.register("caminite_large_tile", () -> new BlockItem(CAMINITE_LARGE_TILE.get(), new Item.Properties()));
	static { CAMINITE_LARGE_TILE_DECO.makeItems(); }
	public static final DeferredItem<BlockItem> CAMINITE_TILES_ITEM = ITEMS.register("caminite_tiles", () -> new BlockItem(CAMINITE_TILES.get(), new Item.Properties()));
	static { CAMINITE_TILES_DECO.makeItems(); }
	public static final DeferredItem<BlockItem> ARCHAIC_BRICKS_ITEM = ITEMS.register("archaic_bricks", () -> new BlockItem(ARCHAIC_BRICKS.get(), new Item.Properties()));
	static { ARCHAIC_BRICKS_DECO.makeItems(); }
	public static final DeferredItem<BlockItem> ARCHAIC_EDGE_ITEM = ITEMS.register("archaic_edge", () -> new BlockItem(ARCHAIC_EDGE.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> ARCHAIC_TILE_ITEM = ITEMS.register("archaic_tile", () -> new BlockItem(ARCHAIC_TILE.get(), new Item.Properties()));
	static { ARCHAIC_TILE_DECO.makeItems(); }
	public static final DeferredItem<BlockItem> ARCHAIC_LARGE_BRICKS_ITEM = ITEMS.register("archaic_large_bricks", () -> new BlockItem(ARCHAIC_LARGE_BRICKS.get(), new Item.Properties()));
	static { ARCHAIC_LARGE_BRICKS_DECO.makeItems(); }
	public static final DeferredItem<BlockItem> ARCHAIC_LIGHT_ITEM = ITEMS.register("archaic_light", () -> new BlockItem(ARCHAIC_LIGHT.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> ASHEN_STONE_ITEM = ITEMS.register("ashen_stone", () -> new BlockItem(ASHEN_STONE.get(), new Item.Properties()));
	static { ASHEN_STONE_DECO.makeItems(); }
	public static final DeferredItem<BlockItem> ASHEN_BRICK_ITEM = ITEMS.register("ashen_brick", () -> new BlockItem(ASHEN_BRICK.get(), new Item.Properties()));
	static { ASHEN_BRICK_DECO.makeItems(); }
	public static final DeferredItem<BlockItem> ASHEN_TILE_ITEM = ITEMS.register("ashen_tile", () -> new BlockItem(ASHEN_TILE.get(), new Item.Properties()));
	static { ASHEN_TILE_DECO.makeItems(); }
	public static final DeferredItem<BlockItem> SEALED_PLANKS_ITEM = ITEMS.register("sealed_planks", () -> new BlockItem(SEALED_PLANKS.get(), new Item.Properties()));
	static { SEALED_PLANKS_DECO.makeItems(); }
	public static final DeferredItem<BlockItem> REINFORCED_SEALED_PLANKS_ITEM = ITEMS.register("reinforced_sealed_planks", () -> new BlockItem(REINFORCED_SEALED_PLANKS.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> SEALED_WOOD_TILE_ITEM = ITEMS.register("sealed_wood_tile", () -> new BlockItem(SEALED_WOOD_TILE.get(), new Item.Properties()));
	static { SEALED_WOOD_TILE_DECO.makeItems(); }
	public static final DeferredItem<BlockItem> SEALED_WOOD_PILLAR_ITEM = ITEMS.register("sealed_wood_pillar", () -> new BlockItem(SEALED_WOOD_PILLAR.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> SEALED_WOOD_KEG_ITEM = ITEMS.register("sealed_wood_keg", () -> new BlockItem(SEALED_WOOD_KEG.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> SOLIDIFIED_METAL_ITEM = ITEMS.register("solidified_metal", () -> new BlockItem(SOLIDIFIED_METAL.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> METAL_PLATFORM_ITEM = ITEMS.register("metal_platform", () -> new BlockItem(METAL_PLATFORM.get(), new Item.Properties()));
	static { METAL_PLATFORM_DECO.makeItems(); }
	public static final DeferredItem<BlockItem> EMBER_LANTERN_ITEM = ITEMS.register("ember_lantern", () -> new BlockItem(EMBER_LANTERN.get(), new Item.Properties()));

	//fluids
	public static final FluidStuff MOLTEN_IRON = addFluid("Molten Iron", new FluidInfo("molten_iron", 0xC72913, 0.1F, 1.5F), MoltenMetalFluidType::new, LiquidBlock::new,
			prop -> prop.explosionResistance(1000F).tickRate(30).slopeFindDistance(2).levelDecreasePerBlock(2), moltenMetalProps());

	public static final FluidStuff MOLTEN_GOLD = addFluid("Molten Gold", new FluidInfo("molten_gold", 0xF9C026, 0.1F, 1.5F), MoltenMetalFluidType::new, LiquidBlock::new,
			prop -> prop.explosionResistance(1000F).tickRate(30).slopeFindDistance(2).levelDecreasePerBlock(2), moltenMetalProps());

	public static final FluidStuff MOLTEN_COPPER = addFluid("Molten Copper", new FluidInfo("molten_copper", 0xEA7E38, 0.1F, 1.5F), MoltenMetalFluidType::new, LiquidBlock::new,
			prop -> prop.explosionResistance(1000F).tickRate(30).slopeFindDistance(2).levelDecreasePerBlock(2), moltenMetalProps());

	public static final FluidStuff MOLTEN_LEAD = addFluid("Molten Lead", new FluidInfo("molten_lead", 0x665975, 0.1F, 1.5F), MoltenMetalFluidType::new, LiquidBlock::new,
			prop -> prop.explosionResistance(1000F).tickRate(30).slopeFindDistance(2).levelDecreasePerBlock(2), moltenMetalProps());

	public static final FluidStuff MOLTEN_SILVER = addFluid("Molten Silver", new FluidInfo("molten_silver", 0xBCEAF7, 0.1F, 1.5F), MoltenMetalFluidType::new, LiquidBlock::new,
			prop -> prop.explosionResistance(1000F).tickRate(30).slopeFindDistance(2).levelDecreasePerBlock(2), moltenMetalProps());

	public static final FluidStuff MOLTEN_DAWNSTONE = addFluid("Molten Dawnstone", new FluidInfo("molten_dawnstone", 0xFF9C36, 0.1F, 1.5F), MoltenMetalFluidType::new, LiquidBlock::new,
			prop -> prop.explosionResistance(1000F).tickRate(30).slopeFindDistance(2).levelDecreasePerBlock(2), moltenMetalProps());

	public static final FluidStuff MOLTEN_MITHRIL = addFluid("Molten Mithril", new FluidInfo("molten_mithril", 0xC8EBA2, 0.1F, 1.5F), MoltenMetalFluidType::new, LiquidBlock::new,
			prop -> prop.explosionResistance(1000F).tickRate(30).slopeFindDistance(2).levelDecreasePerBlock(2), moltenMetalProps());

	public static final FluidStuff MOLTEN_NICKEL = addFluid("Molten Nickel", new FluidInfo("molten_nickel", 0xDDEBC0, 0.1F, 1.5F), MoltenMetalFluidType::new, LiquidBlock::new,
			prop -> prop.explosionResistance(1000F).tickRate(30).slopeFindDistance(2).levelDecreasePerBlock(2), moltenMetalProps());

	public static final FluidStuff MOLTEN_TIN = addFluid("Molten Tin", new FluidInfo("molten_tin", 0xDCEDE5, 0.1F, 1.5F), MoltenMetalFluidType::new, LiquidBlock::new,
			prop -> prop.explosionResistance(1000F).tickRate(30).slopeFindDistance(2).levelDecreasePerBlock(2), moltenMetalProps());

	public static final FluidStuff MOLTEN_ALUMINUM = addFluid("Molten Aluminum", new FluidInfo("molten_aluminum", 0xFFAE9C, 0.1F, 1.5F), MoltenMetalFluidType::new, LiquidBlock::new,
			prop -> prop.explosionResistance(1000F).tickRate(30).slopeFindDistance(2).levelDecreasePerBlock(2), moltenMetalProps());

	public static final FluidStuff MOLTEN_ZINC = addFluid("Molten Zinc", new FluidInfo("molten_zinc", 0x8C8D7B, 0.1F, 1.5F), MoltenMetalFluidType::new, LiquidBlock::new,
			prop -> prop.explosionResistance(1000F).tickRate(30).slopeFindDistance(2).levelDecreasePerBlock(2), moltenMetalProps());

	public static final FluidStuff MOLTEN_PLATINUM = addFluid("Molten Platinum", new FluidInfo("molten_platinum", 0xA6DEE8, 0.1F, 1.5F), MoltenMetalFluidType::new, LiquidBlock::new,
			prop -> prop.explosionResistance(1000F).tickRate(30).slopeFindDistance(2).levelDecreasePerBlock(2), moltenMetalProps());

	public static final FluidStuff MOLTEN_URANIUM = addFluid("Molten Uranium", new FluidInfo("molten_uranium", 0x314630, 0.1F, 1.5F), MoltenMetalFluidType::new, LiquidBlock::new,
			prop -> prop.explosionResistance(1000F).tickRate(30).slopeFindDistance(2).levelDecreasePerBlock(2), moltenMetalProps());

	public static final FluidStuff MOLTEN_BRONZE = addFluid("Molten Bronze", new FluidInfo("molten_bronze", 0xEDAE66, 0.1F, 1.5F), MoltenMetalFluidType::new, LiquidBlock::new,
			prop -> prop.explosionResistance(1000F).tickRate(30).slopeFindDistance(2).levelDecreasePerBlock(2), moltenMetalProps());

	public static final FluidStuff MOLTEN_ELECTRUM = addFluid("Molten Electrum", new FluidInfo("molten_electrum", 0xFAE176, 0.1F, 1.5F), MoltenMetalFluidType::new, LiquidBlock::new,
			prop -> prop.explosionResistance(1000F).tickRate(30).slopeFindDistance(2).levelDecreasePerBlock(2), moltenMetalProps());

	public static final FluidStuff MOLTEN_BRASS = addFluid("Molten Brass", new FluidInfo("molten_brass", 0xD3A756, 0.1F, 1.5F), MoltenMetalFluidType::new, LiquidBlock::new,
			prop -> prop.explosionResistance(1000F).tickRate(30).slopeFindDistance(2).levelDecreasePerBlock(2), moltenMetalProps());

	public static final FluidStuff MOLTEN_CONSTANTAN = addFluid("Molten Constantan", new FluidInfo("molten_constantan", 0xB55C46, 0.1F, 1.5F), MoltenMetalFluidType::new, LiquidBlock::new,
			prop -> prop.explosionResistance(1000F).tickRate(30).slopeFindDistance(2).levelDecreasePerBlock(2), moltenMetalProps());

	public static final FluidStuff MOLTEN_INVAR = addFluid("Molten Invar", new FluidInfo("molten_invar", 0xB1D0D1, 0.1F, 1.5F), MoltenMetalFluidType::new, LiquidBlock::new,
			prop -> prop.explosionResistance(1000F).tickRate(30).slopeFindDistance(2).levelDecreasePerBlock(2), moltenMetalProps());

	public static final FluidStuff STEAM = addFluid("Steam", new FluidInfo("steam", 0xFFFCFC, 0.1F, 1.5F), SteamFluidType::new, LiquidBlock::new,
			prop -> prop.explosionResistance(1000F).tickRate(3),
			FluidType.Properties.create()
			.canSwim(false)
			.canDrown(false)
			.pathType(PathType.LAVA)
			.adjacentPathType(null)
			.motionScale(0.0005D)
			.canPushEntity(false)
			.canHydrate(true)
			.sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
			.sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
			.density(-1000)
			.viscosity(100)
			.temperature(400));

	public static final FluidStuff SOUL_CRUDE = addFluid("Soul Crude", new FluidInfo("soul_crude", 0x35261D, 0.1F, 1.5F), ViscousFluidType::new, LiquidBlock::new,
			prop -> prop.explosionResistance(1000F).tickRate(30).slopeFindDistance(2).levelDecreasePerBlock(2),
			FluidType.Properties.create()
			.canSwim(false)
			.canDrown(true)
			.motionScale(0.0023333333333333335D)
			.sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
			.sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
			.density(3000)
			.viscosity(4000)
			.temperature(330));

	public static final FluidStuff DWARVEN_OIL = addFluid("Dwarven Oil", new FluidInfo("dwarven_oil", 0xffDB18, 0.1F, 1.5F), ViscousFluidType::new, LiquidBlock::new,
			prop -> prop.explosionResistance(1000F).tickRate(8).slopeFindDistance(2).levelDecreasePerBlock(2),
			FluidType.Properties.create()
			.canSwim(false)
			.canDrown(true)
			.motionScale(0.0023333333333333335D)
			.sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
			.sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
			.density(2000)
			.viscosity(2000)
			.temperature(330));

	public static final FluidStuff DWARVEN_GAS = addFluid("Dwarven Gas", new FluidInfo("dwarven_gas", 0x99DC4D, 0.1F, 1.5F), SteamFluidType::new, LiquidBlock::new,
			prop -> prop.explosionResistance(1000F).tickRate(3).slopeFindDistance(2).levelDecreasePerBlock(2),
			FluidType.Properties.create()
			.canSwim(false)
			.canDrown(false)
			.pathType(PathType.LAVA)
			.adjacentPathType(null)
			.motionScale(0.0005D)
			.canPushEntity(false)
			.sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
			.sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
			.density(-100)
			.viscosity(100)
			.temperature(400));

	//block entities
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CopperCellBlockEntity>> COPPER_CELL_ENTITY = registerBlockEntity("copper_cell", () -> BlockEntityType.Builder.of(CopperCellBlockEntity::new, COPPER_CELL.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeEmberBlockEntity>> CREATIVE_EMBER_ENTITY = registerBlockEntity("creative_ember_source", () -> BlockEntityType.Builder.of(CreativeEmberBlockEntity::new, CREATIVE_EMBER.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EmberDialBlockEntity>> EMBER_DIAL_ENTITY = registerBlockEntity("ember_dial", () -> BlockEntityType.Builder.of(EmberDialBlockEntity::new, EMBER_DIAL.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemDialBlockEntity>> ITEM_DIAL_ENTITY = registerBlockEntity("item_dial", () -> BlockEntityType.Builder.of(ItemDialBlockEntity::new, ITEM_DIAL.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidDialBlockEntity>> FLUID_DIAL_ENTITY = registerBlockEntity("fluid_dial", () -> BlockEntityType.Builder.of(FluidDialBlockEntity::new, FLUID_DIAL.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AtmosphericGaugeBlockEntity>> ATMOSPHERIC_GAUGE_ENTITY = registerBlockEntity("atmospheric_gauge", () -> BlockEntityType.Builder.of(AtmosphericGaugeBlockEntity::new, ATMOSPHERIC_GAUGE.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EmberEmitterBlockEntity>> EMBER_EMITTER_ENTITY = registerBlockEntity("ember_emitter", () -> BlockEntityType.Builder.of(EmberEmitterBlockEntity::new, EMBER_EMITTER.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EmberReceiverBlockEntity>> EMBER_RECEIVER_ENTITY = registerBlockEntity("ember_receiver", () -> BlockEntityType.Builder.of(EmberReceiverBlockEntity::new, EMBER_RECEIVER.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemPipeBlockEntity>> ITEM_PIPE_ENTITY = registerBlockEntity("item_pipe", () -> BlockEntityType.Builder.of(ItemPipeBlockEntity::new, ITEM_PIPE.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemExtractorBlockEntity>> ITEM_EXTRACTOR_ENTITY = registerBlockEntity("item_extractor", () -> BlockEntityType.Builder.of(ItemExtractorBlockEntity::new, ITEM_EXTRACTOR.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EmberBoreBlockEntity>> EMBER_BORE_ENTITY = registerBlockEntity("ember_bore", () -> BlockEntityType.Builder.of(EmberBoreBlockEntity::new, EMBER_BORE.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MechanicalCoreBlockEntity>> MECHANICAL_CORE_ENTITY = registerBlockEntity("mechanical_core", () -> BlockEntityType.Builder.of(MechanicalCoreBlockEntity::new, MECHANICAL_CORE.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EmberActivatorBottomBlockEntity>> EMBER_ACTIVATOR_BOTTOM_ENTITY = registerBlockEntity("ember_activator_bottom", () -> BlockEntityType.Builder.of(EmberActivatorBottomBlockEntity::new, EMBER_ACTIVATOR.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EmberActivatorTopBlockEntity>> EMBER_ACTIVATOR_TOP_ENTITY = registerBlockEntity("ember_activator_top", () -> BlockEntityType.Builder.of(EmberActivatorTopBlockEntity::new, EMBER_ACTIVATOR.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MelterBottomBlockEntity>> MELTER_BOTTOM_ENTITY = registerBlockEntity("melter_bottom", () -> BlockEntityType.Builder.of(MelterBottomBlockEntity::new, MELTER.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MelterTopBlockEntity>> MELTER_TOP_ENTITY = registerBlockEntity("melter_top", () -> BlockEntityType.Builder.of(MelterTopBlockEntity::new, MELTER.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidPipeBlockEntity>> FLUID_PIPE_ENTITY = registerBlockEntity("fluid_pipe", () -> BlockEntityType.Builder.of(FluidPipeBlockEntity::new, FLUID_PIPE.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidExtractorBlockEntity>> FLUID_EXTRACTOR_ENTITY = registerBlockEntity("fluid_extractor", () -> BlockEntityType.Builder.of(FluidExtractorBlockEntity::new, FLUID_EXTRACTOR.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidVesselBlockEntity>> FLUID_VESSEL_ENTITY = registerBlockEntity("fluid_vesel", () -> BlockEntityType.Builder.of(FluidVesselBlockEntity::new, FLUID_VESSEL.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StamperBlockEntity>> STAMPER_ENTITY = registerBlockEntity("stamper", () -> BlockEntityType.Builder.of(StamperBlockEntity::new, STAMPER.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StampBaseBlockEntity>> STAMP_BASE_ENTITY = registerBlockEntity("stamp_base", () -> BlockEntityType.Builder.of(StampBaseBlockEntity::new, STAMP_BASE.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BinBlockEntity>> BIN_ENTITY = registerBlockEntity("bin", () -> BlockEntityType.Builder.of(BinBlockEntity::new, BIN.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MixerCentrifugeBottomBlockEntity>> MIXER_CENTRIFUGE_BOTTOM_ENTITY = registerBlockEntity("mixer_centrifuge_bottom", () -> BlockEntityType.Builder.of(MixerCentrifugeBottomBlockEntity::new, MIXER_CENTRIFUGE.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MixerCentrifugeTopBlockEntity>> MIXER_CENTRIFUGE_TOP_ENTITY = registerBlockEntity("mixer_centrifuge_top", () -> BlockEntityType.Builder.of(MixerCentrifugeTopBlockEntity::new, MIXER_CENTRIFUGE.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemDropperBlockEntity>> ITEM_DROPPER_ENTITY = registerBlockEntity("item_dropper", () -> BlockEntityType.Builder.of(ItemDropperBlockEntity::new, ITEM_DROPPER.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PressureRefineryBottomBlockEntity>> PRESSURE_REFINERY_BOTTOM_ENTITY = registerBlockEntity("pressure_refinery_bottom", () -> BlockEntityType.Builder.of(PressureRefineryBottomBlockEntity::new, PRESSURE_REFINERY.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PressureRefineryTopBlockEntity>> PRESSURE_REFINERY_TOP_ENTITY = registerBlockEntity("pressure_refinery_top", () -> BlockEntityType.Builder.of(PressureRefineryTopBlockEntity::new, PRESSURE_REFINERY.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EmberEjectorBlockEntity>> EMBER_EJECTOR_ENTITY = registerBlockEntity("ember_ejector", () -> BlockEntityType.Builder.of(EmberEjectorBlockEntity::new, EMBER_EJECTOR.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EmberFunnelBlockEntity>> EMBER_FUNNEL_ENTITY = registerBlockEntity("ember_funnel", () -> BlockEntityType.Builder.of(EmberFunnelBlockEntity::new, EMBER_FUNNEL.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EmberRelayBlockEntity>> EMBER_RELAY_ENTITY = registerBlockEntity("ember_relay", () -> BlockEntityType.Builder.of(EmberRelayBlockEntity::new, EMBER_RELAY.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MirrorRelayBlockEntity>> MIRROR_RELAY_ENTITY = registerBlockEntity("mirror_relay", () -> BlockEntityType.Builder.of(MirrorRelayBlockEntity::new, MIRROR_RELAY.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BeamSplitterBlockEntity>> BEAM_SPLITTER_ENTITY = registerBlockEntity("beam_splitter", () -> BlockEntityType.Builder.of(BeamSplitterBlockEntity::new, BEAM_SPLITTER.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemVacuumBlockEntity>> ITEM_VACUUM_ENTITY = registerBlockEntity("item_vacuum", () -> BlockEntityType.Builder.of(ItemVacuumBlockEntity::new, ITEM_VACUUM.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HearthCoilBlockEntity>> HEARTH_COIL_ENTITY = registerBlockEntity("hearth_coil", () -> BlockEntityType.Builder.of(HearthCoilBlockEntity::new, HEARTH_COIL.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReservoirBlockEntity>> RESERVOIR_ENTITY = registerBlockEntity("reservoir", () -> BlockEntityType.Builder.of(ReservoirBlockEntity::new, RESERVOIR.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CaminiteValveBlockEntity>> CAMINITE_VALVE_ENTITY = registerBlockEntity("caminite_valve", () -> BlockEntityType.Builder.of(CaminiteValveBlockEntity::new, CAMINITE_VALVE_EDGE.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrystalCellBlockEntity>> CRYSTAL_CELL_ENTITY = registerBlockEntity("crystal_cell", () -> BlockEntityType.Builder.of(CrystalCellBlockEntity::new, CRYSTAL_CELL.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClockworkAttenuatorBlockEntity>> CLOCKWORK_ATTENUATOR_ENTITY = registerBlockEntity("clockwork_attenuator", () -> BlockEntityType.Builder.of(ClockworkAttenuatorBlockEntity::new, CLOCKWORK_ATTENUATOR.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeologicSeparatorBlockEntity>> GEOLOGIC_SEPARATOR_ENTITY = registerBlockEntity("geologic_separator", () -> BlockEntityType.Builder.of(GeologicSeparatorBlockEntity::new, GEOLOGIC_SEPARATOR.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CopperChargerBlockEntity>> COPPER_CHARGER_ENTITY = registerBlockEntity("copper_charger", () -> BlockEntityType.Builder.of(CopperChargerBlockEntity::new, COPPER_CHARGER.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EmberSiphonBlockEntity>> EMBER_SIPHON_ENTITY = registerBlockEntity("ember_siphon", () -> BlockEntityType.Builder.of(EmberSiphonBlockEntity::new, EMBER_SIPHON.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemTransferBlockEntity>> ITEM_TRANSFER_ENTITY = registerBlockEntity("item_transfer", () -> BlockEntityType.Builder.of(ItemTransferBlockEntity::new, ITEM_TRANSFER.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidTransferBlockEntity>> FLUID_TRANSFER_ENTITY = registerBlockEntity("fluid_transfer", () -> BlockEntityType.Builder.of(FluidTransferBlockEntity::new, FLUID_TRANSFER.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AlchemyPedestalBlockEntity>> ALCHEMY_PEDESTAL_ENTITY = registerBlockEntity("alchemy_pedestal", () -> BlockEntityType.Builder.of(AlchemyPedestalBlockEntity::new, ALCHEMY_PEDESTAL.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AlchemyPedestalTopBlockEntity>> ALCHEMY_PEDESTAL_TOP_ENTITY = registerBlockEntity("alchemy_pedestal_top", () -> BlockEntityType.Builder.of(AlchemyPedestalTopBlockEntity::new, ALCHEMY_PEDESTAL.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AlchemyTabletBlockEntity>> ALCHEMY_TABLET_ENTITY = registerBlockEntity("alchemy_tablet", () -> BlockEntityType.Builder.of(AlchemyTabletBlockEntity::new, ALCHEMY_TABLET.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BeamCannonBlockEntity>> BEAM_CANNON_ENTITY = registerBlockEntity("beam_cannon", () -> BlockEntityType.Builder.of(BeamCannonBlockEntity::new, BEAM_CANNON.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MechanicalPumpBottomBlockEntity>> MECHANICAL_PUMP_BOTTOM_ENTITY = registerBlockEntity("mechanical_pump_bottom", () -> BlockEntityType.Builder.of(MechanicalPumpBottomBlockEntity::new, MECHANICAL_PUMP.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MechanicalPumpTopBlockEntity>> MECHANICAL_PUMP_TOP_ENTITY = registerBlockEntity("mechanical_pump_top", () -> BlockEntityType.Builder.of(MechanicalPumpTopBlockEntity::new, MECHANICAL_PUMP.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MiniBoilerBlockEntity>> MINI_BOILER_ENTITY = registerBlockEntity("mini_boiler", () -> BlockEntityType.Builder.of(MiniBoilerBlockEntity::new, MINI_BOILER.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CatalyticPlugBlockEntity>> CATALYTIC_PLUG_ENTITY = registerBlockEntity("catalytic_plug", () -> BlockEntityType.Builder.of(CatalyticPlugBlockEntity::new, CATALYTIC_PLUG.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WildfireStirlingBlockEntity>> WILDFIRE_STIRLING_ENTITY = registerBlockEntity("wildfire_stirling", () -> BlockEntityType.Builder.of(WildfireStirlingBlockEntity::new, WILDFIRE_STIRLING.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EmberInjectorBlockEntity>> EMBER_INJECTOR_ENTITY = registerBlockEntity("ember_injector", () -> BlockEntityType.Builder.of(EmberInjectorBlockEntity::new, EMBER_INJECTOR.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FieldChartBlockEntity>> FIELD_CHART_ENTITY = registerBlockEntity("field_chart", () -> BlockEntityType.Builder.of(FieldChartBlockEntity::new, FIELD_CHART.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IgnemReactorBlockEntity>> IGNEM_REACTOR_ENTITY = registerBlockEntity("ignem_reactor", () -> BlockEntityType.Builder.of(IgnemReactorBlockEntity::new, IGNEM_REACTOR.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CatalysisChamberBlockEntity>> CATALYSIS_CHAMBER_ENTITY = registerBlockEntity("catalysis_chamber", () -> BlockEntityType.Builder.of(CatalysisChamberBlockEntity::new, CATALYSIS_CHAMBER.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CombustionChamberBlockEntity>> COMBUSTION_CHAMBER_ENTITY = registerBlockEntity("combustion_chamber", () -> BlockEntityType.Builder.of(CombustionChamberBlockEntity::new, COMBUSTION_CHAMBER.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CinderPlinthBlockEntity>> CINDER_PLINTH_ENTITY = registerBlockEntity("cinder_plinth", () -> BlockEntityType.Builder.of(CinderPlinthBlockEntity::new, CINDER_PLINTH.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DawnstoneAnvilBlockEntity>> DAWNSTONE_ANVIL_ENTITY = registerBlockEntity("dawnstone_anvil", () -> BlockEntityType.Builder.of(DawnstoneAnvilBlockEntity::new, DAWNSTONE_ANVIL.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AutomaticHammerBlockEntity>> AUTOMATIC_HAMMER_ENTITY = registerBlockEntity("automatic_hammer", () -> BlockEntityType.Builder.of(AutomaticHammerBlockEntity::new, AUTOMATIC_HAMMER.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InfernoForgeBottomBlockEntity>> INFERNO_FORGE_BOTTOM_ENTITY = registerBlockEntity("inferno_forge_bottom", () -> BlockEntityType.Builder.of(InfernoForgeBottomBlockEntity::new, INFERNO_FORGE.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InfernoForgeTopBlockEntity>> INFERNO_FORGE_TOP_ENTITY = registerBlockEntity("inferno_forge_top", () -> BlockEntityType.Builder.of(InfernoForgeTopBlockEntity::new, INFERNO_FORGE.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MnemonicInscriberBlockEntity>> MNEMONIC_INSCRIBER_ENTITY = registerBlockEntity("mnemonic_inscriber", () -> BlockEntityType.Builder.of(MnemonicInscriberBlockEntity::new, MNEMONIC_INSCRIBER.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CharInstillerBlockEntity>> CHAR_INSTILLER_ENTITY = registerBlockEntity("char_instiller", () -> BlockEntityType.Builder.of(CharInstillerBlockEntity::new, CHAR_INSTILLER.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AtmosphericBellowsBlockEntity>> ATMOSPHERIC_BELLOWS_ENTITY = registerBlockEntity("atmospheric_bellows", () -> BlockEntityType.Builder.of(AtmosphericBellowsBlockEntity::new, ATMOSPHERIC_BELLOWS.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EntropicEnumeratorBlockEntity>> ENTROPIC_ENUMERATOR_ENTITY = registerBlockEntity("entropic_enumerator", () -> BlockEntityType.Builder.of(EntropicEnumeratorBlockEntity::new, ENTROPIC_ENUMERATOR.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MithrilBlockEntity>> MITHRIL_BLOCK_ENTITY = registerBlockEntity("mithril_block", () -> BlockEntityType.Builder.of(MithrilBlockEntity::new, MITHRIL_BLOCK.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeatExchangerBlockEntity>> HEAT_EXCHANGER_ENTITY = registerBlockEntity("heat_exchanger", () -> BlockEntityType.Builder.of(HeatExchangerBlockEntity::new, HEAT_EXCHANGER.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeatInsulationBlockEntity>> HEAT_INSULATION_ENTITY = registerBlockEntity("heat_insulation", () -> BlockEntityType.Builder.of(HeatInsulationBlockEntity::new, HEAT_INSULATION.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExcavationBucketsBlockEntity>> EXCAVATION_BUCKETS_ENTITY = registerBlockEntity("excavation_buckets", () -> BlockEntityType.Builder.of(ExcavationBucketsBlockEntity::new, EXCAVATION_BUCKETS.get()));

	//creative tabs
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EMBERS_TAB = CREATIVE_TABS.register("main_tab", () -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup." + Embers.MODID))
			.icon(() -> new ItemStack(EMBER_CRYSTAL.get()))
			.withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
			.displayItems((params, output) -> {
				for (DeferredHolder<Item, ? extends Item> item : ITEMS.getEntries()) {
					if (item == MITHRIL_BLOCK_ITEM || item == MITHRIL_CRYSTAL_SEED.ITEM || item == MITHRIL_ASPECTUS || item == MITHRIL_INGOT || item == MITHRIL_NUGGET || item == MITHRIL_PLATE || item.get() == MOLTEN_MITHRIL.FLUID_BUCKET.get())
						continue;
					output.accept(item.get());
					if (item == COPPER_CELL_ITEM)
						output.accept(CopperCellBlockItem.getCharged());
					if (item.get() instanceof EmberStorageItem)
						output.accept(EmberStorageItem.withFill(item.get(), ((EmberStorageItem) item.get()).getCapacity()));
				}
			})
			.build());

	//entities
	public static final DeferredHolder<EntityType<?>, EntityType<EmberPacketEntity>> EMBER_PACKET = registerEntity("ember_packet", EntityType.Builder.of(EmberPacketEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).fireImmune().clientTrackingRange(3).updateInterval(1));
	public static final DeferredHolder<EntityType<?>, EntityType<EmberProjectileEntity>> EMBER_PROJECTILE = registerEntity("ember_projectile", EntityType.Builder.of(EmberProjectileEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).fireImmune().clientTrackingRange(3).updateInterval(1));
	public static final DeferredHolder<EntityType<?>, EntityType<GlimmerProjectileEntity>> GLIMMER_PROJECTILE = registerEntity("glimmer_projectile", EntityType.Builder.of(GlimmerProjectileEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).fireImmune().clientTrackingRange(3).updateInterval(1));
	public static final DeferredHolder<EntityType<?>, EntityType<AncientGolemEntity>> ANCIENT_GOLEM = registerEntity("ancient_golem", EntityType.Builder.of(AncientGolemEntity::new, MobCategory.MONSTER).sized(0.6F, 1.8F).fireImmune().clientTrackingRange(8));
	public static final DeferredHolder<EntityType<?>, EntityType<EmberWispEntity>> EMBER_WISP = registerEntity("ember_wisp", EntityType.Builder.of(EmberWispEntity::new, MobCategory.MONSTER).sized(0.5F, 0.5F).fireImmune().clientTrackingRange(8));

	//spawn eggs
    /**
    * Not sure why it's deprecated, same usage as in {@link net.minecraft.world.item.Items}
    */
	public static final DeferredItem<SpawnEggItem> ANCIENT_GOLEM_SPAWN_EGG = ITEMS.register("ancient_golem_spawn_egg", () -> new DeferredSpawnEggItem(ANCIENT_GOLEM, Misc.intColor(48, 38, 35), Misc.intColor(79, 66, 61), new Item.Properties()));
	public static final DeferredItem<SpawnEggItem> EMBER_WISP_SPAWN_EGG = ITEMS.register("ember_wisp_spawn_egg", () -> new DeferredSpawnEggItem(EMBER_WISP, Misc.intColor(48, 38, 35), Misc.intColor(79, 66, 61), new Item.Properties())); // TODO: something ember-like color

	//augments
    private static Holder<IAugment> registerAugment(String name, IAugment augment) {
        return Registry.registerForHolder(AUGMENT_REGISTRY, Embers.res(name), augment);
    }

	public static final Holder<IAugment> CORE_AUGMENT = registerAugment("core", new CoreAugment());
	public static final Holder<IAugment> TINKER_LENS_AUGMENT = registerAugment("tinker_lens", new TinkerLensAugment(false));
	public static final Holder<IAugment> SMOKY_LENS_AUGMENT = registerAugment("smoky_tinker_lens", new TinkerLensAugment(true));
	public static final Holder<IAugment> SUPERHEATER_AUGMENT = registerAugment("superheater", new SuperheaterAugment());
	public static final Holder<IAugment> CINDER_JET_AUGMENT = registerAugment("cinder_jet", new CinderJetAugment());
	public static final Holder<IAugment> BLASTING_CORE_AUGMENT = registerAugment("blasting_core", new BlastingCoreAugment());
	public static final Holder<IAugment> CASTER_ORB_AUGMENT = registerAugment("caster_orb", new CasterOrbAugment());
	public static final Holder<IAugment> RESONATING_BELL_AUGMENT = registerAugment("resonating_bell", new ResonatingBellAugment());
	public static final Holder<IAugment> FLAME_BARRIER_AUGMENT = registerAugment("flame_barrier", new FlameBarrierAugment());
	public static final Holder<IAugment> ELDRITCH_INSIGNIA_AUGMENT = registerAugment("eldritch_insignia", new EldritchInsigniaAugment());
	public static final Holder<IAugment> INTELLIGENT_APPARATUS_AUGMENT = registerAugment("intelligent_apparatus", new IntelligentApparatusAugment());
	public static final Holder<IAugment> DIFFRACTION_BARREL_AUGMENT = registerAugment("diffraction_barrel", new DiffractionBarrelAugment());
	public static final Holder<IAugment> FOCAL_LENS_AUGMENT = registerAugment("focal_lens", new FocalLensAugment());
	public static final Holder<IAugment> SHIFTING_SCALES_AUGMENT = registerAugment("shifting_scales", new ShiftingScalesAugment());
	public static final Holder<IAugment> WINDING_GEARS_AUGMENT = registerAugment("winding_gears", new WindingGearsAugment());
    public static final Holder<IAugment> EMBER_REDIRECTION_MODULE_AUGMENT = registerAugment("ember_redirection_module", new EmberRedirectionModuleAugment());

	//particle types
	public static final DeferredHolder<ParticleType<?>, ParticleType<GlowParticleOptions>> GLOW_PARTICLE = registerParticle("glow", false, GlowParticleOptions.CODEC, GlowParticleOptions.STREAM_CODEC);
	public static final DeferredHolder<ParticleType<?>, ParticleType<StarParticleOptions>> STAR_PARTICLE = registerParticle("star", false, StarParticleOptions.CODEC, StarParticleOptions.STREAM_CODEC);
	public static final DeferredHolder<ParticleType<?>, ParticleType<SparkParticleOptions>> SPARK_PARTICLE = registerParticle("spark", false, SparkParticleOptions.CODEC, SparkParticleOptions.STREAM_CODEC);
	public static final DeferredHolder<ParticleType<?>, ParticleType<SmokeParticleOptions>> SMOKE_PARTICLE = registerParticle("smoke", false, SmokeParticleOptions.CODEC, SmokeParticleOptions.STREAM_CODEC);
	public static final DeferredHolder<ParticleType<?>, ParticleType<VaporParticleOptions>> VAPOR_PARTICLE = registerParticle("vapor", false, VaporParticleOptions.CODEC, VaporParticleOptions.STREAM_CODEC);
	public static final DeferredHolder<ParticleType<?>, ParticleType<AlchemyCircleParticleOptions>> ALCHEMY_CIRCLE_PARTICLE = registerParticle("alchemy_circle", false, AlchemyCircleParticleOptions.CODEC, AlchemyCircleParticleOptions.STREAM_CODEC);
	public static final DeferredHolder<ParticleType<?>, ParticleType<TyrfingParticleOptions>> TYRFING_PARTICLE = registerParticle("tyrfing", false, TyrfingParticleOptions.CODEC, TyrfingParticleOptions.STREAM_CODEC);
	public static final DeferredHolder<ParticleType<?>, ParticleType<XRayGlowParticleOptions>> XRAY_GLOW_PARTICLE = registerParticle("xray_glow", false, XRayGlowParticleOptions.CODEC, XRayGlowParticleOptions.STREAM_CODEC);

	//recipe types
	public static final DeferredHolder<RecipeType<?>, RecipeType<IBoringRecipe>> BORING = registerRecipeType("boring");
	public static final DeferredHolder<RecipeType<?>, RecipeType<IBoringRecipe>> EXCAVATION = registerRecipeType("excavation");
	public static final DeferredHolder<RecipeType<?>, RecipeType<IEmberActivationRecipe>> EMBER_ACTIVATION = registerRecipeType("ember_activation");
	public static final DeferredHolder<RecipeType<?>, RecipeType<IMeltingRecipe>> MELTING = registerRecipeType("melting");
	public static final DeferredHolder<RecipeType<?>, RecipeType<IStampingRecipe>> STAMPING = registerRecipeType("stamping");
	public static final DeferredHolder<RecipeType<?>, RecipeType<IMixingRecipe>> MIXING = registerRecipeType("mixing");
	public static final DeferredHolder<RecipeType<?>, RecipeType<IMetalCoefficientRecipe>> METAL_COEFFICIENT = registerRecipeType("metal_coefficient");
	public static final DeferredHolder<RecipeType<?>, RecipeType<IAlchemyRecipe>> ALCHEMY = registerRecipeType("alchemy");
	public static final DeferredHolder<RecipeType<?>, RecipeType<IBoilingRecipe>> BOILING = registerRecipeType("boiling");
	public static final DeferredHolder<RecipeType<?>, RecipeType<IGaseousFuelRecipe>> GASEOUS_FUEL = registerRecipeType("gaseous_fuel");
	public static final DeferredHolder<RecipeType<?>, RecipeType<ICatalysisCombustionRecipe>> CATALYSIS_COMBUSTION = registerRecipeType("catalysis_combustion");
	public static final DeferredHolder<RecipeType<?>, RecipeType<IDawnstoneAnvilRecipe>> DAWNSTONE_ANVIL_RECIPE = registerRecipeType("dawnstone_anvil");

	//recipe serializers
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BoringRecipe>> BORING_SERIALIZER = RECIPE_SERIALIZERS.register("boring", () -> BoringRecipe.SERIALIZER);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ExcavationRecipe>> EXCAVATION_SERIALIZER = RECIPE_SERIALIZERS.register("excavation", () -> ExcavationRecipe.SERIALIZER);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EmberActivationRecipe>> EMBER_ACTIVATION_SERIALIZER = RECIPE_SERIALIZERS.register("ember_activation", () -> EmberActivationRecipe.SERIALIZER);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MeltingRecipe>> MELTING_SERIALIZER = RECIPE_SERIALIZERS.register("melting", () -> MeltingRecipe.SERIALIZER);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<StampingRecipe>> STAMPING_SERIALIZER = RECIPE_SERIALIZERS.register("stamping", () -> StampingRecipe.SERIALIZER);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MixingRecipe>> MIXING_SERIALIZER = RECIPE_SERIALIZERS.register("mixing", () -> MixingRecipe.SERIALIZER);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MetalCoefficientRecipe>> METAL_COEFFICIENT_SERIALIZER = RECIPE_SERIALIZERS.register("metal_coefficient", () -> MetalCoefficientRecipe.SERIALIZER);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AlchemyRecipe>> ALCHEMY_SERIALIZER = RECIPE_SERIALIZERS.register("alchemy", () -> AlchemyRecipe.SERIALIZER);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AlchemyRecipeForBabies>> ALCHEMY_FOR_BABIES_SERIALIZER = RECIPE_SERIALIZERS.register("alchemy_for_babies", () -> AlchemyRecipeForBabies.SERIALIZER);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BoilingRecipe>> BOILING_SERIALIZER = RECIPE_SERIALIZERS.register("boiling", () -> BoilingRecipe.SERIALIZER);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GaseousFuelRecipe>> GASEOUS_FUEL_SERIALIZER = RECIPE_SERIALIZERS.register("gaseous_fuel", () -> GaseousFuelRecipe.SERIALIZER);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CatalysisCombustionRecipe>> CATALYSIS_COMBUSTION_SERIALIZER = RECIPE_SERIALIZERS.register("catalysis_combustion", () -> CatalysisCombustionRecipe.SERIALIZER);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GemSocketRecipe>> GEM_SOCKET_SERIALIZER = RECIPE_SERIALIZERS.register("gem_socket", () -> GemSocketRecipe.SERIALIZER);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GemUnsocketRecipe>> GEM_UNSOCKET_SERIALIZER = RECIPE_SERIALIZERS.register("gem_unsocket", () -> GemUnsocketRecipe.SERIALIZER);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AnvilRepairRecipe>> TOOL_REPAIR = RECIPE_SERIALIZERS.register("tool_repair", () -> AnvilRepairRecipe.SERIALIZER);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AnvilRepairMateriaRecipe>> MATERIA_REPAIR = RECIPE_SERIALIZERS.register("tool_materia_repair", () -> AnvilRepairMateriaRecipe.SERIALIZER);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AnvilBreakdownRecipe>> TOOL_BREAKDOWN = RECIPE_SERIALIZERS.register("tool_breakdown", () -> AnvilBreakdownRecipe.SERIALIZER);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AnvilAugmentRecipe>> TOOL_AUGMENT = RECIPE_SERIALIZERS.register("tool_augment", () -> AnvilAugmentRecipe.SERIALIZER);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AnvilAugmentRemoveRecipe>> TOOL_AUGMENT_REMOVE = RECIPE_SERIALIZERS.register("tool_augment_remove", () -> AnvilAugmentRemoveRecipe.SERIALIZER);

	//loot modifiers
	public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<GrandhammerLootModifier>> GRANDHAMMER_MODIFIER = LOOT_MODIFIERS.register("grandhammer", () -> GrandhammerLootModifier.CODEC);
	public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<SuperHeaterLootModifier>> SUPERHEATER_MODIFIER = LOOT_MODIFIERS.register("superheater", () -> SuperHeaterLootModifier.CODEC);
	public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<AshenAmuletLootModifier>> ASHENAMULET_MODIFIER = LOOT_MODIFIERS.register("ashenamulet", () -> AshenAmuletLootModifier.CODEC);

	//menu types
	public static final DeferredHolder<MenuType<?>, MenuType<SlateMenu>> SLATE_MENU = MENU_TYPES.register("codebreaking_slate", () -> IMenuTypeExtension.create(SlateMenu::fromBuffer));

	//structure types
	public static final DeferredHolder<StructureType<?>, StructureType<CaveStructure>> CAVE_STRUCTURE = STRUCTURE_TYPES.register("cave_structure", () -> () -> CaveStructure.CODEC);

	//structure processor types
	public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<CrystalSeedStructureProcessor>> CRYSTAL_SEED_PROCESSOR = STRUCTURE_PROCESSOR_TYPES.register("crystal_seed_processor", () -> () -> CrystalSeedStructureProcessor.CODEC);
	public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<EntityMobilizerStructureProcessor>> ENTITY_MOBILIZER_PROCESSOR = STRUCTURE_PROCESSOR_TYPES.register("entity_mobilizer", () -> () -> EntityMobilizerStructureProcessor.CODEC);

    //attachment types
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Map<ResourceLocation, Boolean>>> RESEARCH_DATA = ATTACHMENT_TYPES.register("research_data", () -> AttachmentType.<Map<ResourceLocation, Boolean>>builder(() -> new HashMap<>()).serialize(Codec.unboundedMap(ResourceLocation.CODEC, Codec.BOOL).xmap(HashMap::new, Function.identity())).sync(ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.BOOL)).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Double>> SCALES_DATA = ATTACHMENT_TYPES.register("scales_data", () -> AttachmentType.builder(() -> 0.).serialize(Codec.DOUBLE).sync(ByteBufCodecs.DOUBLE).copyOnDeath().build());

    //ingredient types
    public static final DeferredHolder<IngredientType<?>, IngredientType<HeatIngredient>> HEAT_INGREDIENT_TYPE = INGREDIENT_TYPES.register("heat_ingredient", () -> new IngredientType<>(HeatIngredient.CODEC, HeatIngredient.STREAM_CODEC));
    public static final DeferredHolder<IngredientType<?>, IngredientType<AugmentIngredient>> AUGMENT_INGREDIENT_TYPE = INGREDIENT_TYPES.register("augment_ingredient", () -> new IngredientType<>(AugmentIngredient.CODEC));

    public static void addRegistries(NewRegistryEvent event) {
        event.register(AUGMENT_REGISTRY);
    }

	public static void init(final FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			DispenseItemBehavior dispenseBucket = new DefaultDispenseItemBehavior() {
				private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

				@Override
				public @NotNull ItemStack execute(BlockSource source, ItemStack stack) {
					DispensibleContainerItem container = (DispensibleContainerItem)stack.getItem();
					BlockPos blockpos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
					Level level = source.level();
					if (container.emptyContents(null, level, blockpos, null, stack)) {
						container.checkExtraContent(null, level, stack, blockpos);
						return new ItemStack(Items.BUCKET);
					}
                    return this.defaultDispenseItemBehavior.dispense(source, stack);
				}
			};
			for (FluidStuff fluid : fluidList)
				DispenserBlock.registerBehavior(fluid.FLUID_BUCKET.get(), dispenseBucket);

			EmbersAPI.registerEmberResonance(Ingredient.of(DAWNSTONE_TOOLS.SWORD.get(), DAWNSTONE_TOOLS.SHOVEL.get(), DAWNSTONE_TOOLS.PICKAXE.get(), DAWNSTONE_TOOLS.AXE.get(), DAWNSTONE_TOOLS.HOE.get()), 2.0);
			EmbersAPI.registerEmberResonance(Ingredient.of(CLOCKWORK_PICKAXE.get(), CLOCKWORK_AXE.get(), GRANDHAMMER.get(), BLAZING_RAY.get(), CINDER_STAFF.get()), 2.0);
			EmbersAPI.registerEmberResonance(Ingredient.of(ASHEN_GOGGLES.get(), ASHEN_CLOAK.get(), ASHEN_LEGGINGS.get(), ASHEN_BOOTS.get()), 2.0);

			moltenMetalFluidInteractions(MOLTEN_IRON.TYPE.get());
			moltenMetalFluidInteractions(MOLTEN_GOLD.TYPE.get());
			moltenMetalFluidInteractions(MOLTEN_COPPER.TYPE.get());
			moltenMetalFluidInteractions(MOLTEN_LEAD.TYPE.get());
			moltenMetalFluidInteractions(MOLTEN_SILVER.TYPE.get());
			moltenMetalFluidInteractions(MOLTEN_DAWNSTONE.TYPE.get());
			moltenMetalFluidInteractions(MOLTEN_MITHRIL.TYPE.get());
			moltenMetalFluidInteractions(MOLTEN_TIN.TYPE.get());
			moltenMetalFluidInteractions(MOLTEN_ALUMINUM.TYPE.get());
			moltenMetalFluidInteractions(MOLTEN_ZINC.TYPE.get());
			moltenMetalFluidInteractions(MOLTEN_PLATINUM.TYPE.get());
			moltenMetalFluidInteractions(MOLTEN_URANIUM.TYPE.get());
			moltenMetalFluidInteractions(MOLTEN_BRONZE.TYPE.get());
			moltenMetalFluidInteractions(MOLTEN_ELECTRUM.TYPE.get());
			moltenMetalFluidInteractions(MOLTEN_BRASS.TYPE.get());
			moltenMetalFluidInteractions(MOLTEN_CONSTANTAN.TYPE.get());
			moltenMetalFluidInteractions(MOLTEN_INVAR.TYPE.get());

			EmbersAPI.registerLens(Ingredient.of(TINKER_LENS.get()));
			EmbersAPI.registerWearableLens(Ingredient.of(ASHEN_GOGGLES.get()));
		});
	}

	public static void moltenMetalFluidInteractions(FluidType source) {
		FluidInteractionRegistry.addInteraction(source, new FluidInteractionRegistry.InteractionInformation(
				(level, currentPos, relativePos, currentState) -> level.getFluidState(relativePos).is(EmbersFluidTags.WATERY),
				SOLIDIFIED_METAL.get().defaultBlockState()));
	}

    private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> registerBlockEntity(String key, Supplier<BlockEntityType.Builder<T>> builder) {
        Type<?> type = Util.fetchChoiceType(References.BLOCK_ENTITY, key);
        //noinspection DataFlowIssue
        return BLOCK_ENTITY_TYPES.register(key, () -> builder.get().build(type));
    }

	public static class FluidStuff {

		public final BaseFlowingFluid.Properties PROPERTIES;

		public final DeferredHolder<Fluid, BaseFlowingFluid.Source> FLUID;
		public final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLUID_FLOW;
		public final DeferredHolder<FluidType, FluidType> TYPE;

		public final DeferredBlock<LiquidBlock> FLUID_BLOCK;

		public final DeferredItem<BucketItem> FLUID_BUCKET;

		public final String name;
		public final String localizedName;
		public final int color;

		public FluidStuff(String name, String localizedName, int color, FluidType type, BiFunction<FlowingFluid, BlockBehaviour.Properties, LiquidBlock> block, @Nullable Consumer<BaseFlowingFluid.Properties> fluidProperties, Function<BaseFlowingFluid.Properties, BaseFlowingFluid.Source> source, Function<BaseFlowingFluid.Properties, BaseFlowingFluid.Flowing> flowing) {
			this.name = name;
			this.localizedName = localizedName;
			this.color = color;

			FLUID = FLUIDS.register(name, () -> source.apply(getFluidProperties()));
			FLUID_FLOW = FLUIDS.register("flowing_" + name, () -> flowing.apply(getFluidProperties()));
			TYPE = FLUIDTYPES.register(name, () -> type);

			PROPERTIES = new BaseFlowingFluid.Properties(TYPE, FLUID, FLUID_FLOW);
			if (fluidProperties != null)
				fluidProperties.accept(PROPERTIES);

			FLUID_BLOCK = BLOCKS.register(name + "_block", () -> block.apply(FLUID.get(), Block.Properties.of().liquid().pushReaction(PushReaction.DESTROY).lightLevel((state) -> type.getLightLevel()).randomTicks().replaceable().strength(100.0F).noLootTable()));
			FLUID_BUCKET = ITEMS.register(name + "_bucket", () -> new BucketItem(FLUID.get(), new BucketItem.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

			PROPERTIES.bucket(FLUID_BUCKET).block(FLUID_BLOCK);
		}

		public BaseFlowingFluid.Properties getFluidProperties() {
			return PROPERTIES;
		}
	}

	public static class StoneDecoBlocks {

		public String name;

		public DeferredBlock<Block> block;
		public DeferredBlock<StairBlock> stairs = null;
		public DeferredItem<Item> stairsItem = null;
		public DeferredBlock<SlabBlock> slab = null;
		public DeferredItem<Item> slabItem = null;
		public DeferredBlock<WallBlock> wall = null;
		public DeferredItem<Item> wallItem = null;

		public StoneDecoBlocks(String name, DeferredBlock<Block> block, Properties properties, boolean stairs, boolean slab, boolean wall) {
			this.name = name;
			this.block = block;
			if (stairs)
				this.stairs = BLOCKS.register(name + "_stairs", () -> new StairBlock(block.get().defaultBlockState(), properties));
			if (slab)
				this.slab = BLOCKS.register(name + "_slab", () -> new SlabBlock(properties));
			if (wall)
				this.wall = BLOCKS.register(name + "_wall", () -> new WallBlock(properties));
		}

		public void makeItems() {
			if (stairs != null) {
				this.stairsItem = ITEMS.register(name + "_stairs", () -> new BlockItem(this.stairs.get(), new Item.Properties()));
			}
			if (slab != null) {
				this.slabItem = ITEMS.register(name + "_slab", () -> new BlockItem(this.slab.get(), new Item.Properties()));
			}
			if (wall != null) {
				this.wallItem = ITEMS.register(name + "_wall", () -> new BlockItem(this.wall.get(), new Item.Properties()));
			}
		}

		public StoneDecoBlocks(String name, DeferredBlock<Block> block, Properties properties) {
			this(name, block, properties, true, true, true);
		}
	}

	public static class ToolSet {

		public String name;

		public final DeferredItem<SwordItem> SWORD;
		public final DeferredItem<ShovelItem> SHOVEL;
		public final DeferredItem<PickaxeItem> PICKAXE;
		public final DeferredItem<AxeItem> AXE;
		public final DeferredItem<HoeItem> HOE;

		public ToolSet(String name, Tier tier) {
			this.name = name;

			SWORD = ITEMS.register(name + "_sword", () -> new SwordItem(tier, new Item.Properties().attributes(SwordItem.createAttributes(tier, 3, -2.4f))));
			SHOVEL = ITEMS.register(name + "_shovel", () -> new ShovelItem(tier, new Item.Properties().attributes(ShovelItem.createAttributes(tier, 1.5f, -3.0f))));
			PICKAXE = ITEMS.register(name + "_pickaxe", () -> new PickaxeItem(tier, new Item.Properties().attributes(PickaxeItem.createAttributes(tier, 1.0f, -2.8f))));
			AXE = ITEMS.register(name + "_axe", () -> new AxeItem(tier, new Item.Properties().attributes(AxeItem.createAttributes(tier, 6.0f, -3.0f))));
			HOE = ITEMS.register(name + "_hoe", () -> new HoeItem(tier, new Item.Properties().attributes(HoeItem.createAttributes(tier, -tier.getAttackDamageBonus(), Math.min(0.0F, tier.getAttackDamageBonus() - 3.0F)))));
		}
	}

	public static class MetalCrystalSeed {
		public static HashMap<String, MetalCrystalSeed> seeds = new HashMap<>();

		public String name;

		public final DeferredBlock<CrystalSeedBlock> BLOCK;
		public DeferredItem<BlockItem> ITEM;
		public final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrystalSeedBlockEntity>> BLOCKENTITY;

		public MetalCrystalSeed(String type) {
			this.name = type;

			BLOCK = BLOCKS.register(type + "_crystal_seed", () -> new CrystalSeedBlock(Properties.of().mapColor(MapColor.NONE).sound(SoundType.AMETHYST).requiresCorrectToolForDrops().strength(1.6f).noOcclusion().forceSolidOn(), type));
			BLOCKENTITY = registerBlockEntity(type + "_crystal_seed", () -> BlockEntityType.Builder.of((pos, state) -> new CrystalSeedBlockEntity(pos, state, type), BLOCK.get()));
			seeds.put(type, this);
		}

		public void makeItem() {
			ITEM = ITEMS.register(name + "_crystal_seed", () -> new BlockItem(BLOCK.get(), new Item.Properties()));
		}
	}
}