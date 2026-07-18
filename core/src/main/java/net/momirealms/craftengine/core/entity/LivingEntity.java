package net.momirealms.craftengine.core.entity;

import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LivingEntity extends Entity {

    double health();

    void setHealth(double amount);

    double maxHealth();

    default void heal(double amount) {
        double targetHealth = Math.min(this.health() + amount, this.maxHealth());
        targetHealth = Math.max(targetHealth, health());
        this.setHealth(targetHealth);
    }

    double luck();

    void damage(double amount, Key damageType, @Nullable Object causeEntity);

    void addPotionEffect(Key potionEffectType, int duration, int amplifier, boolean ambient, boolean particles, boolean showIcon);

    void removePotionEffect(Key potionEffectType);

    void clearPotionEffects();

    @NotNull
    Item getItemInHand(InteractionHand hand);

    void setItemInHand(InteractionHand hand, Item item);

    boolean isSneaking();

    boolean isSwimming();

    boolean isClimbing();

    boolean isGliding();
}
