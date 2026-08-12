package hu.zoldleo.embers.network.message;

import hu.zoldleo.embers.Embers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.commons.lang3.Validate;

import hu.zoldleo.embers.datagen.EmbersSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class MessageItemSound implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageItemSound> TYPE = new CustomPacketPayload.Type<>(Embers.res("item_sound"));
    public static final StreamCodec<FriendlyByteBuf, MessageItemSound> CODEC = new StreamCodec<>() {
        public @NotNull MessageItemSound decode(FriendlyByteBuf buf) {
            return new MessageItemSound(buf.readVarInt(), buf.readById(BuiltInRegistries.ITEM::byId), buf.readById(BuiltInRegistries.SOUND_EVENT::byId), buf.readEnum(SoundSource.class), buf.readBoolean(), buf.readFloat(), buf.readFloat());
        }

        public void encode(FriendlyByteBuf buf, MessageItemSound msg) {
            buf.writeVarInt(msg.id);
            buf.writeById(BuiltInRegistries.ITEM::getId, msg.item); // TODO: why not use resource location?
            buf.writeById(BuiltInRegistries.SOUND_EVENT::getId, msg.sound);
            buf.writeEnum(msg.source);
            buf.writeBoolean(msg.repeat);
            buf.writeFloat(msg.volume);
            buf.writeFloat(msg.pitch);
        }
    };

	private final int id;
	private final Item item;
	private final SoundEvent sound;
	private final SoundSource source;
	private final boolean repeat;
	private final float volume;
	private final float pitch;

	public MessageItemSound(Entity entity, Item item, SoundEvent sound, SoundSource source, boolean repeat, float volume, float pitch) {
		this(entity.getId(), item, sound, source, repeat, volume, pitch);
	}

	public MessageItemSound(int id, Item item, SoundEvent sound, SoundSource source, boolean repeat, float volume, float pitch) {
		Validate.notNull(sound, "sound");
		this.item = item;
		this.sound = sound;
		this.source = source;
		this.id = id;
		this.repeat = repeat;
		this.volume = volume;
		this.pitch = pitch;
	}

	public static void handle(MessageItemSound msg, IPayloadContext ctx) {
        if (!(ctx.player() instanceof AbstractClientPlayer))
            return;
        ctx.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level != null && level.getEntity(msg.id) instanceof LivingEntity entity)
                EmbersSounds.playItemSoundClient(entity, msg.item, msg.sound, msg.source, msg.repeat, msg.volume, msg.pitch);
        });
	}

    @Override
    public @NotNull Type<MessageItemSound> type() {
        return TYPE;
    }
}