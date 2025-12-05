package net.momirealms.craftengine.bukkit.world.score;

import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.reflection.bukkit.CraftBukkitReflections;
import net.momirealms.craftengine.core.util.LegacyChatFormatter;
import net.momirealms.craftengine.core.world.score.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BukkitTeamManager implements TeamManager {
    private static BukkitTeamManager instance;
    private final BukkitCraftEngine plugin;
    protected Set<LegacyChatFormatter> colorsInUse = new HashSet<>();
    private final Map<LegacyChatFormatter, Object> teamByColor = new EnumMap<>(LegacyChatFormatter.class);
    private boolean changed = false;

    public BukkitTeamManager(BukkitCraftEngine plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public static BukkitTeamManager instance() {
        return instance;
    }

    @Override
    public void setColorInUse(LegacyChatFormatter color) {
        this.colorsInUse.add(color);
        this.changed = true;
    }

    @Override
    public void unload() {
        this.changed = !this.colorsInUse.isEmpty();
        this.colorsInUse.clear();
        this.teamByColor.clear();
    }

    @Nullable
    public Object getTeamByColor(LegacyChatFormatter color) {
        return this.teamByColor.get(color);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void runDelayedSyncTasks() {
        if (!this.changed) {
            return;
        }
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        for (LegacyChatFormatter color : LegacyChatFormatter.values()) {
            Team team = scoreboard.getTeam(TEAM_PREFIX + color.name().toLowerCase(Locale.ROOT));
            if (this.colorsInUse.contains(color)) {
                if (team == null) {
                    team = scoreboard.registerNewTeam(TEAM_PREFIX + color.name().toLowerCase(Locale.ROOT));
                    team.setColor(ChatColor.valueOf(color.name()));
                }
                try {
                    Object nmsTeam = CraftBukkitReflections.field$CraftTeam$team.get(team);
                    this.teamByColor.put(color, nmsTeam);
                } catch (ReflectiveOperationException e) {
                    this.plugin.logger().warn("Could not get nms team", e);
                }
            } else {
                if (team != null) {
                    team.unregister();
                }
            }
        }
    }
}
