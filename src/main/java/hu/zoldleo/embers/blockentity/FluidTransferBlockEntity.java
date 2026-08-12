package hu.zoldleo.embers.blockentity;

import java.util.Random;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.blockentity.capability_helper.IFluidBlock;
import hu.zoldleo.embers.particle.VaporParticleOptions;
import hu.zoldleo.embers.util.EmbersColors;
import hu.zoldleo.embers.util.Misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

public class FluidTransferBlockEntity extends FluidPipeBlockEntityBase implements IFluidBlock {
	public static final int PRIORITY_TRANSFER = -10;
	public FluidStack filterFluid = FluidStack.EMPTY;
	public boolean syncFilter = true;
	IFluidHandler outputSide;

	public FluidTransferBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.FLUID_TRANSFER_ENTITY.get(), pPos, pBlockState);
		syncConnections = false;
		saveConnections = false;
	}

	@Override
	protected void initFluidTank() {
		tank = new FluidTank(getCapacity()) {
			@Override
			protected void onContentsChanged() {
				FluidTransferBlockEntity.this.setChanged();
			}

			@Override
			public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
				if (!filterFluid.isEmpty()) {
                    if (filterFluid.isComponentsPatchEmpty() ? FluidStack.isSameFluid(resource, filterFluid) : FluidStack.isSameFluidSameComponents(resource, filterFluid))
                        return super.fill(resource, action);
                    return 0;
				}
				return super.fill(resource, action);
			}
		};
		outputSide = Misc.makeRestrictedFluidHandler(tank, false, true);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		if (nbt.contains("filter"))
			filterFluid = FluidStack.parseOptional(provider, nbt.getCompound("filter"));
	}

	@Override
	protected boolean requiresSync() {
		return syncFilter || super.requiresSync();
	}

	@Override
	protected void resetSync() {
		super.resetSync();
		syncFilter = false;
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
        nbt.put("filter", filterFluid.saveOptional(provider));
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		if (syncFilter)
            nbt.put("filter", filterFluid.saveOptional(provider));
		return nbt;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, FluidTransferBlockEntity blockEntity) {
		if (level instanceof ServerLevel && blockEntity.clogged && blockEntity.isAnySideUnclogged()) {
			Random posRand = new Random(pos.asLong());
			double angleA = posRand.nextDouble() * Math.PI * 2;
			double angleB = posRand.nextDouble() * Math.PI * 2;
			float xOffset = (float) (Math.cos(angleA) * Math.cos(angleB));
			float yOffset = (float) (Math.sin(angleA) * Math.cos(angleB));
			float zOffset = (float) Math.sin(angleB);
			float speed = 0.1f;
			float vx = xOffset * speed + posRand.nextFloat() * speed * 0.3f;
			float vy = yOffset * speed + posRand.nextFloat() * speed * 0.3f;
			float vz = zOffset * speed + posRand.nextFloat() * speed * 0.3f;
			((ServerLevel) level).sendParticles(new VaporParticleOptions(EmbersColors.VAPOR_ID, new Vec3(vx, vy, vz), 1.0f), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0, 0, 0, 1.0);
		}
		FluidPipeBlockEntityBase.serverTick(level, pos, state, blockEntity);
	}

	@Override
	public int getCapacity() {
		return 240;
	}

	@Override
	public int getPriority(Direction facing) {
		return PRIORITY_TRANSFER;
	}

	@Override
	public PipeConnection getConnection(Direction facing) {
		return getBlockState().hasProperty(BlockStateProperties.FACING) &&
                getBlockState().getValue(BlockStateProperties.FACING).getAxis() == facing.getAxis() ?
                PipeConnection.PIPE : PipeConnection.NONE;
	}

	@Override
	protected boolean isFrom(Direction facing) {
		return getBlockState().getValue(BlockStateProperties.FACING) == facing;
	}

    @Override
    public IFluidHandler getFluidCapability(Direction side) {
        if (!this.remove) {
            if (side == null)
                return tank;
            if (getBlockState().hasProperty(BlockStateProperties.FACING)) {
                Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
                if (side.getOpposite() == facing)
                    return outputSide;
                else if (side.getAxis() == facing.getAxis())
                    return tank;
            }
        }
        return null;
    }
}