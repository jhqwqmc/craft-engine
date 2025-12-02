package net.momirealms.craftengine.core.entity.furniture.hitbox;

import java.util.Map;

public interface FurnitureHitBoxConfigFactory<H extends FurnitureHitBox> {

    FurnitureHitBoxConfig<H> create(Map<String, Object> arguments);
}
