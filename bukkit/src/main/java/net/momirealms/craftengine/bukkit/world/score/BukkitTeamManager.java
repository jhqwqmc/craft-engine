package net.momirealms.craftengine.bukkit.world.score;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.util.LegacyChatFormatter;
import net.momirealms.craftengine.core.world.score.TeamManager;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class BukkitTeamManager implements TeamManager {
    private static BukkitTeamManager instance;
    private final BukkitCraftEngine plugin;
    private final Map<LegacyChatFormatter, Object> teamByColor = new EnumMap<>(LegacyChatFormatter.class);
    private List<Object> addTeamsPackets;

    public BukkitTeamManager(BukkitCraftEngine plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public static BukkitTeamManager instance() {
        return instance;
    }

    @Override
    public Object getTeamByColor(LegacyChatFormatter color) {
        return this.teamByColor.get(color);
    }

    @Override
    public List<Object> addTeamsPackets() {
        return this.addTeamsPackets;
    }

    @Override
    public void init() {
        Object scoreboard = FastNMS.INSTANCE.field$MinecraftServer$scoreboard();
        List<Object> packets = new ObjectArrayList<>();
        LegacyChatFormatter[] values = LegacyChatFormatter.values();
        for (int i = 0; i < 16; i++) {
            LegacyChatFormatter color = values[i];
            String teamName = TeamManager.createTeamName(color);
            Object team = FastNMS.INSTANCE.constructor$PlayerTeam(scoreboard, teamName);
            FastNMS.INSTANCE.method$PlayerTeam$setColor(team, color.name());
            this.teamByColor.put(color, team);
            packets.add(FastNMS.INSTANCE.method$ClientboundSetPlayerTeamPacket$createAddOrModifyPacket(team, true));
        }
        this.addTeamsPackets = packets;
    }
}
