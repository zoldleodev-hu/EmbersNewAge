package hu.zoldleo.embers.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.augment.AugmentUtil;
import hu.zoldleo.embers.api.augment.IAugment;

import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record AugmentPredicate(Holder<IAugment> augment, int level) implements ItemSubPredicate {
    public static final Codec<AugmentPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryManager.AUGMENT_REGISTRY.holderByNameCodec().fieldOf("augment").forGetter(AugmentPredicate::augment),
            Codec.INT.optionalFieldOf("level", 1).forGetter(AugmentPredicate::level)
    ).apply(instance, AugmentPredicate::new));
    public static final ItemSubPredicate.Type<AugmentPredicate> TYPE = new ItemSubPredicate.Type<>(CODEC);
	public static final ResourceLocation ID = Embers.res("augment");

	@Override
	public boolean matches(@NotNull ItemStack stack) {
        return AugmentUtil.hasHeat(stack) && AugmentUtil.getAugmentLevel(stack, augment) >= level;
	}

	public JsonElement serializeToJson() {
		JsonObject json = new JsonObject();
		json.addProperty("type", ID.toString());

		json.addProperty("augment", augment.getRegisteredName());
		if (level != 1)
			json.addProperty("level", level);
		return json;
	}

	public static AugmentPredicate deserialize(JsonObject json) {
        Holder<IAugment> augment = AugmentUtil.getAugment(ResourceLocation.parse(GsonHelper.getAsString(json, "augment")));
		int level = 1;
		if (json.has("level"))
			level = GsonHelper.getAsInt(json, "level");
		return new AugmentPredicate(augment, level);
	}
}
