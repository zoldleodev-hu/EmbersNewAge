package hu.zoldleo.embers.research;

import java.util.ArrayList;
import java.util.List;

import hu.zoldleo.embers.Embers;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ResearchCategoryComingSoon extends ResearchCategory {

	public ResearchCategoryComingSoon(ResourceLocation name, double v) {
		super(name, v);
	}

	public ResearchCategoryComingSoon(ResourceLocation name, double u, double v) {
		super(name, u, v);
	}

	public ResearchCategoryComingSoon(ResourceLocation name, ResourceLocation loc, double u, double v) {
		super(name, loc, u, v);
	}

	@Override
	public boolean isChecked() {
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public List<Component> getTooltip(boolean showTooltips) {
		ArrayList<Component> tooltip = new ArrayList<>();
		if (showTooltips) {
			tooltip.add(Component.translatable(Embers.MODID + ".research.coming_soon"));
		}
		return tooltip;
	}
}
