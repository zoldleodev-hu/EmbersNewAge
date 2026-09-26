package hu.zoldleo.embers.blockentity;

import hu.zoldleo.embers.api.block.IPipeConnection;
import hu.zoldleo.embers.api.tile.IExtractorPipe;
import hu.zoldleo.embers.api.tile.IPipePriority;
import hu.zoldleo.embers.block.transport.PipeBlockBase;
import hu.zoldleo.embers.util.Misc;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

public abstract class PipeBlockEntityBase extends BlockEntity {
	public PipeConnection[] connections = {
			PipeConnection.NONE,
			PipeConnection.NONE,
			PipeConnection.NONE,
			PipeConnection.NONE,
			PipeConnection.NONE,
			PipeConnection.NONE
	};

	public boolean loaded = false;
	public boolean saveConnections = true;
	public boolean syncConnections = true;

	public static final ModelProperty<int[]> DATA_TYPE = new ModelProperty<>();

	public PipeBlockEntityBase(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
		super(pType, pPos, pBlockState);
	}

	public void initConnections() {
		Block block = getBlockState().getBlock();
		for (Direction direction : Direction.values()) {
			if (block instanceof PipeBlockBase pipeBlock) {
				BlockState facingState = level.getBlockState(worldPosition.relative(direction));
				BlockEntity facingBE = level.getBlockEntity(worldPosition.relative(direction));
				if (!(facingBE instanceof PipeBlockEntityBase pipeTile) || pipeTile.getConnection(direction.getOpposite()) != PipeConnection.DISABLED) {
					if (facingState.is(pipeBlock.getConnectionTag())) {
						if (facingBE instanceof PipeBlockEntityBase pipeTile && pipeTile.getConnection(direction.getOpposite()) == PipeConnection.DISABLED) {
							connections[direction.get3DDataValue()] = PipeConnection.DISABLED;
						} else {
							connections[direction.get3DDataValue()] = PipeConnection.PIPE;
						}
					} else {
						if (pipeBlock.connected(direction, facingState)) {
							connections[direction.get3DDataValue()] = PipeConnection.LEVER;
						} else if (pipeBlock.connectToBlock(level, worldPosition.relative(direction), direction)) {
							if (facingState.getBlock() instanceof IPipeConnection pipeConnection) {
								connections[direction.get3DDataValue()] = pipeConnection.getPipeConnection(facingState, direction.getOpposite());
							} else {
								connections[direction.get3DDataValue()] = PipeConnection.END;
							}
						} else {
							connections[direction.get3DDataValue()] = PipeConnection.NONE;
						}
					}
				}
			}
		}
		syncConnections = true;
		Misc.sendToTrackingPlayers(level, worldPosition, getUpdatePacket());
		loaded = true;
		setChanged();
		level.getChunkAt(worldPosition).setUnsaved(true);
		level.updateNeighbourForOutputSignal(worldPosition, block);
		updateNetwork();
	}

	@Override
	public @NotNull ModelData getModelData() {
		int[] data = {
				connections[0].visualIndex,
				connections[1].visualIndex,
				connections[2].visualIndex,
				connections[3].visualIndex,
				connections[4].visualIndex,
				connections[5].visualIndex
		};
		return ModelData.builder().with(DATA_TYPE, data).build();
	}

	public void setConnection(Direction direction, PipeConnection connection) {
		connections[direction.get3DDataValue()] = connection;
		syncConnections = true;
		requestModelDataUpdate();
		setChanged();
		if (level instanceof ServerLevel)
			updateNetwork();
	}

	public PipeConnection getConnection(Direction direction) {
		return connections[direction.get3DDataValue()];
	}

    public void onDataPacket(@NotNull Connection connection, @NotNull ClientboundBlockEntityDataPacket packet, HolderLookup.@NotNull Provider provider) {
        resetSync();
        super.onDataPacket(connection, packet, provider);
        if (level != null && level.isClientSide())
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

	protected void resetSync() {
		syncConnections = false;
	}

	protected boolean requiresSync() {
		return syncConnections || !loaded;
	}

	@Override
	public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.loadAdditional(nbt, provider);
		loadConnections(nbt);
		loaded = true;
	}

	public void loadConnections(CompoundTag nbt) {
		for (Direction direction : Direction.values())
			if (nbt.contains("connection" + direction.get3DDataValue()))
				connections[direction.get3DDataValue()] = PipeConnection.values()[nbt.getInt("connection" + direction.get3DDataValue())];
		requestModelDataUpdate();
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
		super.saveAdditional(nbt, provider);
		if (saveConnections)
			writeConnections(nbt);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = super.getUpdateTag(provider);
		if (saveConnections)
			writeConnections(nbt);
		return nbt;
	}

	public void writeConnections(CompoundTag nbt) {
		for (Direction direction : Direction.values())
			nbt.putInt("connection" + direction.get3DDataValue(), getConnection(direction).index);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
        return requiresSync() ? ClientboundBlockEntityDataPacket.create(this) : null;
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (level instanceof ServerLevel serverLevel)
			serverLevel.getChunkSource().blockChanged(worldPosition);
	}

	public void updateNetwork() {
		ArrayList<IExtractorPipe> extractors = new ArrayList<>();
		Set<PipeBlockEntityBase> network = getNetwork(x -> {
			if (x instanceof IExtractorPipe extractor && extractor.active()) {
				extractors.add(extractor);
				extractor.clearConnections();
			}
		});
		network.forEach(x -> {
			if (x instanceof IExtractorPipe e && e.active())
				return;
			for (Direction dir : Direction.values()) {
				BlockPos connectionPos = x.worldPosition.relative(dir);
				Direction connectionSide = dir.getOpposite();
				BlockEntity connectionBlockentity = level.getBlockEntity(connectionPos);
				if (x.getConnection(dir) == PipeConnection.END ||
						(x.getConnection(dir) == PipeConnection.PIPE &&
								connectionBlockentity instanceof IExtractorPipe pipe &&
								pipe.acceptsPipes()))
					extractors.forEach(y ->
							y.addConnection(connectionPos,
									connectionSide,
									connectionBlockentity instanceof IPipePriority priority ?
											priority.getPriority(connectionSide) : 0));

			}
		});
	}

	public Set<PipeBlockEntityBase> getNetwork(Consumer<PipeBlockEntityBase> action) {
		Set<PipeBlockEntityBase> pipes = new TreeSet<>(Comparator.comparing(pipe -> pipe.worldPosition));
		collectPipesInNetwork(pipes, action, null);
		return pipes;
	}

	protected void collectPipesInNetwork(Set<PipeBlockEntityBase> set, Consumer<PipeBlockEntityBase> action, @Nullable Direction connectFrom) {
		if (set.contains(this))
			return;
		set.add(this);
		action.accept(this);
		for (Direction dir : Direction.values())
			if (connections[dir.get3DDataValue()] == PipeConnection.PIPE &&
					level.getBlockEntity(worldPosition.relative(dir)) instanceof PipeBlockEntityBase pipe)
				pipe.collectPipesInNetwork(set, action, dir.getOpposite());
	}

	public record PipeNetworkConnection(BlockPos pos, Direction side, int priority) implements Comparable<PipeNetworkConnection> {
		// For tree set
		@Override
		public int compareTo(@NotNull PipeBlockEntityBase.PipeNetworkConnection o) {
			if (priority != o.priority)
				return o.priority - priority; // Bigger priority = smaller in order
			int posComparison = pos.compareTo(o.pos);
			if (posComparison != 0)
				return posComparison;
			return side.compareTo(o.side);
		}
	}

	public enum PipeConnection implements StringRepresentable {
		NONE("none", 0, 0, false),
		DISABLED("disabled", 1, 0, false),
		PIPE("pipe", 2, 1, true),
		END("end", 3, 2, true),
		LEVER("lever", 4, 2, false);

		private final String name;
		public final int index;
		public final int visualIndex;
		public final boolean transfer;
		public static final PipeConnection[] visualValues = {
				NONE,
				PIPE,
				END
		};

		PipeConnection(String name, int index, int visualIndex, boolean transfer) {
			this.name = name;
			this.index = index;
			this.visualIndex = visualIndex;
			this.transfer = transfer;
		}

		public static PipeConnection[] visual() {
			return visualValues;
		}

		public String toString() {
			return this.name;
		}

		public @NotNull String getSerializedName() {
			return this.name;
		}
	}
}