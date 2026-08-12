package hu.zoldleo.embers.worldgen;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.RegistryManager;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.NotNull;

public class EntityMobilizerStructureProcessor extends StructureProcessor {

	public static final MapCodec<EntityMobilizerStructureProcessor> CODEC = MapCodec.unit(() -> EntityMobilizerStructureProcessor.INSTANCE);
	public static final EntityMobilizerStructureProcessor INSTANCE = new EntityMobilizerStructureProcessor();

	@Override
	public StructureTemplate.@NotNull StructureEntityInfo processEntity(@NotNull LevelReader world, @NotNull BlockPos seedPos, StructureTemplate.@NotNull StructureEntityInfo rawEntityInfo, StructureTemplate.StructureEntityInfo entityInfo, @NotNull StructurePlaceSettings placementSettings, @NotNull StructureTemplate template) {
		CompoundTag nbt = entityInfo.nbt.copy();
        nbt.putBoolean("NoAI", false);
        nbt.putBoolean("PersistenceRequired", true);
        return new StructureTemplate.StructureEntityInfo(entityInfo.pos, entityInfo.blockPos, nbt);
	}

	@Override
	protected @NotNull StructureProcessorType<?> getType() {
		return RegistryManager.ENTITY_MOBILIZER_PROCESSOR.get();
	}
}