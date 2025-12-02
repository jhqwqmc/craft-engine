package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElementConfig;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBoxConfig;
import net.momirealms.craftengine.core.plugin.entityculling.CullingData;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Optional;

public record FurnitureVariant(String name,
                               @Nullable CullingData cullingData,
                               FurnitureElementConfig<?>[] elementConfigs,
                               FurnitureHitBoxConfig<?>[] hitBoxConfigs,
                               Optional<ExternalModel> externalModel,
                               Optional<Vector3f> dropOffset) {
}