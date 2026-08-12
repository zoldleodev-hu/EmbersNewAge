package hu.zoldleo.embers.datacomponents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record EmberToolComponent(boolean poweredOn, boolean didUse) {
    public static final Codec<EmberToolComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("powered_on").forGetter(EmberToolComponent::poweredOn),
            Codec.BOOL.fieldOf("did_use").forGetter(EmberToolComponent::didUse)
    ).apply(instance, EmberToolComponent::new));
    public static final StreamCodec<ByteBuf, EmberToolComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, EmberToolComponent::poweredOn,
            ByteBufCodecs.BOOL, EmberToolComponent::didUse,
            EmberToolComponent::new
    );
    public static final EmberToolComponent DEFAULT = new EmberToolComponent(false, false);

    public EmberToolComponent setUse(boolean use) {
        return new EmberToolComponent(poweredOn, use);
    }

    public EmberToolComponent setPower(boolean power) {
        return new EmberToolComponent(power, didUse);
    }
}