package hu.zoldleo.embers.network.message;

import java.util.Random;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.particle.GlowParticleOptions;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public class MessageCrystalCellGrowFX implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageCrystalCellGrowFX> TYPE = new CustomPacketPayload.Type<>(Embers.res("crystal_cell_grow_fx"));
    public static final StreamCodec<ByteBuf, MessageCrystalCellGrowFX> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, packet -> packet.pos,
            ByteBufCodecs.DOUBLE, packet -> packet.capacity,
            MessageCrystalCellGrowFX::new
    );

	public static Random random = new Random();
	public BlockPos pos;
	double capacity;

	public MessageCrystalCellGrowFX(BlockPos pos, double capacity) {
		this.pos = pos;
		this.capacity = capacity;
	}

	public static void handle(MessageCrystalCellGrowFX msg, IPayloadContext ctx) {
		if (ctx.player() instanceof AbstractClientPlayer)
			ctx.enqueueWork(() -> spawnParticles(msg));
	}

	@OnlyIn(Dist.CLIENT)
    public static void spawnParticles(MessageCrystalCellGrowFX msg) {
		Level level = Minecraft.getInstance().level;
		double angle = random.nextDouble() * 2.0 * Math.PI;
		double x = msg.pos.getX() + 0.5 + 0.5 * Math.sin(angle);
		double z = msg.pos.getZ() + 0.5 + 0.5 * Math.cos(angle);
		double x2 = msg.pos.getX() + 0.5;
		double z2 = msg.pos.getZ() + 0.5;
		float layerHeight = 0.25f;
		float numLayers = 2 + (float) Math.floor(msg.capacity / 120000.0f);
		float height = layerHeight * numLayers;
		for (float i = 0; i < 72; i++) {
			float coeff = i / 72.0f;
			level.addParticle(GlowParticleOptions.EMBER_NOMOTION, x * (1.0f - coeff) + x2 * coeff, msg.pos.getY() + (1.0f - coeff) + (height / 2.0f + 1.5f) * coeff, z * (1.0f - coeff) + z2 * coeff, 0, 0, 0);
		}
		level.playLocalSound(x, msg.pos.getY() + 0.5, z, EmbersSounds.CRYSTAL_CELL_GROW.get(), SoundSource.BLOCKS, 1.0f, 1.0f + random.nextFloat(), false);
	}

    @Override
    public @NotNull Type<MessageCrystalCellGrowFX> type() {
        return TYPE;
    }
}
