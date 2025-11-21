package net.momirealms.craftengine.core.pack.host.impl;

import com.google.common.util.concurrent.RateLimiter;
import io.github.bucket4j.Bandwidth;
import net.momirealms.craftengine.core.pack.host.ResourcePackDownloadData;
import net.momirealms.craftengine.core.pack.host.ResourcePackHost;
import net.momirealms.craftengine.core.pack.host.ResourcePackHostFactory;
import net.momirealms.craftengine.core.pack.host.ResourcePackHosts;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SelfHost implements ResourcePackHost {
    public static final Factory FACTORY = new Factory();
    private static final SelfHost INSTANCE = new SelfHost();

    public SelfHost() {
        SelfHostHttpServer.instance().readResourcePack(Config.fileToUpload());
    }

    @Override
    public CompletableFuture<List<ResourcePackDownloadData>> requestResourcePackDownloadLink(UUID player) {
        ResourcePackDownloadData data = SelfHostHttpServer.instance().generateOneTimeUrl();
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
    public Key type() {
        return ResourcePackHosts.SELF;
    }

    public static class Factory implements ResourcePackHostFactory {

        @SuppressWarnings("UnstableApiUsage")
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
            Map<String, Object> rateLimitPerIp = MiscUtils.castToMap(arguments.get("rate-limitation-per-ip"), true);
            boolean enabledLimitPerIp = false;
            Bandwidth limit = null;
            out:
            if (rateLimitPerIp != null) {
                enabledLimitPerIp = ResourceConfigUtils.getAsBoolean(rateLimitPerIp.getOrDefault("enable", false), "enable");
                if (!enabledLimitPerIp) break out;
                int maxRequests = Math.max(ResourceConfigUtils.getAsInt(rateLimitPerIp.getOrDefault("max-requests", 5), "max-requests"), 1);
                int resetInterval = Math.max(ResourceConfigUtils.getAsInt(rateLimitPerIp.getOrDefault("reset-interval", 20), "reset-interval"), 1);
                limit = Bandwidth.builder()
                        .capacity(maxRequests)
                        .refillGreedy(maxRequests, Duration.ofSeconds(resetInterval))
                        .build();
            }
            Map<String, Object> tokenBucket = MiscUtils.castToMap(arguments.get("token-bucket"), true);
            boolean enabledTokenBucket = false;
            RateLimiter globalLimiter = null;
            out:
            if (tokenBucket != null) {
                enabledTokenBucket = ResourceConfigUtils.getAsBoolean(tokenBucket.getOrDefault("enable", false), "enable");
                if (!enabledTokenBucket) break out;
                globalLimiter = RateLimiter.create(ResourceConfigUtils.getAsDouble(tokenBucket.getOrDefault("qps", 1000), "qps"));
            }
            selfHostHttpServer.updateProperties(ip, port, url, denyNonMinecraftRequest, protocol, limit, enabledLimitPerIp, enabledTokenBucket, globalLimiter, oneTimeToken);
            return INSTANCE;
        }
    }
}
