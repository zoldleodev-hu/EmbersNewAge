package hu.zoldleo.embers.api.augment;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class AugmentUtil {
	public static IAugmentUtil IMPL;

    public static Collection<Holder<IAugment>> getAllAugments() {
		return IMPL.getAllAugments();
	}

    @Nullable
	public static Holder<IAugment> getAugment(ResourceLocation name) {
		return IMPL.getAugment(name);
	}

	public static List<Holder<IAugment>> getAugments(ItemStack stack) {
		return IMPL.getAugments(stack);
	}

	public static int getTotalAugmentLevel(ItemStack stack) {
		return IMPL.getTotalAugmentLevel(stack);
	}

	public static boolean hasAugment(ItemStack stack, Holder<IAugment> augment) {
		return IMPL.hasAugment(stack, augment);
	}

    public static boolean hasAugment(ItemStack stack, ResourceLocation augment) {
        return IMPL.hasAugment(stack, augment);
    }

    public static boolean hasAugment(ItemStack stack, ResourceKey<IAugment> augment) {
        return IMPL.hasAugment(stack, augment);
    }

    public static boolean hasAugment(ItemStack stack, Predicate<ResourceKey<IAugment>> augment) {
        return IMPL.hasAugment(stack, augment);
    }

    public static boolean hasAugment(ItemStack stack, TagKey<IAugment> augment) {
        return IMPL.hasAugment(stack, augment);
    }

    @Deprecated
    public static boolean hasAugment(ItemStack stack, IAugment augment) {
        return IMPL.hasAugment(stack, augment);
    }

	public static void addAugment(ItemStack stack, ItemStack augmentStack, Holder<IAugment> augment) {
		IMPL.addAugment(stack, augmentStack, augment);
	}

	public static List<ItemStack> removeAllAugments(ItemStack stack) {
		return IMPL.removeAllAugments(stack);
	}

	public static void addAugmentLevel(ItemStack stack, Holder<IAugment> augment, int levels) {
		IMPL.addAugmentLevel(stack, augment, levels);
	}

	public static void setAugmentLevel(ItemStack stack, Holder<IAugment> augment, int level) {
		IMPL.setAugmentLevel(stack, augment, level);
	}

	public static int getAugmentLevel(ItemStack stack, Holder<IAugment> augment) {
		return IMPL.getAugmentLevel(stack, augment);
	}

    public static int getAugmentLevel(ItemStack stack, ResourceLocation augment) {
        return IMPL.getAugmentLevel(stack, augment);
    }

    public static int getAugmentLevel(ItemStack stack, ResourceKey<IAugment> augment) {
        return IMPL.getAugmentLevel(stack, augment);
    }

    @Deprecated(forRemoval = true)
    public static int getAugmentLevel(ItemStack stack, IAugment augment) {
        return IMPL.getAugmentLevel(stack, augment);
    }

	public static boolean hasHeat(ItemStack stack) {
		return IMPL.hasHeat(stack);
	}

	public static void addHeat(ItemStack stack, float heat) {
		IMPL.addHeat(stack, heat);
	}

	public static void setHeat(ItemStack stack, float heat) {
		IMPL.setHeat(stack, heat);
	}

	public static float getHeat(ItemStack stack) {
		return IMPL.getHeat(stack);
	}

	public static float getMaxHeat(ItemStack stack) {
		return IMPL.getMaxHeat(stack);
	}

	public static int getLevel(ItemStack stack) {
		return IMPL.getLevel(stack);
	}

	public static void setLevel(ItemStack stack, int level) {
		IMPL.setLevel(stack, level);
	}

	public static int getArmorAugmentLevel(LivingEntity entity, Holder<IAugment> augment) {
		return IMPL.getArmorAugmentLevel(entity, augment);
	}

    public static int getArmorAugmentLevel(LivingEntity entity, ResourceLocation augment) {
        return IMPL.getArmorAugmentLevel(entity, augment);
    }

    public static int getArmorAugmentLevel(LivingEntity entity, ResourceKey<IAugment> augment) {
        return IMPL.getArmorAugmentLevel(entity, augment);
    }

    @Deprecated
    public static int getArmorAugmentLevel(LivingEntity entity, IAugment augment) {
        return IMPL.getArmorAugmentLevel(entity, augment);
    }

    public static DeferredRegister<IAugment> createDeferredRegister(String modId) {
        return IMPL.createDeferredRegister(modId);
    }
}