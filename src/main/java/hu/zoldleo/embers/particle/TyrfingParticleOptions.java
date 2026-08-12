package hu.zoldleo.embers.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hu.zoldleo.embers.RegistryManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class TyrfingParticleOptions implements ParticleOptions {
	protected final Vec3 motion;
	protected final float scale;
	protected final int lifetime;
	public static final TyrfingParticleOptions TYRFING = new TyrfingParticleOptions(2.0F);
	public static final TyrfingParticleOptions TYRFING_NOMOTION = new TyrfingParticleOptions(new Vec3(0.0, 0.000001, 0.0), 2.0F);

	public static final MapCodec<TyrfingParticleOptions> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            Vec3.CODEC.fieldOf("motion").forGetter((options) -> options.motion),
            Codec.FLOAT.fieldOf("scale").forGetter((options) -> options.scale),
            Codec.INT.fieldOf("lifetime").forGetter((options) -> options.lifetime)
    ).apply(instance, TyrfingParticleOptions::new));

    public static final StreamCodec<ByteBuf, TyrfingParticleOptions> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC.codec());

	public TyrfingParticleOptions(Vec3 pMotion, float pScale, int plifetime) {
		this.motion = pMotion;
		this.scale = pScale;
		this.lifetime = plifetime;
	}

	public TyrfingParticleOptions(Vec3 pMotion, float pScale) {
		this(pMotion, pScale, -1);
	}

	public TyrfingParticleOptions(float pScale, int plifetime) {
		this(Vec3.ZERO, pScale, plifetime);
	}

	public TyrfingParticleOptions(float pScale) {
		this(Vec3.ZERO, pScale);
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
		return RegistryManager.TYRFING_PARTICLE.get();
	}
}