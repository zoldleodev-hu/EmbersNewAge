package hu.zoldleo.embers.network.message;

import java.util.UUID;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.EmbersAPI;
import hu.zoldleo.embers.api.augment.AugmentUtil;
import hu.zoldleo.embers.api.event.EmberProjectileEvent;
import hu.zoldleo.embers.api.projectile.EffectDamage;
import hu.zoldleo.embers.api.projectile.IProjectilePreset;
import hu.zoldleo.embers.api.projectile.ProjectileFireball;
import hu.zoldleo.embers.augment.CasterOrbAugment;
import hu.zoldleo.embers.damage.DamageEmber;
import hu.zoldleo.embers.datagen.EmbersDamageTypes;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.util.EmberInventoryUtil;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public class MessageCasterOrb implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageCasterOrb> TYPE = new CustomPacketPayload.Type<>(Embers.res("caster_orb"));
    public static final StreamCodec<ByteBuf, MessageCasterOrb> CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, packet -> packet.lookX,
            ByteBufCodecs.DOUBLE, packet -> packet.lookY,
            ByteBufCodecs.DOUBLE, packet -> packet.lookZ,
            MessageCasterOrb::new
    );

	double lookX;
	double lookY;
	double lookZ;

	public MessageCasterOrb(double lookX, double lookY, double lookZ) {
		this.lookX = lookX;
		this.lookY = lookY;
		this.lookZ = lookZ;
	}

	public static void handle(MessageCasterOrb msg, IPayloadContext ctx) {
		if (ctx.player() instanceof ServerPlayer player) {
			ctx.enqueueWork(() -> {
				ItemStack heldStack = player.getMainHandItem();
				if (AugmentUtil.hasHeat(heldStack)) {
					int level = AugmentUtil.getAugmentLevel(heldStack, RegistryManager.CASTER_ORB_AUGMENT);
					UUID uuid = player.getUUID();
					if (level > 0 && EmberInventoryUtil.getEmberTotal(player) > RegistryManager.CASTER_ORB_AUGMENT.value().getCost() && !CasterOrbAugment.hasCooldown(uuid)) {
						float handmod = player.getMainArm() == HumanoidArm.RIGHT ? 1.0f : -1.0f;
						float offX = handmod * 0.5f * (float) Math.sin(Math.toRadians(-player.getYHeadRot() - 90));
						float offZ = handmod * 0.5f * (float) Math.cos(Math.toRadians(-player.getYHeadRot() - 90));
						EmberInventoryUtil.removeEmber(player, RegistryManager.CASTER_ORB_AUGMENT.value().getCost());
						double lookDist = Math.sqrt(msg.lookX * msg.lookX + msg.lookY * msg.lookY + msg.lookZ * msg.lookZ);
						if (lookDist == 0)
							return;
						double xVel = (msg.lookX / lookDist) * 0.5;
						double yVel = (msg.lookY / lookDist) * 0.5;
						double zVel = (msg.lookZ / lookDist) * 0.5;
						double xOrigin = player.getX() + offX;
						double yOrigin = player.getY() + player.getEyeHeight();
						double zOrigin = player.getZ() + offZ;

						double resonance = EmbersAPI.getEmberResonance(heldStack);
						double value = 8.0 * (Math.atan(0.6 * (level)) / (1.25));
						value *= resonance;

						DamageSource damage = new DamageEmber(player.level().registryAccess().registry(Registries.DAMAGE_TYPE).get().getHolderOrThrow(EmbersDamageTypes.EMBER_KEY), player, true);
						EffectDamage effect = new EffectDamage((float) value, e -> damage, 1, 1.0);
						ProjectileFireball fireball = new ProjectileFireball(player, new Vec3(xOrigin, yOrigin, zOrigin), new Vec3(xVel, yVel, zVel), value, 160, effect);
						EmberProjectileEvent event = new EmberProjectileEvent(player, heldStack, 0.0, fireball);
						NeoForge.EVENT_BUS.post(event);
						if (!event.isCanceled())
							for (IProjectilePreset projectile : event.getProjectiles())
								projectile.shoot(player.level());
						player.level().playSound(null, xOrigin, yOrigin, zOrigin, EmbersSounds.FIREBALL.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
						CasterOrbAugment.setCooldown(uuid, 20);
					}
				}
			});
		}
	}

    @Override
    public @NotNull Type<MessageCasterOrb> type() {
        return TYPE;
    }
}