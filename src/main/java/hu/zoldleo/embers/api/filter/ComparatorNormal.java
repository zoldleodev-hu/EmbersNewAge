package hu.zoldleo.embers.api.filter;

public abstract class ComparatorNormal implements IFilterComparator {
	private final String name;
	private final int priority;

	public ComparatorNormal(String name, int priority) {
		this.name = name;
		this.priority = priority;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public String getName() {
		return name;
	}
}