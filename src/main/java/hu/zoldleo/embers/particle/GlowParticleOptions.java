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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class GlowParticleOptions implements ParticleOptions {
	protected final ResourceLocation colorId;
	protected final Vector3f color;
	protected final Vec3 motion;
	protected final float scale;
	protected final int lifetime;
	@Deprecated
	public static Vector3f EMBER_COLOR = EmbersColors.EMBER;
	public static GlowParticleOptions EMBER = new GlowParticleOptions(EmbersColors.EMBER_ID, 2.0F);
	public static GlowParticleOptions EMBER_NOMOTION = new GlowParticleOptions(EmbersColors.EMBER_ID, new Vec3(0.0, 0.000001, 0.0), 2.0F);

	public static final MapCodec<GlowParticleOptions> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            ResourceLocation.CODEC.fieldOf("color_id").forGetter((options) -> options.colorId),
            ExtraCodecs.VECTOR3F.fieldOf("color").forGetter((options) -> options.color),
            Vec3.CODEC.fieldOf("motion").forGetter((options) -> options.motion),
            Codec.FLOAT.fieldOf("scale").forGetter((options) -> options.scale),
            Codec.INT.fieldOf("lifetime").forGetter((options) -> options.lifetime)
    ).apply(instance, GlowParticleOptions::new));

    public static final StreamCodec<ByteBuf, GlowParticleOptions> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC.codec());

	public GlowParticleOptions(ResourceLocation pColorId, Vector3f pColor, Vec3 pMotion, float pScale, int plifetime) {
		this.colorId = pColorId;
		this.color = pColor;
		this.motion = pMotion;
		this.scale = pScale;
		this.lifetime = plifetime;
	}

	public GlowParticleOptions(Vector3f pColor, Vec3 pMotion, float pScale, int plifetime) {
		this(EmbersColors.CUSTOM_ID, pColor, pMotion, pScale, plifetime);
	}

	public GlowParticleOptions(ResourceLocation pColorId, Vec3 pMotion, float pScale, int plifetime) {
		this(pColorId, EmbersColors.EMBER, pMotion, pScale, plifetime);
	}

	public GlowParticleOptions(Vector3f pColor, Vec3 pMotion, float pScale) {
		this(pColor, pMotion, pScale, -1);
	}

	public GlowParticleOptions(Vector3f pColor, float pScale, int plifetime) {
		this(pColor, Vec3.ZERO, pScale, plifetime);
	}

	public GlowParticleOptions(Vector3f pColor, float pScale) {
		this(pColor, Vec3.ZERO, pScale);
	}

	public GlowParticleOptions(ResourceLocation pColorId, Vec3 pMotion, float pScale) {
		this(pColorId, pMotion, pScale, -1);
	}

	public GlowParticleOptions(ResourceLocation pColorId, float pScale, int plifetime) {
		this(pColorId, Vec3.ZERO, pScale, plifetime);
	}

	public GlowParticleOptions(ResourceLocation pColorId, float pScale) {
		this(pColorId, Vec3.ZERO, pScale);
	}

	public Vector3f getColor() {
		return EmbersAPI.getColor(this.colorId, this.color);
	}

	public Vec3 getMotion() {
		return this.motion;
	}

	public float getScale() {
		return this.scale;
	}

	public int getLifetime() {
		return this.lifetime;
	}

	@Override
	public @NotNull ParticleType<?> getType() {
		return RegistryManager.GLOW_PARTICLE.get();
	}
}