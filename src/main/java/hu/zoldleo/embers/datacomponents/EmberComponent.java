package hu.zoldleo.embers.datacomponents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record EmberComponent(double ember, double capacity) {
    public static final Codec<EmberComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("ember").forGetter(EmberComponent::ember),
            Codec.DOUBLE.fieldOf("capacity").forGetter(EmberComponent::capacity)
    ).apply(instance, EmberComponent::new));
    public static final StreamCodec<ByteBuf, EmberComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, EmberComponent::ember,
            ByteBufCodecs.DOUBLE, EmberComponent::capacity,
            EmberComponent::new
    );
}