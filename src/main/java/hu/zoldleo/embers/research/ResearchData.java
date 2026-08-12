package hu.zoldleo.embers.research;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.datafixers.util.Pair;
import hu.zoldleo.embers.Embers;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

public class ResearchData implements INBTSerializable<CompoundTag> {
    /*/public static final Codec<ResearchData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.pair(ResourceLocation.CODEC, Codec.BOOL).listOf().fieldOf("checkmarks").forGetter(ResearchData::checkmarks)
    ).apply(instance, ResearchData::new));*/

    Map<ResourceLocation, Boolean> checkmarks = new HashMap<>();

    public ResearchData() {

    }

    public ResearchData(List<Pair<ResourceLocation, Boolean>> pairs) {
        for (Pair<ResourceLocation, Boolean> pair : pairs)
            checkmarks.put(pair.getFirst(), pair.getSecond());
    }

    public void setCheckmark(ResourceLocation research, boolean checked) {
        checkmarks.put(research,checked);
    }

    public boolean isChecked(ResourceLocation research) {
        return checkmarks.getOrDefault(research,false);
    }

    public Map<ResourceLocation, Boolean> getCheckmarks() {
        return checkmarks;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        CompoundTag checkmarksTag = new CompoundTag();
        for (Map.Entry<ResourceLocation, Boolean> entry : checkmarks.entrySet())
            checkmarksTag.putBoolean(entry.getKey().toString(), entry.getValue());
        return checkmarksTag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, CompoundTag tag) {
        checkmarks.clear();
        for (String key : tag.getAllKeys()) {
            if (key.contains(":")) {
                checkmarks.put(ResourceLocation.parse(key), tag.getBoolean(key));
                continue;
            }
            checkmarks.put(Embers.res(key), tag.getBoolean(key));
        }
    }

    protected List<Pair<ResourceLocation, Boolean>> checkmarks() {
        return checkmarks.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue())).toList();
    }
}
