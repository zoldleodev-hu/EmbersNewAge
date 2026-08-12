package hu.zoldleo.embers.augment;

import java.util.Map;
import java.util.WeakHashMap;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.EmbersAPI;
import hu.zoldleo.embers.api.augment.AugmentUtil;
import hu.zoldleo.embers.datacomponents.GearsChargeComponent;
import hu.zoldleo.embers.datagen.EmbersSounds;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber
public class WindingGearsAugment extends AugmentBase {
	public static final ResourceLocation TEXTURE_HUD = Embers.res("textures/gui/icons.png");
	public static final int BAR_U = 0;
	public static final int BAR_V = 32;
	public static final int BAR_WIDTH = 180;
	public static final int BAR_HEIGHT = 8;

	public static final String TAG_CHARGE = "windingGearsCharge";
	public static final String TAG_CHARGE_TIME = "windingGearsLastTime";
	public static final double MAX_CHARGE = 500.0;
	public static final int CHARGE_DECAY_DELAY = 20;
	public static final double CHARGE_DECAY = 0.25;

	static int ticks;
	static double angle, angleLast;
	static int spool, spoolLast;
	static ThreadLocal<Map<Entity, Double>> bounceLocal = ThreadLocal.withInitial(WeakHashMap::new);

	public WindingGearsAugment() {
		super(0.0);
	}

	@OnlyIn(Dist.CLIENT)
	private static int getBarY(int height) {
		return height - 31;
	}

	@OnlyIn(Dist.CLIENT)
	private static int getBarX(int width) {
		return width / 2 - 11 - 81;
	}

	public static ItemStack getHeldClockworkTool(LivingEntity entity) {
		ItemStack mainStack = entity.getMainHandItem();
		ItemStack offStack = entity.getOffhandItem();
		boolean isClockworkMain = isClockworkTool(mainStack);
		boolean isClockworkOff = isClockworkTool(offStack);
		if (isClockworkMain == isClockworkOff)
			return ItemStack.EMPTY;
		if (isClockworkMain)
			return mainStack;
		return offStack;
	}

	public static boolean isClockworkTool(ItemStack stack) {
		return AugmentUtil.hasHeat(stack) && AugmentUtil.hasAugment(stack, RegistryManager.WINDING_GEARS_AUGMENT);
	}

	public static double getChargeDecay(Level world, ItemStack stack) {
		return CHARGE_DECAY;
	}

	public static double getCharge(Level world, ItemStack stack) {
		if (stack.has(RegistryManager.GEARS_CHARGE_COMPONENT)) {
			long dTime = getTimeSinceLastCharge(world, stack);
            GearsChargeComponent component = stack.get(RegistryManager.GEARS_CHARGE_COMPONENT);
            double charge = component != null ? component.charge() : 0;
			return Math.max(0, charge - Math.max(0, dTime - CHARGE_DECAY_DELAY) * getChargeDecay(world,stack));
		}
		return 0;
	}

	private static long getTimeSinceLastCharge(Level world, ItemStack stack) {
		if (stack.has(RegistryManager.GEARS_CHARGE_COMPONENT)) {
            GearsChargeComponent component = stack.get(RegistryManager.GEARS_CHARGE_COMPONENT);
			long lastTime = component != null ? component.chargeTime() : 0;
			long currentTime = world.getGameTime();
			if (lastTime > currentTime)
				return 0;
			else
				return currentTime - lastTime;
		}
		return Long.MAX_VALUE;
	}

	public static double getMaxCharge(Level world, ItemStack stack) {
		int level = getClockworkLevel(stack);
		return Math.min(200.0 * level, MAX_CHARGE);
	}

	private static int getClockworkLevel(ItemStack stack) {
        return AugmentUtil.getAugmentLevel(stack, RegistryManager.WINDING_GEARS_AUGMENT);
	}

	public static void setCharge(Level world, ItemStack stack, double charge) {
		if (world.isClientSide())
			return;
        stack.set(RegistryManager.GEARS_CHARGE_COMPONENT, new GearsChargeComponent(charge, world.getGameTime()));
	}

	public static void depleteCharge(Level world, ItemStack stack, double charge) {
		setCharge(world, stack, Math.max(0, getCharge(world, stack) - charge));
	}

	public static void addCharge(Level world, ItemStack stack, double charge) {
		if (world.isClientSide())
			return;
		setCharge(world, stack, Math.min(getMaxCharge(world, stack), getCharge(world, stack) + charge));
	}

	public static float getSpeedBonus(Level world,ItemStack stack) {
		double charge = getCharge(world,stack);
		return (float) Mth.clampedLerp(-0.2, 20.0, (charge - 50.0) / 300.0);
	}

	public static float getDamageBonus(Level world,ItemStack stack) {
		double charge = getCharge(world,stack);
		return (float) Mth.clampedLerp(1.0, 6.0, (charge - 50.0) / 300.0);
	}

	public static double getRotationSpeed(Level world,ItemStack stack) {
		long dTime = getTimeSinceLastCharge(world, stack);
		double charge = getCharge(world,stack);
		double standardSpeed = Mth.clampedLerp(0.0, 400.0, charge / 500.0);
		if (dTime > CHARGE_DECAY_DELAY && charge > 0)
			return Mth.clampedLerp(0, -10, (dTime - CHARGE_DECAY_DELAY) / 10.0);
		else
			return Mth.clampedLerp(standardSpeed, 0, (dTime - 10) / 10.0);
	}

	@SubscribeEvent
	public static void onJump(LivingEvent.LivingJumpEvent event) {
		LivingEntity entity = event.getEntity();
		ItemStack stack = getHeldClockworkTool(entity);
		if (!stack.isEmpty() && isClockworkTool(entity.getItemBySlot(EquipmentSlot.FEET))) {
			double charge = getCharge(entity.level(), stack);
			double cost = Math.max(16, charge * (80.0 / 500.0));
			if (charge > 0) {
				double x = 0;
				double z = 0;
				if (entity.isSprinting() && charge > Math.max(40, cost * 1.5)) {
					x = entity.getDeltaMovement().x;
					z = entity.getDeltaMovement().z;
					cost = Math.max(40, cost * 1.5);
				}
				entity.setDeltaMovement(entity.getDeltaMovement().add(new Vec3(x, Mth.clampedLerp(0.0, 7.0 / 20.0, charge / 500.0), z)));
				if (charge >= cost)
					entity.playSound(EmbersSounds.WINDING_GEARS_SPRING.get(), 1.0f, 1.0f);
			}

			if (!entity.level().isClientSide())
				depleteCharge(entity.level(), stack, cost);
		}
	}

	@SubscribeEvent
	public static void onTick(EntityTickEvent.Post event) { // TODO
        if (!(event.getEntity() instanceof LivingEntity entity))
            return;
		Map<Entity,Double> bounce = bounceLocal.get();
		if (bounce.containsKey(entity)) {
			entity.setDeltaMovement(entity.getDeltaMovement().add(new Vec3(0, bounce.get(entity), 0)));
			bounce.remove(entity);
		}
	}

	@SubscribeEvent
	public static void onFall(LivingFallEvent event) {
		LivingEntity entity = event.getEntity();
		ItemStack stack = getHeldClockworkTool(entity);
		if (!stack.isEmpty() && isClockworkTool(entity.getItemBySlot(EquipmentSlot.FEET))) {
			double spoolCost = Math.max(0, event.getDistance() - 1) * 5;
			if (getCharge(entity.level(), stack) >= spoolCost) {
				event.setDamageMultiplier(0);
				if (entity.getDeltaMovement().y < -0.5) {
					if (!entity.level().isClientSide())
						depleteCharge(entity.level(), stack,spoolCost);
					bounceLocal.get().put(entity,-entity.getDeltaMovement().y);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onAttack(LivingDamageEvent.Post event) {
		DamageSource source = event.getSource();
		if (source.getEntity() instanceof LivingEntity player) {
			ItemStack mainStack = player.getMainHandItem();
			//float damage = event.getAmount();
			if (isClockworkTool(mainStack)) {
				double charge = getCharge(player.level(), mainStack);
				double cost = 5;
				if (charge >= getMaxCharge(player.level(), mainStack)) {
					//event.setAmount(damage + getDamageBonus(mainStack));
					cost = charge;
				}
				if (!player.level().isClientSide())
					depleteCharge(player.level(), mainStack, cost);
			}
		}
	}

	@SubscribeEvent
	public static void getBreakSpeed(PlayerEvent.BreakSpeed event) {
		Player player = event.getEntity();
		ItemStack mainStack = player.getMainHandItem();
		float speed = event.getNewSpeed();
		if (isClockworkTool(mainStack) && getCharge(player.level(), mainStack) > 0)
            event.setNewSpeed(Math.max(Math.min(speed, 0.1f), speed + getSpeedBonus(player.level(), mainStack)));
	}

	@SubscribeEvent
	public static void onBreak(BlockEvent.BreakEvent event) {
		Player player = event.getPlayer();
		if (!event.isCanceled()) {
			ItemStack mainStack = player.getMainHandItem();
            if (isClockworkTool(mainStack) && getCharge(player.level(), mainStack) > 0 && !player.level().isClientSide())
                depleteCharge(player.level(), mainStack, 40);
		}
	}

	@SubscribeEvent
	public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
		Player player = event.getEntity();
		ItemStack stack = event.getItemStack();

		if (isClockworkTool(stack)) {
			int level = getClockworkLevel(stack);
			double maxCharge = getMaxCharge(player.level(),stack);

			if (level > 0) {
				double resonance = EmbersAPI.getEmberResonance(stack);
				double charge = getCharge(player.level(), stack);
				double addAmount = Math.max((0.025 + 0.01 * level) * (maxCharge - charge), 5 * resonance);
				addCharge(player.level(), stack, addAmount);
				player.swing(event.getHand());
				event.setCancellationResult(InteractionResult.PASS);
				event.setCanceled(true);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onClientUpdate(ClientTickEvent.Pre event) {
        ticks++;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player != null) {
            ItemStack stack = getHeldClockworkTool(player);
            if (!stack.isEmpty()) {
                spoolLast = spool;
                spool = (int) (BAR_WIDTH * 4 * getCharge(player.level(), stack) / MAX_CHARGE);
                angleLast = angle;
                angle += getRotationSpeed(player.level(), stack);
                //Auto-Attack
                if(mc.options.keyAttack.isDown() && mc.hitResult instanceof EntityHitResult entityHit && canAutoAttack(player, stack, entityHit))
                    mc.gameMode.attack(player, entityHit.getEntity());
            }
        }
	}

	@OnlyIn(Dist.CLIENT)
	private static boolean canAutoAttack(LocalPlayer player, ItemStack stack, EntityHitResult objectMouseOver) {
		return player.getAttackStrengthScale(0) >= 1.0f && getCharge(player.level(), stack) > 0 /* && !isInvulnerable(objectMouseOver.entityHit)*/;
	}

	@OnlyIn(Dist.CLIENT)
	private boolean isInvulnerable(Entity entity) {
		return entity.isInvulnerable() || (entity instanceof LivingEntity && entity.invulnerableTime > 0);
	}

    @OnlyIn(Dist.CLIENT)
    public static class Underlay implements LayeredDraw.Layer {
        @Override
        public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
            float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
            int width = guiGraphics.guiWidth();
            int height = guiGraphics.guiHeight();
            int fill = (int) (spoolLast * (1 - partialTicks) + spool * partialTicks);
            fill += 16;
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null)
                return;
            ItemStack stack = getHeldClockworkTool(player);
            if (!stack.isEmpty()) {
                int x = getBarX(width);
                int y = getBarY(height);

                int segs = fill / 32;
                int last = fill % 32;
                int u = BAR_U;
                int v = BAR_V + 8;

                int evenWidth = (segs) * 8;
                int oddWidth = (segs) * 8 - 4;
                int evenFillBack = Mth.clamp(last - 16, 0, 8);
                int oddFillBack = Mth.clamp(last, 0, 8);

                guiGraphics.blit(TEXTURE_HUD, x, y, u, v, evenWidth, BAR_HEIGHT);
                guiGraphics.blit(TEXTURE_HUD, x + evenWidth, y + 8 - evenFillBack, u + evenWidth, v + 8 - evenFillBack, 8, evenFillBack);
                v += 16;
                guiGraphics.blit(TEXTURE_HUD, x, y, u, v, oddWidth, BAR_HEIGHT);
                guiGraphics.blit(TEXTURE_HUD, x + oddWidth, y + 8 - oddFillBack, u + oddWidth, v + 8 - oddFillBack, 8, oddFillBack);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Overlay implements LayeredDraw.Layer {
        @Override
        public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
            float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
            int width = guiGraphics.guiWidth();
            int height = guiGraphics.guiHeight();
            int fill = (int) (spoolLast * (1 - partialTicks) + spool * partialTicks);
            double currentAngle = angleLast * (1 - partialTicks) + angle * partialTicks;
            int gearFrame = (int) (currentAngle * 4f / 360f);
            int uGear = (gearFrame % 4) * 10;
            int vGear = 16;
            fill += 16;
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null)
                return;
            ItemStack stack = getHeldClockworkTool(player);
            if (!stack.isEmpty()) {
                int x = getBarX(width);
                int y = getBarY(height);

                int segs = fill / 32;
                int last = fill % 32;
                int u = BAR_U;
                int v = BAR_V;

                int evenWidth = (segs) * 8;
                int oddWidth = (segs) * 8 - 4;
                int evenFillFront = Mth.clamp(last - 24, 0, 8);
                int oddFillFront = Mth.clamp(last - 8, 0, 8);

                guiGraphics.blit(TEXTURE_HUD, x - 9, y - 1, uGear, vGear, 10, 10);

                guiGraphics.blit(TEXTURE_HUD, x, y, u, v, evenWidth, BAR_HEIGHT);
                guiGraphics.blit(TEXTURE_HUD, x + evenWidth, y, u + evenWidth, v, 8, evenFillFront);
                v += 16;
                guiGraphics.blit(TEXTURE_HUD, x, y, u, v, oddWidth, BAR_HEIGHT);
                guiGraphics.blit(TEXTURE_HUD, x + oddWidth, y, u + oddWidth, v, 8, oddFillFront);
            }
        }
    }
}