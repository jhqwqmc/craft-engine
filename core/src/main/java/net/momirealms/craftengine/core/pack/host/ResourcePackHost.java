package net.momirealms.craftengine.core.pack.host;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ResourcePackHost {
    private static ResourcePackHost instance;
    private HttpServer server;
    private String ip;
    private int port;
    private Path resourcePackPath;
    private final ConcurrentHashMap<String, IpAccessRecord> ipAccessMap = new ConcurrentHashMap<>();
    private int rateLimit = 1;
    private long rateLimitInterval = 1000;

    public String url() {
        return ConfigManager.hostProtocol() + "://" + ip + ":" + port + "/";
    }

    public void enable(String ip, int port, Path resourcePackPath) {
        if (server != null) {
            disable();
        }
        this.ip = ip;
        this.port = port;
        this.resourcePackPath = resourcePackPath;

        try {
            server = HttpServer.create(new InetSocketAddress(ip, port), 0);
            server.createContext("/", new ResourcePackHandler());
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            CraftEngine.instance().logger().info("HTTP resource pack server running on " + ip + ":" + port);
        } catch (IOException e) {
            CraftEngine.instance().logger().warn("Failed to start HTTP server", e);
        }
    }

    public void disable() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public boolean isAlive() {
        return server != null;
    }

    public static ResourcePackHost instance() {
        if (instance == null) {
            instance = new ResourcePackHost();
        }
        return instance;
    }

    public void setRateLimit(int rateLimit, long rateLimitInterval, TimeUnit timeUnit) {
        this.rateLimit = rateLimit;
        this.rateLimitInterval = timeUnit.toMillis(rateLimitInterval);
    }

    private class ResourcePackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();

            // 速率限制逻辑（可选启用）
            IpAccessRecord record = ipAccessMap.compute(clientIp, (k, v) -> {
                long currentTime = System.currentTimeMillis();
                if (v == null || currentTime - v.lastAccessTime > rateLimitInterval) {
                    return new IpAccessRecord(currentTime, 1);
                } else {
                    v.accessCount++;
                    return v;
                }
            });

            if (record.accessCount > rateLimit) {
                CraftEngine.instance().debug(() -> "Rate limit exceeded for IP: " + clientIp);
                sendError(exchange, 429); // 429 Too Many Requests
                return;
            }

            if (!Files.exists(resourcePackPath)) {
                CraftEngine.instance().logger().warn("ResourcePack not found: " + resourcePackPath);
                sendError(exchange, 404);
                return;
            }

            // 设置响应头
            exchange.getResponseHeaders().set("Content-Type", "application/zip");
            exchange.getResponseHeaders().set("Content-Length", String.valueOf(Files.size(resourcePackPath)));
            exchange.sendResponseHeaders(200, Files.size(resourcePackPath));

            // 流式传输文件
            try (OutputStream os = exchange.getResponseBody()) {
                Files.copy(resourcePackPath, os);
            } catch (IOException e) {
                CraftEngine.instance().logger().warn("Failed to send pack", e);
            }
        }

        private void sendError(HttpExchange exchange, int code) throws IOException {
            exchange.sendResponseHeaders(code, 0);
            exchange.getResponseBody().close();
        }
    }

    private static class IpAccessRecord {
        long lastAccessTime;
        int accessCount;

        IpAccessRecord(long lastAccessTime, int accessCount) {
            this.lastAccessTime = lastAccessTime;
            this.accessCount = accessCount;
        }
    }
}