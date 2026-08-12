package hu.zoldleo.embers.block;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.blockentity.ItemDialBlockEntity;
import hu.zoldleo.embers.util.DecimalFormats;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class ItemDialBlock extends DialBaseBlock {
    public static final MapCodec<ItemDialBlock> CODEC = simpleCodec(ItemDialBlock::new);

	public static final String DIAL_TYPE = "item";

	public ItemDialBlock(Properties pProperties) {
		super(pProperties);
	}

    @Override
    protected @NotNull MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
	public boolean hasAnalogOutputSignal(@NotNull BlockState pState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        IItemHandler cap = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.relative(state.getValue(FACING), -1), state.getValue(FACING).getOpposite());
        if (cap != null) {
            double contents = 0.0;
            double capacity = 0.0;
            for (int i = 0; i < cap.getSlots(); i++) {
                contents += cap.getStackInSlot(i).getCount();
                capacity += cap.getSlotLimit(i);
            }
            if (contents >= capacity)
                return 15;
            return (int) (Math.ceil(14.0 * contents / capacity));
        }
        return 0;
	}

	@Override
	protected void getBEData(Direction facing, ArrayList<Component> text, BlockEntity blockEntity, int maxLines) {
		if (blockEntity instanceof ItemDialBlockEntity dial && dial.display) {
			for (int i = 0; i < dial.itemStacks.length && i < maxLines; i++) {
				text.add(Component.translatable(Embers.MODID + ".tooltip.itemdial.slot", i, formatItemStack(dial.itemStacks[i])));
			}
			if ((dial.itemStacks.length + dial.extraLines) > Math.min(maxLines, dial.itemStacks.length)) {
				text.add(Component.translatable(Embers.MODID + ".tooltip.too_many", dial.itemStacks.length - Math.min(maxLines, dial.itemStacks.length) + dial.extraLines));
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static MutableComponent formatItemStack(ItemStack stack) {
		DecimalFormat stackFormat = DecimalFormats.getDecimalFormat(Embers.MODID + ".decimal_format.item_amount");
		if (!stack.isEmpty())
			return Component.translatable(Embers.MODID + ".tooltip.itemdial.item", stackFormat.format(stack.getCount()), stack.getHoverName().getString());
		else
			return Component.translatable(Embers.MODID + ".tooltip.itemdial.noitem");
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
		return RegistryManager.ITEM_DIAL_ENTITY.get().create(pPos, pState);
	}

	@Override
	public String getDialType() {
		return DIAL_TYPE;
	}
}
