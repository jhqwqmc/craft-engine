package net.momirealms.craftengine.core.util;

import net.momirealms.craftengine.core.pack.mcmeta.PackVersion;

import java.util.HashMap;
import java.util.Map;

public final class MinecraftVersion implements Comparable<MinecraftVersion> {
    public static final Map<Integer, PackVersion> PACK_FORMATS = new HashMap<>();
    static {
        PACK_FORMATS.put(1_20_00, new PackVersion(15, 0));
        PACK_FORMATS.put(1_20_01, new PackVersion(15, 0));
        PACK_FORMATS.put(1_20_02, new PackVersion(18, 0));
        PACK_FORMATS.put(1_20_03, new PackVersion(22, 0));
        PACK_FORMATS.put(1_20_04, new PackVersion(22, 0));
        PACK_FORMATS.put(1_20_05, new PackVersion(32, 0));
        PACK_FORMATS.put(1_20_06, new PackVersion(32, 0));
        PACK_FORMATS.put(1_21_00, new PackVersion(34, 0));
        PACK_FORMATS.put(1_21_01, new PackVersion(34, 0));
        PACK_FORMATS.put(1_21_02, new PackVersion(42, 0));
        PACK_FORMATS.put(1_21_03, new PackVersion(42, 0));
        PACK_FORMATS.put(1_21_04, new PackVersion(46, 0));
        PACK_FORMATS.put(1_21_05, new PackVersion(55, 0));
        PACK_FORMATS.put(1_21_06, new PackVersion(63, 0));
        PACK_FORMATS.put(1_21_07, new PackVersion(64, 0));
        PACK_FORMATS.put(1_21_08, new PackVersion(64, 0));
        PACK_FORMATS.put(1_21_09, new PackVersion(69, 0));
        PACK_FORMATS.put(1_21_10, new PackVersion(69, 0));
        PACK_FORMATS.put(1_21_11, new PackVersion(75, 0));
        PACK_FORMATS.put(99_99_99, new PackVersion(1000, 0));
    }

    private final int version;
    private final String versionString;
    private final PackVersion packFormat;

    public static MinecraftVersion parse(final String version) {
        return new MinecraftVersion(version);
    }

    public String version() {
        return versionString;
    }

    public PackVersion packFormat() {
        return packFormat;
    }

    public MinecraftVersion(String version) {
        this.version = VersionHelper.parseVersionToInteger(version);
        this.versionString = version;
        this.packFormat = PACK_FORMATS.get(this.version);
    }

    public boolean isAtOrAbove(MinecraftVersion other) {
        return version >= other.version;
    }

    public boolean isAtOrBelow(MinecraftVersion other) {
        return version <= other.version;
    }

    public boolean isAt(MinecraftVersion other) {
        return version == other.version;
    }

    public boolean isBelow(MinecraftVersion other) {
        return version < other.version;
    }

    public boolean isAbove(MinecraftVersion other) {
        return version > other.version;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MinecraftVersion that)) return false;
        return version == that.version;
    }

    @Override
    public int hashCode() {
        return version;
    }

    @Override
    public int compareTo(MinecraftVersion other) {
        return Integer.compare(this.version, other.version);
    }
}
