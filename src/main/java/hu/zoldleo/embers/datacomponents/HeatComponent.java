package hu.zoldleo.embers.datacomponents;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.augment.IAugment;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

public record HeatComponent(float heat, int heatLevel, ArrayList<AugmentItems> augments) {
    public static final Codec<HeatComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("heat").forGetter(HeatComponent::heat),
            Codec.INT.fieldOf("heat_level").forGetter(HeatComponent::heatLevel),
            AugmentItems.CODEC.listOf().xmap(ArrayList::new, Function.identity()).fieldOf("augments").forGetter(HeatComponent::augments)
    ).apply(instance, HeatComponent::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, HeatComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, HeatComponent::heat,
            ByteBufCodecs.INT, HeatComponent::heatLevel,
            AugmentItems.STREAM_CODEC.apply(ByteBufCodecs.collection(Lists::newArrayListWithCapacity)), HeatComponent::augments,
            HeatComponent::new
    );

    public static final HeatComponent DEFAULT = new HeatComponent(0, 0, new ArrayList<>());

    public float getMaxHeat() {
        return 500f + 250f * heatLevel;
    }

    public HeatComponent addHeat(float heat) {
        return new HeatComponent(Math.min(this.heat + heat, 500f + 250f * heatLevel), heatLevel, augments);
    }

    public HeatComponent setHeat(float heat) {
        return new HeatComponent(heat, heatLevel, augments);
    }

    public HeatComponent setLevel(int heatLevel) {
        return new HeatComponent(heat, heatLevel, augments);
    }

    public HeatComponent withAugments(ArrayList<AugmentItems> augments) {
        return new HeatComponent(heat, heatLevel, augments);
    }

    public HeatComponent copy() {
        return new HeatComponent(heat, heatLevel, augments.stream().map(AugmentItems::copy).collect(Collectors.toCollection(ArrayList::new)));
    }

    public record AugmentItems(Holder<IAugment> augment, int level, ArrayList<ItemStack> items) {
        public static final Codec<AugmentItems> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                RegistryManager.AUGMENT_REGISTRY.holderByNameCodec().fieldOf("name").forGetter(AugmentItems::augment),
                Codec.INT.fieldOf("level").forGetter(AugmentItems::level),
                ItemStack.OPTIONAL_CODEC.listOf().xmap(ArrayList::new, Function.identity()).fieldOf("items").forGetter(AugmentItems::items)
        ).apply(instance, AugmentItems::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, AugmentItems> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.holderRegistry(RegistryManager.AUGMENT_REGISTRY_KEY), AugmentItems::augment,
                ByteBufCodecs.INT, AugmentItems::level,
                ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.collection(Lists::newArrayListWithCapacity)), AugmentItems::items,
                AugmentItems::new
        );

        public double getCost() {
            return augment.value().getCost();
        }

        public boolean countTowardsTotalLevel() {
            return augment.value().countTowardsTotalLevel();
        }

        public boolean canRemove() {
            return augment.value().countTowardsTotalLevel();
        }

        public boolean shouldRenderTooltip() {
            return augment.value().shouldRenderTooltip();
        }

        public AugmentItems addLevel(int level) {
            return new AugmentItems(augment, this.level + level, items);
        }

        public AugmentItems setLevel(int level) {
            return new AugmentItems(augment, level, items);
        }

        public AugmentItems copy() {
            return new AugmentItems(augment, level, items.stream().map(ItemStack::copy).collect(Collectors.toCollection(ArrayList::new)));
        }

        @Override
        @SuppressWarnings("deprecation")
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof AugmentItems(Holder<IAugment> augment1, int level1, ArrayList<ItemStack> items1)))
                return false;
            return level == level1 && augment.equals(augment1) && ItemStack.listMatches(items, items1);
        }

        @Override
        @SuppressWarnings("deprecation")
        public int hashCode() {
            int i = 31 + augment.hashCode();
            i = i * 31 + level;
            return i * 31 + ItemStack.hashStackList(items);
        }
    }
}