package hu.zoldleo.embers.block;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.capabilities.EmbersCapabilities;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.blockentity.EmberDialBlockEntity;
import hu.zoldleo.embers.util.DecimalFormats;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class EmberDialBlock extends DialBaseBlock {
    public static final MapCodec<EmberDialBlock> CODEC = simpleCodec(EmberDialBlock::new);

	public static final String DIAL_TYPE = "ember";

	public EmberDialBlock(Properties pProperties) {
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
        IEmberCapability cap = level.getCapability(EmbersCapabilities.EMBER_CAPABILITY_BLOCK, pos.relative(state.getValue(FACING), -1), state.getValue(FACING).getOpposite());
        if (cap != null) {
            if (cap.getEmber() >= cap.getEmberCapacity())
                return 15;
            return (int) (Math.ceil(14.0 * cap.getEmber() / cap.getEmberCapacity()));
        }
        return 0;
	}

	@Override
	protected void getBEData(Direction facing, ArrayList<Component> text, BlockEntity blockEntity, int maxLines) {
		if (blockEntity instanceof EmberDialBlockEntity dial && dial.display) {
			text.add(formatEmber(dial.ember, dial.capacity));
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static MutableComponent formatEmber(double ember, double emberCapacity) {
		DecimalFormat emberFormat = DecimalFormats.getDecimalFormat(Embers.MODID + ".decimal_format.ember");
		return Component.translatable(Embers.MODID + ".tooltip.emberdial.ember", emberFormat.format(ember), emberFormat.format(emberCapacity));
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
		return RegistryManager.EMBER_DIAL_ENTITY.get().create(pPos, pState);
	}

	@Override
	public String getDialType() {
		return DIAL_TYPE;
	}
}