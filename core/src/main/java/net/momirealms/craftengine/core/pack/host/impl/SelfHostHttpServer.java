package net.momirealms.craftengine.core.pack.host.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedStream;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.traffic.GlobalChannelTrafficShapingHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import net.momirealms.craftengine.core.pack.host.ResourcePackDownloadData;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class SelfHostHttpServer {
    private static SelfHostHttpServer instance;
    private final Cache<String, Boolean> oneTimePackUrls = Caffeine.newBuilder()
            .maximumSize(1024)
            .scheduler(Scheduler.systemScheduler())
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();
    private final Cache<String, Bucket> ipRateLimiters = Caffeine.newBuilder()
            .maximumSize(1024)
            .scheduler(Scheduler.systemScheduler())
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();

    private final AtomicLong totalRequests = new AtomicLong();
    private final AtomicLong blockedRequests = new AtomicLong();

    private Bandwidth limitPerIp = Bandwidth.builder()
            .capacity(1)
            .refillGreedy(1, Duration.ofSeconds(1))
            .initialTokens(1)
            .build();

    private String ip = "localhost";
    private int port = -1;
    private String protocol = "http";
    private String url;
    private boolean denyNonMinecraft = true;
    private boolean useToken;

    private long globalUploadRateLimit = 0;
    private long minDownloadSpeed = 50_000;
    private GlobalChannelTrafficShapingHandler trafficShapingHandler;
    private ScheduledExecutorService virtualTrafficExecutor;
    private final ChannelGroup activeDownloadChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private byte[] resourcePackBytes;
    private String packHash;
    private UUID packUUID;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    public static SelfHostHttpServer instance() {
        if (instance == null) {
            instance = new SelfHostHttpServer();
        }
        return instance;
    }

    public void updateProperties(String ip,
                                 int port,
                                 String url,
                                 boolean denyNonMinecraft,
                                 String protocol,
                                 Bandwidth limitPerIp,
                                 boolean token,
                                 long globalUploadRateLimit,
                                 long minDownloadSpeed) {
        this.ip = ip;
        this.url = url;
        this.denyNonMinecraft = denyNonMinecraft;
        this.protocol = protocol;
        this.limitPerIp = limitPerIp;
        this.useToken = token;
        if (this.globalUploadRateLimit != globalUploadRateLimit || this.minDownloadSpeed != minDownloadSpeed) {
            this.globalUploadRateLimit = globalUploadRateLimit;
            this.minDownloadSpeed = minDownloadSpeed;
            if (this.trafficShapingHandler != null) {
                long initSize = globalUploadRateLimit <= 0 ? 0 : Math.max(minDownloadSpeed, globalUploadRateLimit);
                this.trafficShapingHandler.setWriteLimit(initSize);
                this.trafficShapingHandler.setWriteChannelLimit(initSize);
            }
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port: " + port);
        }

        if (this.port == port && serverChannel != null) return;
        disable();

        this.port = port;
        initializeServer();
    }

    public String url() {
        if (this.url != null && !this.url.isEmpty()) {
            return this.url;
        }
        return this.protocol + "://" + this.ip + ":" + this.port + "/";
    }

    private void initializeServer() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        virtualTrafficExecutor = Executors.newScheduledThreadPool(1, Thread.ofVirtual().factory());
        long initSize = globalUploadRateLimit <= 0 ? 0 : Math.max(minDownloadSpeed, globalUploadRateLimit);
        trafficShapingHandler = new GlobalChannelTrafficShapingHandler(
                virtualTrafficExecutor,
                initSize,
                0, // 全局读取不限
                initSize, // 默认单通道和总体一致
                0, // 单通道读取不限
                100, // checkInterval (ms)
                10_000 // maxTime (ms)
        );
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("trafficShaping", trafficShapingHandler);
                        pipeline.addLast(new HttpServerCodec());
                        pipeline.addLast(new ChunkedWriteHandler());
                        pipeline.addLast(new HttpObjectAggregator(1048576));
                        pipeline.addLast(new RequestHandler());
                    }
                });
        try {
            serverChannel = b.bind(port).sync().channel();
            CraftEngine.instance().logger().info("Netty HTTP server started on port: " + port);
        } catch (InterruptedException e) {
            CraftEngine.instance().logger().warn("Failed to start Netty server", e);
            Thread.currentThread().interrupt();
        }
    }

    @ChannelHandler.Sharable
    private class RequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            // 有人走了，其他人的速度上限提高
            if (activeDownloadChannels.contains(ctx.channel())) {
                activeDownloadChannels.remove(ctx.channel());
                rebalanceBandwidth();
            }
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
            totalRequests.incrementAndGet();

            try {
                String clientIp = ((InetSocketAddress) ctx.channel().remoteAddress())
                        .getAddress().getHostAddress();

                if (!checkIpRateLimit(clientIp)) {
                    sendError(ctx, HttpResponseStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");
                    blockedRequests.incrementAndGet();
                    return;
                }

                QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
                String path = queryDecoder.path();

                if ("/download".equals(path)) {
                    handleDownload(ctx, request, queryDecoder);
                } else if ("/metrics".equals(path)) {
                    handleMetrics(ctx);
                } else {
                    sendError(ctx, HttpResponseStatus.NOT_FOUND, "Not Found");
                }
            } catch (Exception e) {
                CraftEngine.instance().logger().warn("Request handling failed", e);
                sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Internal Error");
            }
        }

        private void handleDownload(ChannelHandlerContext ctx, FullHttpRequest request, QueryStringDecoder queryDecoder) {
            // 使用一次性token
            if (useToken) {
                String token = queryDecoder.parameters().getOrDefault("token", java.util.Collections.emptyList()).stream().findFirst().orElse(null);
                if (!validateToken(token)) {
                    sendError(ctx, HttpResponseStatus.FORBIDDEN, "Invalid token");
                    blockedRequests.incrementAndGet();
                    return;
                }
            }

            // 不是Minecraft客户端
            if (denyNonMinecraft) {
                String userAgent = request.headers().get(HttpHeaderNames.USER_AGENT);
                if (userAgent == null || !userAgent.startsWith("Minecraft Java/")) {
                    sendError(ctx, HttpResponseStatus.FORBIDDEN, "Invalid client");
                    blockedRequests.incrementAndGet();
                    return;
                }
            }

            // 没有资源包
            if (resourcePackBytes == null) {
                sendError(ctx, HttpResponseStatus.NOT_FOUND, "Resource pack missing");
                blockedRequests.incrementAndGet();
                return;
            }

            // 新人来了，所有人的速度上限降低
            if (!activeDownloadChannels.contains(ctx.channel())) {
                activeDownloadChannels.add(ctx.channel());
                rebalanceBandwidth();
            }

            // 告诉客户端资源包大小
            long fileLength = resourcePackBytes.length;
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            HttpUtil.setContentLength(response, fileLength);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/zip");
            boolean keepAlive = HttpUtil.isKeepAlive(request);
            if (keepAlive) {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }
            ctx.write(response);

            // 发送分段资源包
            ChunkedStream chunkedStream = new ChunkedStream(new ByteArrayInputStream(resourcePackBytes), 8192);
            HttpChunkedInput httpChunkedInput = new HttpChunkedInput(chunkedStream);
            ChannelFuture sendFileFuture = ctx.writeAndFlush(httpChunkedInput);
            if (!keepAlive) {
                sendFileFuture.addListener(ChannelFutureListener.CLOSE);
            }

            // 监听下载完成（成功或失败），以便在下载结束后（如果不关闭连接）也能移除计数
            // 注意：如果是 Keep-Alive，连接不会断，但下载结束了。
            // 为了精确控制，可以在这里监听 operationComplete
            sendFileFuture.addListener((ChannelFutureListener) future -> {
                if (activeDownloadChannels.contains(ctx.channel())) {
                    activeDownloadChannels.remove(ctx.channel());
                    rebalanceBandwidth();
                }
            });
        }

        private void handleMetrics(ChannelHandlerContext ctx) {
            String metrics = "# TYPE total_requests counter\n"
                    + "total_requests " + totalRequests.get() + "\n"
                    + "# TYPE blocked_requests counter\n"
                    + "blocked_requests " + blockedRequests.get();

            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.copiedBuffer(metrics, CharsetUtil.UTF_8)
            );
            response.headers()
                    .set(HttpHeaderNames.CONTENT_TYPE, "text/plain")
                    .set(HttpHeaderNames.CONTENT_LENGTH, metrics.length());

            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }

        private boolean checkIpRateLimit(String clientIp) {
            if (limitPerIp == null) return true;
            Bucket rateLimiter = ipRateLimiters.get(clientIp, k -> Bucket.builder().addLimit(limitPerIp).build());
            assert rateLimiter != null;
            return rateLimiter.tryConsume(1);
        }

        private boolean validateToken(String token) {
            if (token == null || token.length() != 36) return false;
            Boolean valid = oneTimePackUrls.getIfPresent(token);
            if (valid != null) {
                oneTimePackUrls.invalidate(token);
                return true;
            }
            return false;
        }

        private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status, String message) {
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    status,
                    Unpooled.copiedBuffer(message, CharsetUtil.UTF_8)
            );
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.close();
        }
    }

    private synchronized void rebalanceBandwidth() {
        if (globalUploadRateLimit == 0) {
            trafficShapingHandler.setWriteChannelLimit(0);
            return;
        }

        int activeCount = activeDownloadChannels.size();
        if (activeCount == 0) {
            trafficShapingHandler.setWriteChannelLimit(globalUploadRateLimit);
            return;
        }

        // 计算平均带宽：全局总量 / 当前人数
        long fairRate = globalUploadRateLimit / activeCount;

        // 确保不低于最小保障速率（可选，防止除法导致过小）
        fairRate = Math.max(fairRate, this.minDownloadSpeed);

        // 更新 Handler 配置
        trafficShapingHandler.setWriteChannelLimit(fairRate);
    }

    @Nullable
    public ResourcePackDownloadData generateOneTimeUrl() {
        if (this.resourcePackBytes == null) return null;

        if (!this.useToken) {
            return new ResourcePackDownloadData(url() + "download", this.packUUID, this.packHash);
        }

        String token = UUID.randomUUID().toString();
        oneTimePackUrls.put(token, true);
        return new ResourcePackDownloadData(
                url() + "download?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8),
                packUUID,
                packHash
        );
    }

    public void disable() {
        // 释放流量整形资源
        if (trafficShapingHandler != null) {
            trafficShapingHandler.release();
            trafficShapingHandler = null;
        }
        // 关闭专用线程池
        if (virtualTrafficExecutor != null) {
            virtualTrafficExecutor.shutdown();
            virtualTrafficExecutor = null;
        }
        activeDownloadChannels.close();
        if (serverChannel != null) {
            serverChannel.close().awaitUninterruptibly();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serverChannel = null;
        }
    }

    public void readResourcePack(Path path) {
        try {
            if (Files.exists(path)) {
                this.resourcePackBytes = Files.readAllBytes(path);
                calculateHash();
            } else {
                this.resourcePackBytes = null;
            }
        } catch (IOException e) {
            CraftEngine.instance().logger().severe("Failed to load resource pack", e);
        }
    }

    private void calculateHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(this.resourcePackBytes);
            byte[] hashBytes = digest.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            this.packHash = hexString.toString();
            this.packUUID = UUID.nameUUIDFromBytes(this.packHash.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            CraftEngine.instance().logger().severe("SHA-1 algorithm not available", e);
        }
    }
}