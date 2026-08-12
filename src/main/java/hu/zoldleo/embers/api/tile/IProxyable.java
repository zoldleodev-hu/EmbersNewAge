package hu.zoldleo.embers.api.tile;

import net.minecraft.core.Direction;

public interface IProxyable {
	boolean isSideProxyable(Direction face);
}