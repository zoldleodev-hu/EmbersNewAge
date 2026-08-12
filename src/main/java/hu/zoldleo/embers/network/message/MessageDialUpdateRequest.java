package hu.zoldleo.embers.network.message;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.api.tile.IDialEntity;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public class MessageDialUpdateRequest implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageDialUpdateRequest> TYPE = new CustomPacketPayload.Type<>(Embers.res("dial_update_request"));
    public static final StreamCodec<ByteBuf, MessageDialUpdateRequest> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, packet -> packet.pos,
            ByteBufCodecs.INT, packet -> packet.maxLines,
            MessageDialUpdateRequest::new
    );

    public BlockPos pos;
    public int maxLines;

    public MessageDialUpdateRequest(BlockPos pos, int maxLines) {
        this.pos = pos;
        this.maxLines = maxLines;
    }

    public static void handle(MessageDialUpdateRequest msg, IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) {
            ctx.enqueueWork(() -> {
                BlockEntity blockEntity = player.level().getBlockEntity(msg.pos);
                if (blockEntity instanceof IDialEntity dial) {
                    Packet<ClientGamePacketListener> packet = dial.getUpdatePacket(msg.maxLines);
                    if (packet != null)
                        player.connection.send(packet);
                }
            });
        }
    }

    @Override
    public @NotNull Type<MessageDialUpdateRequest> type() {
        return TYPE;
    }
}