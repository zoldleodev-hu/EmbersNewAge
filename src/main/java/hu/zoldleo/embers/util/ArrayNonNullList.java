package hu.zoldleo.embers.util;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.NonNullList;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class ArrayNonNullList<E> extends NonNullList<E> {
    protected ArrayNonNullList(List<E> list, @Nullable E defaultValue) {
        super(list, defaultValue);
    }

    @NotNull
    public static <E> Codec<NonNullList<E>> codecOf(@NotNull Codec<E> entryCodec) {
        return entryCodec.listOf().xmap(ArrayNonNullList::copyOf, Function.identity());
    }

    @NotNull
    public static <B extends ByteBuf, E> StreamCodec<B, NonNullList<E>> streamCodecOf(@NotNull StreamCodec<B, E> entryCodec) {
        return entryCodec.apply(ByteBufCodecs.collection(ArrayNonNullList::createWithCapacity));
    }

    @NotNull
    public static <E> NonNullList<E> copyOf(Collection<? extends E> entries, E defaultValue) {
        return new ArrayNonNullList<>(new ArrayList<>(entries), defaultValue);
    }

    @NotNull
    public static <E> NonNullList<E> copyOf(Collection<? extends E> entries) {
        return new ArrayNonNullList<>(new ArrayList<>(entries), null);
    }

    @NotNull
    public static <E> NonNullList<E> withSize(int size, E defaultValue) {
        return new ArrayNonNullList<>(new ArrayList<>(size), defaultValue);
    }

    @NotNull
    @SafeVarargs
    public static <E> NonNullList<E> of(@NotNull E defaultValue, E... elements) {
        return new ArrayNonNullList<>(Lists.newArrayList(elements), defaultValue);
    }
}