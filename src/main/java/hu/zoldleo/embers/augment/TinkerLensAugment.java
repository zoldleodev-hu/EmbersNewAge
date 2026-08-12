package hu.zoldleo.embers.augment;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.EmbersAPI;
import hu.zoldleo.embers.api.augment.AugmentUtil;

public class TinkerLensAugment extends AugmentBase {
	boolean inverted;

	public TinkerLensAugment(boolean inverted) {
		super(0.0);
		//MinecraftForge.EVENT_BUS.register(this);
		this.inverted = inverted;
		if (!inverted)
			EmbersAPI.registerLens((player) -> AugmentUtil.getArmorAugmentLevel(player, RegistryManager.TINKER_LENS_AUGMENT) > 0);
	}

	@Override
	public boolean countTowardsTotalLevel() {
		return false;
	}

	/*@SubscribeEvent
	public void shouldShowInfo(InfoGogglesEvent event) {
		Player player = event.getPlayer();
		int level = AugmentUtil.getArmorAugmentLevel(player, this);
		if (level > 0)
			event.setShouldDisplay(!inverted);
	}*/
}