package hu.zoldleo.embers.network.message;

import java.util.Random;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.api.EmbersAPI;
import hu.zoldleo.embers.particle.GlowParticleOptions;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public class MessageEmberRayFX implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageEmberRayFX> TYPE = new CustomPacketPayload.Type<>(Embers.res("ember_ray_fx"));
    public static final StreamCodec<FriendlyByteBuf, MessageEmberRayFX> CODEC = new StreamCodec<>() {
        public @NotNull MessageEmberRayFX decode(FriendlyByteBuf buf) {
            return new MessageEmberRayFX(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readInt(), buf.readResourceLocation());
        }

        public void encode(FriendlyByteBuf buf, MessageEmberRayFX msg) {
            buf.writeDouble(msg.posX);
            buf.writeDouble(msg.posY);
            buf.writeDouble(msg.posZ);
            buf.writeDouble(msg.dX);
            buf.writeDouble(msg.dY);
            buf.writeDouble(msg.dZ);
            buf.writeDouble(msg.hitDistance);
            buf.writeInt(msg.packedColor);
            buf.writeResourceLocation(msg.colorId);
        }
    };

    protected static final Random random = new Random();
    double posX, posY, posZ;
    double dX, dY, dZ;
    double hitDistance;
    int packedColor;
    ResourceLocation colorId;

    public MessageEmberRayFX(double x, double y, double z, double dX, double dY, double dZ, double hitDistance, int packedColor, ResourceLocation colorId) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.dX = dX;
        this.dY = dY;
        this.dZ = dZ;
        this.hitDistance = hitDistance;
        this.packedColor = packedColor;
        this.colorId = colorId;
    }

    public static void handle(MessageEmberRayFX msg, IPayloadContext ctx) {
        if (ctx.player() instanceof AbstractClientPlayer)
            ctx.enqueueWork(() -> spawnParticles(msg));
    }

    @OnlyIn(Dist.CLIENT)
    public static void spawnParticles(MessageEmberRayFX msg) {
        Level world = Minecraft.getInstance().level;
        double distance = Math.sqrt(msg.dX * msg.dX + msg.dY * msg.dY + msg.dZ * msg.dZ);
        double segments = distance * 4;
        GlowParticleOptions options = new GlowParticleOptions(EmbersAPI.getColor(msg.colorId, Misc.colorFromInt(msg.packedColor)), 2.0F);
        for (double i = 0; i < segments; i++) {
            if (i >= msg.hitDistance * 4) {
                for (int k = 0; k < 80; k++) {
                    world.addParticle(options, msg.posX, msg.posY, msg.posZ, 1.125f * (random.nextFloat() - 0.5f), 1.125f * (random.nextFloat() - 0.5f), 1.125f * (random.nextFloat() - 0.5f));
                }
                break;
            }
            for (int j = 0; j < 5; j++) {
                msg.posX += 0.2 * msg.dX / segments;
                msg.posY += 0.2 * msg.dY / segments;
                msg.posZ += 0.2 * msg.dZ / segments;
                world.addParticle(options, msg.posX, msg.posY, msg.posZ, 0, 0.000001, 0);
            }
        }
    }

    @Override
    public @NotNull Type<MessageEmberRayFX> type() {
        return TYPE;
    }
}