package net.momirealms.craftengine.core.plugin.config;

import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class ConfigSection {
    private final Map<String, Object> config;

    public ConfigSection(Map<String, Object> config) {
        this.config = config;
    }

    public static ConfigSection of(Map<String, Object> config) {
        return new ConfigSection(config);
    }

    /**
     * Gets a boolean value from the configuration.
     * If the key doesn't exist or value is null, returns the default value (false).
     *
     * @param key Configuration key
     * @return Boolean value for the specified key, or false if not found
     */
    public boolean getBoolean(final String key) {
        return getBoolean(false, key);
    }

    /**
     * Gets a boolean value by trying multiple keys in order.
     * Returns the first found non-null value, or false if none found.
     *
     * @param keys Configuration keys to try in order
     * @return Boolean value for the first found key, or false if none found
     * @throws IllegalArgumentException If keys array is empty
     */
    public boolean getBoolean(final String... keys) {
        return getBoolean(false, keys);
    }

    /**
     * Gets a boolean value from the configuration with a custom default.
     *
     * @param def Default value to return if key doesn't exist
     * @param key Configuration key
     * @return Boolean value for the specified key, or the default value if not found
     */
    public boolean getBoolean(boolean def, final String key) {
        return ResourceConfigUtils.getAsBoolean(this.config.getOrDefault(key, def), key);
    }

    /**
     * Gets a boolean value by trying multiple keys in order with a custom default.
     *
     * @param def Default value to return if none of the keys are found
     * @param keys Configuration keys to try in order
     * @return Boolean value for the first found key, or the default value if none found
     * @throws IllegalArgumentException If keys array is empty
     */
    public boolean getBoolean(boolean def, final String... keys) {
        if (keys.length == 0) throw new IllegalArgumentException("keys must not be empty");
        return ResourceConfigUtils.getAsBoolean(ResourceConfigUtils.getOrDefault(this.config, def, keys), keys[0]);
    }

    /**
     * Gets an integer value from the configuration.
     * If the key doesn't exist or value is null, returns 0.
     *
     * @param key Configuration key
     * @return Integer value for the specified key, or 0 if not found
     */
    public int getInt(final String key) {
        return ResourceConfigUtils.getAsInt(this.config.get(key), key);
    }

    /**
     * Gets an integer value by trying multiple keys in order.
     * Returns the first found non-null value, or 0 if none found.
     *
     * @param keys Configuration keys to try in order
     * @return Integer value for the first found key, or 0 if none found
     * @throws IllegalArgumentException If keys array is empty
     */
    public int getInt(final String... keys) {
        if (keys.length == 0) throw new IllegalArgumentException("keys must not be empty");
        return ResourceConfigUtils.getAsInt(ResourceConfigUtils.get(this.config, keys), keys[0]);
    }

    /**
     * Gets an integer value from the configuration with a custom default.
     *
     * @param def Default value to return if key doesn't exist
     * @param key Configuration key
     * @return Integer value for the specified key, or the default value if not found
     */
    public int getInt(int def, final String key) {
        return ResourceConfigUtils.getAsInt(this.config.getOrDefault(key, def), key);
    }

    /**
     * Gets an integer value by trying multiple keys in order with a custom default.
     *
     * @param def Default value to return if none of the keys are found
     * @param keys Configuration keys to try in order
     * @return Integer value for the first found key, or the default value if none found
     * @throws IllegalArgumentException If keys array is empty
     */
    public int getInt(int def, final String... keys) {
        if (keys.length == 0) throw new IllegalArgumentException("keys must not be empty");
        return ResourceConfigUtils.getAsInt(ResourceConfigUtils.getOrDefault(this.config, def, keys), keys[0]);
    }

    /**
     * Gets a long value from the configuration.
     * If the key doesn't exist or value is null, returns 0L.
     *
     * @param key Configuration key
     * @return Long value for the specified key, or 0L if not found
     */
    public long getLong(final String key) {
        return ResourceConfigUtils.getAsLong(this.config.get(key), key);
    }

    /**
     * Gets a long value by trying multiple keys in order.
     * Returns the first found non-null value, or 0L if none found.
     *
     * @param keys Configuration keys to try in order
     * @return Long value for the first found key, or 0L if none found
     * @throws IllegalArgumentException If keys array is empty
     */
    public long getLong(final String... keys) {
        if (keys.length == 0) throw new IllegalArgumentException("keys must not be empty");
        return ResourceConfigUtils.getAsLong(ResourceConfigUtils.get(this.config, keys), keys[0]);
    }

    /**
     * Gets a long value from the configuration with a custom default.
     *
     * @param def Default value to return if key doesn't exist
     * @param key Configuration key
     * @return Long value for the specified key, or the default value if not found
     */
    public long getLong(long def, final String key) {
        return ResourceConfigUtils.getAsLong(this.config.getOrDefault(key, def), key);
    }

    /**
     * Gets a long value by trying multiple keys in order with a custom default.
     *
     * @param def Default value to return if none of the keys are found
     * @param keys Configuration keys to try in order
     * @return Long value for the first found key, or the default value if none found
     * @throws IllegalArgumentException If keys array is empty
     */
    public long getLong(long def, final String... keys) {
        if (keys.length == 0) throw new IllegalArgumentException("keys must not be empty");
        return ResourceConfigUtils.getAsLong(ResourceConfigUtils.getOrDefault(this.config, def, keys), keys[0]);
    }

    /**
     * Gets a float value from the configuration.
     * If the key doesn't exist or value is null, returns 0.0f.
     *
     * @param key Configuration key
     * @return Float value for the specified key, or 0.0f if not found
     */
    public float getFloat(final String key) {
        return ResourceConfigUtils.getAsFloat(this.config.get(key), key);
    }

    /**
     * Gets a float value by trying multiple keys in order.
     * Returns the first found non-null value, or 0.0f if none found.
     *
     * @param keys Configuration keys to try in order
     * @return Float value for the first found key, or 0.0f if none found
     * @throws IllegalArgumentException If keys array is empty
     */
    public float getFloat(final String... keys) {
        if (keys.length == 0) throw new IllegalArgumentException("keys must not be empty");
        return ResourceConfigUtils.getAsFloat(ResourceConfigUtils.get(this.config, keys), keys[0]);
    }

    /**
     * Gets a float value from the configuration with a custom default.
     *
     * @param def Default value to return if key doesn't exist
     * @param key Configuration key
     * @return Float value for the specified key, or the default value if not found
     */
    public float getFloat(float def, final String key) {
        return ResourceConfigUtils.getAsFloat(this.config.getOrDefault(key, def), key);
    }

    /**
     * Gets a float value by trying multiple keys in order with a custom default.
     *
     * @param def Default value to return if none of the keys are found
     * @param keys Configuration keys to try in order
     * @return Float value for the first found key, or the default value if none found
     * @throws IllegalArgumentException If keys array is empty
     */
    public float getFloat(float def, final String... keys) {
        if (keys.length == 0) throw new IllegalArgumentException("keys must not be empty");
        return ResourceConfigUtils.getAsFloat(ResourceConfigUtils.getOrDefault(this.config, def, keys), keys[0]);
    }

    /**
     * Gets a double value from the configuration.
     * If the key doesn't exist or value is null, returns 0.0.
     *
     * @param key Configuration key
     * @return Double value for the specified key, or 0.0 if not found
     */
    public double getDouble(final String key) {
        return ResourceConfigUtils.getAsDouble(this.config.get(key), key);
    }

    /**
     * Gets a double value by trying multiple keys in order.
     * Returns the first found non-null value, or 0.0 if none found.
     *
     * @param keys Configuration keys to try in order
     * @return Double value for the first found key, or 0.0 if none found
     * @throws IllegalArgumentException If keys array is empty
     */
    public double getDouble(final String... keys) {
        if (keys.length == 0) throw new IllegalArgumentException("keys must not be empty");
        return ResourceConfigUtils.getAsDouble(ResourceConfigUtils.get(this.config, keys), keys[0]);
    }

    /**
     * Gets a double value from the configuration with a custom default.
     *
     * @param def Default value to return if key doesn't exist
     * @param key Configuration key
     * @return Double value for the specified key, or the default value if not found
     */
    public double getDouble(double def, final String key) {
        return ResourceConfigUtils.getAsDouble(this.config.getOrDefault(key, def), key);
    }

    /**
     * Gets a double value by trying multiple keys in order with a custom default.
     *
     * @param def Default value to return if none of the keys are found
     * @param keys Configuration keys to try in order
     * @return Double value for the first found key, or the default value if none found
     * @throws IllegalArgumentException If keys array is empty
     */
    public double getDouble(double def, final String... keys) {
        if (keys.length == 0) throw new IllegalArgumentException("keys must not be empty");
        return ResourceConfigUtils.getAsDouble(ResourceConfigUtils.getOrDefault(this.config, def, keys), keys[0]);
    }

    /**
     * Retrieves a configuration section for the specified key.
     * If the value is not a Map, returns null.
     *
     * @param key Configuration key to retrieve the section
     * @return ConfigSection object for the specified key, or null if the value is not a valid map
     * @throws ClassCastException If the stored value is not compatible with Map
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public ConfigSection getSection(final String key) {
        try {
            return of((Map<String, Object>) this.config.get(key));
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Retrieves a configuration section by trying multiple keys in order.
     * Returns the first non-null ConfigSection found, or null if none found.
     *
     * @param keys Configuration keys to try in order
     * @return First non-null ConfigSection found, or null if none
     * @throws IllegalArgumentException If keys array is empty
     */
    @Nullable
    public ConfigSection getSection(final String... keys) {
        if (keys.length == 0) throw new IllegalArgumentException("keys must not be empty");
        for (final String key : keys) {
            ConfigSection section = getSection(key);
            if (section != null) return section;
        }
        return null;
    }

    /**
     * Retrieves a raw Map object for the specified key.
     * Returns the map directly without wrapping it in a ConfigSection.
     *
     * @param key Configuration key to retrieve the map
     * @return Raw Map for the specified key, or null if not a valid map
     * @throws ClassCastException If the stored value is not compatible with Map
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMap(final String key) {
        try {
            return (Map<String, Object>) this.config.get(key);
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Retrieves a raw Map object by trying multiple keys in order.
     * Returns the first non-null Map found, or null if none found.
     *
     * @param keys Configuration keys to try in order
     * @return First non-null Map found, or null if none
     * @throws IllegalArgumentException If keys array is empty
     */
    @Nullable
    public Map<String, Object> getMap(final String... keys) {
        if (keys.length == 0) throw new IllegalArgumentException("keys must not be empty");
        for (final String key : keys) {
            Map<String, Object> map = getMap(key);
            if (map != null) return map;
        }
        return null;
    }
}
