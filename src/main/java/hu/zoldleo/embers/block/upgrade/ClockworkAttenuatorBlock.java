package hu.zoldleo.embers.block.upgrade;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.block.DialBaseBlock;
import hu.zoldleo.embers.blockentity.ClockworkAttenuatorBlockEntity;
import hu.zoldleo.embers.util.DecimalFormats;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class ClockworkAttenuatorBlock extends DialBaseBlock implements EntityBlock {
    public static final MapCodec<ClockworkAttenuatorBlock> CODEC = simpleCodec(ClockworkAttenuatorBlock::new);

	public static final String DIAL_TYPE = "work";

	public ClockworkAttenuatorBlock(Properties pProperties) {
		super(pProperties);
	}

    @Override
    protected @NotNull MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
	public List<Component> getDisplayInfo(Level world, BlockPos pos, BlockState state, int maxLines) {
		List<Component> text = super.getDisplayInfo(world, pos, state, maxLines);
		BlockEntity tile = world.getBlockEntity(pos);
		if (tile instanceof ClockworkAttenuatorBlockEntity) {
			DecimalFormat multiplierFormat = DecimalFormats.getDecimalFormat(Embers.MODID + ".decimal_format.attenuator_multiplier");
			double activeSpeed = ((ClockworkAttenuatorBlockEntity) tile).activeSpeed;
			double inactiveSpeed = ((ClockworkAttenuatorBlockEntity) tile).inactiveSpeed;
			boolean active = world.hasNeighborSignal(pos);
			text.add(Component.translatable(Embers.MODID + ".tooltip.attenuator.on", multiplierFormat.format(activeSpeed)).withStyle(active ? ChatFormatting.GREEN : ChatFormatting.DARK_GREEN));
			text.add(Component.translatable(Embers.MODID + ".tooltip.attenuator.off", multiplierFormat.format(inactiveSpeed)).withStyle(!active ? ChatFormatting.RED : ChatFormatting.DARK_RED));
		}
		return text;
	}

	@Override
	protected void getBEData(Direction facing, ArrayList<Component> text, BlockEntity blockEntity, int maxLines) {}

	@Override
	public @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof ClockworkAttenuatorBlockEntity attenuatorEntity) {
			if (level.hasNeighborSignal(pos))
				attenuatorEntity.activeSpeed = player.isSecondaryUseActive() ? attenuatorEntity.getPrevious(attenuatorEntity.activeSpeed) : attenuatorEntity.getNext(attenuatorEntity.activeSpeed);
			else
				attenuatorEntity.inactiveSpeed = player.isSecondaryUseActive() ? attenuatorEntity.getPrevious(attenuatorEntity.inactiveSpeed) : attenuatorEntity.getNext(attenuatorEntity.inactiveSpeed);
			attenuatorEntity.setChanged();
			return ItemInteractionResult.SUCCESS;
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
		return RegistryManager.CLOCKWORK_ATTENUATOR_ENTITY.get().create(pPos, pState);
	}

	@Override
	public String getDialType() {
		return DIAL_TYPE;
	}
}