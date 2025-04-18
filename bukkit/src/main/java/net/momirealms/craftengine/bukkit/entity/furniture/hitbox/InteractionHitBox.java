package net.momirealms.craftengine.bukkit.entity.furniture.hitbox;

import net.momirealms.craftengine.bukkit.entity.data.InteractionEntityData;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.entity.furniture.*;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class InteractionHitBox extends AbstractHitBox {
    public static final Factory FACTORY = new Factory();
    public static final InteractionHitBox DEFAULT = new InteractionHitBox(new Seat[0], new Vector3f(), new Vector3f(1,1,1), true);

    private final Vector3f size;
    private final boolean responsive;
    private final List<Object> cachedValues = new ArrayList<>();

    public InteractionHitBox(Seat[] seats, Vector3f position, Vector3f size, boolean responsive) {
        super(seats, position);
        this.size = size;
        this.responsive = responsive;
        InteractionEntityData.Height.addEntityDataIfNotDefaultValue(size.y, cachedValues);
        InteractionEntityData.Width.addEntityDataIfNotDefaultValue(size.x, cachedValues);
        InteractionEntityData.Responsive.addEntityDataIfNotDefaultValue(responsive, cachedValues);
    }

    public boolean responsive() {
        return responsive;
    }

    public Vector3f size() {
        return size;
    }

    @Override
    public Key type() {
        return HitBoxTypes.INTERACTION;
    }

    @Override
    public void initPacketsAndColliders(int[] entityId, double x, double y, double z, float yaw, Quaternionf conjugated, BiConsumer<Object, Boolean> packets, Consumer<Collider> collider) {
        Vector3f offset = conjugated.transform(new Vector3f(position()));
        packets.accept(FastNMS.INSTANCE.constructor$ClientboundAddEntityPacket(
                entityId[0], UUID.randomUUID(), x + offset.x, y + offset.y, z - offset.z, 0, yaw,
                Reflections.instance$EntityType$INTERACTION, 0, Reflections.instance$Vec3$Zero, 0
        ), true);
        packets.accept(FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(entityId[0], List.copyOf(this.cachedValues)), true);
    }

    @Override
    public int[] acquireEntityIds(Supplier<Integer> entityIdSupplier) {
        return new int[] {entityIdSupplier.get()};
    }

    public static class Factory implements HitBoxFactory {

        @Override
        public HitBox create(Map<String, Object> arguments) {
            Vector3f position = MiscUtils.getVector3f(arguments.getOrDefault("position", "0"));
            float width;
            float height;
            if (arguments.containsKey("scale")) {
                String[] split = arguments.get("scale").toString().split(",");
                width = Float.parseFloat(split[0]);
                height = Float.parseFloat(split[1]);
            } else {
                width = MiscUtils.getAsFloat(arguments.getOrDefault("width", "1"));
                height = MiscUtils.getAsFloat(arguments.getOrDefault("height", "1"));
            }
            return new InteractionHitBox(
                    HitBoxFactory.getSeats(arguments),
                    position,
                    new Vector3f(width, height, width),
                    (boolean) arguments.getOrDefault("interactive", true)
            );
        }
    }
}
