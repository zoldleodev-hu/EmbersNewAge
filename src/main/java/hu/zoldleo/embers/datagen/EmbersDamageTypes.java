package hu.zoldleo.embers.datagen;

import hu.zoldleo.embers.Embers;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

public class EmbersDamageTypes {
	public static final ResourceKey<DamageType> EMBER_KEY = ResourceKey.create(Registries.DAMAGE_TYPE, Embers.res("ember"));
	public static final DamageType EMBER = new DamageType("ember", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1F);

	public static void generate(BootstrapContext<DamageType> bootstrap) {
		bootstrap.register(EMBER_KEY, EMBER);
	}
}