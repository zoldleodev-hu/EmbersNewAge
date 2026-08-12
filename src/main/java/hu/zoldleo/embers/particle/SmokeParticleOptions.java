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

public class SmokeParticleOptions implements ParticleOptions {
	protected final ResourceLocation colorId;
	protected final Vector3f color;
	protected final float scale;
	public static final SmokeParticleOptions SMOKE = new SmokeParticleOptions(EmbersColors.SMOKE_ID, 2.0F);
	public static final SmokeParticleOptions BIG_SMOKE = new SmokeParticleOptions(EmbersColors.SMOKE_ID, 5.0F); //a number 6 with extra dip

	public static final MapCodec<SmokeParticleOptions> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            ResourceLocation.CODEC.fieldOf("color_id").forGetter((options) -> options.colorId),
            ExtraCodecs.VECTOR3F.fieldOf("color").forGetter((options) -> options.color),
            Codec.FLOAT.fieldOf("scale").forGetter((options) -> options.scale)
    ).apply(instance, SmokeParticleOptions::new));

    public static final StreamCodec<ByteBuf, SmokeParticleOptions> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC.codec());

	public SmokeParticleOptions(ResourceLocation pColorId, Vector3f pColor, float pScale) {
		this.colorId = pColorId;
		this.color = pColor;
		this.scale = pScale;
	}

	public SmokeParticleOptions(Vector3f pColor, float pScale) {
		this(EmbersColors.CUSTOM_ID, pColor, pScale);
	}

	public SmokeParticleOptions(ResourceLocation pColorId, float pScale) {
		this(pColorId, EmbersColors.SMOKE, pScale);
	}

	public Vector3f getColor() {
		return EmbersAPI.getColor(this.colorId, this.color);
	}

	public float getScale() {
		return this.scale;
	}

	@Override
	public @NotNull ParticleType<?> getType() {
		return RegistryManager.SMOKE_PARTICLE.get();
	}
}