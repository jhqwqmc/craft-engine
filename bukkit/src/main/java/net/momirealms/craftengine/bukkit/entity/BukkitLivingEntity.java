package net.momirealms.craftengine.bukkit.entity;

import net.momirealms.craftengine.bukkit.item.BukkitItem;
import net.momirealms.craftengine.bukkit.util.ItemStackUtils;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.bukkit.util.LegacyAttributeUtils;
import net.momirealms.craftengine.bukkit.util.RegistryUtils;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.proxy.minecraft.core.registries.BuiltInRegistriesProxy;
import net.momirealms.craftengine.proxy.minecraft.world.effect.MobEffectInstanceProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.EquipmentSlotProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.LivingEntityProxy;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;

public class BukkitLivingEntity extends BukkitEntity implements net.momirealms.craftengine.core.entity.LivingEntity {

    protected BukkitLivingEntity(WeakReference<Object> entity) {
        super(entity);
    }

    public BukkitLivingEntity(Object entity) {
        super(entity);
    }

    @Override
    public double health() {
        return platformEntity().getHealth();
    }

    @Override
    public void setHealth(double amount) {
        platformEntity().setHealth(amount);
    }

    @Override
    public double maxHealth() {
        if (VersionHelper.isOrAbove1_21) {
            return Objects.requireNonNull(platformEntity().getAttribute(Attribute.MAX_HEALTH)).getValue();
        } else {
            return LegacyAttributeUtils.getMaxHealth(platformEntity());
        }
    }

    @Override
    public double luck() {
        if (VersionHelper.isOrAbove1_21_3) {
            return Optional.ofNullable(platformEntity().getAttribute(Attribute.LUCK)).map(AttributeInstance::getValue).orElse(1d);
        } else {
            return LegacyAttributeUtils.getLuck(platformEntity());
        }
    }

    @Override
    public void damage(double amount, Key damageType, @Nullable net.momirealms.craftengine.core.entity.Entity causeEntity) {
        @SuppressWarnings("deprecation")
        DamageType type = Registry.DAMAGE_TYPE.get(KeyUtils.toNamespacedKey(damageType));
        DamageSource source = DamageSource.builder(type != null ? type : DamageType.GENERIC)
                .withCausingEntity(causeEntity != null ? (Entity) causeEntity.platformEntity() : this.platformEntity())
                .withDirectEntity(this.platformEntity())
                .withDamageLocation(this.platformEntity().getLocation())
                .build();
        this.platformEntity().damage(amount, source);
    }

    @Override
    public void addPotionEffect(Key potionEffectType, int duration, int amplifier, boolean ambient, boolean particles, boolean showIcon) {
        if (VersionHelper.isOrAbove1_20_5) {
            Object holder = RegistryUtils.getHolderById(BuiltInRegistriesProxy.MOB_EFFECT, KeyUtils.toIdentifier(potionEffectType));
            if (holder != null) {
                Object mobEffect = MobEffectInstanceProxy.INSTANCE.newInstance(holder, duration, amplifier, ambient, particles, showIcon);
                LivingEntityProxy.INSTANCE.addEffect(minecraftEntity(), mobEffect);
            }
        } else {
            Object mobEffect = RegistryUtils.getRegistryValue(BuiltInRegistriesProxy.MOB_EFFECT, KeyUtils.toIdentifier(potionEffectType));
            if (mobEffect != null) {
                LivingEntityProxy.INSTANCE.addEffect(minecraftEntity(), MobEffectInstanceProxy.INSTANCE.newInstance$legacy(mobEffect, duration, amplifier, ambient, particles, showIcon));
            }
        }
    }

    @Override
    public void removePotionEffect(Key potionEffectType) {
        if (VersionHelper.isOrAbove1_20_5) {
            Object holder = RegistryUtils.getHolderById(BuiltInRegistriesProxy.MOB_EFFECT, KeyUtils.toIdentifier(potionEffectType));
            if (holder != null) {
                LivingEntityProxy.INSTANCE.removeEffect(minecraftEntity(), holder);
            }
        } else {
            Object mobEffect = RegistryUtils.getRegistryValue(BuiltInRegistriesProxy.MOB_EFFECT, KeyUtils.toIdentifier(potionEffectType));
            if (mobEffect != null) {
                LivingEntityProxy.INSTANCE.removeEffect$legacy(minecraftEntity(), mobEffect);
            }
        }
    }

    @Override
    public void clearPotionEffects() {
        LivingEntityProxy.INSTANCE.removeAllEffects(minecraftEntity());
    }

    @NotNull
    @Override
    public BukkitItem getItemInHand(InteractionHand hand) {
        return ItemStackUtils.wrap(LivingEntityProxy.INSTANCE.getItemBySlot(minecraftEntity(), hand == InteractionHand.MAIN_HAND ? EquipmentSlotProxy.MAINHAND : EquipmentSlotProxy.OFFHAND));
    }

    @Override
    public void setItemInHand(InteractionHand hand, Item item) {
        LivingEntityProxy.INSTANCE.setItemSlot(minecraftEntity(), hand == InteractionHand.MAIN_HAND ? EquipmentSlotProxy.MAINHAND : EquipmentSlotProxy.OFFHAND, item.minecraftItem());
    }

    @Override
    public LivingEntity platformEntity() {
        return (LivingEntity) super.platformEntity();
    }

    @Override
    public boolean isSneaking() {
        return platformEntity().isSneaking();
    }

    @Override
    public boolean isSwimming() {
        return platformEntity().isSwimming();
    }

    @Override
    public boolean isClimbing() {
        return platformEntity().isClimbing();
    }

    @Override
    public boolean isGliding() {
        return platformEntity().isGliding();
    }
}
