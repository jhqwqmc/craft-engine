package net.momirealms.craftengine.core.world.score;

import net.momirealms.craftengine.core.plugin.Manageable;
import net.momirealms.craftengine.core.util.LegacyChatFormatter;

public interface TeamManager extends Manageable {
    String TEAM_PREFIX = "ce_";

    void setColorInUse(LegacyChatFormatter color);
}
