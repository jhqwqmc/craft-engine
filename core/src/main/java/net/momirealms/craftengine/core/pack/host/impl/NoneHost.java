package net.momirealms.craftengine.core.pack.host.impl;

import net.momirealms.craftengine.core.pack.host.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class NoneHost implements ResourcePackHost {
    public static final ResourcePackHostFactory FACTORY = new Factory();
    public static final NoneHost INSTANCE = new NoneHost();

    private NoneHost() {}

    @Override
    public CompletableFuture<List<ResourcePackDownloadData>> requestResourcePackDownloadLink(UUID player) {
        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public CompletableFuture<Void> upload(Path resourcePackPath) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public ResourcePackHostType type() {
        return ResourcePackHosts.NONE;
    }

    @Override
    public boolean canUpload() {
        return false;
    }

    private static class Factory implements ResourcePackHostFactory {

        @Override
        public ResourcePackHost create(Map<String, Object> arguments) {
            return INSTANCE;
        }
    }
}
