package net.momirealms.craftengine.core.plugin.config;

import net.momirealms.craftengine.core.pack.CachedConfigSection;
import org.jetbrains.annotations.NotNull;

public interface ConfigParser extends Comparable<ConfigParser> {

    String[] sectionId();

    int loadingSequence();

    @Override
    default int compareTo(@NotNull ConfigParser another) {
        return Integer.compare(loadingSequence(), another.loadingSequence());
    }

    default void postProcess() {
    }

    default void preProcess() {
    }

    void addConfig(CachedConfigSection section);

    void loadAll();

    void clear();

    default int count() {
        return -1;
    }

    default boolean silentIfNotExists() {
        return true;
    }
}
