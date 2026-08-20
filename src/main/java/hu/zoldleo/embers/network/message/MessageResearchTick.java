package hu.zoldleo.embers.network.message;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.research.ResearchManager;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MessageResearchTick implements CustomPacketPayload {
    public static final Type<MessageResearchTick> TYPE = new Type<>(Embers.res("research_tick"));
    public static final StreamCodec<ByteBuf, MessageResearchTick> CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, packet -> packet.research,
            ByteBufCodecs.BOOL, packet -> packet.ticked,
            MessageResearchTick::new
    );

	public ResourceLocation research;
	public boolean ticked;

	public MessageResearchTick(ResourceLocation research, boolean ticked) {
		this.research = research;
		this.ticked = ticked;
	}

	public static void handle(MessageResearchTick msg, IPayloadContext ctx) {
		if (!(ctx.player() instanceof ServerPlayer player))
            return;
        ctx.enqueueWork(() -> {
            Map<ResourceLocation, Boolean> research = ResearchManager.getPlayerResearch(player);
            research.put(msg.research, msg.ticked);
            ResearchManager.sendResearchData(player);
        });
	}

    @Override
    public @NotNull Type<MessageResearchTick> type() {
        return TYPE;
    }
}