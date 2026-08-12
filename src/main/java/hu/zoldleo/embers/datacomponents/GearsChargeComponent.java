package hu.zoldleo.embers.datacomponents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record GearsChargeComponent(double charge, long chargeTime) {
    public static final Codec<GearsChargeComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("charge").forGetter(GearsChargeComponent::charge),
            Codec.LONG.fieldOf("charge_time").forGetter(GearsChargeComponent::chargeTime)
    ).apply(instance, GearsChargeComponent::new));
    public static final StreamCodec<ByteBuf, GearsChargeComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, GearsChargeComponent::charge,
            ByteBufCodecs.VAR_LONG, GearsChargeComponent::chargeTime,
            GearsChargeComponent::new
    );
}