package hu.zoldleo.embers.api.tile;

import java.util.List;

import hu.zoldleo.embers.Embers;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.capabilities.BlockCapability;

public interface IExtraCapabilityInformation {
	default boolean hasCapabilityDescription(BlockCapability<?, ?> capability) {
		return false;
	}

	default void addCapabilityDescription(List<Component> strings, BlockCapability<?, ?> capability, Direction facing) {
		//NOOP
	}

	default void addOtherDescription(List<Component> strings, Direction facing) {
		//NOOP
	}

	static Component formatCapability(EnumIOType ioType, String type, Component filter) {
		Component typeString = filter == null ? Component.translatable(type) : Component.translatable(Embers.MODID + ".tooltip.goggles.filter", Component.translatable(type), filter);
        return switch (ioType) {
            case NONE -> null;
            case INPUT -> Component.translatable(Embers.MODID + ".tooltip.goggles.input", typeString);
            case OUTPUT -> Component.translatable(Embers.MODID + ".tooltip.goggles.output", typeString);
            default -> Component.translatable(Embers.MODID + ".tooltip.goggles.storage", typeString);
        };
	}

	enum EnumIOType {
		NONE,
		INPUT,
		OUTPUT,
		BOTH
	}
}