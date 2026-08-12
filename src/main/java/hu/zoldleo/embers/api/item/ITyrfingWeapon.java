package hu.zoldleo.embers.api.item;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public interface ITyrfingWeapon {
	void attack(LivingIncomingDamageEvent event, double armor);
}