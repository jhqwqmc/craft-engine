package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.entity.Billboard;
import net.momirealms.craftengine.core.entity.ItemDisplayContext;
import net.momirealms.craftengine.core.util.Key;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public abstract class AbstractFurnitureElement implements FurnitureElement {
    private final Key item;
    private final Billboard billboard;
    private final ItemDisplayContext transform;
    private final Vector3f scale;
    private final Vector3f translation;
    private final Vector3f position;
    private final Quaternionf rotation;
    private final boolean applyDyedColor;
    private final float shadowRadius;
    private final float shadowStrength;

    public AbstractFurnitureElement(Key item,
                                    Billboard billboard,
                                    ItemDisplayContext transform,
                                    Vector3f scale,
                                    Vector3f translation,
                                    Vector3f position,
                                    Quaternionf rotation,
                                    float shadowRadius,
                                    float shadowStrength,
                                    boolean applyDyedColor) {
        this.billboard = billboard;
        this.transform = transform;
        this.scale = scale;
        this.translation = translation;
        this.item = item;
        this.rotation = rotation;
        this.position = position;
        this.applyDyedColor = applyDyedColor;
        this.shadowRadius = shadowRadius;
        this.shadowStrength = shadowStrength;
    }

    @Override
    public float shadowRadius() {
        return shadowRadius;
    }

    @Override
    public float shadowStrength() {
        return shadowStrength;
    }

    @Override
    public boolean applyDyedColor() {
        return applyDyedColor;
    }

    @Override
    public Quaternionf rotation() {
        return rotation;
    }

    @Override
    public Key item() {
        return item;
    }

    @Override
    public Billboard billboard() {
        return billboard;
    }

    @Override
    public ItemDisplayContext transform() {
        return transform;
    }

    @Override
    public Vector3f scale() {
        return scale;
    }

    @Override
    public Vector3f translation() {
        return translation;
    }

    @Override
    public Vector3f position() {
        return position;
    }
}
