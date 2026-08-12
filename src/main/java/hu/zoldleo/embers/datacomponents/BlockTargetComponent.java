package hu.zoldleo.embers.datacomponents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record BlockTargetComponent(ResourceLocation dimension, BlockPos target, Direction face) {
    public static final Codec<BlockTargetComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("dimension").forGetter(BlockTargetComponent::dimension),
            BlockPos.CODEC.fieldOf("target").forGetter(BlockTargetComponent::target),
            Direction.CODEC.fieldOf("face").forGetter(BlockTargetComponent::face)
    ).apply(instance, BlockTargetComponent::new));
    public static final StreamCodec<ByteBuf, BlockTargetComponent> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, BlockTargetComponent::dimension,
            BlockPos.STREAM_CODEC, BlockTargetComponent::target,
            Direction.STREAM_CODEC, BlockTargetComponent::face,
            BlockTargetComponent::new
    );
}