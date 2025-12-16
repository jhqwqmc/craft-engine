package net.momirealms.craftengine.core.pack.overlay;

import net.momirealms.craftengine.core.util.MinecraftVersion;

import java.nio.file.Path;

public class ResourcePackOverlay {
    private final MinecraftVersion minVersion;
    private final MinecraftVersion maxVersion;
    private final Path folder;

    public ResourcePackOverlay(MinecraftVersion minVersion,
                               MinecraftVersion maxVersion,
                               Path folder
    ) {
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
        this.folder = folder;
    }

    public MinecraftVersion minVersion() {
        return minVersion;
    }

    public MinecraftVersion maxVersion() {
        return maxVersion;
    }

    public Path folder() {
        return folder;
    }
}
