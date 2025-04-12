package net.momirealms.craftengine.bukkit.util;

import com.google.gson.JsonElement;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.core.util.AdventureHelper;

public class ComponentUtils {

    private ComponentUtils() {}

    public static Object adventureToMinecraft(Component component) {
        return jsonElementToMinecraft(AdventureHelper.componentToJsonElement(component));
    }

    public static Object jsonElementToMinecraft(JsonElement json) {
        return FastNMS.INSTANCE.method$Component$Serializer$fromJson(json);
    }

    public static Object jsonToMinecraft(String json) {
        return FastNMS.INSTANCE.method$Component$Serializer$fromJson(json);
    }

    public static String minecraftToJson(Object component) {
        return FastNMS.INSTANCE.method$Component$Serializer$toJson(component);
    }

    public static String paperAdventureToJson(Object component) {
        return Reflections.instance$GsonComponentSerializer$Gson.toJson(component);
    }

    public static Object jsonToPaperAdventure(String json) {
        return Reflections.instance$GsonComponentSerializer$Gson.fromJson(json, Reflections.clazz$AdventureComponent);
    }

    public static JsonElement paperAdventureToJsonElement(Object component) {
        return Reflections.instance$GsonComponentSerializer$Gson.toJsonTree(component);
    }

    public static Object jsonElementToPaperAdventure(JsonElement json) {
        return Reflections.instance$GsonComponentSerializer$Gson.fromJson(json, Reflections.clazz$AdventureComponent);
    }
}
