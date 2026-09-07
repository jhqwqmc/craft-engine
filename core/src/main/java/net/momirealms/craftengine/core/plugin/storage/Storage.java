package net.momirealms.craftengine.core.plugin.storage;

import java.util.Map;
import java.util.UUID;

public interface Storage extends AutoCloseable {

    Map<String, Boolean> loadPackPreferences(UUID player) throws Exception;

    void setPackPreference(UUID player, String pack, Boolean enabled) throws Exception;

    @Override
    void close();
}
