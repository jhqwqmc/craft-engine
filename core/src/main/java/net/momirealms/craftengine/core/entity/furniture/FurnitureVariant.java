package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElementConfig;
import net.momirealms.craftengine.core.entity.furniture.hitbox.HitBoxConfig;
import org.joml.Vector3f;

import java.util.Optional;

public record FurnitureVariant(FurnitureElementConfig<?>[] elements,
                               HitBoxConfig[] hitBoxConfigs,
                               Optional<ExternalModel> externalModel,
                               Optional<Vector3f> dropOffset) {
}