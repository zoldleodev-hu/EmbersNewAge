package hu.zoldleo.embers.blockentity;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.tile.IDialEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public class FluidDialBlockEntity extends BlockEntity implements IDialEntity {
	public FluidStack[] fluids = new FluidStack[0];
	public int[] capacities = new int[0];
	public int extraLines = 0;
	public boolean display = false;

	public FluidDialBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.FLUID_DIAL_ENTITY.get(), pPos, pBlockState);
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
        super.loadAdditional(nbt, provider); // TODO: why wasn't this called?
		ListTag tanks = nbt.getList("tanks", Tag.TAG_COMPOUND);
		fluids = new FluidStack[tanks.size()];
		capacities = new int[tanks.size()];
		if (!tanks.isEmpty()) {
			for (int i = 0; i < tanks.size(); i++) {
				CompoundTag tank = tanks.getCompound(i);
				fluids[i] = FluidStack.parseOptional(provider, tank.getCompound("Fluid"));
				capacities[i] = tank.getInt("capacity");
			}
		}
		if (nbt.contains("more_lines"))
			extraLines = nbt.getInt("more_lines");
		if (nbt.contains("display"))
			display = nbt.getBoolean("display");
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		return getUpdateTag(100, provider); // TODO: Does this even fit on the screen?
	}

	public CompoundTag getUpdateTag(int maxLines, HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		boolean display = false;
		if (level != null && getBlockState().hasProperty(BlockStateProperties.FACING)) {
			Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
			BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(facing, -1));
			if (blockEntity != null) {
				IFluidHandler cap = level.getCapability(Capabilities.FluidHandler.BLOCK, worldPosition.relative(facing, -1), facing);
                if (cap == null)
                    cap = level.getCapability(Capabilities.FluidHandler.BLOCK, worldPosition.relative(facing, -1), null);
                if (cap != null) {
					ListTag tanks = new ListTag();
					for (int i = 0; i < cap.getTanks() && (i + extraLines) < maxLines; i++) {
						FluidStack contents = cap.getFluidInTank(i);
                        CompoundTag tank = new CompoundTag();
						tank.put("Fluid", contents.saveOptional(provider));
						tank.putInt("capacity", cap.getTankCapacity(i));

						tanks.add(tank);
					}
					nbt.put("tanks", tanks);

					if (cap.getTanks() > maxLines) {
						nbt.putInt("more_lines", cap.getTanks() - maxLines);
					} else {
						nbt.putInt("more_lines", 0);
					}
					display = true;
				}
			}
		}
		nbt.putBoolean("display", display);
		return nbt;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket(int maxLines) { // TODO: why not use the default one?
		return ClientboundBlockEntityDataPacket.create(this, (BE, provider) -> getUpdateTag(maxLines, provider));
	}
}