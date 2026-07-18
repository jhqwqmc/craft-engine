package net.momirealms.craftengine.bukkit.attribute;

import net.momirealms.craftengine.bukkit.util.EntityUtils;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.core.attribute.DamageSource;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.proxy.minecraft.world.damagesource.DamageSourceProxy;
import org.jetbrains.annotations.Nullable;

public final class BukkitDamageSource implements DamageSource {
    private final Object source;

    public BukkitDamageSource(Object source) {
        this.source = source;
    }

    public Key type() {
        return KeyUtils.unwrapHolder(DamageSourceProxy.INSTANCE.getType(this.source));
    }

    @Override
    public boolean isCritical() {
        return DamageSourceProxy.INSTANCE.isCritical(this.source);
    }

    @Nullable
    @Override
    public Entity causingEntity() {
        Object causingEntity = DamageSourceProxy.INSTANCE.getCausingEntity(this.source);
        if (causingEntity == null) {
            return null;
        }
        return EntityUtils.adaptNMS(causingEntity);
    }

    public boolean isDirect() {
        return DamageSourceProxy.INSTANCE.getDirectEntity(this.source) == DamageSourceProxy.INSTANCE.getCausingEntity(this.source);
    }

    @Nullable
    @Override
    public Entity directEntity() {
        Object directEntity = DamageSourceProxy.INSTANCE.getDirectEntity(this.source);
        if (directEntity == null) {
            return null;
        }
        return EntityUtils.adaptNMS(directEntity);
    }
}
