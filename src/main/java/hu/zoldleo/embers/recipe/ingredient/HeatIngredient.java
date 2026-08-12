package hu.zoldleo.embers.recipe.ingredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hu.zoldleo.embers.RegistryManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import hu.zoldleo.embers.api.augment.AugmentUtil;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.stream.Stream;

public class HeatIngredient implements ICustomIngredient {
    public static final MapCodec<HeatIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("base").forGetter(i -> i.base),
            Codec.BOOL.optionalFieldOf("inverted", false).forGetter(i -> i.inverted)
    ).apply(instance, HeatIngredient::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, HeatIngredient> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, x -> x.base,
            ByteBufCodecs.BOOL, x -> x.inverted,
            HeatIngredient::new
    );

	private final Ingredient base;
	private ItemStack[] heatedMatchingStacks;
	private final boolean inverted;

	public HeatIngredient(Ingredient base, boolean inverted) {
		this.base = base;
		this.inverted = inverted;
	}

	public static HeatIngredient of(Ingredient base, boolean inverted) {
		return new HeatIngredient(base, inverted);
	}

	public static HeatIngredient of(Ingredient base) {
		return new HeatIngredient(base, false);
	}

	@Override
	public boolean test(@Nullable ItemStack stack) {
		if (stack == null || stack.isEmpty())
			return false;
		return base.test(stack) && (inverted ^ AugmentUtil.hasHeat(stack));
	}

	@Override
	public @NotNull Stream<ItemStack> getItems() {
		if (!inverted) {
			if (this.heatedMatchingStacks == null) {
				ItemStack[] items = base.getItems();
				this.heatedMatchingStacks = new ItemStack[items.length];
				for (int i = 0; i < items.length; i++) {
					ItemStack stack = items[i].copy();
					AugmentUtil.setHeat(stack, 0);
					this.heatedMatchingStacks[i] = stack;
				}
			}
			return Stream.of(heatedMatchingStacks);
		}
		return Stream.of(base.getItems());
	}

	@Override
	public boolean isSimple() {
		return base.isSimple();
	}

    @Override
    public @NotNull IngredientType<?> getType() {
        return RegistryManager.HEAT_INGREDIENT_TYPE.get();
    }

    @Override
    public int hashCode() {
        return 31 * base.hashCode() + (inverted ? 1 : 0);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof HeatIngredient other &&
                base.equals(other.base) &&
                inverted == other.inverted;
    }
}