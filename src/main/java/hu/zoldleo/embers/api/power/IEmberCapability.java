package hu.zoldleo.embers.api.power;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public interface IEmberCapability {
	double getEmber();
	double getEmberCapacity();
	void setEmber(double value);
	void setEmberCapacity(double value);
	double addAmount(double value, boolean doAdd);
	double removeAmount(double value, boolean doRemove);
    default CompoundTag serializeNBT(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("ember", getEmber());
        tag.putDouble("capacity", getEmberCapacity());
        return tag;
    }
    default void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt) {
        setEmber(nbt.getDouble("ember"));
        setEmberCapacity(nbt.getDouble("capacity"));
    }
    default void writeToNBT(HolderLookup.Provider registries, CompoundTag nbt) {
        nbt.put("EmberStorage", serializeNBT(registries));
    }
    default void readFromNBT(HolderLookup.Provider registries, CompoundTag nbt) {
        deserializeNBT(registries, nbt.getCompound("EmberStorage"));
    }
	default void onContentsChanged() { }
}