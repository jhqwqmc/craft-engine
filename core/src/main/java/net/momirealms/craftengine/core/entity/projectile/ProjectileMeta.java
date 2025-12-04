package net.momirealms.craftengine.core.entity.projectile;

import net.momirealms.craftengine.core.entity.display.Billboard;
import net.momirealms.craftengine.core.entity.display.ItemDisplayContext;
import net.momirealms.craftengine.core.util.Key;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public record ProjectileMeta(Key item,
                             ItemDisplayContext displayType,
                             Billboard billboard,
                             Vector3f scale,
                             Vector3f translation,
                             Quaternionf rotation,
                             double range) {
}
