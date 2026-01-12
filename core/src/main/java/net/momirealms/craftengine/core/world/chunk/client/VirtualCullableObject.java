package net.momirealms.craftengine.core.world.chunk.client;

import net.momirealms.craftengine.core.entity.Cullable;
import net.momirealms.craftengine.core.entity.player.Player;

public final class VirtualCullableObject {
    public Cullable cullable;
    public boolean isShown;

    public VirtualCullableObject(Cullable cullable) {
        this.cullable = cullable;
        this.isShown = false;
    }

    public void setCullable(Cullable cullable) {
        this.cullable = cullable;
    }

    public Cullable cullable() {
        return cullable;
    }

    public boolean isShown() {
        return isShown;
    }

    public void setShown(Player player, boolean shown) {
        if (this.isShown == shown) return;
        this.isShown = shown;
        if (shown) {
            this.cullable.show(player);
        } else {
            this.cullable.hide(player);
        }
    }
}
