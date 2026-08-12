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

public class StarParticleOptions implements ParticleOptions {
	protected final ResourceLocation colorId;
	protected final Vector3f color;
	protected final float scale;
	public static final StarParticleOptions EMBER = new StarParticleOptions(EmbersColors.EMBER_ID, 2.0F);

	public static final MapCodec<StarParticleOptions> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            ResourceLocation.CODEC.fieldOf("color_id").forGetter((options) -> options.colorId),
            ExtraCodecs.VECTOR3F.fieldOf("color").forGetter((options) -> options.color),
            Codec.FLOAT.fieldOf("scale").forGetter((options) -> options.scale)
    ).apply(instance, StarParticleOptions::new));

    public static final StreamCodec<ByteBuf, StarParticleOptions> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC.codec());

	public StarParticleOptions(ResourceLocation pColorId, Vector3f pColor, float pScale) {
		this.colorId = pColorId;
		this.color = pColor;
		this.scale = pScale;
	}

	public StarParticleOptions(Vector3f pColor, float pScale) {
		this(EmbersColors.CUSTOM_ID, pColor, pScale);
	}

	public StarParticleOptions(ResourceLocation pColorId, float pScale) {
		this(pColorId, EmbersColors.EMBER, pScale);
	}

	public Vector3f getColor() {
		return EmbersAPI.getColor(this.colorId, this.color);
	}

	public float getScale() {
		return this.scale;
	}

	@Override
	public @NotNull ParticleType<?> getType() {
		return RegistryManager.STAR_PARTICLE.get();
	}
}