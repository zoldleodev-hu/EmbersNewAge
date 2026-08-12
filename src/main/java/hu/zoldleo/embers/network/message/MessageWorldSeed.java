package hu.zoldleo.embers.network.message;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.EmbersClientEvents;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public class MessageWorldSeed implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageWorldSeed> TYPE = new CustomPacketPayload.Type<>(Embers.res("world_seed"));
    public static final StreamCodec<ByteBuf, MessageWorldSeed> CODEC = ByteBufCodecs.VAR_LONG.map(MessageWorldSeed::new, msg -> msg.seed);

	long seed;

	public MessageWorldSeed(long seed) {
		this.seed = seed;
	}

	public static void handle(MessageWorldSeed msg, IPayloadContext ctx) {
		if (!(ctx.player() instanceof AbstractClientPlayer))
            return;
        ctx.enqueueWork(() -> {
            EmbersClientEvents.seed = msg.seed;
        });
	}

    @Override
    public @NotNull Type<MessageWorldSeed> type() {
        return TYPE;
    }
}