package hu.zoldleo.embers.upgrade;

import java.util.List;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.event.AlchemyResultEvent;
import hu.zoldleo.embers.api.event.AlchemyStartEvent;
import hu.zoldleo.embers.api.event.UpgradeEvent;
import hu.zoldleo.embers.api.upgrades.UpgradeContext;
import hu.zoldleo.embers.block.upgrade.MnemonicInscriberBlock;
import hu.zoldleo.embers.blockentity.MnemonicInscriberBlockEntity;
import hu.zoldleo.embers.datagen.EmbersItemTags;
import hu.zoldleo.embers.datagen.EmbersSounds;
import hu.zoldleo.embers.particle.GlowParticleOptions;
import hu.zoldleo.embers.util.EmbersColors;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MnemonicInscriberUpgrade extends DefaultUpgradeProvider {
	public MnemonicInscriberUpgrade(BlockEntity tile) {
		super(Embers.res("mnemonic_inscriber"), tile);
	}

	@Override
	public int getPriority() {
		return 100; //after everything else
	}

	@Override
	public void throwEvent(BlockEntity tile, List<UpgradeContext> upgrades, UpgradeEvent event, int distance, int count) {
        if (tile.getLevel() == null)
            return;
		if (event instanceof AlchemyStartEvent alchemyEvent && alchemyEvent.getRecipe() != null) {
			BlockState state = tile.getLevel().getBlockState(this.tile.getBlockPos());
			if (state.hasProperty(MnemonicInscriberBlock.ACTIVE) && this.tile.getLevel() != null)
				this.tile.getLevel().setBlock(this.tile.getBlockPos(), state.setValue(MnemonicInscriberBlock.ACTIVE, true), Block.UPDATE_ALL);
		}
		if (event instanceof AlchemyResultEvent alchemyEvent && this.tile instanceof MnemonicInscriberBlockEntity inscriber) {
			if (!alchemyEvent.isFailure() && inscriber.inventory.getStackInSlot(0).is(EmbersItemTags.INSCRIBABLE_PAPER)) {
				inscriber.inventory.setStackInSlot(0, alchemyEvent.getResult().createResultStack(new ItemStack(RegistryManager.ALCHEMICAL_NOTE.get())));
				tile.getLevel().playSound(null, this.tile.getBlockPos(), EmbersSounds.EMBER_EMIT_BIG.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
				if (tile.getLevel() instanceof ServerLevel serverLevel)
					serverLevel.sendParticles(new GlowParticleOptions(EmbersColors.EMBER_ID, new Vec3(0.0, 0.000001, 0.0), 2.0F, 40), this.tile.getBlockPos().getX() + 0.5, this.tile.getBlockPos().getY() + 0.5, this.tile.getBlockPos().getZ() + 0.5, 40, 0.12f, 0.12f, 0.12f, 0.0);
			}
			BlockState state = tile.getLevel().getBlockState(this.tile.getBlockPos());
			if (state.hasProperty(MnemonicInscriberBlock.ACTIVE) && this.tile.getLevel() != null)
                this.tile.getLevel().setBlock(this.tile.getBlockPos(), state.setValue(MnemonicInscriberBlock.ACTIVE, false), Block.UPDATE_ALL);
		}
	}
}