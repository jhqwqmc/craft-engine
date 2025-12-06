package net.momirealms.craftengine.core.pack;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public class CachedConfigSection {
    private final Pack pack;
    private final Path filePath;
    private final String prefix;
    private final Map<String, Object> config;

    public CachedConfigSection(String prefix, Map<String, Object> config, Path filePath, Pack pack) {
        this.config = Objects.requireNonNull(config);
        this.filePath = Objects.requireNonNull(filePath);
        this.pack = Objects.requireNonNull(pack);
        this.prefix = Objects.requireNonNull(prefix);
    }

    public Map<String, Object> config() {
        return config;
    }

    public Path filePath() {
        return filePath;
    }

    public Pack pack() {
        return pack;
    }

    public String prefix() {
        return prefix;
    }
}
