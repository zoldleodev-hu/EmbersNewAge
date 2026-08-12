package hu.zoldleo.embers.network.message;

import java.util.Map;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.research.ResearchManager;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.codehaus.plexus.util.FastMap;
import org.jetbrains.annotations.NotNull;

public class MessageResearchData implements CustomPacketPayload {
    public static final Type<MessageResearchData> TYPE = new Type<>(Embers.res("research_data"));
    public static final StreamCodec<ByteBuf, MessageResearchData> CODEC = ByteBufCodecs.map(FastMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.BOOL).map(MessageResearchData::new, x -> new FastMap<>(x.ticks));

	protected Map<ResourceLocation, Boolean> ticks;

	public MessageResearchData(Map<ResourceLocation, Boolean> ticks) {
		this.ticks = ticks;
	}

	public static void handle(MessageResearchData msg, IPayloadContext ctx) {
		if (ctx.player() instanceof AbstractClientPlayer)
			ctx.enqueueWork(() -> ResearchManager.receiveResearchData(msg.ticks));
	}

    @Override
    public @NotNull Type<MessageResearchData> type() {
        return TYPE;
    }
}