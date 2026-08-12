package hu.zoldleo.embers.util;

import hu.zoldleo.embers.Embers;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class EmberWorldData extends SavedData {
	private static final String NAME = Embers.MODID + "_data";

	public EmberWorldData(CompoundTag nbt) {
		EmberGenUtil.offX = nbt.getInt("offX");
		EmberGenUtil.offZ = nbt.getInt("offZ");
	}

	public static EmberWorldData get(ServerLevel world) {
		return world.getDataStorage().computeIfAbsent(new SavedData.Factory<>(() -> new EmberWorldData(new CompoundTag()), (tag, provider) -> new EmberWorldData(tag)), NAME);
	}

	@Override
	public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.@NotNull Provider provider) {
		tag.putInt("offX", EmberGenUtil.offX);
		tag.putInt("offZ", EmberGenUtil.offZ);
		return tag;
	}
}