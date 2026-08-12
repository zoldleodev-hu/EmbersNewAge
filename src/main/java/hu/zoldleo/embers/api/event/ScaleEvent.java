package hu.zoldleo.embers.api.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

public class ScaleEvent extends Event {
	private final LivingEntity entity;
	private double scalePassRate;
	private double scaleDamageRate;
	private final float damage;
	private final DamageSource source;

	public ScaleEvent(LivingEntity entity, float damage, DamageSource source, double scaleDamageRate, double scalePassRate) {
		this.entity = entity;
		this.scalePassRate = scalePassRate;
		this.scaleDamageRate = scaleDamageRate;
		this.damage = damage;
		this.source = source;
	}

	public LivingEntity getEntity(){
		return entity;
	}

	public double getDamage() {
		return damage;
	}

	public DamageSource getDamageSource() {
		return source;
	}

	public double getScalePassRate() {
		return scalePassRate;
	}

	public void setScalePassRate(double scalePassRate) {
		this.scalePassRate = scalePassRate;
	}

	public double getScaleDamageRate() {
		return scaleDamageRate;
	}

	public void setScaleDamageRate(double scaleDamageRate) {
		this.scaleDamageRate = scaleDamageRate;
	}
}