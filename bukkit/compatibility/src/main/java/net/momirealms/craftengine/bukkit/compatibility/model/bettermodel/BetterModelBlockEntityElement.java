package net.momirealms.craftengine.bukkit.compatibility.model.bettermodel;

import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.data.renderer.ModelRenderer;
import kr.toxicity.model.api.tracker.DummyTracker;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElement;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.World;
import org.bukkit.Location;
import org.joml.Vector3f;

public class BetterModelBlockEntityElement implements BlockEntityElement {
    private DummyTracker dummyTracker;
    private final Location location;
    private final BetterModelBlockEntityElementConfig config;

    public BetterModelBlockEntityElement(World world, BlockPos pos, BetterModelBlockEntityElementConfig config) {
        this.config = config;
        Vector3f position = config.position();
        this.location = new Location((org.bukkit.World) world.platformWorld(), pos.x() + position.x, pos.y() + position.y, pos.z() + position.z, config.yaw(), config.pitch());
        this.dummyTracker = createDummyTracker();
    }

    private DummyTracker createDummyTracker() {
        ModelRenderer modelRenderer = BetterModel.plugin().modelManager().renderer(this.config.model());
        if (modelRenderer == null) {
            return null;
        } else {
            return modelRenderer.create(this.location);
        }
    }

    @Override
    public void hide(Player player) {
        if (this.dummyTracker != null) {
            this.dummyTracker.remove((org.bukkit.entity.Player) player.platformPlayer());
        }
    }

    @Override
    public void show(Player player) {
        if (this.dummyTracker != null) {
            this.dummyTracker.spawn((org.bukkit.entity.Player) player.platformPlayer());
        }
    }

    @Override
    public void deactivate() {
        if (this.dummyTracker != null) {
            this.dummyTracker.close();
            this.dummyTracker = null;
        }
    }

    @Override
    public void activate() {
        if (this.dummyTracker == null) {
            this.dummyTracker = createDummyTracker();
        }
    }
}
