package net.momirealms.craftengine.core.pack.host.impl;

import net.momirealms.craftengine.core.pack.host.ResourcePackDownloadData;
import net.momirealms.craftengine.core.pack.host.ResourcePackHost;
import net.momirealms.craftengine.core.pack.host.ResourcePackHostFactory;
import net.momirealms.craftengine.core.pack.host.ResourcePackHosts;
import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.util.Key;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class ExternalHost implements ResourcePackHost {
    public static final ResourcePackHostFactory FACTORY = new Factory();
    private final ResourcePackDownloadData downloadData;

    public ExternalHost(ResourcePackDownloadData downloadData) {
        this.downloadData = downloadData;
    }

    @Override
    public CompletableFuture<List<ResourcePackDownloadData>> requestResourcePackDownloadLink(UUID player) {
        return CompletableFuture.completedFuture(List.of(this.downloadData));
    }

    @Override
    public CompletableFuture<Void> upload(Path resourcePackPath) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean canUpload() {
        return false;
    }

    @Override
    public Key type() {
        return ResourcePackHosts.EXTERNAL;
    }

    private static class Factory implements ResourcePackHostFactory {

        @Override
        public ResourcePackHost create(Map<String, Object> arguments) {
            String url = Optional.ofNullable(arguments.get("url")).map(String::valueOf).orElse(null);
            if (url == null || url.isEmpty()) {
                throw new LocalizedException("warning.config.host.external.missing_url");
            }
            String uuid = Optional.ofNullable(arguments.get("uuid")).map(String::valueOf).orElse(null);
            if (uuid == null || uuid.isEmpty()) {
                uuid = UUID.nameUUIDFromBytes(url.getBytes(StandardCharsets.UTF_8)).toString();
            }
            UUID hostUUID = UUID.fromString(uuid);
            String sha1 = arguments.getOrDefault("sha1", "").toString();
            return new ExternalHost(new ResourcePackDownloadData(url, hostUUID, sha1));
        }
    }
}
