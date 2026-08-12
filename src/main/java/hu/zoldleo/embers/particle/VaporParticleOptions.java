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

public class VaporParticleOptions implements ParticleOptions {
	protected final ResourceLocation colorId;
	protected final Vector3f color;
	protected final Vec3 motion;
	protected final float scale;
	public static final VaporParticleOptions VAPOR = new VaporParticleOptions(EmbersColors.VAPOR_ID, 1.0F);

	public static final MapCodec<VaporParticleOptions> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            ResourceLocation.CODEC.fieldOf("color_id").forGetter((options) -> options.colorId),
            ExtraCodecs.VECTOR3F.fieldOf("color").forGetter((options) -> options.color),
            Vec3.CODEC.fieldOf("motion").forGetter((options) -> options.motion),
            Codec.FLOAT.fieldOf("scale").forGetter((options) -> options.scale)
    ).apply(instance, VaporParticleOptions::new));

    public static final StreamCodec<ByteBuf, VaporParticleOptions> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC.codec());

	public VaporParticleOptions(ResourceLocation pColorId, Vector3f pColor, Vec3 pMotion, float pScale) {
		this.colorId = pColorId;
		this.color = pColor;
		this.motion = pMotion;
		this.scale = pScale;
	}

	public VaporParticleOptions(Vector3f pColor, Vec3 pMotion, float pScale) {
		this(EmbersColors.CUSTOM_ID, pColor, pMotion, pScale);
	}

	public VaporParticleOptions(ResourceLocation pColorId, Vec3 pMotion, float pScale) {
		this(pColorId, EmbersColors.SMOKE, pMotion, pScale);
	}

	public VaporParticleOptions(ResourceLocation pColorId, float pScale) {
		this(pColorId, Vec3.ZERO, pScale);
	}

	public VaporParticleOptions(Vector3f pColor, float pScale) {
		this(pColor, Vec3.ZERO, pScale);
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

	@Override
	public @NotNull ParticleType<?> getType() {
		return RegistryManager.VAPOR_PARTICLE.get();
	}
}