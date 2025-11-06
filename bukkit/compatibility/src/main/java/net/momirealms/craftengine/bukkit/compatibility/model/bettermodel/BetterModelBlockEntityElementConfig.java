package net.momirealms.craftengine.bukkit.compatibility.model.bettermodel;

import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElement;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfig;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfigFactory;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.World;
import org.bukkit.Bukkit;
import org.joml.Vector3f;

import java.util.Map;

public class BetterModelBlockEntityElementConfig implements BlockEntityElementConfig<BetterModelBlockEntityElement> {
    private final Vector3f position;
    private final float yaw;
    private final float pitch;
    private final String model;
    private final float viewRange;
    private final boolean sightTrace;
    private final boolean shadow;

    public BetterModelBlockEntityElementConfig(String model,
                                               Vector3f position,
                                               float yaw,
                                               float pitch,
                                               float viewRange,
                                               boolean sightTrace,
                                               boolean shadow) {
        this.pitch = pitch;
        this.position = position;
        this.yaw = yaw;
        this.model = model;
        this.viewRange = viewRange;
        this.sightTrace = sightTrace;
        this.shadow = shadow;
    }

    public String model() {
        return this.model;
    }

    public float pitch() {
        return this.pitch;
    }

    public Vector3f position() {
        return this.position;
    }

    public float yaw() {
        return this.yaw;
    }

    public float viewRange() {
        return this.viewRange;
    }

    public boolean sightTrace() {
        return this.sightTrace;
    }

    public boolean shadow() {
        return this.shadow;
    }

    @Override
    public BetterModelBlockEntityElement create(World world, BlockPos pos) {
        return new BetterModelBlockEntityElement(world, pos, this);
    }

    @Override
    public Class<BetterModelBlockEntityElement> elementClass() {
        return BetterModelBlockEntityElement.class;
    }

    public static class Factory implements BlockEntityElementConfigFactory {

        @SuppressWarnings("unchecked")
        @Override
        public <E extends BlockEntityElement> BlockEntityElementConfig<E> create(Map<String, Object> arguments) {
            String model = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("model"), "warning.config.block.state.entity_renderer.better_model.missing_model");
            return (BlockEntityElementConfig<E>) new BetterModelBlockEntityElementConfig(
                    model,
                    ResourceConfigUtils.getAsVector3f(arguments.getOrDefault("position", 0.5f), "position"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("yaw", 0f), "yaw"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("pitch", 0f), "pitch"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("view-range", (float) Bukkit.getViewDistance() / 4.0F), "view-range"),
                    ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("sight-trace", true), "sight-trace"),
                    ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("shadow", true), "shadow")
            );
        }
    }
}
