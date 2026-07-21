package net.momirealms.craftengine.core.plugin.text.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.stream.JsonToken;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.event.DataComponentValueConverterRegistry;
import net.kyori.adventure.text.serializer.gson.GsonDataComponentValue;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.GsonHelper;
import net.momirealms.craftengine.core.util.TagParser;
import net.momirealms.sparrow.nbt.EndTag;
import net.momirealms.sparrow.nbt.Tag;
import net.momirealms.sparrow.nbt.adventure.NBTDataComponentValue;
import net.momirealms.sparrow.reflection.clazz.SparrowClass;
import net.momirealms.sparrow.reflection.constructor.SConstructor2;
import net.momirealms.sparrow.reflection.constructor.matcher.ConstructorMatcher;
import net.momirealms.sparrow.reflection.field.matcher.FieldMatcher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NBTDataComponentConverter {
    private NBTDataComponentConverter() {}

    @SuppressWarnings("unchecked")
    public static void register() {
        Map<Class<?>, Map<Class<?>, Object>> CACHE = (Map<Class<?>, Map<Class<?>, Object>>) SparrowClass.of(SparrowClass.find("net.kyori.adventure.text.event.DataComponentValueConverterRegistry$ConversionCache"))
                .getDeclaredSparrowField(FieldMatcher.named("CACHE"))
                .mh()
                .get(null);

        DataComponentValueConverterRegistry.Conversion<NBTDataComponentValue.NBTDataComponentValueImpl, GsonDataComponentValue> convertor1 = DataComponentValueConverterRegistry.Conversion.convert(
                NBTDataComponentValue.NBTDataComponentValueImpl.class,
                GsonDataComponentValue.class,
                (key, srcValue) -> GsonDataComponentValue.gsonDataComponentValue(CraftEngine.instance().platform().sparrowNBTToJson(srcValue.tag()))
        );
        DataComponentValueConverterRegistry.Conversion<NBTDataComponentValue.RemovedNBTDataComponentValue, GsonDataComponentValue> convertor2 = DataComponentValueConverterRegistry.Conversion.convert(
                NBTDataComponentValue.RemovedNBTDataComponentValue.class,
                GsonDataComponentValue.class,
                (key, srcValue) -> GsonDataComponentValue.gsonDataComponentValue(JsonNull.INSTANCE)
        );
        DataComponentValueConverterRegistry.Conversion<BinaryTagHolder, GsonDataComponentValue> convertor3 = DataComponentValueConverterRegistry.Conversion.convert(
                BinaryTagHolder.class,
                GsonDataComponentValue.class,
                (key, srcValue) -> GsonDataComponentValue.gsonDataComponentValue(GsonHelper.get().fromJson(srcValue.toString(), JsonElement.class))
        );
        DataComponentValueConverterRegistry.Conversion<BinaryTagHolder, NBTDataComponentValue> convertor4 = DataComponentValueConverterRegistry.Conversion.convert(
                BinaryTagHolder.class,
                NBTDataComponentValue.class,
                (key, srcValue) -> {
                    try {
                        Tag tag = TagParser.parseTagFully(srcValue.string());
                        if (tag == EndTag.INSTANCE) {
                            return NBTDataComponentValue.removed();
                        } else {
                            return NBTDataComponentValue.nbtDataComponentValue(tag);
                        }
                    } catch (Exception e) {
                        CraftEngine.instance().logger().warn("Failed to parse NBTDataComponentValue from " + srcValue, e);
                        return NBTDataComponentValue.removed();
                    }
                }
        );
        SConstructor2 constructor = SparrowClass.of(SparrowClass.find("net.kyori.adventure.text.event.DataComponentValueConverterRegistry$RegisteredConversion"))
                .getDeclaredSparrowConstructor(ConstructorMatcher.takeArguments(Key.class, DataComponentValueConverterRegistry.Conversion.class))
                .asm$2();

        CACHE.computeIfAbsent(NBTDataComponentValue.NBTDataComponentValueImpl.class, $ -> new ConcurrentHashMap<>())
                .computeIfAbsent(GsonDataComponentValue.class, $ -> constructor.newInstance(Key.key("craftengine", "serializer/nbt"), convertor1));
        CACHE.computeIfAbsent(NBTDataComponentValue.RemovedNBTDataComponentValue.class, $ -> new ConcurrentHashMap<>())
                .computeIfAbsent(GsonDataComponentValue.class, $ -> constructor.newInstance(Key.key("craftengine", "serializer/nbt"), convertor2));
        CACHE.computeIfAbsent(SparrowClass.find("net.kyori.adventure.nbt.api.BinaryTagHolderImpl"), $ -> new ConcurrentHashMap<>())
                .computeIfAbsent(GsonDataComponentValue.class, $ -> constructor.newInstance(Key.key("craftengine", "serializer/nbt"), convertor3));
        CACHE.computeIfAbsent(SparrowClass.find("net.kyori.adventure.nbt.api.BinaryTagHolderImpl"), $ -> new ConcurrentHashMap<>())
                .computeIfAbsent(NBTDataComponentValue.class, $ -> constructor.newInstance(Key.key("craftengine", "serializer/nbt"), convertor4));
    }
}
