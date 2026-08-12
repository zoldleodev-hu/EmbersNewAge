package hu.zoldleo.embers.render;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import hu.zoldleo.embers.Embers;

import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public class PipeModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
	T center;
	T connection;
	T connection2;
	T end;
	T end2;

	public static <T extends ModelBuilder<T>> PipeModelBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) {
		return new PipeModelBuilder<>(parent, existingFileHelper);
	}

	protected PipeModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
		super(Embers.res("pipe"), parent, existingFileHelper, true);
	}

	public PipeModelBuilder<T> parts(T center, T connection, T connection2, T end, T end2) {
		Preconditions.checkNotNull(center, "center must not be null");
		Preconditions.checkNotNull(connection, "connection must not be null");
		Preconditions.checkNotNull(connection2, "connection2 must not be null");
		Preconditions.checkNotNull(end, "end must not be null");
		Preconditions.checkNotNull(end2, "end2 must not be null");
		this.center = center;
		this.connection = connection;
		this.connection2 = connection2;
		this.end = end;
		this.end2 = end2;
		return this;
	}

	@Override
	public @NotNull JsonObject toJson(@NotNull JsonObject json) {
		json = super.toJson(json);

		json.add("center", center.toJson());
		json.add("connection", connection.toJson());
		json.add("connection2", connection2.toJson());
		json.add("end", end.toJson());
		json.add("end2", end2.toJson());

		return json;
	}
}