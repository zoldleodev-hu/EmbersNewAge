package hu.zoldleo.embers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Axis;
import hu.zoldleo.embers.api.augment.AugmentUtil;
import hu.zoldleo.embers.api.augment.IAugment;
import hu.zoldleo.embers.api.block.IDial;
import hu.zoldleo.embers.api.capabilities.EmbersCapabilities;
import hu.zoldleo.embers.api.misc.HammerTarget;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.api.power.IEmberPacketProducer;
import hu.zoldleo.embers.api.power.IEmberPacketReceiver;
import hu.zoldleo.embers.api.tile.IEmberInputHint;
import hu.zoldleo.embers.api.tile.IExtraCapabilityInformation;
import hu.zoldleo.embers.api.tile.IUpgradeable;
import hu.zoldleo.embers.api.upgrades.IUpgradeProxy;
import hu.zoldleo.embers.blockentity.MechanicalCoreBlockEntity.BlockEntityDirection;
import hu.zoldleo.embers.blockentity.render.*;
import hu.zoldleo.embers.datagen.EmbersItemTags;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.gui.GuiCodex;
import hu.zoldleo.embers.mixin.ModelBakerImplMixin;
import hu.zoldleo.embers.render.EmbersRenderTypes;
import hu.zoldleo.embers.research.ResearchBase;
import hu.zoldleo.embers.research.ResearchManager;
import hu.zoldleo.embers.upgrade.ExcavationBucketsUpgrade;
import hu.zoldleo.embers.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.resources.model.ModelBakery.ModelBakerImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30C;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class EmbersClientEvents {
	public static int ticks = 0;
	public static double gaugeAngle = 0;
	public static long seed = 0;
	public static BlockPos lastTarget = null;
	public static BlockPos lastEmitter = null;
	public static ResourceLocation GAUGE = Embers.res("textures/gui/ember_meter_overlay.png"); 
	public static ResourceLocation GAUGE_POINTER = Embers.res("textures/gui/ember_meter_pointer.png"); 

	public static void onLevelLoad(LevelEvent.Load ignored) {
		ticks = 0;
	}

	public static void onClientTick(ClientTickEvent.Pre ignored) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.isPaused()) {
            ticks++;

            if (mc.hitResult instanceof BlockHitResult result && mc.level != null &&
                    result.getType() == BlockHitResult.Type.BLOCK &&
                    mc.level.getBlockState(result.getBlockPos()).getBlock() instanceof IDial dial)
                dial.updateBEData(result.getBlockPos(), Math.max(0, (mc.getWindow().getScreenHeight() / 2 - 100) / 11));
        }
	}

	public static void onMovementInput(MovementInputUpdateEvent event) {
		if (event.getEntity().isUsingItem() && !event.getEntity().isPassenger() && event.getEntity().getItemInHand(event.getEntity().getUsedItemHand()).is(EmbersItemTags.NORMAL_WALK_SPEED_TOOL)) {
			event.getInput().forwardImpulse /= 0.2f;
			event.getInput().leftImpulse /= 0.2f;
			if (event.getEntity().isSprinting())
				event.getEntity().setSprinting(false);
		}
	}

	//do not render the normal block highlight when the glowing highlight is drawn
	public static void onBlockHighlight(RenderHighlightEvent.Block event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.hideGui)
			return;
		HammerTarget target = Misc.getHammerTarget(mc.player);
		if (target != null && event.getTarget().getBlockPos().equals(target.pos)) {
			event.setCanceled(true);
		}
	}

	public static void onLevelRender(RenderLevelStageEvent event) {
		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.options.hideGui)
				return;

			Player player = mc.player;
			HammerTarget target = Misc.getHammerTarget(player);
			if (target != null && mc.level != null && mc.level.isLoaded(target.pos)) {
				BlockPos targetPos = target.pos;
				BlockState state = mc.level.getBlockState(targetPos);
				if (state.isAir())
					return;
				Direction targetDir = target.face;
				Vec3 camPos = event.getCamera().getPosition();
				VertexConsumer consumer = mc.renderBuffers().bufferSource().getBuffer(EmbersRenderTypes.GLOW_LINES);
				Vector3f color = Misc.multColor(EmbersColors.EMBER, (float) (Math.sin(Math.toRadians(4.0f*(event.getRenderTick() + event.getPartialTick().getGameTimeDeltaPartialTick(true))))+1.0f) / 2.0f);
				float alpha = 0.8F;
				double x = targetPos.getX() - camPos.x;
				double y = targetPos.getY() - camPos.y;
				double z = targetPos.getZ() - camPos.z;
				PoseStack.Pose pose = event.getPoseStack().last();

				Shapes.DoubleLineConsumer lineDrawer = (fromX, fromY, fromZ, toX, toY, toZ) -> {
					float f = (float)(toX - fromX);
					float f1 = (float)(toY - fromY);
					float f2 = (float)(toZ - fromZ);
					float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
					f /= f3;
					f1 /= f3;
					f2 /= f3;
					consumer.addVertex(pose.pose(), (float)(fromX + x), (float)(fromY + y), (float)(fromZ + z)).setColor(color.x, color.y, color.z, alpha).setNormal(pose, f, f1, f2);
					consumer.addVertex(pose.pose(), (float)(toX+ x), (float)(toY + y), (float)(toZ + z)).setColor(color.x, color.y, color.z, alpha).setNormal(pose, f, f1, f2);
				};

				Vec3 motion = null;
				if (mc.level.getBlockEntity(targetPos) instanceof IEmberPacketProducer emitter) {
					motion = emitter.getEmittingDirection(targetDir);
				}

				if (mc.hitResult instanceof BlockHitResult result && result.getType() == BlockHitResult.Type.BLOCK && !result.getBlockPos().equals(targetPos) && mc.level.getBlockEntity(result.getBlockPos()) instanceof IEmberPacketReceiver) {
					lastTarget = result.getBlockPos();
				}

				if (motion != null) {
					//LevelRenderer.renderShape(event.getPoseStack(), consumer, player.level.getBlockState(targetPos).getShape(player.level, targetPos), x, y, z, red, green, blue, alpha);
					state.getShape(mc.level, targetPos).forAllEdges(lineDrawer);

					if (lastTarget != null) {
						Vec3 hitPos = Vec3.atCenterOf(lastTarget.subtract(targetPos));
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
							lineDrawer.consume(oldPos.x, oldPos.y, oldPos.z, newPos.x, newPos.y, newPos.z);
							oldPos = newPos;
						}
					} else {
						motion = motion.scale(2.0);
						lineDrawer.consume(0.5, 0.5, 0.5, 0.5 + motion.x, 0.5 + motion.y, 0.5 + motion.z);
					}
				}
			} else {
				lastTarget = null;
			}
			if (Misc.isWearingLens(player) && mc.level != null) {
				if (mc.hitResult instanceof BlockHitResult result && result.getType() == BlockHitResult.Type.BLOCK && mc.level.getBlockEntity(result.getBlockPos()) instanceof IEmberPacketProducer) {
					lastEmitter = result.getBlockPos();
				}
				Vec3 camPos = event.getCamera().getPosition();
				VertexConsumer consumer = mc.renderBuffers().bufferSource().getBuffer(EmbersRenderTypes.GLOW_LINES);
				Vector3f color = Misc.multColor(EmbersColors.EMBER, (float) (Math.sin(Math.toRadians(4.0f*(event.getRenderTick() + event.getPartialTick().getGameTimeDeltaPartialTick(true))))+1.0f) / 2.0f);
				float alpha = 0.6F;
				double x = -camPos.x;
				double y = -camPos.y;
				double z = -camPos.z;
				PoseStack.Pose pose = event.getPoseStack().last();

				Shapes.DoubleLineConsumer lineDrawer = (fromX, fromY, fromZ, toX, toY, toZ) -> {
					float f = (float)(toX - fromX);
					float f1 = (float)(toY - fromY);
					float f2 = (float)(toZ - fromZ);
					float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
					f /= f3;
					f1 /= f3;
					f2 /= f3;
					consumer.addVertex(pose.pose(), (float)(fromX + x), (float)(fromY + y), (float)(fromZ + z)).setColor(color.x, color.y, color.z, alpha).setNormal(pose, f, f1, f2);
					consumer.addVertex(pose.pose(), (float)(toX+ x), (float)(toY + y), (float)(toZ + z)).setColor(color.x, color.y, color.z, alpha).setNormal(pose, f, f1, f2);
				};

				HashSet<BlockPos> drawnLines = new HashSet<>();
				HashSet<BlockPos> linesToDraw = new HashSet<>();
				HashSet<BlockPos> nextLinesToDraw = new HashSet<>();
				linesToDraw.add(lastEmitter);
				for (int i = 0; i <= 20 && !linesToDraw.isEmpty();) {
					for (BlockPos emitterPos : linesToDraw) {
						for (Direction side : Direction.values()) {
							BlockPos newTarget = drawEmittingLine(event, mc.level, mc, lineDrawer, emitterPos, side);
							if (newTarget != null) {
								i++;
								if (!drawnLines.contains(newTarget))
									nextLinesToDraw.add(newTarget);
							}
							drawnLines.add(emitterPos);
						}
					}
					linesToDraw = nextLinesToDraw;
					nextLinesToDraw = new HashSet<>();
				}
			} else {
				lastEmitter = null;
			}
		}
	}

	public static BlockPos drawEmittingLine(RenderLevelStageEvent event, Level level, Minecraft mc, Shapes.DoubleLineConsumer lineDrawer, BlockPos emitterPos, Direction side) {
		BlockPos target = null;
		if (emitterPos != null && level.getBlockEntity(emitterPos) instanceof IEmberPacketProducer emitter) {
			target = emitter.getTarget(side);
			if (target == null)
				return null;
			Vec3 hitPos = Vec3.atCenterOf(target);
			Vec3 motion = emitter.getEmittingDirection(side);
			Vec3 oldPos = Vec3.atCenterOf(emitterPos);
			Vec3 newPos = oldPos.add(motion);

			for (int i = 0; i <= 80; ++i) {
				Vec3 targetVector = hitPos.subtract(newPos);
				double length = targetVector.length();
				targetVector = targetVector.scale(0.3 / length);
				double weight = 0;
				if (length <= 3) {
					weight = 0.9 * ((3.0 - length) / 3.0);
					if (length <= 0.2) {
						lineDrawer.consume(oldPos.x, oldPos.y, oldPos.z, hitPos.x, hitPos.y, hitPos.z);
						break;
					}
				}
				motion = new Vec3(
						(0.9 - weight) * motion.x + (0.1 + weight) * targetVector.x,
						(0.9 - weight) * motion.y + (0.1 + weight) * targetVector.y,
						(0.9 - weight) * motion.z + (0.1 + weight) * targetVector.z);
				newPos = oldPos.add(motion);
				lineDrawer.consume(oldPos.x, oldPos.y, oldPos.z, newPos.x, newPos.y, newPos.z);
				oldPos = newPos;
			}
		}
		return target;
	}

	public static void afterModelBake(ModelEvent.BakingCompleted event) {
		ModelBakery bakery = event.getModelManager().getModelBakery();
		EmberBoreBlockEntityRenderer.blades = getModel(bakery, "ember_bore_blades");
		MechanicalPumpBlockEntityRenderer.pistonBottom = getModel(bakery, "mechanical_pump_piston_bottom");
		MechanicalPumpBlockEntityRenderer.pistonTop = getModel(bakery, "mechanical_pump_piston_top");
		StamperBlockEntityRenderer.arm = getModel(bakery, "stamper_arm");
		AutomaticHammerBlockEntityRenderer.hammer = getModel(bakery, "automatic_hammer_end");
		InfernoForgeTopBlockEntityRenderer.hatch = getModel(bakery, "inferno_forge_hatch");
		AtmosphericBellowsBlockEntityRenderer.top = getModel(bakery, "atmospheric_bellows_top");
		AtmosphericBellowsBlockEntityRenderer.leather = getModel(bakery, "atmospheric_bellows_leather");
		EntropicEnumeratorBlockEntityRenderer.cubies[0][0][0] = getModel(bakery, "entropic_enumerator_drf");
		EntropicEnumeratorBlockEntityRenderer.cubies[1][0][0] = getModel(bakery, "entropic_enumerator_dlf");
		EntropicEnumeratorBlockEntityRenderer.cubies[0][1][0] = getModel(bakery, "entropic_enumerator_urf");
		EntropicEnumeratorBlockEntityRenderer.cubies[1][1][0] = getModel(bakery, "entropic_enumerator_ulf");
		EntropicEnumeratorBlockEntityRenderer.cubies[0][0][1] = getModel(bakery, "entropic_enumerator_drb");
		EntropicEnumeratorBlockEntityRenderer.cubies[1][0][1] = getModel(bakery, "entropic_enumerator_dlb");
		EntropicEnumeratorBlockEntityRenderer.cubies[0][1][1] = getModel(bakery, "entropic_enumerator_urb");
		EntropicEnumeratorBlockEntityRenderer.cubies[1][1][1] = getModel(bakery, "entropic_enumerator_ulb");
		ExcavationBucketsBlockEntityRenderer.wheel = getModel(bakery, "excavation_buckets_wheel");
		ExcavationBucketsUpgrade.buckets = getModel(bakery, "ember_bore_excavation_buckets");
	}

	public static BakedModel getModel(ModelBakery bakery, String name) {
		ResourceLocation location = Embers.res("block/" + name);
		ModelBakerImpl bakerImpl = ModelBakerImplMixin.ctor(bakery, (modelLoc, material) -> material.sprite(), ModelResourceLocation.standalone(location));
		UnbakedModel model = bakerImpl.getModel(location);
		return model.bake(bakerImpl, Material::sprite, BlockModelRotation.X0_Y0);
	}

	public static int tickStartedHoldingCtrl = Integer.MAX_VALUE;

	public static void onTooltip(RenderTooltipEvent.GatherComponents event) {
		Minecraft mc = Minecraft.getInstance();
		int codexIndex = -1;
		if (ConfigManager.CODEX_REQUIRED_FOR_LOOKUP.get() && mc.player != null) {
			for (int i = 0; i < Inventory.getSelectionSize(); i++) {
				if (mc.player.getInventory().getItem(i).is(EmbersItemTags.ANCIENT_CODEX)) {
					codexIndex = i;
					break;
				}
			}
		}
		if (codexIndex >= 0 || !ConfigManager.CODEX_REQUIRED_FOR_LOOKUP.get()) {
			ResearchBase research = ResearchManager.researchByItem.get(event.getItemStack().getItem());
			if (research != null) {
				float openProgress = 0;
				if (Screen.hasControlDown()) {
					if (tickStartedHoldingCtrl == Integer.MAX_VALUE) {
						tickStartedHoldingCtrl = ticks;
					}
					openProgress = mc.getTimer().getGameTimeDeltaPartialTick(true) + ticks - tickStartedHoldingCtrl;
				} else {
					tickStartedHoldingCtrl = Integer.MAX_VALUE;
				}
				float intensity = (float) (5.0f * (1 - Math.sqrt(1 - Math.pow(openProgress / ((float) ConfigManager.TICKS_TO_OPEN_CODEX.get()), 2)))) - 1.0f;
				event.getTooltipElements().add(1, Either.right(new GlowingTextTooltip(Component.translatable(Embers.MODID + ".tooltip.research").withStyle(ChatFormatting.DARK_GRAY), intensity)));
				if (openProgress >= ConfigManager.TICKS_TO_OPEN_CODEX.get() && mc.player != null && mc.level != null) {
					if (ConfigManager.CODEX_REQUIRED_FOR_LOOKUP.get())
						mc.player.getInventory().selected = codexIndex;
					GuiCodex.instance.previousScreen = mc.screen;
					GuiCodex.instance.researchPage = research;
					mc.setScreen(GuiCodex.instance);
					ResearchManager.sendCheckmark(research, true);
					mc.level.playSound(mc.player, mc.player, EmbersSounds.CODEX_PAGE_OPEN.get(), SoundSource.MASTER, 0.75f, 1.0f);
				}
			}
		}
		if (AugmentUtil.hasHeat(event.getItemStack())) {
			event.getTooltipElements().add(Either.left(Component.empty()));
			if (AugmentUtil.getLevel(event.getItemStack()) > 0) {
				event.getTooltipElements().add(Either.right(new GlowingTextTooltip(Component.translatable(Embers.MODID + ".tooltip.heat_level").withStyle(ChatFormatting.GRAY), Component.literal("" + AugmentUtil.getLevel(event.getItemStack())))));
				int slots = AugmentUtil.getLevel(event.getItemStack()) - AugmentUtil.getTotalAugmentLevel(event.getItemStack());
				if (slots > 0)
					event.getTooltipElements().add(Either.right(new GlowingTextTooltip(Component.translatable(Embers.MODID + ".tooltip.augment_slots").withStyle(ChatFormatting.GRAY), Component.literal("" + slots))));
			}
			float heat = AugmentUtil.getHeat(event.getItemStack());
			float maxHeat = AugmentUtil.getMaxHeat(event.getItemStack());
			event.getTooltipElements().add(Either.right(new HeatBarTooltip(Component.translatable(Embers.MODID + ".tooltip.heat_amount").withStyle(ChatFormatting.GRAY).getVisualOrderText(), heat, maxHeat)));
			if (mc.options.advancedItemTooltips)
				event.getTooltipElements().add(Either.left(Component.translatable(Embers.MODID + ".tooltip.heat_debug", heat, maxHeat).withStyle(ChatFormatting.DARK_GRAY)));

			List<Holder<IAugment>> augments = AugmentUtil.getAugments(event.getItemStack()).stream().filter(x -> x.value().shouldRenderTooltip()).toList();
			if (!augments.isEmpty()) {
				event.getTooltipElements().add(Either.left(Component.translatable(Embers.MODID + ".tooltip.augments").withStyle(ChatFormatting.GRAY)));
				for (Holder<IAugment> augment : augments) {
					int level = AugmentUtil.getAugmentLevel(event.getItemStack(), augment);
					event.getTooltipElements().add(Either.right(new GlowingTextTooltip(Component.translatable(Embers.MODID + ".tooltip.augment." + augment.getKey().location().toLanguageKey(), Component.translatable(getFormattedModifierLevel(level))))));
				}
			}
		}
	}

	public static String getFormattedModifierLevel(int level) {
		String key = Embers.MODID + ".tooltip.num" + level;
		if (I18n.exists(key))
			return key;
		else
			return Embers.MODID + ".tooltip.numstop";
	}

	public static RenderTarget depthBuffer;

	public static void onWorldRender(RenderLevelStageEvent event) {
		if (event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) && Minecraft.useFancyGraphics()) {

			Minecraft mc = Minecraft.getInstance();

			if (depthBuffer == null) {
				depthBuffer = new TextureTarget(mc.getMainRenderTarget().width, mc.getMainRenderTarget().height, true, Minecraft.ON_OSX);
			}

			if (mc.getMainRenderTarget().isStencilEnabled()) {
				depthBuffer.enableStencil();
			}

			RenderTarget mainRenderTarget = mc.getMainRenderTarget();
			depthBuffer.copyDepthFrom(mainRenderTarget);
			GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, mainRenderTarget.frameBufferId);
		}
	}

    public static class Overlay implements LayeredDraw.Layer {
        @Override
        public void render(GuiGraphics graphics, @NotNull DeltaTracker deltaTracker) {
            Minecraft mc = Minecraft.getInstance();
            int width = graphics.guiWidth(); // TODO: adjust
            int height = graphics.guiHeight();
            if (mc.options.hideGui)
                return;

            Player player = mc.player;
            if (player == null)
                return;

            if (mc.hitResult instanceof BlockHitResult result) {
                ClientLevel world = mc.level;
                if (world != null && result.getType() == BlockHitResult.Type.BLOCK) {
                    BlockPos pos = result.getBlockPos();
                    BlockState state = world.getBlockState(pos);
                    BlockEntity tileEntity = world.getBlockEntity(result.getBlockPos());
                    Direction facing = result.getDirection();
                    List<Component> text = new ArrayList<>();

                    if (tileEntity instanceof IEmberInputHint input && input.shouldShowHintTooltip()) {
                        text.add(Component.translatable(Embers.MODID + ".tooltip.craft_lens_0"));
                        text.add(Component.translatable(Embers.MODID + ".tooltip.craft_lens_1"));
                    }
                    if ((player.getMainHandItem().is(EmbersItemTags.ANCIENT_CODEX) || player.getOffhandItem().is(EmbersItemTags.ANCIENT_CODEX)) && ResearchManager.researchByItem.get(state.getBlock().asItem()) != null) {
                        text.add(Component.translatable(Embers.MODID + ".tooltip.research.world"));
                    }
                    if (state.getBlock() instanceof IDial) {
                        text.addAll(((IDial) state.getBlock()).getDisplayInfo(world, result.getBlockPos(), state, Math.max(0, (height / 2 - 100) / 11)));
                    } else if (state.getBlock() == RegistryManager.ATMOSPHERIC_GAUGE.get() && !player.getMainHandItem().is(EmbersItemTags.GAUGE_OVERLAY) && !player.getOffhandItem().is(EmbersItemTags.GAUGE_OVERLAY)) {
                        renderAtmosphericGauge(graphics, player, width, height);
                    } else if (Misc.isWearingLens(player)) {
                        if (tileEntity != null) {
                            addCapabilityInformation(text, state, tileEntity, facing);
                        }
                    }
                    if (!text.isEmpty()) {
                        for (int i = 0; i < text.size(); i++) {
                            graphics.drawString(mc.font, text.get(i), width / 2 - mc.font.width(text.get(i)) / 2, height / 2 + 40 + 11 * i, 0xFFFFFF);
                        }
                    }
                }
            }

            if (player.getMainHandItem().getItem() == RegistryManager.ATMOSPHERIC_GAUGE_ITEM.get() || !player.getMainHandItem().is(EmbersItemTags.GAUGE_OVERLAY) && player.getOffhandItem().getItem() == RegistryManager.ATMOSPHERIC_GAUGE_ITEM.get()) {
                renderAtmosphericGauge(graphics, player, width, height);
            }
        }

        public static void renderAtmosphericGauge(GuiGraphics graphics, Player player, int width, int height) {
            int x = width / 2;
            int y = height / 2;

            graphics.pose().pushPose();

            //int offsetX = 0;

            graphics.blit(GAUGE, x - 16, y - 16, 0, 0, 0, 32, 32, 32, 32);

            //double angle = 195.0;
            //EmberWorldData data = EmberWorldData.get(world);
            if (player != null) {
                //if (data.emberData != null){
                //if (data.emberData.containsKey(""+((int)player.posX) / 16 + " " + ((int)player.posZ) / 16)){
                double ratio = EmberGenUtil.getEmberDensity(seed, player.getBlockX(), player.getBlockZ());
                if (gaugeAngle == 0) {
                    gaugeAngle = 165.0 + 210.0 * ratio;
                } else {
                    gaugeAngle = gaugeAngle * 0.99 + 0.01 * (165.0 + 210.0 * ratio);
                }
                //}
                //}
            }

            graphics.pose().translate(x, y, 0);
            graphics.pose().mulPose(Axis.ZP.rotationDegrees((float) gaugeAngle));
            graphics.pose().translate(-2.5, -2.5, 0);

            graphics.blit(GAUGE_POINTER, 0, 0, 0, 0, 0, 12, 5, 16, 16);

            graphics.pose().popPose();
        }

        private static void addCapabilityInformation(List<Component> text, BlockState state, BlockEntity tile, Direction facing) {
            addCapabilityItemDescription(text, tile, facing);
            addCapabilityFluidDescription(text, tile, facing);
            addCapabilityEmberDescription(text, tile, facing);
            //if (ConfigManager.isMysticalMechanicsIntegrationEnabled())
            //MysticalMechanicsIntegration.addCapabilityInformation(text, tile, facing);

            if (tile.getLevel() != null && tile.getLevel().getCapability(EmbersCapabilities.UPGRADE_PROVIDER_CAPABILITY, tile.getBlockPos(), tile.getBlockState(), tile, facing) != null)
                text.add(Component.translatable(Embers.MODID + ".tooltip.goggles.upgrade"));
            boolean proxyable = Misc.isSideProxyable(state, tile, facing);
            if (!proxyable && tile instanceof IUpgradeProxy proxy) {
                BlockEntityDirection multiBlock = proxy.getAttachedMultiblock(ConfigManager.MAX_PROXY_DISTANCE.get() - 1);
                proxyable = multiBlock != null && multiBlock.blockEntity.getLevel() != null && Misc.isSideProxyable(multiBlock.blockEntity.getLevel().getBlockState(multiBlock.blockEntity.getBlockPos()), multiBlock.blockEntity, multiBlock.direction);
            }
            if (proxyable)
                text.add(Component.translatable(Embers.MODID + ".tooltip.goggles.accessor_slot"));
            if (tile instanceof IUpgradeable upgradeable && upgradeable.isSideUpgradeSlot(facing))
                text.add(Component.translatable(Embers.MODID + ".tooltip.goggles.upgrade_slot"));
            //if (tile instanceof IMechanicallyPowered)
            //	text.add(Component.translatable(Embers.MODID + ".tooltip.goggles.actuator_slot"));
            if (tile instanceof IExtraCapabilityInformation)
                ((IExtraCapabilityInformation) tile).addOtherDescription(text, facing);
        }

        public static void addCapabilityItemDescription(List<Component> text, BlockEntity tile, Direction facing) {
            BlockCapability<IItemHandler, Direction> capability = Capabilities.ItemHandler.BLOCK;
            if (tile.getLevel() != null && tile.getLevel().getCapability(capability, tile.getBlockPos(), tile.getBlockState(), tile, facing) != null) {
                IExtraCapabilityInformation.EnumIOType ioType = IExtraCapabilityInformation.EnumIOType.BOTH;
                if (tile instanceof IExtraCapabilityInformation capInfo && capInfo.hasCapabilityDescription(capability)) {
                    capInfo.addCapabilityDescription(text, capability, facing);
                } else {
                    text.add(IExtraCapabilityInformation.formatCapability(ioType, Embers.MODID + ".tooltip.goggles.item", null));
                }
            }
        }

        public static void addCapabilityFluidDescription(List<Component> text, BlockEntity tile, Direction facing) {
            BlockCapability<IFluidHandler, Direction> capability = Capabilities.FluidHandler.BLOCK;
            if (tile.getLevel() != null && tile.getLevel().getCapability(capability, tile.getBlockPos(), tile.getBlockState(), tile, facing) != null) {
                IExtraCapabilityInformation.EnumIOType ioType = IExtraCapabilityInformation.EnumIOType.BOTH;
                Component filter = null;
                if (tile instanceof IExtraCapabilityInformation capInfo && capInfo.hasCapabilityDescription(capability)) {
                    capInfo.addCapabilityDescription(text, capability, facing);
                } else {
                    //fluid handlers no longer tell you if you can insert or remove fluids anymore

				/*IFluidHandler handler = tile.getCapability(capability, facing).orElse(null);
				for (IFluidTankProperties properties : handler.getTankProperties()) {
					boolean input = properties.canFill();
					boolean output = properties.canDrain();
					if (!input && !output)
						ioType = IExtraCapabilityInformation.EnumIOType.NONE;
					else if (input && !output)
						ioType = IExtraCapabilityInformation.EnumIOType.INPUT;
					else if (output && !input)
						ioType = IExtraCapabilityInformation.EnumIOType.OUTPUT;
				}*/
                    text.add(IExtraCapabilityInformation.formatCapability(ioType, Embers.MODID + ".tooltip.goggles.fluid", filter));
                }

            }
        }

        public static void addCapabilityEmberDescription(List<Component> text, BlockEntity tile, Direction facing) {
            BlockCapability<IEmberCapability, Direction> capability = EmbersCapabilities.EMBER_CAPABILITY_BLOCK;
            if (tile.getLevel() != null && tile.getLevel().getCapability(capability, tile.getBlockPos(), tile.getBlockState(), tile, facing) != null) {
                IExtraCapabilityInformation.EnumIOType ioType = IExtraCapabilityInformation.EnumIOType.BOTH;
                if (tile instanceof IExtraCapabilityInformation capInfo && capInfo.hasCapabilityDescription(capability)) {
                    capInfo.addCapabilityDescription(text, capability, facing);
                } else {
                    text.add(IExtraCapabilityInformation.formatCapability(ioType, Embers.MODID + ".tooltip.goggles.ember", null));
                }
            }
        }
    }
}