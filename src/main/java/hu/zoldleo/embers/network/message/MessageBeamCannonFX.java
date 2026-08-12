package hu.zoldleo.embers.network.message;

import java.util.Random;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.particle.GlowParticleOptions;
import hu.zoldleo.embers.particle.StarParticleOptions;
import hu.zoldleo.embers.util.EmbersColors;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public class MessageBeamCannonFX implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageBeamCannonFX> TYPE = new CustomPacketPayload.Type<>(Embers.res("beam_cannon_fx"));
    public static final StreamCodec<ByteBuf, MessageBeamCannonFX> CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, packet -> packet.posX,
            ByteBufCodecs.DOUBLE, packet -> packet.posY,
            ByteBufCodecs.DOUBLE, packet -> packet.posZ,
            ByteBufCodecs.DOUBLE, packet -> packet.dX,
            ByteBufCodecs.DOUBLE, packet -> packet.dY,
            ByteBufCodecs.DOUBLE, packet -> packet.dZ,
            MessageBeamCannonFX::new
    );
	protected static final Random random = new Random();
	double posX, posY, posZ;
	double dX, dY, dZ;

	public MessageBeamCannonFX(double x, double y, double z, double dX, double dY, double dZ) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.dX = dX;
		this.dY = dY;
		this.dZ = dZ;
	}

	public static void encode(MessageBeamCannonFX msg, FriendlyByteBuf buf) {
		buf.writeDouble(msg.posX);
		buf.writeDouble(msg.posY);
		buf.writeDouble(msg.posZ);
		buf.writeDouble(msg.dX);
		buf.writeDouble(msg.dY);
		buf.writeDouble(msg.dZ);
	}

	public static MessageBeamCannonFX decode(FriendlyByteBuf buf) {
		return new MessageBeamCannonFX(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble());
	}

	public static void handle(MessageBeamCannonFX msg, IPayloadContext ctx) {
		if (ctx.player() instanceof AbstractClientPlayer)
			ctx.enqueueWork(() -> spawnParticles(msg));
	}

	@OnlyIn(Dist.CLIENT)
    public static void spawnParticles(MessageBeamCannonFX msg) {
		Level world = Minecraft.getInstance().level;
		double distance = Math.sqrt(msg.dX * msg.dX + msg.dY * msg.dY + msg.dZ * msg.dZ);
		double segments = distance * 4;
		GlowParticleOptions options = new GlowParticleOptions(EmbersColors.EMBER_ID, 5.0F);
		StarParticleOptions star = new StarParticleOptions(EmbersColors.EMBER_ID, 5.0F);
		for (double i = 0; i < segments; i++) {
			for (int j = 0; j < 5; j++) {
				msg.posX += 0.2 * msg.dX / segments;
				msg.posY += 0.2 * msg.dY / segments;
				msg.posZ += 0.2 * msg.dZ / segments;
				world.addParticle(star, msg.posX, msg.posY, msg.posZ, 0, 0.000001, 0);
			}
		}
		for (int k = 0; k < 80; k++) {
			world.addParticle(options, msg.posX, msg.posY, msg.posZ, 2.5f * (random.nextFloat() - 0.5f), 2.5f * (random.nextFloat() - 0.5f), 2.5f * (random.nextFloat() - 0.5f));
		}
	}

    @Override
    public @NotNull Type<MessageBeamCannonFX> type() {
        return TYPE;
    }
}