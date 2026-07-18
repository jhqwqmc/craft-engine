package net.momirealms.craftengine.bukkit.attribute;

import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.core.attribute.AbstractAttributeManager;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.Bukkit;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.List;

public final class BukkitAttributeManager extends AbstractAttributeManager {
    private final BukkitCraftEngine plugin;
    private final AttributeEventListener attributeEventListener;
    private final PaperAttributeEventListener paperAttributeEventListener;

    public BukkitAttributeManager(BukkitCraftEngine plugin) {
        super(plugin);
        this.plugin = plugin;
        this.attributeEventListener = new AttributeEventListener(this);
        this.paperAttributeEventListener = VersionHelper.hasPaperPatch ? new PaperAttributeEventListener() : null;
    }

    @Override
    public void delayedInit() {
        Bukkit.getPluginManager().registerEvents(this.attributeEventListener, this.plugin.javaPlugin());
        if (this.paperAttributeEventListener != null) {
            Bukkit.getPluginManager().registerEvents(this.paperAttributeEventListener, this.plugin.javaPlugin());
        }
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this.attributeEventListener);
        if (this.paperAttributeEventListener != null) {
            HandlerList.unregisterAll(this.paperAttributeEventListener);
        }
    }

    @Override
    protected List<Key> resolveEntities(Key tag) {
        Tag<EntityType> bukkitTag = Bukkit.getTag(Tag.REGISTRY_ENTITY_TYPES, KeyUtils.toNamespacedKey(tag), EntityType.class);
        if (bukkitTag == null) return List.of();
        List<Key> result = new ArrayList<>();
        for (EntityType type : bukkitTag.getValues()) {
            result.add(KeyUtils.namespacedKeyToKey(type.getKey()));
        }
        return result;
    }
}
