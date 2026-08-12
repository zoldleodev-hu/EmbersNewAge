package hu.zoldleo.embers;

import hu.zoldleo.embers.api.augment.AugmentUtil;
import hu.zoldleo.embers.api.event.EmberProjectileEvent;
import hu.zoldleo.embers.api.item.IInflictorGem;
import hu.zoldleo.embers.api.item.IInflictorGemHolder;
import hu.zoldleo.embers.api.item.ITyrfingWeapon;
import hu.zoldleo.embers.augment.ShiftingScalesAugment;
import hu.zoldleo.embers.blockentity.ExplosionPedestalBlockEntity;
import hu.zoldleo.embers.datagen.EmbersDamageTypes;
import hu.zoldleo.embers.datagen.EmbersItemTags;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.network.message.MessageEmberGenOffset;
import hu.zoldleo.embers.network.message.MessageWorldSeed;
import hu.zoldleo.embers.research.ResearchManager;
import hu.zoldleo.embers.util.EmberGenUtil;
import hu.zoldleo.embers.util.EmberWorldData;
import hu.zoldleo.embers.util.ExplosionCharmWorldInfo;
import hu.zoldleo.embers.util.Misc;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Optional;

public class EmbersEvents {
	public static void onJoin(EntityJoinLevelEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || event.getLevel().isClientSide())
            return;
        ResearchManager.sendResearchData(player);
        ShiftingScalesAugment.sendScalesData(player);
        PacketDistributor.sendToPlayer(player, new MessageWorldSeed(((ServerLevel) event.getLevel()).getSeed()));
        PacketDistributor.sendToPlayer(player, new MessageEmberGenOffset(EmberGenUtil.offX, EmberGenUtil.offZ));
	}

	public static void onEntityDamaged(LivingIncomingDamageEvent event) {
        Optional<Registry<DamageType>> optReg = event.getEntity().level().registryAccess().registry(Registries.DAMAGE_TYPE);
		if (optReg.isPresent() && event.getSource().type().equals(optReg.get().getHolderOrThrow(EmbersDamageTypes.EMBER_KEY).value()))
			if (event.getEntity().fireImmune() || event.getEntity().hasEffect(MobEffects.FIRE_RESISTANCE))
				event.setAmount(event.getAmount() * 0.5f);

		attuneInflictorGem(event.getEntity(), event.getSource(), event.getEntity().getMainHandItem());
		attuneInflictorGem(event.getEntity(), event.getSource(), event.getEntity().getOffhandItem());

		float mult = 1.0f;
		for (ItemStack armor : event.getEntity().getArmorSlots()) {
			mult -= getInflictorGemResistance(event, armor);
			addHeat(event.getEntity(), armor, 5.0f);
		}
		if (mult <= 0)
			event.setCanceled(true);
		event.setAmount(event.getAmount() * mult);

		if (event.getSource().getDirectEntity() instanceof LivingEntity livingSource) {
			final ItemStack heldStack = livingSource.getMainHandItem();
			if (heldStack.getItem() instanceof ITyrfingWeapon tyrfing)
				tyrfing.attack(event, event.getEntity().getArmorValue());
			addHeat(livingSource, heldStack, Math.max(1.0f, 0.5f * event.getAmount()));
		}
	}

	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		Player player = event.getPlayer();
        if (event.getState().getDestroySpeed(event.getLevel(), event.getPos()) > 0)
            addHeat(player, player.getMainHandItem(), 1.0f);
    }

	public static void onProjectileFired(EmberProjectileEvent event) {
		addHeat(event.getShooter(), event.getStack(), event.getProjectiles().size() * (float) Mth.clampedLerp(0.5, 3.0, event.getCharge()));
	}

	public static void onArrowLoose(ArrowLooseEvent event) {
		addHeat(event.getEntity(), event.getBow(), 1.0f);
	}

	public static void onAnvilUpdate(AnvilUpdateEvent event) {
		if (event.getLeft().isRepairable() && !event.getLeft().is(EmbersItemTags.MATERIA_BLACKLIST) && event.getRight().is(RegistryManager.ISOLATED_MATERIA)) {
			int modifications = 0;
			int renames = 0;
			int toRepair = Math.min(event.getLeft().getDamageValue(), event.getLeft().getMaxDamage() / 4);
			if (toRepair <= 0) {
				event.setOutput(ItemStack.EMPTY);
				event.setCost(0);
				return;
			}
			int usedMateria;
			ItemStack result = event.getLeft().copy();
			for (usedMateria = 0; toRepair > 0 && usedMateria < event.getRight().getCount(); ++usedMateria) {
				result.setDamageValue(result.getDamageValue() - toRepair);
				modifications++;
				toRepair = Math.min(event.getLeft().getDamageValue(), event.getLeft().getMaxDamage() / 4);
			}
			event.setMaterialCost(usedMateria);

			if (event.getName() != null && !event.getName().isBlank()) {
				if (!event.getName().equals(event.getLeft().getHoverName().getString())) {
					renames = 1;
					modifications += renames;
					result.set(DataComponents.CUSTOM_NAME, Component.literal(event.getName()));
				}
			} else if (event.getLeft().get(DataComponents.CUSTOM_NAME) != null) {
				renames = 1;
				modifications += renames;
                result.remove(DataComponents.CUSTOM_NAME);
			}
			event.setCost(modifications);
			if (modifications <= 0)
				result = ItemStack.EMPTY;
			if (renames == modifications && renames > 0 && event.getCost() >= 40)
				event.setCost(39);
			if (event.getCost() >= 40 && !event.getPlayer().getAbilities().instabuild)
				result = ItemStack.EMPTY;
			if (!result.isEmpty()) {
				int repairCost = result.getOrDefault(DataComponents.REPAIR_COST, 0);
				if (renames != modifications || renames == 0)
					repairCost = repairCost * 2 + 1;
				result.set(DataComponents.REPAIR_COST, repairCost);
			}
			event.setOutput(result);
		}
	}

	public static void onLevelLoad(LevelEvent.Load event) {
		if (event.getLevel() instanceof ServerLevel server && server.dimension() == Level.OVERWORLD)
			EmberWorldData.get(server);
	}

	public static void onServerTick(LevelTickEvent.Post event) { // TODO
		if (event.getLevel() instanceof ServerLevel level && level.dimension() == Level.OVERWORLD) {
			boolean changed = false;
			if (Misc.random.nextInt(400) == 0) {
				EmberGenUtil.offX++;
				changed = true;
			}
			if (Misc.random.nextInt(400) == 0) {
				EmberGenUtil.offZ++;
				changed = true;
			}
			if (changed) {
                PacketDistributor.sendToAllPlayers(new MessageEmberGenOffset(EmberGenUtil.offX, EmberGenUtil.offZ));
				EmberWorldData.get(level).setDirty();
			}
		}
	}

	public static HashMap<ResourceLocation, ExplosionCharmWorldInfo> explosionCharmData = new HashMap<>();

	public static void putExplosionCharm(Level world, BlockPos pos) {
		if (!explosionCharmData.containsKey(world.dimension().location()))
			explosionCharmData.put(world.dimension().location(), new ExplosionCharmWorldInfo());
		ExplosionCharmWorldInfo data = explosionCharmData.get(world.dimension().location());
		data.put(pos);
	}

	public static void onExplosion(ExplosionEvent.Start event) {
		Level world = event.getLevel();
		Explosion explosion = event.getExplosion();
		ExplosionCharmWorldInfo data = explosionCharmData.get(world.dimension().location());
		if (data == null)
			return;
		BlockPos charmPos = data.getClosestExplosionCharm(world, BlockPos.containing(explosion.center()), 8);
		if (charmPos != null && world.getBlockEntity(charmPos) instanceof ExplosionPedestalBlockEntity pedestal) {
            pedestal.absorb(explosion);
            event.setCanceled(true);
		}
	}

	public static void onTagsReload(TagsUpdatedEvent event) {
		if (event.shouldUpdateStaticData()) {
			ResearchManager.reloadLookupIngredients();
		}
	}

	public static void addHeat(Entity entity, ItemStack stack, float added) {
		if (AugmentUtil.hasHeat(stack)) {
			double maxHeat = AugmentUtil.getMaxHeat(stack);
			double heat = AugmentUtil.getHeat(stack);
			if (heat < maxHeat) {
				AugmentUtil.addHeat(stack, added);
				if (heat + added >= maxHeat)
					entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), EmbersSounds.HEATED_ITEM_LEVELUP.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
			}
		}
	}

	public static void attuneInflictorGem(LivingEntity entityLiving, DamageSource source, ItemStack stack) {
		if (stack.getItem() instanceof IInflictorGem inflictorGem)
			inflictorGem.attuneSource(stack, entityLiving, source);
	}

	public static float getInflictorGemResistance(LivingIncomingDamageEvent event, ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof IInflictorGemHolder inflictorGemHolder)
            return inflictorGemHolder.getTotalDamageResistance(event.getEntity(), event.getSource(), stack);
		return 0;
	}
}
