package hu.zoldleo.embers.blockentity;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import hu.zoldleo.embers.api.upgrades.IUpgradeProvider;
import hu.zoldleo.embers.blockentity.capability_helper.IFluidBlock;
import hu.zoldleo.embers.blockentity.capability_helper.IUpgradeBlock;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import hu.zoldleo.embers.ConfigManager;
import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.projectile.EffectDamage;
import hu.zoldleo.embers.api.tile.IExtraCapabilityInformation;
import hu.zoldleo.embers.api.tile.IExtraDialInformation;
import hu.zoldleo.embers.block.FluidDialBlock;
import hu.zoldleo.embers.datagen.EmbersBlockTags;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.entity.EmberProjectileEntity;
import hu.zoldleo.embers.particle.VaporParticleOptions;
import hu.zoldleo.embers.recipe.context.FluidHandlerContext;
import hu.zoldleo.embers.recipe.base.IBoilingRecipe;
import hu.zoldleo.embers.upgrade.MiniBoilerUpgrade;
import hu.zoldleo.embers.util.Misc;
import hu.zoldleo.embers.util.sound.ISoundController;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

public class MiniBoilerBlockEntity extends PipeBlockEntityBase implements ISoundController, IExtraDialInformation, IExtraCapabilityInformation, IUpgradeBlock, IFluidBlock {
	public static final int SOUND_SLOW = 1;
	public static final int SOUND_MEDIUM = 2;
	public static final int SOUND_FAST = 3;
	public static final int SOUND_PRESSURE_LOW = 4;
	public static final int SOUND_PRESSURE_MEDIUM = 5;
	public static final int SOUND_PRESSURE_HIGH = 6;
	public static final int[] SOUND_IDS = new int[]{SOUND_SLOW, SOUND_MEDIUM, SOUND_FAST, SOUND_PRESSURE_LOW, SOUND_PRESSURE_MEDIUM, SOUND_PRESSURE_HIGH};

	Random random = new Random();
	HashSet<Integer> soundsPlaying = new HashSet<>();
	protected FluidTank fluidTank = new FluidTank(ConfigManager.MINI_BOILER_CAPACITY.get()) {
		@Override
		public void onContentsChanged() {
			MiniBoilerBlockEntity.this.setChanged();
		}
	};
	protected FluidTank gasTank = new FluidTank(ConfigManager.MINI_BOILER_CAPACITY.get()) {
		@Override
		public void onContentsChanged() {
			MiniBoilerBlockEntity.this.setChanged();
		}
	};
	protected MiniBoilerUpgrade upgrade;
	int lastBoil;
	int boilTime;
	public RecipeHolder<IBoilingRecipe> cachedRecipe = null;

	public MiniBoilerBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.MINI_BOILER_ENTITY.get(), pPos, pBlockState);
		upgrade = new MiniBoilerUpgrade(this);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		fluidTank.readFromNBT(provider, nbt.getCompound("fluidTank"));
		gasTank.readFromNBT(provider, nbt.getCompound("gasTank"));
		lastBoil = nbt.getInt("lastBoil");
		boilTime = nbt.getInt("boilTime");
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
		nbt.put("fluidTank", fluidTank.writeToNBT(provider, new CompoundTag()));
		nbt.put("gasTank", gasTank.writeToNBT(provider, new CompoundTag()));
		nbt.putInt("lastBoil", lastBoil);
		nbt.putInt("boilTime", boilTime);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		nbt.put("fluidTank", fluidTank.writeToNBT(provider, new CompoundTag()));
		nbt.put("gasTank", gasTank.writeToNBT(provider, new CompoundTag()));
		nbt.putInt("lastBoil", lastBoil);
		nbt.putInt("boilTime", boilTime);
		return nbt;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public void initConnections() {
		Block block = getBlockState().getBlock();
		for (Direction direction : Misc.horizontals) {
			BlockState facingState = level.getBlockState(worldPosition.relative(direction));
			BlockEntity facingBE = level.getBlockEntity(worldPosition.relative(direction));
			if (facingState.is(EmbersBlockTags.FLUID_PIPE_CONNECTION)) {
				if (facingBE instanceof PipeBlockEntityBase && !((PipeBlockEntityBase) facingBE).getConnection(direction.getOpposite()).transfer) {
					connections[direction.get3DDataValue()] = PipeConnection.NONE;
				} else {
					connections[direction.get3DDataValue()] = PipeConnection.PIPE;
				}
			} else {
				connections[direction.get3DDataValue()] = PipeConnection.NONE;
			}
		}
		loaded = true;
		setChanged();
		level.getChunkAt(worldPosition).setUnsaved(true);
		level.updateNeighbourForOutputSignal(worldPosition, block);
	}

	public int getCapacity() {
		return ConfigManager.MINI_BOILER_CAPACITY.get();
	}

	public int getFluidAmount() {
		return fluidTank.getFluidAmount();
	}

	public int getGasAmount() {
		return gasTank.getFluidAmount();
	}

	public FluidTank getFluidTank() {
		return fluidTank;
	}

	public FluidTank getGasTank() {
		return gasTank;
	}

	public Fluid getFluid() {
        return fluidTank.getFluid().getFluid();
    }

	public Fluid getGas() {
        return gasTank.getFluid().getFluid();
    }

	public FluidStack getFluidStack() {
		return fluidTank.getFluid();
	}

	public FluidStack getGasStack() {
		return gasTank.getFluid();
	}

	public void boil(double heat) {
		FluidStack fluid = getFluidStack();
		FluidHandlerContext context = new FluidHandlerContext(fluidTank);
		cachedRecipe = Misc.getRecipe(cachedRecipe, RegistryManager.BOILING.get(), context, level);

		if (cachedRecipe != null && fluid.getAmount() > 0 && heat > 0) {
			int fluidBoiled = Mth.clamp((int) (ConfigManager.MINI_BOILER_HEAT_MULTIPLIER.get() * heat), 1, fluid.getAmount());
			if (fluidBoiled > 0) {
				//the recipe is responsible for draining boiled fluid
				FluidStack gas = cachedRecipe.value().process(context, fluidBoiled);
				if (gas != null) {
					int leftover = gas.getAmount() - gasTank.fill(gas, IFluidHandler.FluidAction.EXECUTE);
					if (ConfigManager.MINI_BOILER_CAN_EXPLODE.get() && leftover > 0 && !level.isClientSide()) {
						explode();
					}
				}
			}
			lastBoil = fluidBoiled;
			boilTime = fluidBoiled / 200;
		}
	}

	public void explode() {
		double posX = worldPosition.getX() + 0.5;
		double posY = worldPosition.getY() + 0.5;
		double posZ = worldPosition.getZ() + 0.5;
		level.playSound(null, worldPosition, EmbersSounds.MINI_BOILER_RUPTURE.get(), SoundSource.BLOCKS, 0.6f, 1.0f); //TODO: Random pitch
		Explosion explosion = level.explode(null, posX, posY, posZ, 3f, ExplosionInteraction.NONE);
		level.removeBlock(worldPosition, false);
		EffectDamage effect = new EffectDamage(4.0f, preset -> level.damageSources().explosion(explosion), 10, 0.0f);
		for(int i = 0; i < 12; i++) {
			EmberProjectileEntity proj = RegistryManager.EMBER_PROJECTILE.get().create(level);
			proj.shoot(random.nextDouble()-0.5, random.nextDouble()-0.5, random.nextDouble()-0.5, 0.5f, 0.0f, 10.0f);
			proj.setPos(posX, posY, posZ);
			proj.setLifetime(20 + random.nextInt(40));
			proj.setEffect(effect);
			level.addFreshEntity(proj);
		}
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, MiniBoilerBlockEntity blockEntity) {
		blockEntity.handleSound();
		blockEntity.spawnParticles();
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, MiniBoilerBlockEntity blockEntity) {
		if (!blockEntity.loaded)
			blockEntity.initConnections();
		if (blockEntity.boilTime > 0) {
			blockEntity.boilTime--;
			blockEntity.setChanged();
		}
	}

	@Override
	public void addDialInformation(Direction facing, List<Component> information, String dialType) {
		if (FluidDialBlock.DIAL_TYPE.equals(dialType) && facing.getAxis() != Direction.Axis.Y) {
			ChatFormatting gasFormat = ChatFormatting.WHITE;
			if(getGasAmount() > getCapacity() * 0.8)
				gasFormat = ChatFormatting.RED;
			else if(getGasAmount() > getCapacity() * 0.5)
				gasFormat = ChatFormatting.YELLOW;
			information.addFirst(FluidDialBlock.formatFluidStack(getGasStack(), getCapacity()).withStyle(gasFormat));
		}
	}

	@Override
	public int getComparatorData(Direction facing, int data, String dialType) {
		if (FluidDialBlock.DIAL_TYPE.equals(dialType) && facing.getAxis() != Direction.Axis.Y) {
			double fill = getGasAmount() / (double)getCapacity();
			return fill > 0 ? (int) (1 + fill * 14) : 0;
		}
		return data;
	}

	public void spawnParticles() {
		double gasRatio = getGasAmount() / (double)getCapacity();
		int spouts = 0;
		if(gasRatio > 0.8)
			spouts = 3;
		else if(gasRatio > 0.5)
			spouts = 2;
		else if(gasRatio > 0.25)
			spouts = 1;

		Vector3f color = IClientFluidTypeExtensions.of(getGas().getFluidType()).modifyFogColor(Minecraft.getInstance().gameRenderer.getMainCamera(), 0, (ClientLevel) level, 6, 0, new Vector3f(1, 1, 1));
		Random posRand = new Random(worldPosition.asLong());
		for (int i = 0; i < spouts; i++) {
			double angleA = posRand.nextDouble() * Math.PI * 2;
			double angleB = posRand.nextDouble() * Math.PI * 2;
			float xOffset = (float) (Math.cos(angleA) * Math.cos(angleB));
			float yOffset = (float) (Math.sin(angleA) * Math.cos(angleB));
			float zOffset = (float) Math.sin(angleB);
			float speed = 0.13875f;
			float vx = xOffset * speed + posRand.nextFloat() * speed * 0.3f;
			float vy = yOffset * speed + posRand.nextFloat() * speed * 0.3f;
			float vz = zOffset * speed + posRand.nextFloat() * speed * 0.3f;
			level.addParticle(new VaporParticleOptions(color, new Vec3(vx, vy, vz), 1.0F), worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 0, 0, 0);
		}
	}

	@Override
	public void playSound(int id) {
		float soundX = (float) worldPosition.getX() + 0.5f;
		float soundY = (float) worldPosition.getY() + 0.5f;
		float soundZ = (float) worldPosition.getZ() + 0.5f;
		switch (id) {
		case SOUND_SLOW:
			EmbersSounds.playMachineSound(this, SOUND_SLOW, EmbersSounds.MINI_BOILER_LOOP_SLOW.get(), SoundSource.BLOCKS, true, 1.0f, 1.0f, soundX, soundY, soundZ);
			break;
		case SOUND_MEDIUM:
			EmbersSounds.playMachineSound(this, SOUND_MEDIUM, EmbersSounds.MINI_BOILER_LOOP_MID.get(), SoundSource.BLOCKS, true, 1.0f, 1.0f, soundX, soundY, soundZ);
			break;
		case SOUND_FAST:
			EmbersSounds.playMachineSound(this, SOUND_FAST, EmbersSounds.MINI_BOILER_LOOP_FAST.get(), SoundSource.BLOCKS, true, 1.0f, 1.0f, soundX, soundY, soundZ);
			break;
		case SOUND_PRESSURE_LOW:
			EmbersSounds.playMachineSound(this, SOUND_PRESSURE_LOW, EmbersSounds.MINI_BOILER_PRESSURE_LOW.get(), SoundSource.BLOCKS, true, 1.0f, 1.0f, soundX, soundY, soundZ);
			break;
		case SOUND_PRESSURE_MEDIUM:
			EmbersSounds.playMachineSound(this, SOUND_PRESSURE_MEDIUM, EmbersSounds.MINI_BOILER_PRESSURE_MID.get(), SoundSource.BLOCKS, true, 1.0f, 1.0f, soundX, soundY, soundZ);
			break;
		case SOUND_PRESSURE_HIGH:
			EmbersSounds.playMachineSound(this, SOUND_PRESSURE_HIGH, EmbersSounds.MINI_BOILER_PRESSURE_HIGH.get(), SoundSource.BLOCKS, true, 1.0f, 1.0f, soundX, soundY, soundZ);
			break;
		}
		soundsPlaying.add(id);
	}

	@Override
	public void stopSound(int id) {
		soundsPlaying.remove(id);
	}

	@Override
	public boolean isSoundPlaying(int id) {
		return soundsPlaying.contains(id);
	}

	@Override
	public int[] getSoundIDs() {
		return SOUND_IDS;
	}

	@Override
	public boolean shouldPlaySound(int id) {
		int speedId = 0;
		int pressureId = 0;

		int gasAmount = getGasAmount();
		double gasRatio = gasAmount / (double) getCapacity();
		if (gasRatio > 0.8)
			pressureId = SOUND_PRESSURE_HIGH;
		else if (gasRatio > 0.5)
			pressureId = SOUND_PRESSURE_MEDIUM;
		else if (gasRatio > 0.25)
			pressureId = SOUND_PRESSURE_LOW;

		if (boilTime > 0 && lastBoil > 0) {
			if (lastBoil >= 2400)
				speedId = SOUND_FAST;
			else if (lastBoil >= 400)
				speedId = SOUND_MEDIUM;
			else
				speedId = SOUND_SLOW;
		}

		return speedId == id || pressureId == id;
	}

	@Override
	public float getCurrentPitch(int id, float pitch) {
		if (id == SOUND_PRESSURE_MEDIUM)
			return 1.0f + (getGasAmount() / (float) getCapacity() - 0.5f) * 1.7f;
		if (id == SOUND_PRESSURE_HIGH)
			return 1.0f + (getGasAmount() / (float) getCapacity() - 0.8f);
		return pitch;
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (level instanceof ServerLevel serverLevel)
			serverLevel.getChunkSource().blockChanged(worldPosition);
	}

	@Override
	public boolean hasCapabilityDescription(BlockCapability<?, ?> capability) {
		return capability == Capabilities.FluidHandler.BLOCK;
	}

	@Override
	public void addCapabilityDescription(List<Component> strings, BlockCapability<?, ?> capability, Direction facing) {
		if (facing == Direction.UP)
			strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.OUTPUT, Embers.MODID + ".tooltip.goggles.fluid", Component.translatable(Embers.MODID + ".tooltip.goggles.fluid.steam")));
		else if (facing == Direction.DOWN || facing != getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING))
			strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.INPUT, Embers.MODID + ".tooltip.goggles.fluid", Component.translatable(Embers.MODID + ".tooltip.goggles.fluid.water")));
	}

    @Override
    public IUpgradeProvider getUpgradeCapability(Direction side) {
        if (side == getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING))
            return upgrade;
        return null;
    }

    @Override
    public IFluidHandler getFluidCapability(Direction side) {
        if (side == Direction.UP)
            return gasTank;
        if (side == Direction.DOWN || side != getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING))
            return fluidTank;
        return null;
    }
}