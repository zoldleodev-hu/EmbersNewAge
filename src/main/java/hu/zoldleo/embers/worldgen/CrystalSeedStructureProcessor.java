package hu.zoldleo.embers.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.blockentity.CrystalSeedBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.NotNull;

public class CrystalSeedStructureProcessor extends StructureProcessor {

	public static final MapCodec<CrystalSeedStructureProcessor> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            Codec.INT.fieldOf("min_xp").forGetter((processor) -> processor.minXp),
            Codec.INT.fieldOf("max_xp").forGetter((processor) -> processor.maxXp),
            Codec.INT.fieldOf("size").forGetter((processor) -> processor.size)
    ).apply(instance, CrystalSeedStructureProcessor::new));

	public final int minXp;
	public final int maxXp;
	public final int size;

	public CrystalSeedStructureProcessor(int minXp, int maxXp, int size) {
		this.minXp = minXp;
		this.maxXp = maxXp;
		this.size = size;
	}

	@Override
	public StructureTemplate.StructureBlockInfo process(@NotNull LevelReader level, @NotNull BlockPos offset, @NotNull BlockPos pos, StructureTemplate.@NotNull StructureBlockInfo blockInfo, StructureTemplate.StructureBlockInfo relativeBlockInfo, @NotNull StructurePlaceSettings settings, StructureTemplate template) {
		CompoundTag nbt = relativeBlockInfo.nbt();
		if (nbt != null && nbt.contains("id")) {
			ResourceLocation id = ResourceLocation.parse(nbt.getString("id"));
			if (id.getNamespace().equals(Embers.MODID) && id.getPath().contains("crystal_seed")) {
				nbt = nbt.copy();
				int xp = getXp(settings.getRandom(relativeBlockInfo.pos()));

				nbt.putInt("xp", xp);
				nbt.putInt("size", size);
				nbt.putString("spawns", CrystalSeedBlockEntity.getSpawnString(CrystalSeedBlockEntity.getSpawns(xp)));

				return new StructureTemplate.StructureBlockInfo(relativeBlockInfo.pos(), relativeBlockInfo.state(), nbt);
			}
		}
		return relativeBlockInfo;
	}

	public int getXp(RandomSource rand) {
		int xp = rand.nextInt(minXp, maxXp);
		xp = xp - xp % 1000;
		xp += size;
		return xp;
	}

	@Override
	protected @NotNull StructureProcessorType<?> getType() {
		return RegistryManager.CRYSTAL_SEED_PROCESSOR.get();
	}
}