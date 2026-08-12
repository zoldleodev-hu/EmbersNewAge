package hu.zoldleo.embers.item;

import java.util.List;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.EmbersClientEvents;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.EmbersAPI;
import hu.zoldleo.embers.api.power.IEmberPacketProducer;
import hu.zoldleo.embers.api.power.IEmberPacketReceiver;
import hu.zoldleo.embers.api.power.ITargetable;

import hu.zoldleo.embers.datacomponents.BlockTargetComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class TinkerHammerItem extends Item {
	public TinkerHammerItem(Properties pProperties) {
		super(pProperties);
		EmbersAPI.registerLinkingHammer(this);
		EmbersAPI.registerHammerTargetGetter(this);
	}

	@Override
	public final @NotNull ItemStack getCraftingRemainingItem(ItemStack stack) {
		if (stack.isEmpty())
			return new ItemStack(this);
		return stack.copy();
	}

	@Override
	public boolean hasCraftingRemainingItem(@NotNull ItemStack stack) {
		return true;
	}

	@Override
	public @NotNull InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        BlockTargetComponent component = stack.get(RegistryManager.BLOCK_TARGET_COMPONENT);
		BlockPos pos = context.getClickedPos();
		Level world = context.getLevel();
		BlockEntity tile = world.getBlockEntity(pos);
        if (component != null && component.dimension().equals(world.dimension().location())) {
            BlockEntity targetTile = world.getBlockEntity(component.target());
            if (targetTile instanceof IEmberPacketProducer producer && targetTile instanceof ITargetable targetable) {
                Vec3 motion = producer.getEmittingDirection(component.face());
                if (tile instanceof IEmberPacketReceiver receiver && motion != null) {
                    targetable.setTargetPosition(pos, component.face());
                    //calculate the trajectory of the ember packet
                    Vec3 hitPos = Vec3.atCenterOf(pos.subtract(component.target()));
                    Vec3 oldPos = new Vec3(0.5, 0.5, 0.5);
                    Vec3 newPos = oldPos.add(motion);

                    for (int i = 0; i <= 80; ++i) {
                        Vec3 targetVector = hitPos.subtract(newPos);
                        double length = targetVector.length();
                        targetVector = targetVector.scale(0.3 / length);
                        double weight = 0;
                        if (length <= 3) {
                            weight = 0.9 * ((3.0 - length) / 3.0);
                            if (length <= 0.2) {
                                break;
                            }
                        }
                        motion = new Vec3(
                                (0.9 - weight) * motion.x + (0.1 + weight) * targetVector.x,
                                (0.9 - weight) * motion.y + (0.1 + weight) * targetVector.y,
                                (0.9 - weight) * motion.z + (0.1 + weight) * targetVector.z);
                        newPos = oldPos.add(motion);
                        oldPos = newPos;
                    }
                    receiver.setIncomingDirection(motion);
                    world.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.5f, 1.5f + world.random.nextFloat() * 0.1f, false);
                    stack.remove(RegistryManager.BLOCK_TARGET_COMPONENT);
                    return InteractionResult.SUCCESS;
                }
            }
        }
		if (tile instanceof IEmberPacketProducer producer && tile instanceof ITargetable) {
			Direction face = context.getClickedFace();
			Vec3 emitDirection = producer.getEmittingDirection(face);
			if (emitDirection == null)
				return InteractionResult.PASS;
            stack.set(RegistryManager.BLOCK_TARGET_COMPONENT, new BlockTargetComponent(world.dimension().location(), pos, face));
			world.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.5f, 1.95f + world.random.nextFloat() * 0.2f, false);
			if (world.isClientSide)
				EmbersClientEvents.lastTarget = null;
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public void appendHoverText(ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag isAdvanced) {
        BlockTargetComponent component = stack.get(RegistryManager.BLOCK_TARGET_COMPONENT);
        Level level = context.level();
		if (level != null && component != null) {
            if(level.dimension().location().equals(component.dimension())) {
                tooltip.add(Component.translatable(Embers.MODID + ".tooltip.aiming_block", level.getBlockState(component.target()).getBlock().getName()).withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable(" X=" + component.target().getX()).withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable(" Y=" + component.target().getY()).withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable(" Z=" + component.target().getZ()).withStyle(ChatFormatting.GRAY));
            }
        }
	}
}