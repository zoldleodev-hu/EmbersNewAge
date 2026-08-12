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

public interface IAugmentUtil {
	Collection<Holder<IAugment>> getAllAugments();

    @Nullable
    Holder<IAugment> getAugment(ResourceLocation name);

	List<Holder<IAugment>> getAugments(ItemStack stack);

	int getTotalAugmentLevel(ItemStack stack);

	boolean hasAugment(ItemStack stack, Holder<IAugment> augment);

    boolean hasAugment(ItemStack stack, ResourceLocation augment);

    boolean hasAugment(ItemStack stack, ResourceKey<IAugment> augment);

    boolean hasAugment(ItemStack stack, Predicate<ResourceKey<IAugment>> augment);

    boolean hasAugment(ItemStack stack, TagKey<IAugment> augment);

    @Deprecated
    boolean hasAugment(ItemStack stack, IAugment augment);

	void addAugment(ItemStack stack, ItemStack augmentStack, Holder<IAugment> augment);

	List<ItemStack> removeAllAugments(ItemStack stack);

	void addAugmentLevel(ItemStack stack, Holder<IAugment> augment, int levels);

	void setAugmentLevel(ItemStack stack, Holder<IAugment> augment, int level);

	int getAugmentLevel(ItemStack stack, Holder<IAugment> augment);

    int getAugmentLevel(ItemStack stack, ResourceLocation augment);

    int getAugmentLevel(ItemStack stack, ResourceKey<IAugment> augment);

    @Deprecated
    int getAugmentLevel(ItemStack stack, IAugment augment);

	boolean hasHeat(ItemStack stack);

	void addHeat(ItemStack stack, float heat);

	void setHeat(ItemStack stack, float heat);

	float getHeat(ItemStack stack);

	float getMaxHeat(ItemStack stack);

	int getLevel(ItemStack stack);

	void setLevel(ItemStack stack, int level);

	int getArmorAugmentLevel(LivingEntity entity, Holder<IAugment> augment);

    int getArmorAugmentLevel(LivingEntity entity, ResourceLocation augment);

    int getArmorAugmentLevel(LivingEntity entity, ResourceKey<IAugment> augment);

    @Deprecated
    int getArmorAugmentLevel(LivingEntity entity, IAugment augment);

    DeferredRegister<IAugment> createDeferredRegister(String modId);
}