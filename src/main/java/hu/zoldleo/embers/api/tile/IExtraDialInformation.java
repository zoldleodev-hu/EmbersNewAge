package hu.zoldleo.embers.api.tile;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface IExtraDialInformation {
	void addDialInformation(Direction facing, List<Component> information, String dialType);

	default int getComparatorData(Direction facing, int data, String dialType) {
		return data;
	}
}