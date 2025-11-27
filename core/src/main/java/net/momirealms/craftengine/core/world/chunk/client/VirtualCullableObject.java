package net.momirealms.craftengine.core.world.chunk.client;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.world.Cullable;

public class VirtualCullableObject {
    private final Cullable cullable;
    private boolean isShown;

    public VirtualCullableObject(Cullable cullable) {
        this.cullable = cullable;
        this.isShown = false;
    }

    public Cullable cullable() {
        return cullable;
    }

    public boolean isShown() {
        return isShown;
    }

    public void setShown(Player player, boolean shown) {
        this.isShown = shown;
        if (shown) {
            this.cullable.show(player);
        } else {
            this.cullable.hide(player);
        }
    }
}
