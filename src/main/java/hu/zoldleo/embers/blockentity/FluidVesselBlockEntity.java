package hu.zoldleo.embers.blockentity;

import java.util.Random;

import hu.zoldleo.embers.blockentity.capability_helper.IFluidBlock;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import hu.zoldleo.embers.ConfigManager;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.particle.VaporParticleOptions;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FluidVesselBlockEntity extends OpenTankBlockEntity implements IFluidBlock {
	int ticksExisted = 0;
	public float renderOffset;
	int previousFluid;

	public FluidVesselBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.FLUID_VESSEL_ENTITY.get(), pPos, pBlockState);
		tank = new FluidTank(ConfigManager.FLUID_VESSEL_CAPACITY.get()) {
			@Override
			public void onContentsChanged() {
				FluidVesselBlockEntity.this.setChanged();
			}

			@Override
			public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
				if (Misc.isGaseousFluid(resource)) {
					FluidVesselBlockEntity.this.setEscapedFluid(resource);
					return resource.getAmount();
				}
                return super.fill(resource, action);
			}
		};
	}

	public int getCapacity(){
		return tank.getCapacity();
	}

	public FluidStack getFluidStack() {
		return tank.getFluid();
	}

	public FluidTank getTank() {
		return tank;
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, FluidVesselBlockEntity blockEntity) {
		blockEntity.ticksExisted++;

		//I know I'm supposed to use onLoad for stuff on the first tick but the tank isn't synced to the client yet when that happens
		if (blockEntity.ticksExisted == 1)
			blockEntity.previousFluid = blockEntity.tank.getFluidAmount();
		if (blockEntity.tank.getFluidAmount() != blockEntity.previousFluid) {
			blockEntity.renderOffset = blockEntity.renderOffset + blockEntity.tank.getFluidAmount() - blockEntity.previousFluid;
			blockEntity.previousFluid = blockEntity.tank.getFluidAmount();
		}

		if (blockEntity.shouldEmitParticles())
			blockEntity.updateEscapeParticles();
	}

	@Override
	protected void updateEscapeParticles() {
		Vector3f color = IClientFluidTypeExtensions.of(lastEscaped.getFluid().getFluidType()).modifyFogColor(Minecraft.getInstance().gameRenderer.getMainCamera(), 0, (ClientLevel) this.level, 6, 0, new Vector3f(1, 1, 1));
		Random random = new Random();
		for (int i = 0; i < 3; i++) {
			float xOffset = 0.5f + (random.nextFloat() - 0.5f) * 2 * 0.2f;
			float yOffset = 0.9f;
			float zOffset = 0.5f + (random.nextFloat() - 0.5f) * 2 * 0.2f;
			level.addParticle(new VaporParticleOptions(color, 2.0f), worldPosition.getX() + xOffset, worldPosition.getY() + yOffset, worldPosition.getZ() + zOffset, 0, 1 / 5f, 0);
		}
	}

    @Override
    public IFluidHandler getFluidCapability(Direction side) {
        return tank;
    }
}