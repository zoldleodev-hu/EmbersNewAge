package hu.zoldleo.embers.item;

import hu.zoldleo.embers.gui.SlateMenu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class CodebreakingSlateItem extends Item implements MenuProvider {
	public CodebreakingSlateItem(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand hand) {
		if (!level.isClientSide)
			player.openMenu(this, buf -> ItemStack.STREAM_CODEC.encode(buf, player.getItemInHand(hand)));
		return InteractionResultHolder.success(player.getItemInHand(hand));
	}

	@Override
	public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
		return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
	}

	@Override
	public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, Player player) {
		ItemStack heldItem = player.getMainHandItem();
		return new SlateMenu(id, inv, heldItem);
	}

	@Override
	public @NotNull Component getDisplayName() {
		return getDescription();
	}
}