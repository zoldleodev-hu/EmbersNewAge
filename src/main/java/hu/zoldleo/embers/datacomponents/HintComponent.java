package hu.zoldleo.embers.datacomponents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record HintComponent(int blackPins, int whitePins, List<ItemStack> aspects, List<ItemStack> inputs, ItemStack result) {
    public static final Codec<HintComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("black_pins").forGetter(HintComponent::blackPins),
            Codec.INT.fieldOf("white_pins").forGetter(HintComponent::whitePins),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("aspects").forGetter(HintComponent::aspects),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("inputs").forGetter(HintComponent::inputs),
            ItemStack.CODEC.fieldOf("result").forGetter(HintComponent::result)
    ).apply(instance, HintComponent::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, HintComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, HintComponent::blackPins,
            ByteBufCodecs.INT, HintComponent::whitePins,
            ItemStack.OPTIONAL_LIST_STREAM_CODEC, HintComponent::aspects,
            ItemStack.OPTIONAL_LIST_STREAM_CODEC, HintComponent::inputs,
            ItemStack.STREAM_CODEC, HintComponent::result,
            HintComponent::new
    );

    @Override
    @SuppressWarnings("deprecation")
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof HintComponent(int blackPins1, int whitePins1, List<ItemStack> aspects1, List<ItemStack> inputs1, ItemStack result1)))
            return false;
        return blackPins == blackPins1 && whitePins == whitePins1 && ItemStack.listMatches(aspects, aspects1) &&
                ItemStack.listMatches(inputs, inputs1) && ItemStack.matches(result, result1);
    }

    @Override
    @SuppressWarnings("deprecation")
    public int hashCode() {
        int i = 31 + blackPins;
        i = i * 31 + whitePins;
        i = i * 31 + ItemStack.hashStackList(aspects);
        i = i * 31 + ItemStack.hashStackList(inputs);
        return i * 31 + ItemStack.hashItemAndComponents(result);
    }
}