package hu.zoldleo.embers.item;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.item.IEmberChargedTool;
import hu.zoldleo.embers.datacomponents.EmberToolComponent;
import hu.zoldleo.embers.particle.GlowParticleOptions;
import hu.zoldleo.embers.util.EmberInventoryUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ClockworkToolItem extends DiggerItem implements IEmberChargedTool {
	public ClockworkToolItem(float attackDamageModifier, float attackSpeedModifier, Tier tier, TagKey<Block> blocks, Properties properties) {
		super(tier, blocks, properties.attributes(DiggerItem.createAttributes(tier, attackDamageModifier, attackSpeedModifier)).component(RegistryManager.EMBER_TOOL_COMPONENT, EmberToolComponent.DEFAULT));
	}

	@Override
	public boolean onLeftClickEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull Entity entity) {
		if (hasEmber(stack)) {
            entity.igniteForSeconds(2);
			return false;
		}
		return true;
	}

	@Override
	public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
		if (hasEmber(stack))
			return super.getDestroySpeed(stack, state);
		return 0;
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, @NotNull LivingEntity attacker) {
        stack.update(RegistryManager.EMBER_TOOL_COMPONENT, EmberToolComponent.DEFAULT, x -> x.setUse(true));
		if (target.level() instanceof ServerLevel serverLevel)
			serverLevel.sendParticles(GlowParticleOptions.EMBER, target.getX(), target.getY() + target.getEyeHeight() / 1.5, target.getZ(), 70, 0.15, 0.15, 0.15, 0.6);
		return super.hurtEnemy(stack, target, attacker);
	}

	@Override
	public boolean mineBlock(ItemStack stack, @NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity entityLiving) {
        stack.update(RegistryManager.EMBER_TOOL_COMPONENT, EmberToolComponent.DEFAULT, x -> x.setUse(true));
		return super.mineBlock(stack, level, state, pos, entityLiving);
	}

	@Override
	public boolean isEnchantable(@NotNull ItemStack pStack) {
		return true;
	}

	/*/@Override TODO
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchant){
		return enchant.category != EnchantmentCategory.BREAKABLE && enchant != Enchantments.SWEEPING_EDGE;
	}*/
    // BREAKABLE: unbreaking, mending

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
        EmberToolComponent oldComponent = oldStack.get(RegistryManager.EMBER_TOOL_COMPONENT);
        EmberToolComponent newComponent = newStack.get(RegistryManager.EMBER_TOOL_COMPONENT);
		if (oldComponent != null && newComponent != null)
			return slotChanged || oldComponent.poweredOn() != newComponent.poweredOn() || newStack.getItem() != oldStack.getItem();
		return slotChanged || newStack.getItem() != oldStack.getItem();
	}

	@Override
	public boolean shouldCauseBlockBreakReset(ItemStack oldStack, @NotNull ItemStack newStack) { // TODO: default implementation should probably work as well
        EmberToolComponent oldComponent = oldStack.get(RegistryManager.EMBER_TOOL_COMPONENT);
        EmberToolComponent newComponent = newStack.get(RegistryManager.EMBER_TOOL_COMPONENT);
        if (oldComponent != null && newComponent != null)
			return oldComponent.poweredOn() != newComponent.poweredOn();
		return false;
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level world, @NotNull Entity entity, int slot, boolean selected) {
		if (!selected || world.isClientSide())
			return;
        EmberToolComponent component = stack.get(RegistryManager.EMBER_TOOL_COMPONENT);
		if (component == null) {
            stack.set(RegistryManager.EMBER_TOOL_COMPONENT, EmberToolComponent.DEFAULT);
		} else {
			if (entity instanceof Player player) {
				if (world.getGameTime() % 5 == 0) {
					if (EmberInventoryUtil.getEmberTotal(player) > 5.0) {
						if (!component.poweredOn())
                            stack.set(RegistryManager.EMBER_TOOL_COMPONENT, component.setPower(true));
					} else if (component.poweredOn()) {
                        stack.set(RegistryManager.EMBER_TOOL_COMPONENT, component.setPower(false));
					}
				}
				if (component.didUse()) {
                    stack.set(RegistryManager.EMBER_TOOL_COMPONENT, component.setUse(false));
					EmberInventoryUtil.removeEmber(player, 5.0);
					if (EmberInventoryUtil.getEmberTotal(player) < 5.0)
                        stack.set(RegistryManager.EMBER_TOOL_COMPONENT, component.setPower(false));
				}
			}
		}
	}

	@Override
	public boolean hasEmber(ItemStack stack) {
        EmberToolComponent component = stack.get(RegistryManager.EMBER_TOOL_COMPONENT);
		return component != null && component.poweredOn();
	}
}