package hu.zoldleo.embers.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidOutput {
    public static final Codec<FluidOutput> TAG_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TagKey.hashedCodec(Registries.FLUID).fieldOf("tag").forGetter(x -> x.tag),
            Codec.INT.fieldOf("amount").forGetter(x -> x.amount)
    ).apply(instance, FluidOutput::new));
    public static final Codec<FluidOutput> STACK_CODEC = FluidStack.OPTIONAL_CODEC.xmap(FluidOutput::new, x -> x.stack);
    public static final Codec<FluidOutput> CODEC = Codec.either(TAG_CODEC, STACK_CODEC).xmap(Either::unwrap, x -> x.tag != null ? Either.left(x) : Either.right(x));

    public static final StreamCodec<ByteBuf, FluidOutput> TAG_STREAM_CODEC = StreamCodec.composite(
            Misc.tagKeyStreamCodec(Registries.FLUID), x -> x.tag,
            ByteBufCodecs.INT, x -> x.amount,
            FluidOutput::new
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidOutput> STACK_STREAM_CODEC = FluidStack.OPTIONAL_STREAM_CODEC.map(FluidOutput::new, x -> x.stack);
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidOutput> STREAM_CODEC = ByteBufCodecs.either(TAG_STREAM_CODEC, STACK_STREAM_CODEC).map(Either::unwrap, x -> x.tag != null ? Either.left(x) : Either.right(x));

	public static final FluidOutput EMPTY = new FluidOutput(FluidTags.create(ResourceLocation.parse("empty")), -1);

	public FluidStack stack = FluidStack.EMPTY;
	public TagKey<Fluid> tag;
	public int amount = 0;

	public FluidOutput(FluidStack stack) {
		this.stack = stack;
	}

	public FluidOutput(TagKey<Fluid> tag, int amount) {
		this.tag = tag;
		this.amount = amount;
	}

	public boolean isEmpty() {
		return amount < 0;
	}

	public FluidStack getStack() {
		if (!stack.isEmpty())
			return stack;
		stack = new FluidStack(Misc.getTaggedFluid(tag), amount);
		return stack;
	}

	/*/public static FluidOutput fromJson(JsonObject json) {
		if (json.has("tag")) {
			return new FluidOutput(FluidTags.create(ResourceLocation.parse(GsonHelper.getAsString(json, "tag"))), GsonHelper.getAsInt(json, "amount", 1));
		}
		return new FluidOutput(Misc.deserializeFluidStack(json));
	}

	public JsonObject toJson() {
		if (tag != null) {
			JsonObject json = new JsonObject();
			json.addProperty("tag", tag.location().toString());
			json.addProperty("amount", amount);
			return json;
		}
		return Misc.serializeFluidStack(stack);
	}

	public static FluidOutput fromNetwork(FriendlyByteBuf buffer) {
		if (buffer.readBoolean()) {
			return new FluidOutput(FluidTags.create(buffer.readResourceLocation()), buffer.readInt());
		}
		return new FluidOutput(FluidStack.STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer));
	}

	public void toNetwork(FriendlyByteBuf buffer) {
		if (tag != null) {
			buffer.writeBoolean(true);
			buffer.writeInt(amount);
			buffer.writeResourceLocation(tag.location());
		} else {
			buffer.writeBoolean(false);
            FluidStack.STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, stack);
		}
	}*/
}