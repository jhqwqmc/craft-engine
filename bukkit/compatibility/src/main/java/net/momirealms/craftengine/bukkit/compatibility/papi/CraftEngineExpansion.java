package net.momirealms.craftengine.bukkit.compatibility.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptors;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.CooldownData;
import net.momirealms.craftengine.core.util.DurationFormatter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CraftEngineExpansion extends PlaceholderExpansion {
    private final CraftEngine plugin;

    public CraftEngineExpansion(CraftEngine plugin) {
        this.plugin = plugin;
    }

    @NotNull
    @Override
    public String getIdentifier() {
        return "ce";
    }

    @NotNull
    @Override
    public String getAuthor() {
        return "jhqwqmc";
    }

    @NotNull
    @Override
    public String getVersion() {
        return "1.0";
    }

    /**
     * 用法:（小括号括起来的为必填，中括号括起来的为选填）
     * <p>
     * %ce_cd_(key)|[format]%
     */
    @Override
    public @Nullable String onPlaceholderRequest(Player bukkitPlayer, @NotNull String params) {
        BukkitServerPlayer player = bukkitPlayer != null ? BukkitAdaptors.adapt(bukkitPlayer) : null;
        int index = params.indexOf('_');
        String action = index > 0 ? params.substring(0, index) : params;
        String[] param;
        if (index > 0) {
            String substring = params.substring(index + 1);
            int i = substring.indexOf('|');
            if (i > 0) {
                param = new String[]{substring.substring(0, i), substring.substring(i + 1)};
            } else {
                param = new String[]{substring};
            }
        } else {
            param = new String[0];
        }
        return switch (action) {
            case "cd", "cooldown" -> handleCooldown(player, param);
            default -> null;
        };
    }

    @Nullable
    private static String handleCooldown(@Nullable BukkitServerPlayer player, String[] param) {
        if (player == null || param.length < 1) {
            return null;
        }
        CooldownData cooldown = player.cooldown();
        if (cooldown == null) {
            return null;
        }
        Long ms = cooldown.getExpirationTime(param[0]);
        if (ms == null) {
            return "-1";
        }
        ms -= System.currentTimeMillis();
        if (param.length >= 2) {
            return DurationFormatter.of(param[1]).format(ms);
        }
        return String.valueOf(ms);
    }
}
