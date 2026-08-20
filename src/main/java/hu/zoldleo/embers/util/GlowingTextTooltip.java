package hu.zoldleo.embers.util;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import hu.zoldleo.embers.gui.GuiCodex;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.LinkedList;
import java.util.List;

public class GlowingTextTooltip implements TooltipComponent {
    protected final List<GlowComponent> components;
	public float intensity;

    public GlowingTextTooltip(float intensity) {
        this.intensity = intensity;
        components = new LinkedList<>();
    }

    public GlowingTextTooltip() {
        this(-2f);
    }

    public GlowingTextTooltip addNormal(Component normalText) {
        components.add(new GlowComponent(normalText, false));
        return this;
    }

    public GlowingTextTooltip addGlowing(Component normalText) {
        components.add(new GlowComponent(normalText, true));
        return this;
    }

	public static class GlowingTextClientTooltip implements ClientTooltipComponent {
		GlowingTextTooltip tooltip;

		public GlowingTextClientTooltip(GlowingTextTooltip tooltip) {
			this.tooltip = tooltip;
		}

		@Override
		public int getHeight() {
			return 10;
		}

		@Override
		public int getWidth(@NotNull Font font) {
            int width = 0;
            for (GlowComponent component : tooltip.components)
                width += font.width(component.component());
			return width;
		}

		@Override
		public void renderText(@NotNull Font font, int mouseX, int mouseY, @NotNull Matrix4f matrix, MultiBufferSource.@NotNull BufferSource bufferSource) {
            int offset = 0;
            Matrix4f translatedMatrix = new Matrix4f(matrix).translate(0, 0, 0.06f);
            for (GlowComponent component : tooltip.components) {
                if (component.glowing()) {
                    if (tooltip.intensity < -1f) {
                        GuiCodex.drawTextGlowingAura(font, bufferSource, matrix, component.component().getVisualOrderText(), mouseX + offset, mouseY);
                    } else {
                        font.drawInBatch(component.component(), mouseX, mouseY, -1, true, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
                        GuiCodex.drawTextGlowingAura(font, bufferSource, translatedMatrix, component.component().plainCopy().getVisualOrderText(), mouseX + offset, mouseY, tooltip.intensity);
                    }
                } else {
                    font.drawInBatch(component.component(), mouseX + offset, mouseY, -1, true, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
                }
                offset += font.width(component.component());
            }
		}
	}

    protected record GlowComponent(Component component, boolean glowing) {}
}