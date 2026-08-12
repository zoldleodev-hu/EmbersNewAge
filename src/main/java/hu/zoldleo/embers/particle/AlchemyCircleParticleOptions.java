package hu.zoldleo.embers.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.EmbersAPI;
import hu.zoldleo.embers.util.EmbersColors;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class AlchemyCircleParticleOptions implements ParticleOptions {
	protected final Vector3f color;
	protected final ResourceLocation colorId;
	protected final float scale;
	protected final int lifetime;
	public static final AlchemyCircleParticleOptions DEFAULT = new AlchemyCircleParticleOptions(EmbersColors.EMBER, 1.0F, 420);

	public static final MapCodec<AlchemyCircleParticleOptions> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            ResourceLocation.CODEC.fieldOf("color_id").forGetter((options) -> options.colorId),
            ExtraCodecs.VECTOR3F.fieldOf("color").forGetter((options) -> options.color),
            Codec.FLOAT.fieldOf("scale").forGetter((options) -> options.scale),
            Codec.INT.fieldOf("lifetime").forGetter((options) -> options.lifetime)
    ).apply(instance, AlchemyCircleParticleOptions::new));

    public static final StreamCodec<ByteBuf, AlchemyCircleParticleOptions> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC.codec());

	public AlchemyCircleParticleOptions(ResourceLocation pColorId, Vector3f pColor, float pScale, int lifetime) {
		this.colorId = pColorId;
		this.color = pColor;
		this.scale = pScale;
		this.lifetime = lifetime;
	}

	public AlchemyCircleParticleOptions(Vector3f pColor, float pScale, int lifetime) {
		this(EmbersColors.CUSTOM_ID, pColor, pScale, lifetime);
	}

	public AlchemyCircleParticleOptions(ResourceLocation pColorId, float pScale, int lifetime) {
		this(pColorId, EmbersColors.EMBER, pScale, lifetime);
	}

	public Vector3f getColor() {
		return EmbersAPI.getColor(this.colorId, this.color);
	}

	public float getScale() {
		return this.scale;
	}

	public int getLifetime() {
		return this.lifetime;
	}

	@Override
	public @NotNull ParticleType<?> getType() {
		return RegistryManager.ALCHEMY_CIRCLE_PARTICLE.get();
	}
}