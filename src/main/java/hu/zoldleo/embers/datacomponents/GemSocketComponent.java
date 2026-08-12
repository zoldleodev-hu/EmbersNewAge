package hu.zoldleo.embers.datacomponents;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import hu.zoldleo.embers.util.ItemStackNonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class GemSocketComponent extends ItemStackNonNullList {
    public static final Codec<GemSocketComponent> CODEC = ItemStack.OPTIONAL_CODEC.listOf().xmap(GemSocketComponent::copy, Function.identity());
    public static final StreamCodec<RegistryFriendlyByteBuf, GemSocketComponent> STREAM_CODEC = ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.collection(GemSocketComponent::createWithCapacity));

    protected GemSocketComponent(List<ItemStack> list) {
        super(list);
    }

    @NotNull
    public static GemSocketComponent copy(Collection<ItemStack> entries) {
        return new GemSocketComponent(new ArrayList<>(entries));
    }

    @NotNull
    public static GemSocketComponent createWithCapacity(int initialCapacity) {
        return new GemSocketComponent(Lists.newArrayListWithCapacity(initialCapacity));
    }

    @NotNull
    public static GemSocketComponent withSize(int size) {
        ItemStack[] aobject = new ItemStack[size];
        Arrays.fill(aobject, ItemStack.EMPTY);
        return new GemSocketComponent(Lists.newArrayList(aobject));
    }

    @NotNull
    public static GemSocketComponent of(ItemStack... elements) {
        return new GemSocketComponent(Lists.newArrayList(elements));
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof GemSocketComponent other))
            return false;
        return ItemStack.listMatches(this, other);
    }

    @Override
    @SuppressWarnings("deprecation")
    public int hashCode() {
        return ItemStack.hashStackList(this);
    }
}