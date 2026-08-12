package hu.zoldleo.embers.item;

import java.util.List;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import hu.zoldleo.embers.render.AlchemicalNoteItemRenderer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class AlchemicalNoteItem extends Item {
	public AlchemicalNoteItem(Properties pProperties) {
		super(pProperties);
	}

	/*/@Override
	@OnlyIn(Dist.CLIENT)
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new AlchemicalNoteItemExtensions());
	}*/

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag isAdvanced) {
		ItemStack result = AlchemyHintItem.getResult(stack);
		if (!result.isEmpty())
			tooltip.add(Component.translatable(result.getDescriptionId()).withStyle(ChatFormatting.GRAY));
	}

	@OnlyIn(Dist.CLIENT)
	public static class AlchemicalNoteItemExtensions implements IClientItemExtensions {
		@Override
		public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
			Minecraft minecraft = Minecraft.getInstance();
			return new AlchemicalNoteItemRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
		}
	}
}