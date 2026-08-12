package hu.zoldleo.embers.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.item.ItemStack;

public class WeightedItemStack extends WeightedEntry.IntrusiveBase {
	public static WeightedItemStack EMPTY = new WeightedItemStack(ItemStack.EMPTY, 0);

    public static final Codec<WeightedItemStack> CODEC = RecordCodecBuilder.create(instance ->instance.group(
            ItemStack.CODEC.fieldOf("stack").forGetter(WeightedItemStack::getStack),
            Codec.INT.fieldOf("weight").forGetter(x -> x.getWeight().asInt())
    ).apply(instance, WeightedItemStack::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WeightedItemStack> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, WeightedItemStack::getStack,
            ByteBufCodecs.INT, x -> x.getWeight().asInt(),
            WeightedItemStack::new
    );

	ItemStack stack;

	public WeightedItemStack(ItemStack stack, int itemWeightIn) {
		super(itemWeightIn);
		this.stack = stack;
	}

	public ItemStack getStack() {
		return stack;
	}
}