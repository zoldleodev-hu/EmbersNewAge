package hu.zoldleo.embers.recipe.ingredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hu.zoldleo.embers.RegistryManager;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import hu.zoldleo.embers.api.augment.AugmentUtil;
import hu.zoldleo.embers.api.augment.IAugment;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.stream.Stream;

public class AugmentIngredient implements ICustomIngredient {
    public static final MapCodec<AugmentIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("base").forGetter(i -> i.base),
            RegistryManager.AUGMENT_REGISTRY.holderByNameCodec().fieldOf("augment").forGetter(i -> i.augment),
            Codec.INT.fieldOf("level").forGetter(i -> i.level),
            Codec.BOOL.optionalFieldOf("inverted", false).forGetter(i -> i.inverted)
    ).apply(instance, AugmentIngredient::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, AugmentIngredient> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, x -> x.base,
            ByteBufCodecs.holderRegistry(RegistryManager.AUGMENT_REGISTRY_KEY), x -> x.augment,
            ByteBufCodecs.INT, x -> x.level,
            ByteBufCodecs.BOOL, x -> x.inverted,
            AugmentIngredient::new
    );

	private final Ingredient base;
	private final Holder<IAugment> augment;
	private final int level;
	private ItemStack[] augmentedMatchingStacks;
	private final boolean inverted;

	public AugmentIngredient(Ingredient base, Holder<IAugment> augment, int level, boolean inverted) {
		this.base = base;
		this.augment = augment;
		this.level = level;
		this.inverted = inverted;
	}

	public static AugmentIngredient of(Ingredient base, Holder<IAugment> augment, int level, boolean inverted) {
		return new AugmentIngredient(base, augment, level, inverted);
	}

	public static AugmentIngredient of(Ingredient base, Holder<IAugment> augment, boolean inverted) {
		return new AugmentIngredient(base, augment, 1, inverted);
	}

	public static AugmentIngredient of(Ingredient base, Holder<IAugment> augment, int level) {
		return new AugmentIngredient(base, augment, level, false);
	}

	public static AugmentIngredient of(Ingredient base, Holder<IAugment> augment) {
		return new AugmentIngredient(base, augment, 1, false);
	}

	@Override
	public boolean test(@Nullable ItemStack stack) {
		if (stack == null || stack.isEmpty())
			return false;
		return base.test(stack) && AugmentUtil.hasHeat(stack) && (inverted ^ AugmentUtil.getAugmentLevel(stack, augment) >= level);
	}

	@Override
	public @NotNull Stream<ItemStack> getItems() {
		if (this.augmentedMatchingStacks == null) {
			ItemStack[] items = base.getItems();
			this.augmentedMatchingStacks = new ItemStack[items.length];
			for (int i = 0; i < items.length; i ++) {
				ItemStack stack = items[i].copy();
				if (inverted)
					AugmentUtil.setHeat(stack, 0);
				else {
					AugmentUtil.setLevel(stack, AugmentUtil.getLevel(stack) + level);
					AugmentUtil.addAugment(stack, ItemStack.EMPTY, augment);
					AugmentUtil.setAugmentLevel(stack, augment, level);
				}
				this.augmentedMatchingStacks[i] = stack;
			}
		}
		return Arrays.stream(augmentedMatchingStacks);
	}

	@Override
	public boolean isSimple() {
		return base.isSimple();
	}

    @Override
    public @NotNull IngredientType<?> getType() {
        return RegistryManager.AUGMENT_INGREDIENT_TYPE.get();
    }

    @Override
    public int hashCode() {
        return 31 * (31 * (31 * base.hashCode() + augment.getRegisteredName().hashCode()) + level) + (inverted ? 1 : 0);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AugmentIngredient other &&
                base.equals(other.base) &&
                augment.equals(other.augment) &&
                level == other.level &&
                inverted == other.inverted;
    }
}