package hu.zoldleo.embers.compat.curios;

import java.util.Optional;
import java.util.function.Predicate;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.capabilities.EmbersCapabilities;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.block.machine.ExplosionPedestalBlock;
import hu.zoldleo.embers.blockentity.ExplosionPedestalBlockEntity;
import hu.zoldleo.embers.datacomponents.EmberComponent;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.item.DawnstoneMailItem;
import hu.zoldleo.embers.item.EmberBulbItem;
import hu.zoldleo.embers.item.EmberDiscountBaubleItem;
import hu.zoldleo.embers.item.EmberStorageItem;
import hu.zoldleo.embers.item.ExplosionCharmItem;
import hu.zoldleo.embers.item.GenericCurioItemItem;
import hu.zoldleo.embers.item.NonbeleiverAmuletItem;
import hu.zoldleo.embers.research.ResearchBase;
import hu.zoldleo.embers.research.ResearchManager;
import hu.zoldleo.embers.research.subtypes.ResearchShowItem;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

public class CuriosCompat {
	public static final DeferredBlock<ExplosionPedestalBlock> EXPLOSION_PEDESTAL = RegistryManager.BLOCKS.register("explosion_pedestal", () -> new ExplosionPedestalBlock(Properties.of().mapColor(MapColor.COLOR_ORANGE).sound(EmbersSounds.CAMINITE).requiresCorrectToolForDrops().strength(1.6f).noOcclusion(), EmbersSounds.MULTIBLOCK_EXTRA));

	public static final DeferredItem<EmberDiscountBaubleItem> EMBER_RING = RegistryManager.ITEMS.register("ember_ring", () -> new EmberDiscountBaubleItem(new Item.Properties().stacksTo(1), 0.15));
	public static final DeferredItem<EmberDiscountBaubleItem> EMBER_BELT = RegistryManager.ITEMS.register("ember_belt", () -> new EmberDiscountBaubleItem(new Item.Properties().stacksTo(1), 0.25));
	public static final DeferredItem<EmberDiscountBaubleItem> EMBER_AMULET = RegistryManager.ITEMS.register("ember_amulet", () -> new EmberDiscountBaubleItem(new Item.Properties().stacksTo(1), 0.2));
	public static final DeferredItem<EmberBulbItem> EMBER_BULB = RegistryManager.ITEMS.register("ember_bulb", () -> new EmberBulbItem(new Item.Properties().stacksTo(1).component(RegistryManager.EMBER_COMPONENT, new EmberComponent(0, 1000))));
	public static final DeferredItem<DawnstoneMailItem> DAWNSTONE_MAIL = RegistryManager.ITEMS.register("dawnstone_mail", () -> new DawnstoneMailItem(new Item.Properties().stacksTo(1)));
	public static final DeferredItem<GenericCurioItemItem> ASHEN_AMULET = RegistryManager.ITEMS.register("ashen_amulet", () -> new GenericCurioItemItem(new Item.Properties().stacksTo(1)));
	public static final DeferredItem<NonbeleiverAmuletItem> NONBELEIVER_AMULET = RegistryManager.ITEMS.register("nonbeliever_amulet", () -> new NonbeleiverAmuletItem(new Item.Properties().stacksTo(1)));
	public static final DeferredItem<ExplosionCharmItem> EXPLOSION_CHARM = RegistryManager.ITEMS.register("explosion_charm", () -> new ExplosionCharmItem(new Item.Properties().stacksTo(1)));

	public static final DeferredItem<BlockItem> EXPLOSION_PEDESTAL_ITEM = RegistryManager.ITEMS.register("explosion_pedestal", () -> new BlockItem(EXPLOSION_PEDESTAL.get(), new Item.Properties()));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExplosionPedestalBlockEntity>> EXPLOSION_PEDESTAL_ENTITY = RegistryManager.BLOCK_ENTITY_TYPES.register("explosion_pedestal", () -> BlockEntityType.Builder.of(ExplosionPedestalBlockEntity::new, EXPLOSION_PEDESTAL.get()).build(null));

	public static void init() {}

	@OnlyIn(Dist.CLIENT)
	public static void registerColorHandler(RegisterColorHandlersEvent.Item event, ItemColor itemColor) {
		event.register(itemColor, EMBER_BULB.get());
	}

	public static boolean checkForCurios(LivingEntity living, Predicate<ItemStack> predicate) {
		Optional<ICuriosItemHandler> inv = CuriosApi.getCuriosInventory(living);
        if (inv.isEmpty())
            return false;
        for (ICurioStacksHandler curio : inv.get().getCurios().values())
            for (int i = 0; i < curio.getStacks().getSlots(); i++)
                if (predicate.test(curio.getStacks().getStackInSlot(i)))
                    return true;
		return false;
	}

	public static double getEmberCapacityTotal(LivingEntity living) {
        Optional<ICuriosItemHandler> inv = CuriosApi.getCuriosInventory(living);
        if (inv.isEmpty())
            return 0;
        double amount = 0;
        for (ICurioStacksHandler curio : inv.get().getCurios().values())
            for (int i = 0; i < curio.getStacks().getSlots(); i++) {
                IEmberCapability capability = curio.getStacks().getStackInSlot(i).getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM, null);
                if (capability != null)
                    amount += capability.getEmberCapacity();
            }
		return amount;
	}

	public static double getEmberTotal(LivingEntity living) {
        Optional<ICuriosItemHandler> inv = CuriosApi.getCuriosInventory(living);
        if (inv.isEmpty())
            return 0;
        double amount = 0;
        for (ICurioStacksHandler curio : inv.get().getCurios().values())
            for (int i = 0; i < curio.getStacks().getSlots(); i++) {
                IEmberCapability capability = curio.getStacks().getStackInSlot(i).getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM, null);
                if (capability != null)
                    amount += capability.getEmber();
            }
		return amount;
	}

	public static double removeEmber(LivingEntity living, double amount) {
        Optional<ICuriosItemHandler> inv = CuriosApi.getCuriosInventory(living);
        if (inv.isEmpty())
            return amount;
        for (ICurioStacksHandler curio : inv.get().getCurios().values())
            for (int i = 0; i < curio.getStacks().getSlots(); i++) {
                IEmberCapability capability = curio.getStacks().getStackInSlot(i).getCapability(EmbersCapabilities.EMBER_CAPABILITY_ITEM, null);
                if (capability != null) {
                    amount -= capability.removeAmount(amount, true);
                    if (amount <= 0)
                        return amount;
                }
            }
		return amount;
	}

	public static void initCuriosCategory() {
		ItemStack fullBulb = EmberStorageItem.withFill(EMBER_BULB.get(), EMBER_BULB.get().getCapacity());

		ResearchManager.cost_reduction = new ResearchShowItem(Embers.res("cost_reduction"), new ItemStack(EMBER_AMULET.get()), 5, 5).addItem(new ResearchShowItem.DisplayItem(new ItemStack(EMBER_AMULET.get()), new ItemStack(EMBER_BELT.get()), new ItemStack(EMBER_RING.get()))).setLookupIngredient(Ingredient.of(EMBER_AMULET.get(), EMBER_BELT.get(), EMBER_RING.get()));
		ResearchManager.mantle_bulb = new ResearchBase(Embers.res("mantle_bulb"), fullBulb, 7, 3);
		ResearchManager.dawnstone_mail = new ResearchBase(Embers.res("dawnstone_mail"), new ItemStack(DAWNSTONE_MAIL.get()), 3, 7);
		ResearchManager.ashen_amulet = new ResearchBase(Embers.res("ashen_amulet"), new ItemStack(ASHEN_AMULET.get()), 4, 3);
		ResearchManager.nonbeliever_amulet = new ResearchBase(Embers.res("nonbeliever_amulet"), new ItemStack(NONBELEIVER_AMULET.get()), 1, 3);
		ResearchManager.explosion_charm = new ResearchBase(Embers.res("explosion_charm"), new ItemStack(EXPLOSION_CHARM.get()), 9, 2);
		ResearchManager.explosion_pedestal = new ResearchBase(Embers.res("explosion_pedestal"), new ItemStack(EXPLOSION_PEDESTAL_ITEM.get()), 11, 1).addAncestor(ResearchManager.explosion_charm);

		ResearchManager.subCategoryBaubles.addResearch(ResearchManager.cost_reduction);
		ResearchManager.subCategoryBaubles.addResearch(ResearchManager.mantle_bulb);
		ResearchManager.subCategoryBaubles.addResearch(ResearchManager.dawnstone_mail);
		ResearchManager.subCategoryBaubles.addResearch(ResearchManager.ashen_amulet);
		ResearchManager.subCategoryBaubles.addResearch(ResearchManager.nonbeliever_amulet);
		ResearchManager.subCategoryBaubles.addResearch(ResearchManager.explosion_charm);
		ResearchManager.subCategoryBaubles.addResearch(ResearchManager.explosion_pedestal);
	}
}