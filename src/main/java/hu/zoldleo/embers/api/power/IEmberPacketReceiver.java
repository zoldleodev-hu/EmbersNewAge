package hu.zoldleo.embers.api.power;

import hu.zoldleo.embers.entity.EmberPacketEntity;

import net.minecraft.world.phys.Vec3;

public interface IEmberPacketReceiver {
	boolean hasRoomFor(double ember);
	boolean onReceive(EmberPacketEntity packet);
	default void setIncomingDirection(Vec3 direction) {};
}