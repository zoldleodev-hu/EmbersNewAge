package hu.zoldleo.embers.blockentity;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.capabilities.EmbersCapabilities;
import hu.zoldleo.embers.api.power.IEmberCapability;
import hu.zoldleo.embers.api.power.IEmberPacketReceiver;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.entity.EmberPacketEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class EmberEjectorBlockEntity extends EmberEmitterBlockEntity {
	public static final double TRANSFER_RATE = 400.0;
	public static final double PULL_RATE = 100.0;

	public EmberEjectorBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(RegistryManager.EMBER_EJECTOR_ENTITY.get(), pPos, pBlockState);
		capability.setEmberCapacity(2000);
		capability.setEmber(0);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, EmberEjectorBlockEntity blockEntity) {
		blockEntity.ticksExisted ++;
		Direction facing = state.getValue(BlockStateProperties.FACING);
		if (blockEntity.ticksExisted % 5 == 0) {
            IEmberCapability cap = level.getCapability(EmbersCapabilities.EMBER_CAPABILITY_BLOCK, pos.relative(facing, -1), facing);
			if (cap != null) {
				if (cap.getEmber() > 0 && blockEntity.capability.getEmber() < blockEntity.capability.getEmberCapacity()){
					double removed = cap.removeAmount(PULL_RATE, true);
					blockEntity.capability.addAmount(removed, true);
				}
			}
		}
		if ((blockEntity.ticksExisted + blockEntity.offset) % 20 == 0 && blockEntity.canSendBurst() && blockEntity.capability.getEmber() > PULL_RATE) {
			BlockEntity targetTile = level.getBlockEntity(blockEntity.target);
			if (targetTile instanceof IEmberPacketReceiver receiver) {
				if (receiver.hasRoomFor(TRANSFER_RATE)) {
					EmberPacketEntity packet = RegistryManager.EMBER_PACKET.get().create(blockEntity.level);
					Vec3 velocity = getBurstVelocity(facing);
					packet.initCustom(pos, blockEntity.target, velocity.x, velocity.y, velocity.z, Math.min(TRANSFER_RATE, blockEntity.capability.getEmber()));
					blockEntity.capability.removeAmount(Math.min(TRANSFER_RATE, blockEntity.capability.getEmber()), true);
					blockEntity.level.addFreshEntity(packet);
					level.playSound(null, pos, EmbersSounds.EMBER_EMIT_BIG.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
				}
			}
		}
	}
}