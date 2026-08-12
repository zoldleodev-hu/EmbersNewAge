package hu.zoldleo.embers.augment;

public class CoreAugment extends AugmentBase {
	public CoreAugment() {
		super(0.0);
	}

	@Override
	public boolean countTowardsTotalLevel() {
		return false;
	}

	@Override
	public boolean canRemove() {
		return false;
	}

	@Override
	public boolean shouldRenderTooltip() {
		return false;
	}
}