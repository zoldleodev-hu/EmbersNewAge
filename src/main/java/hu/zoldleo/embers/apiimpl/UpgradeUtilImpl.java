package hu.zoldleo.embers.apiimpl;

import hu.zoldleo.embers.ConfigManager;
import hu.zoldleo.embers.api.capabilities.EmbersCapabilities;
import hu.zoldleo.embers.api.event.UpgradeEvent;
import hu.zoldleo.embers.api.upgrades.IUpgradeProvider;
import hu.zoldleo.embers.api.upgrades.IUpgradeProxy;
import hu.zoldleo.embers.api.upgrades.IUpgradeUtil;
import hu.zoldleo.embers.api.upgrades.UpgradeContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.*;

public class UpgradeUtilImpl implements IUpgradeUtil {
	public List<UpgradeContext> getUpgrades(Level world, BlockPos pos, Direction[] facings) {
		LinkedList<UpgradeContext> upgrades = new LinkedList<>();
		getUpgrades(world,pos,facings,upgrades);
		return upgrades;
	}

	public void getUpgrades(Level world, BlockPos pos, Direction[] facings, List<UpgradeContext> upgrades) {
		for (Direction facing : facings)
			collectUpgrades(world, pos.relative(facing), facing.getOpposite(), upgrades);
		resetCheckedProxies();
	}

	ThreadLocal<Set<IUpgradeProxy>> checkedProxies = ThreadLocal.withInitial(HashSet::new);

	private boolean isProxyChecked(IUpgradeProxy proxy) {
		return checkedProxies.get().contains(proxy);
	}

	private void addCheckedProxy(IUpgradeProxy proxy) {
		checkedProxies.get().add(proxy);
	}

	private void resetCheckedProxies() {
		checkedProxies.remove();
	}

	public void collectUpgrades(Level world, BlockPos pos, Direction side, List<UpgradeContext> upgrades) {
		collectUpgrades(world, pos, side, upgrades, ConfigManager.MAX_PROXY_DISTANCE.get());
	}

	public void collectUpgrades(Level world, BlockPos pos, Direction side, List<UpgradeContext> upgrades, int distanceLeft) {
		if (distanceLeft < 0)
			return;
        IUpgradeProvider cap = world.getCapability(EmbersCapabilities.UPGRADE_PROVIDER_CAPABILITY, pos, side);
        if (cap != null)
            upgrades.add(new UpgradeContext(cap, ConfigManager.MAX_PROXY_DISTANCE.get() - distanceLeft));
        if (world.getBlockEntity(pos) instanceof IUpgradeProxy proxy) {
            if (!isProxyChecked(proxy) && proxy.isProvider(side)) { //Prevent infinite recursion
				addCheckedProxy(proxy);
				proxy.collectUpgrades(upgrades, distanceLeft - 1);
			}
		}
	}

	public void verifyUpgrades(BlockEntity tile, List<UpgradeContext> list) {
		//Count, remove, sort
		//This call is expensive. Ideally should be cached. The total time complexity is O(n + n^2 + n log n) = O(n^2) for an ArrayList.
		//Total time complexity for a LinkedList should be O(n + n + n log n) = O(n log n). Slightly better.
		HashMap<String, Integer> upgradeCounts = new HashMap<>();
		list.forEach(x -> {
			String id = x.upgrade().getUpgradeId().toString();
			upgradeCounts.put(id, upgradeCounts.getOrDefault(id, 0) + 1);
		});
		list.removeIf(x -> upgradeCounts.get(x.upgrade().getUpgradeId().toString()) > x.upgrade().getLimit(tile));
		list.sort(Comparator.comparingInt(x -> x.upgrade().getPriority()));

		for (UpgradeContext upgrade : list) {
			upgrade.setCount(upgradeCounts.getOrDefault(upgrade.upgrade().getUpgradeId().toString(), 0));
		}
	}

	@Override
	public int getWorkTime(BlockEntity tile, int time, List<UpgradeContext> list) {
		double speedmod = getTotalSpeedModifier(tile, list);
		if (speedmod == 0) //Stop.
			return Integer.MAX_VALUE;
		return (int)(time * (1.0 / speedmod));
	}

	public double getTotalSpeedModifier(BlockEntity tile, List<UpgradeContext> list) {
		double total = 1.0f;

		for (UpgradeContext upgrade : list) {
			total = upgrade.upgrade().getSpeed(tile, total, upgrade.distance(), upgrade.count());
		}

		return total;
	}

	@Override
	public boolean doTick(BlockEntity tile, List<UpgradeContext> list) {
		for (UpgradeContext upgrade : list) {
			if (upgrade.upgrade().doTick(tile, list, upgrade.distance(), upgrade.count()))
				return true;
		}

		return false;
	}

	//DO NOT CALL FROM AN UPGRADE'S doWork METHOD!!
	public boolean doWork(BlockEntity tile, List<UpgradeContext> list) {
		for (UpgradeContext upgrade : list) {
			if (upgrade.upgrade().doWork(tile, list, upgrade.distance(), upgrade.count()))
				return true;
		}

		return false;
	}

	public double getTotalEmberConsumption(BlockEntity tile, double ember, List<UpgradeContext> list) {
		for (UpgradeContext upgrade : list) {
			ember = upgrade.upgrade().transformEmberConsumption(tile, ember, upgrade.distance(), upgrade.count());
		}

		return ember;
	}

	public double getTotalEmberProduction(BlockEntity tile, double ember, List<UpgradeContext> list) {
		for (UpgradeContext upgrade : list) {
			ember = upgrade.upgrade().transformEmberProduction(tile, ember, upgrade.distance(), upgrade.count());
		}

		return ember;
	}

	public void transformOutput(BlockEntity tile, List<ItemStack> outputs, List<UpgradeContext> list) {
		for (UpgradeContext upgrade : list) {
			upgrade.upgrade().transformOutput(tile, outputs, upgrade.distance(), upgrade.count());
		}
	}

	public FluidStack transformOutput(BlockEntity tile, FluidStack output, List<UpgradeContext> list) {
		for (UpgradeContext upgrade : list) {
			output = upgrade.upgrade().transformOutput(tile, output, upgrade.distance(), upgrade.count());
		}

		return output;
	}

	public boolean getOtherParameter(BlockEntity tile, String type, boolean initial, List<UpgradeContext> list) {
		for (UpgradeContext upgrade : list) {
			initial = upgrade.upgrade().getOtherParameter(tile, type, initial, upgrade.distance(), upgrade.count());
		}

		return initial;
	}

	public double getOtherParameter(BlockEntity tile, String type, double initial, List<UpgradeContext> list) {
		for (UpgradeContext upgrade : list)
			initial = upgrade.upgrade().getOtherParameter(tile, type, initial, upgrade.distance(), upgrade.count());

		return initial;
	}

	public int getOtherParameter(BlockEntity tile, String type, int initial, List<UpgradeContext> list) {
		for (UpgradeContext upgrade : list) {
			initial = upgrade.upgrade().getOtherParameter(tile, type, initial, upgrade.distance(), upgrade.count());
		}

		return initial;
	}

	public String getOtherParameter(BlockEntity tile, String type, String initial, List<UpgradeContext> list) {
		for (UpgradeContext upgrade : list) {
			initial = upgrade.upgrade().getOtherParameter(tile, type, initial, upgrade.distance(), upgrade.count());
		}

		return initial;
	}

	public <T> T getOtherParameter(BlockEntity tile, String type, T initial, List<UpgradeContext> list) {
		for (UpgradeContext upgrade : list) {
			initial = upgrade.upgrade().getOtherParameter(tile, type, initial, upgrade.distance(), upgrade.count());
		}

		return initial;
	}

	@Override
	public void throwEvent(BlockEntity tile, UpgradeEvent event, List<UpgradeContext> list) {
		for (UpgradeContext upgrade : list) {
			upgrade.upgrade().throwEvent(tile, list, event, upgrade.distance(), upgrade.count());
		}
	}
}