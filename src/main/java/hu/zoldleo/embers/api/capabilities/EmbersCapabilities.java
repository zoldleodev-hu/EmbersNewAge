package hu.zoldleo.embers.api.capabilities;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.api.upgrades.IUpgradeProvider;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;

public class EmbersCapabilities {
	public static final BlockCapability<IUpgradeProvider, Direction> UPGRADE_PROVIDER_CAPABILITY = BlockCapability.createSided(Embers.res("upgrade_provider_capability"), IUpgradeProvider.class);
	public static final BlockCapability<IEmberCapability, Direction> EMBER_CAPABILITY_BLOCK = BlockCapability.createSided(Embers.res("ember_capability"), IEmberCapability.class);
	public static final ItemCapability<IEmberCapability, Void> EMBER_CAPABILITY_ITEM = ItemCapability.createVoid(Embers.res("ember_capability"), IEmberCapability.class);
}