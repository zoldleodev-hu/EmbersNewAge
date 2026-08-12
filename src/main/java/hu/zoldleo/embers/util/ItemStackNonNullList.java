package hu.zoldleo.embers.util;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ItemStackNonNullList extends ArrayNonNullList<ItemStack> {
    public static final Codec<NonNullList<ItemStack>> CODEC = ItemStack.OPTIONAL_CODEC.listOf().xmap(ItemStackNonNullList::copy, Function.identity());
    public static final StreamCodec<RegistryFriendlyByteBuf, NonNullList<ItemStack>> STREAM_CODEC = ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.collection(ItemStackNonNullList::createWithCapacity));

    protected ItemStackNonNullList(List<ItemStack> list) {
        super(list, ItemStack.EMPTY);
    }

    @NotNull
    public static NonNullList<ItemStack> copy(Collection<ItemStack> entries) {
        return new ItemStackNonNullList(new ArrayList<>(entries));
    }

    @NotNull
    public static NonNullList<ItemStack> createWithCapacity(int initialCapacity) {
        return new ItemStackNonNullList(Lists.newArrayListWithCapacity(initialCapacity));
    }

    @NotNull
    public static NonNullList<ItemStack> withSize(int size) {
        ItemStack[] aobject = new ItemStack[size];
        Arrays.fill(aobject, ItemStack.EMPTY);
        return new ItemStackNonNullList(Lists.newArrayList(aobject));
    }

    @NotNull
    public static NonNullList<ItemStack> of(ItemStack... elements) {
        return new ItemStackNonNullList(Lists.newArrayList(elements));
    }

    public void clear() {
        Collections.fill(this, ItemStack.EMPTY);
    }
}