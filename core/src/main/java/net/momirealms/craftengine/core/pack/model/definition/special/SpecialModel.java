package net.momirealms.craftengine.core.pack.model.definition.special;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.util.MinecraftVersion;

import java.util.List;
import java.util.function.Function;

public interface SpecialModel extends Function<MinecraftVersion, JsonObject> {

    List<Revision> revisions();
}
