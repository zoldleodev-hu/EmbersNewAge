package hu.zoldleo.embers.upgrade;

import java.util.List;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.api.event.AlchemyResultEvent;
import hu.zoldleo.embers.api.event.AlchemyStartEvent;
import hu.zoldleo.embers.api.event.UpgradeEvent;
import hu.zoldleo.embers.api.misc.AlchemyResult;
import hu.zoldleo.embers.api.upgrades.UpgradeContext;
import hu.zoldleo.embers.api.upgrades.UpgradeUtil;
import hu.zoldleo.embers.blockentity.AlchemyTabletBlockEntity;
import hu.zoldleo.embers.blockentity.EntropicEnumeratorBlockEntity;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.world.level.block.entity.BlockEntity;

public class EntropicEnumeratorUpgrade extends DefaultUpgradeProvider {
	public EntropicEnumeratorUpgrade(BlockEntity tile) {
		super(Embers.res("entropic_enumerator"), tile);
	}

	@Override
	public int getPriority() {
		return -90; //before most other things
	}

	@Override
	public void throwEvent(BlockEntity tile, List<UpgradeContext> upgrades, UpgradeEvent event, int distance, int count) {
		if (event instanceof AlchemyStartEvent alchemyEvent && alchemyEvent.getRecipe() != null && this.tile instanceof EntropicEnumeratorBlockEntity enumerator) {
			boolean willFail = true;
			AlchemyResult result = alchemyEvent.getRecipe().value().getResult(alchemyEvent.context, alchemyEvent.getRecipe().id());
			int requirement = alchemyEvent.getRecipe().value().getInputs().size();
			if (result.blackPins == requirement) {
				willFail = false;
			} else {
				boolean first = false;
				for (UpgradeContext upgrade : upgrades) {
					if (upgrade.upgrade() instanceof EntropicEnumeratorUpgrade firstEnumerator) {
						if (firstEnumerator == this) {
							first = true;
						} else {
							willFail = ((EntropicEnumeratorBlockEntity) firstEnumerator.tile).willFail;
						}
						break;
					}
				}
				if (first) {
					if (Misc.random.nextFloat(count + 3) > 3) {
						willFail = true; // TODO
					} else {
						int bonusWhite = Math.min(result.whitePins, count + 1);
						int bonusNothing = count / 2;
						if (requirement <= result.blackPins + bonusWhite + bonusNothing) {
							willFail = false;
						}
					}
				}
				enumerator.willFail = willFail;
			}

			int solveTime = UpgradeUtil.getWorkTime(tile, AlchemyTabletBlockEntity.PROCESSING_TIME * 10, upgrades) - 10;
			if (solveTime < 38 * EntropicEnumeratorBlockEntity.solvingMoveTime) {
				return; //not enough time to solve so don't bother
			}
			enumerator.solve(false, solveTime, willFail);
		}
		if (event instanceof AlchemyResultEvent alchemyEvent && this.tile instanceof EntropicEnumeratorBlockEntity enumerator) {
			alchemyEvent.setFailure(enumerator.willFail);
			enumerator.restartScramble(Misc.random.nextInt(EntropicEnumeratorBlockEntity.queueTime));
		}
	}
}