package net.momirealms.craftengine.core.pack.conflict.resolution;

import net.momirealms.craftengine.core.pack.conflict.PathContext;
import net.momirealms.craftengine.core.util.Key;

public interface Resolution {

    void run(PathContext existing, PathContext conflict);
}
