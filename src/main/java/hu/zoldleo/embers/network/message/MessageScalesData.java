package hu.zoldleo.embers.network.message;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.augment.ShiftingScalesAugment;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public class MessageScalesData implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageScalesData> TYPE = new CustomPacketPayload.Type<>(Embers.res("scales_data"));
    public static final StreamCodec<ByteBuf, MessageScalesData> CODEC = ByteBufCodecs.INT.map(MessageScalesData::new, msg -> msg.scales);

	public int scales;

	public MessageScalesData() {
		this.scales = 0;
	}

	public MessageScalesData(int scales) {
		this.scales = scales;
	}

	public MessageScalesData(double scales) {
		this.scales = (int) Math.ceil(scales);
	}

	public static void handle(MessageScalesData msg, IPayloadContext ctx) {
		if (ctx.player() instanceof AbstractClientPlayer)
			ctx.enqueueWork(() -> ShiftingScalesAugment.scales = msg.scales);
	}

    @Override
    public @NotNull Type<MessageScalesData> type() {
        return TYPE;
    }
}