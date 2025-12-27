package net.momirealms.craftengine.core.pack.host.impl;

import io.github.bucket4j.Bandwidth;
import net.momirealms.craftengine.core.pack.host.*;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class SelfHost implements ResourcePackHost {
    public static final ResourcePackHostFactory FACTORY = new Factory();
    private static final SelfHost INSTANCE = new SelfHost();

    public SelfHost() {
        SelfHostHttpServer.instance().readResourcePack(Config.fileToUpload());
    }

    @Override
    public CompletableFuture<List<ResourcePackDownloadData>> requestResourcePackDownloadLink(UUID player) {
        ResourcePackDownloadData data = SelfHostHttpServer.instance().generateOneTimeUrl(player);
        if (data == null) return CompletableFuture.completedFuture(List.of());
        return CompletableFuture.completedFuture(List.of(data));
    }

    @Override
    public CompletableFuture<Void> upload(Path resourcePackPath) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        CraftEngine.instance().scheduler().executeAsync(() -> {
            try {
                SelfHostHttpServer.instance().readResourcePack(resourcePackPath);
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public boolean canUpload() {
        return true;
    }

    @Override
    public ResourcePackHostType type() {
        return ResourcePackHosts.SELF;
    }

    private static class Factory implements ResourcePackHostFactory {

        @Override
        public ResourcePackHost create(Map<String, Object> arguments) {
            SelfHostHttpServer selfHostHttpServer = SelfHostHttpServer.instance();
            String ip = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("ip"), () -> new LocalizedException("warning.config.host.self.missing_ip"));
            int port = ResourceConfigUtils.getAsInt(arguments.getOrDefault("port", 8163), "port");
            if (port <= 0 || port > 65535) {
                throw new LocalizedException("warning.config.host.self.invalid_port", String.valueOf(port));
            }
            String url = arguments.getOrDefault("url", "").toString();
            if (!url.isEmpty()) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    throw new LocalizedException("warning.config.host.self.invalid_url", url);
                }
                if (!url.endsWith("/")) url  += "/";
            }
            boolean oneTimeToken = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("one-time-token", true), "one-time-token");
            String protocol = arguments.getOrDefault("protocol", "http").toString();
            boolean denyNonMinecraftRequest = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("deny-non-minecraft-request", true), "deny-non-minecraft-request");
            boolean strictValidation = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("strict-validation", false), "strict-validation");

            Bandwidth limit = null;
            Map<String, Object> rateLimitingSection = ResourceConfigUtils.getAsMapOrNull(arguments.get("rate-limiting"), "rate-limiting");
            long maxBandwidthUsage = 0L;
            long minDownloadSpeed = 50_000L;
            if (rateLimitingSection != null) {
                if (rateLimitingSection.containsKey("qps-per-ip")) {
                    String qps = rateLimitingSection.get("qps-per-ip").toString();
                    String[] split = qps.split("/", 2);
                    if (split.length == 1) split = new String[]{split[0], "1"};
                    int maxRequests = ResourceConfigUtils.getAsInt(split[0], "qps-per-ip");
                    int resetInterval = ResourceConfigUtils.getAsInt(split[1], "qps-per-ip");
                    limit = Bandwidth.builder()
                            .capacity(maxRequests)
                            .refillGreedy(maxRequests, Duration.ofSeconds(resetInterval))
                            .build();
                }
                maxBandwidthUsage = ResourceConfigUtils.getAsLong(rateLimitingSection.getOrDefault("max-bandwidth-per-second", 0), "max-bandwidth");
                minDownloadSpeed = ResourceConfigUtils.getAsLong(rateLimitingSection.getOrDefault("min-download-speed-per-player", 50_000), "min-download-speed-per-player");
            }
            selfHostHttpServer.updateProperties(ip, port, url, denyNonMinecraftRequest, protocol, limit, oneTimeToken, maxBandwidthUsage, minDownloadSpeed, strictValidation);
            return INSTANCE;
        }
    }
}
