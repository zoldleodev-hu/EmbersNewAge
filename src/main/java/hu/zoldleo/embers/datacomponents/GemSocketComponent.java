package hu.zoldleo.embers.datacomponents;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class GemSocketComponent {
    public static final Codec<GemSocketComponent> CODEC = ItemStack.OPTIONAL_CODEC.listOf().xmap(GemSocketComponent::new, x -> x.list);
    public static final StreamCodec<RegistryFriendlyByteBuf, GemSocketComponent> STREAM_CODEC = ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()).map(GemSocketComponent::new, x -> x.list);

    private final List<ItemStack> list;
    private int socketedGemCount;

    public GemSocketComponent(int sockets) {
        this(NonNullList.withSize(sockets, ItemStack.EMPTY), 0);
    }

    protected GemSocketComponent(List<ItemStack> list) {
        this.list = list;
        for (ItemStack gem : list) {
            if (gem.isEmpty())
                break;
            socketedGemCount++;
        }
    }

    private GemSocketComponent(List<ItemStack> list, int socketedGemCount) {
        this.list = list;
        this.socketedGemCount = socketedGemCount;
    }

    public int sockets() {
        return list.size();
    }

    public int socketedGems() {
        return socketedGemCount;
    }

    public ItemStack[] getAttachedGems() {
        return list.toArray(new ItemStack[0]);
    }

    public boolean socketGem(ItemStack gem) {
        if (socketedGemCount >= list.size())
            return false;
        list.set(socketedGemCount++, gem);
        return true;
    }

    public ItemStack unsocketGem() {
        if (socketedGemCount <= 0)
            return ItemStack.EMPTY;
        if (socketedGemCount > list.size())
            socketedGemCount = list.size();
        return list.set(--socketedGemCount, ItemStack.EMPTY);
    }

    public ItemStack getLast() {
        return socketedGemCount <= 0 ? ItemStack.EMPTY : list.get(socketedGemCount - 1);
    }

    public GemSocketComponent copy() {
        List<ItemStack> newList = NonNullList.withSize(list.size(), ItemStack.EMPTY);
        for (int i = 0; i < list.size(); i++)
            newList.set(i, list.get(i).copy());
        return new GemSocketComponent(newList, socketedGemCount);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof GemSocketComponent other))
            return false;
        return ItemStack.listMatches(list, other.list);
    }

    @Override
    @SuppressWarnings("deprecation")
    public int hashCode() {
        return ItemStack.hashStackList(list);
    }
}