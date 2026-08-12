package hu.zoldleo.embers.item;

import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.gui.GuiCodex;
import hu.zoldleo.embers.research.ResearchBase;
import hu.zoldleo.embers.research.ResearchManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class AncientCodexItem extends Item {
	public AncientCodexItem(Properties pProperties) {
		super(pProperties);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public @NotNull InteractionResult onItemUseFirst(@NotNull ItemStack stack, UseOnContext context) {
		if (context.getLevel().isClientSide() && Screen.hasControlDown() && context.getPlayer() != null) {
			ResearchBase research = ResearchManager.researchByItem.get(context.getLevel().getBlockState(context.getClickedPos()).getBlock().asItem());
			if (research != null) {
				GuiCodex.instance.researchPage = research;
				ResearchManager.sendCheckmark(research, true);
				Minecraft.getInstance().setScreen(GuiCodex.instance);
				context.getLevel().playSound(context.getPlayer(), context.getPlayer(), EmbersSounds.CODEX_OPEN.get(), SoundSource.MASTER, 0.75f, 1.0f);
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand hand) {
		if (level.isClientSide()) {
			Minecraft.getInstance().setScreen(GuiCodex.instance);
			level.playSound(player, player, EmbersSounds.CODEX_OPEN.get(), SoundSource.MASTER, 0.75f, 1.0f);
		}
		return InteractionResultHolder.success(player.getItemInHand(hand));
	}
}