package hu.zoldleo.embers.gui;

import java.util.ArrayList;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.item.AlchemyHintItem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class SlateMenu extends AbstractContainerMenu {
	public static int slateHeight = 158;
	public static int slateWidth = 192;
	public static int heightMargin = 12;
	public static int widthMargin = 15;
	public static int invHeight = 100;
	public static int invWidth = 176;
	public static int slotIndent = -28;
	public static int slotWidth = 26;
	public static int slotHeight = 22;
	public static int slotCount = 5;
	public static int layerHeight = (slateHeight - slotHeight - (2 * heightMargin)) / slotCount;
	public static int layerWidth = slateWidth - slotWidth - (2 * (widthMargin));

	public IItemHandler inventory;
	public ItemStack slate;

	public SlateMenu(int id, Inventory inv, ItemStack slate) {
		super(RegistryManager.SLATE_MENU.get(), id);
		this.slate = slate;
        inventory = slate.getCapability(Capabilities.ItemHandler.ITEM);

		//slate inventory
        if (inventory != null) {
            Slot master = this.addSlot(new SlotItemHandler(inventory, 0, slotIndent, heightMargin + layerHeight / 2 + 16));
            for (int j = 1; j < slotCount; ++j)
                this.addSlot(new SlateSlot(inventory, j, slotIndent, heightMargin + j * layerHeight + layerHeight / 2 + 16, master));
        }
		//player inventory
		for (int l = 0; l < 3; ++l)
			for (int k = 0; k < 9; ++k)
				this.addSlot(new Slot(inv, k + l * 9 + 9, 8 + k * 18, l * 18 + (slateHeight + 18)));
		//hotbar
		for (int i = 0; i < 9; ++i) {
			if (ItemStack.isSameItemSameComponents(inv.getItem(i), slate)) {
				this.addSlot(new LockedSlot(inv, i, 8 + i * 18, slateHeight + 76));
			} else {
				this.addSlot(new Slot(inv, i, 8 + i * 18, slateHeight + 76));
			}
		}
	}

	public boolean stillValid(Player player) {
		return player.getMainHandItem() == slate || player.getOffhandItem() == slate;
	}

	@Override // TODO: why
	public void clicked(int slotId, int dragType, @NotNull ClickType clickTypeIn, @NotNull Player player) {
		super.clicked(slotId, dragType, clickTypeIn, player);
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (index < inventory.getSlots()) {
				if (!this.moveItemStackTo(itemstack1, inventory.getSlots(), this.slots.size(), true))
					return ItemStack.EMPTY;
			} else if (!this.moveItemStackTo(itemstack1, 0, inventory.getSlots(), false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}

		return itemstack;
	}

	public void removed(@NotNull Player player) { // TODO: why
		super.removed(player);
	}

	public static SlateMenu fromBuffer(int id, Inventory inv, RegistryFriendlyByteBuf buf) {
		return new SlateMenu(id, inv, ItemStack.STREAM_CODEC.decode(buf));
	}

	public static class SlateSlot extends SlotItemHandler {
		public Slot master;

		public SlateSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, Slot master) {
			super(itemHandler, index, xPosition, yPosition);
			this.master = master;
		}

		public boolean mayPlace(@NotNull ItemStack stack) {
			ItemStack masterItem = master.getItem();
			if (masterItem.isEmpty())
				return false;
			ArrayList<ItemStack> masterInputs = AlchemyHintItem.getInputs(masterItem);
			ArrayList<ItemStack> inputs = AlchemyHintItem.getInputs(stack);
			if (masterInputs.size() != inputs.size())
				return false;
			for (int i = 0; i < masterInputs.size(); ++i) {
				if (!ItemStack.isSameItem(masterInputs.get(i), inputs.get(i)))
					return false;
			}
			return true;
		}
	}

	public static class LockedSlot extends Slot {
		public LockedSlot(Container container, int id, int x, int y) {
			super(container, id, x, y);
		}

		public boolean mayPlace(@NotNull ItemStack stack) {
			return false;
		}

		public boolean mayPickup(@NotNull Player player) {
			return false;
		}

		public boolean allowModification(@NotNull Player player) {
			return false;
		}
	}
}
