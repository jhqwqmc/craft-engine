package net.momirealms.craftengine.bukkit.entity.data;

public class ArmorStandData<T> extends LivingEntityData<T> {
    public static final ArmorStandData<Byte> ArmorStandFlags = new ArmorStandData<>(ArmorStandData.class, EntityDataValue.Serializers$BYTE, (byte) 0);
    // rotations

    public ArmorStandData(Class<?> clazz, Object serializer, T defaultValue) {
        super(clazz, serializer, defaultValue);
    }
}