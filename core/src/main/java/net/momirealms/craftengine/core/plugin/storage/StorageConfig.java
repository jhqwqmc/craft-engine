package net.momirealms.craftengine.core.plugin.storage;

import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public final class StorageConfig {
    private StorageConfig() {}

    private static String mongoUrl(ConfigSection section) {
        String host = nonBlank(section, "host", "localhost");
        if (host.contains(":") && !host.startsWith("[")) host = "[" + host + "]";
        int port = section.getValue("port", value -> value.getAsInt(1, 65535), 27017);
        String database = nonBlank(section, "database", "craftengine");
        return "mongodb://" + host + ":" + port + "/" + encode(database);
    }

    private static Path localPath(ConfigSection section, String key, String defaultValue, Path dataDirectory) {
        try {
            return dataDirectory.resolve(nonBlank(section, key, defaultValue)).toAbsolutePath().normalize();
        } catch (InvalidPathException e) {
            throw new KnownResourceException("storage.invalid_path", section.assemblePath(key), e);
        }
    }

    private static String nonBlank(ConfigSection section, String key, String defaultValue) {
        String value = section.getString(key, defaultValue).trim();
        if (value.isEmpty()) {
            throw new KnownResourceException(ConfigConstants.PARSE_NONEMPTY_STRING_FAILED, section.assemblePath(key));
        }
        return value;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    public record Json(Path directory) {
        public static Json fromConfig(ConfigSection section, Path dataDirectory) {
            return new Json(localPath(section, "directory", "data/json", dataDirectory));
        }
    }

    public record Sql(SqlDialect type,
                      String url,
                      String username,
                      String password,
                      int poolSize) {
        public Sql {
            if (type == SqlDialect.SQLITE) poolSize = 1;
            if (poolSize < 1) throw new IllegalArgumentException("SQL pool size must be positive");
        }

        public static Sql fromConfig(SqlDialect type, ConfigSection section) {
            String url = nonBlank(section, "url", "");
            String username = section.getString("username", type == SqlDialect.H2 ? "sa" : "");
            String password = section.getString("password", "");
            int poolSize = type == SqlDialect.SQLITE ? 1 : section.getValue("pool_size", value -> value.getAsInt(1), 4);
            return new Sql(type, url, username, password, poolSize);
        }
    }

    public record Mongo(String url, String username, String password) {
        public static Mongo fromConfig(ConfigSection section) {
            String url = section.getString("url", "").trim();
            if (url.isEmpty()) {
                int poolSize = section.getValue("max_pool_size", value -> value.getAsInt(1), 10);
                url = mongoUrl(section)
                        + "?authSource=" + encode(nonBlank(section, "auth_source", "admin"))
                        + "&maxPoolSize=" + poolSize;
            }
            return new Mongo(url, section.getString("username", ""), section.getString("password", ""));
        }
    }
}
