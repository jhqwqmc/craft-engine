package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.entity.Entity;

public interface ExternalModel {

    String plugin();

    String id();

    void bindModel(Entity entity);
}
