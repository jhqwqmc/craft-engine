package net.momirealms.craftengine.bukkit.util;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;

import java.util.Objects;
import java.util.Optional;

public final class LegacyAttributeUtils {
    private LegacyAttributeUtils() {}

    public static void setMaxHealth(ArmorStand entity) {
        Objects.requireNonNull(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(0.01);
    }

    public static double getMaxHealth(LivingEntity entity) {
        return Objects.requireNonNull(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
    }

    public static double getLuck(LivingEntity entity) {
        return Optional.ofNullable(entity.getAttribute(Attribute.GENERIC_LUCK)).map(AttributeInstance::getValue).orElse(1d);
    }
}
