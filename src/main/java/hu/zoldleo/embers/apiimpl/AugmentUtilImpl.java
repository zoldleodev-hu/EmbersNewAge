package hu.zoldleo.embers.apiimpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.augment.IAugment;
import hu.zoldleo.embers.api.augment.IAugmentUtil;

import hu.zoldleo.embers.datacomponents.HeatComponent;
import hu.zoldleo.embers.util.ItemStackNonNullList;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

public class AugmentUtilImpl implements IAugmentUtil {
	@Override
	public Collection<Holder<IAugment>> getAllAugments() {
        return RegistryManager.AUGMENT_REGISTRY.holders().collect(Collectors.toList());
        //return RegistryManager.AUGMENT_REGISTRY.holders().map(x -> (Holder<IAugment>)x).toList();
	}

	@Override
    @Nullable
	public Holder<IAugment> getAugment(ResourceLocation name) {
		return RegistryManager.AUGMENT_REGISTRY.getHolder(name).orElse(null);
	}

	@Override
    @SuppressWarnings("all")
	public List<Holder<IAugment>> getAugments(ItemStack stack) {
		if (hasHeat(stack)) {
            HeatComponent component = stack.get(RegistryManager.HEAT_COMPONENT);
            if (!component.augments().isEmpty())
                return component.augments().stream().map(HeatComponent.AugmentItems::augment).toList();
		}
		return Lists.newArrayList();
	}

	@Override
    @SuppressWarnings("all")
	public int getTotalAugmentLevel(ItemStack stack) {
		if (hasHeat(stack)) {
            HeatComponent component = stack.get(RegistryManager.HEAT_COMPONENT);
            if (!component.augments().isEmpty())
                return component.augments().stream().filter(HeatComponent.AugmentItems::countTowardsTotalLevel).mapToInt(HeatComponent.AugmentItems::level).reduce(0, Integer::sum);
		}
		return 0;
	}

	@Override
    @SuppressWarnings("all")
	public boolean hasAugment(ItemStack stack, Holder<IAugment> augment) {
		if (hasHeat(stack)) {
            HeatComponent component = stack.get(RegistryManager.HEAT_COMPONENT);
            if (!component.augments().isEmpty())
                return component.augments().stream().anyMatch(x -> x.augment().equals(augment));
		}
		return false;
	}

    @Override
    @SuppressWarnings("all")
    public boolean hasAugment(ItemStack stack, ResourceLocation augment) {
        if (hasHeat(stack)) {
            HeatComponent component = stack.get(RegistryManager.HEAT_COMPONENT);
            if (!component.augments().isEmpty())
                return component.augments().stream().anyMatch(x -> x.augment().is(augment));
        }
        return false;
    }

    @Override
    @SuppressWarnings("all")
    public boolean hasAugment(ItemStack stack, ResourceKey<IAugment> augment) {
        if (hasHeat(stack)) {
            HeatComponent component = stack.get(RegistryManager.HEAT_COMPONENT);
            if (!component.augments().isEmpty())
                return component.augments().stream().anyMatch(x -> x.augment().is(augment));
        }
        return false;
    }

    @Override
    @SuppressWarnings("all")
    public boolean hasAugment(ItemStack stack, Predicate<ResourceKey<IAugment>> augment) {
        if (hasHeat(stack)) {
            HeatComponent component = stack.get(RegistryManager.HEAT_COMPONENT);
            if (!component.augments().isEmpty())
                return component.augments().stream().anyMatch(x -> x.augment().is(augment));
        }
        return false;
    }

    @Override
    @SuppressWarnings("all")
    public boolean hasAugment(ItemStack stack, TagKey<IAugment> augment) {
        if (hasHeat(stack)) {
            HeatComponent component = stack.get(RegistryManager.HEAT_COMPONENT);
            if (!component.augments().isEmpty())
                return component.augments().stream().anyMatch(x -> x.augment().is(augment));
        }
        return false;
    }

    @Override
    @Deprecated
    @SuppressWarnings("all")
    public boolean hasAugment(ItemStack stack, IAugment augment) {
        if (hasHeat(stack)) {
            HeatComponent component = stack.get(RegistryManager.HEAT_COMPONENT);
            if (!component.augments().isEmpty())
                return component.augments().stream().anyMatch(x -> x.augment().value().equals(augment));
        }
        return false;
    }

	@Override
    @SuppressWarnings("all")
	public void addAugment(ItemStack stack, ItemStack augmentStack, Holder<IAugment> augment) {
		checkForTag(stack);
        HeatComponent component = stack.get(RegistryManager.HEAT_COMPONENT).copy();
        for (int i = 0; i < component.augments().size(); i++) {
            HeatComponent.AugmentItems augmentItems = component.augments().get(i);
            if (augmentItems.augment().equals(augment)) {
                augmentItems.items().add(augmentStack);
                component.augments().set(i, augmentItems.addLevel(1));
                stack.set(RegistryManager.HEAT_COMPONENT, component);
                augment.value().onApply(stack);
                return;
            }
        }
        component.augments().add(new HeatComponent.AugmentItems(augment, 1, Lists.newArrayList(augmentStack)));
        stack.set(RegistryManager.HEAT_COMPONENT, component);
        augment.value().onApply(stack);
	}

	@Override
    @SuppressWarnings("all")
	public List<ItemStack> removeAllAugments(ItemStack stack) {
		if (hasHeat(stack)) {
            HeatComponent component = stack.get(RegistryManager.HEAT_COMPONENT);
            if (!component.augments().isEmpty()) {
                List<ItemStack> results = ItemStackNonNullList.of();
                ArrayList<HeatComponent.AugmentItems> remainingAugments = new ArrayList<>();
                List<IAugment> removedAugments = new ArrayList<>();
                for (HeatComponent.AugmentItems augmentItems : component.augments()) {
                    if (augmentItems.augment().value().canRemove()) {
                        results.addAll(augmentItems.items());
                        for (int i = 0; i < augmentItems.level(); i++)
                            removedAugments.add(augmentItems.augment().value());
                    }
                    else {
                        remainingAugments.add(augmentItems.copy());
                    }
                }
                stack.set(RegistryManager.HEAT_COMPONENT, component.withAugments(remainingAugments));
                for (IAugment augment : removedAugments)
                    augment.onRemove(stack);
                return results;
            }
		}
		return Lists.newArrayList();
	}

	@Override
    @SuppressWarnings("all")
	public void addAugmentLevel(ItemStack stack, Holder<IAugment> augment, int levels) {
        checkForTag(stack);
        HeatComponent component = stack.get(RegistryManager.HEAT_COMPONENT).copy();
        for (int i = 0; i < component.augments().size(); i++) {
            HeatComponent.AugmentItems augmentItems = component.augments().get(i);
            if (augmentItems.augment().equals(augment))
                component.augments().set(i, augmentItems.addLevel(levels));
        }
        stack.set(RegistryManager.HEAT_COMPONENT, component);
	}

	@Override
    @SuppressWarnings("all")
	public void setAugmentLevel(ItemStack stack, Holder<IAugment> augment, int level) {
		checkForTag(stack);
        HeatComponent component = stack.get(RegistryManager.HEAT_COMPONENT).copy();
        for (int i = 0; i < component.augments().size(); i++) {
            HeatComponent.AugmentItems augmentItems = component.augments().get(i);
            if (augmentItems.augment().equals(augment))
                component.augments().set(i, augmentItems.setLevel(level));
        }
        stack.set(RegistryManager.HEAT_COMPONENT, component);
	}

	@Override
    @SuppressWarnings("all")
	public int getAugmentLevel(ItemStack stack, Holder<IAugment> augment) {
		if (!hasHeat(stack))
		    return 0;
        return stack.get(RegistryManager.HEAT_COMPONENT).augments().stream().filter(x -> x.augment().equals(augment)).map(HeatComponent.AugmentItems::level).findAny().orElse(0);
	}

    @Override
    @SuppressWarnings("all")
    public int getAugmentLevel(ItemStack stack, ResourceLocation augment) {
        if (!hasHeat(stack))
            return 0;
        return stack.get(RegistryManager.HEAT_COMPONENT).augments().stream().filter(x -> x.augment().is(augment)).map(HeatComponent.AugmentItems::level).findAny().orElse(0);
    }

    @Override
    @SuppressWarnings("all")
    public int getAugmentLevel(ItemStack stack, ResourceKey<IAugment> augment) {
        if (!hasHeat(stack))
            return 0;
        return stack.get(RegistryManager.HEAT_COMPONENT).augments().stream().filter(x -> x.augment().is(augment)).map(HeatComponent.AugmentItems::level).findAny().orElse(0);
    }

    @Override
    @Deprecated
    @SuppressWarnings("all")
    public int getAugmentLevel(ItemStack stack, IAugment augment) {
        if (!hasHeat(stack))
            return 0;
        return stack.get(RegistryManager.HEAT_COMPONENT).augments().stream().filter(x -> x.augment().value().equals(augment)).map(HeatComponent.AugmentItems::level).findAny().orElse(0);
    }

	@Override
	public boolean hasHeat(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        return stack.has(RegistryManager.HEAT_COMPONENT);
	}

	@Override
	public void addHeat(ItemStack stack, float heat) {
		checkForTag(stack);
        stack.update(RegistryManager.HEAT_COMPONENT, HeatComponent.DEFAULT, x -> x.copy().addHeat(heat));
	}

	@Override
	public void setHeat(ItemStack stack, float heat) {
		checkForTag(stack);
        stack.update(RegistryManager.HEAT_COMPONENT, HeatComponent.DEFAULT, x -> x.copy().setHeat(heat));
	}

	@Override
    @SuppressWarnings("all")
	public float getHeat(ItemStack stack) {
		if (hasHeat(stack))
			return stack.get(RegistryManager.HEAT_COMPONENT).heat();
		return 0.0f;
	}

	@Override
    @SuppressWarnings("all")
	public float getMaxHeat(ItemStack stack) {
		if (hasHeat(stack))
			return stack.get(RegistryManager.HEAT_COMPONENT).getMaxHeat();
		return 0.0f;
	}

	@Override
    @SuppressWarnings("all")
	public int getLevel(ItemStack stack) {
		if (hasHeat(stack))
            return stack.get(RegistryManager.HEAT_COMPONENT).heatLevel();
		return 0;
	}

	@Override
	public void setLevel(ItemStack stack, int level) {
		checkForTag(stack);
        stack.update(RegistryManager.HEAT_COMPONENT, HeatComponent.DEFAULT, x -> x.copy().setLevel(level));
	}

    protected <T> int getArmorAugmentLevel(LivingEntity entity, BiFunction<ItemStack, T, Integer> getAugmentLevel, T augment) {
        int maxLevel = 0;
        if (hasHeat(entity.getItemBySlot(EquipmentSlot.HEAD))) {
            int l = getAugmentLevel.apply(entity.getItemBySlot(EquipmentSlot.HEAD), augment);
            if (l > maxLevel)
                maxLevel = l;
        }
        if (hasHeat(entity.getItemBySlot(EquipmentSlot.CHEST))) {
            int l = getAugmentLevel.apply(entity.getItemBySlot(EquipmentSlot.CHEST), augment);
            if (l > maxLevel)
                maxLevel = l;
        }
        if (hasHeat(entity.getItemBySlot(EquipmentSlot.LEGS))) {
            int l = getAugmentLevel.apply(entity.getItemBySlot(EquipmentSlot.LEGS), augment);
            if (l > maxLevel)
                maxLevel = l;
        }
        if (hasHeat(entity.getItemBySlot(EquipmentSlot.FEET))) {
            int l = getAugmentLevel.apply(entity.getItemBySlot(EquipmentSlot.FEET), augment);
            if (l > maxLevel)
                maxLevel = l;
        }
        return maxLevel;
    }

	@Override
	public int getArmorAugmentLevel(LivingEntity entity, Holder<IAugment> augment) {
		return getArmorAugmentLevel(entity, this::getAugmentLevel, augment);
	}

    @Override
    public int getArmorAugmentLevel(LivingEntity entity, ResourceLocation augment) {
        return getArmorAugmentLevel(entity, this::getAugmentLevel, augment);
    }

    @Override
    public int getArmorAugmentLevel(LivingEntity entity, ResourceKey<IAugment> augment) {
        return getArmorAugmentLevel(entity, this::getAugmentLevel, augment);
    }

    @Override
    @Deprecated
    public int getArmorAugmentLevel(LivingEntity entity, IAugment augment) {
        return getArmorAugmentLevel(entity, this::getAugmentLevel, augment);
    }

    @Override
    public DeferredRegister<IAugment> createDeferredRegister(String modId) {
        return DeferredRegister.create(RegistryManager.AUGMENT_REGISTRY_KEY, modId);
    }

	public static void checkForTag(ItemStack stack) {
        if (!stack.has(RegistryManager.HEAT_COMPONENT))
            stack.set(RegistryManager.HEAT_COMPONENT, HeatComponent.DEFAULT);
	}
}