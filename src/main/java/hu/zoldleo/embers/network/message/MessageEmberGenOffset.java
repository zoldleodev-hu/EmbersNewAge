package hu.zoldleo.embers.network.message;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.util.EmberGenUtil;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public class MessageEmberGenOffset implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageEmberGenOffset> TYPE = new CustomPacketPayload.Type<>(Embers.res("ember_gen_offset"));
    public static final StreamCodec<ByteBuf, MessageEmberGenOffset> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, packet -> packet.offX,
            ByteBufCodecs.INT, packet -> packet.offZ,
            MessageEmberGenOffset::new
    );

	public int offX;
	public int offZ;

	public MessageEmberGenOffset(int x, int z) {
		this.offX = x;
		this.offZ = z;
	}

	public static void handle(MessageEmberGenOffset msg, IPayloadContext ctx) {
        if (!(ctx.player() instanceof AbstractClientPlayer))
            return;
        ctx.enqueueWork(() -> {
            EmberGenUtil.offX = msg.offX;
            EmberGenUtil.offZ = msg.offZ;
        });
	}

    @Override
    public @NotNull Type<MessageEmberGenOffset> type() {
        return TYPE;
    }
}