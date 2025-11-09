package net.momirealms.craftengine.bukkit.entity.data;

import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;

public class ItemEntityData<T> extends BaseEntityData<T> {
    public static final ItemEntityData<Object> Item = new ItemEntityData<>(ItemEntityData.class, EntityDataValue.Serializers$ITEM_STACK, CoreReflections.instance$ItemStack$EMPTY);

    public ItemEntityData(Class<?> clazz, Object serializer, T defaultValue) {
        super(clazz, serializer, defaultValue);
    }
}
