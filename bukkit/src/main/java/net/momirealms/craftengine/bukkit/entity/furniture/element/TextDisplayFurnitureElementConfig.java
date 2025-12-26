package net.momirealms.craftengine.bukkit.entity.furniture.element;

import net.momirealms.craftengine.bukkit.entity.data.TextDisplayEntityData;
import net.momirealms.craftengine.bukkit.util.ComponentUtils;
import net.momirealms.craftengine.core.entity.display.Billboard;
import net.momirealms.craftengine.core.entity.display.ItemDisplayContext;
import net.momirealms.craftengine.core.entity.display.TextDisplayAlignment;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElementConfig;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElementConfigFactory;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.NetworkTextReplaceContext;
import net.momirealms.craftengine.core.plugin.context.PlayerContext;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.Color;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class TextDisplayFurnitureElementConfig implements FurnitureElementConfig<TextDisplayFurnitureElement> {
    public static final Factory FACTORY = new Factory();
    public final Function<Player, List<Object>> metadata;
    public final String text;
    public final Vector3f scale;
    public final Vector3f position;
    public final Vector3f translation;
    public final float xRot;
    public final float yRot;
    public final Quaternionf rotation;
    public final ItemDisplayContext displayContext;
    public final Billboard billboard;
    public final float shadowRadius;
    public final float shadowStrength;
    public final Color glowColor;
    public final int blockLight;
    public final int skyLight;
    public final float viewRange;
    public final int lineWidth;
    public final int backgroundColor;
    public final byte opacity;
    public final boolean hasShadow;
    public final boolean isSeeThrough;
    public final boolean useDefaultBackgroundColor;
    public final TextDisplayAlignment alignment;
    public final Predicate<PlayerContext> predicate;
    public final boolean hasCondition;

    public TextDisplayFurnitureElementConfig(String text,
                                             Vector3f scale,
                                             Vector3f position,
                                             Vector3f translation,
                                             float xRot,
                                             float yRot,
                                             Quaternionf rotation,
                                             ItemDisplayContext displayContext,
                                             Billboard billboard,
                                             float shadowRadius,
                                             float shadowStrength,
                                             @Nullable Color glowColor,
                                             int blockLight,
                                             int skyLight,
                                             float viewRange,
                                             int lineWidth,
                                             int backgroundColor,
                                             byte opacity,
                                             boolean hasShadow,
                                             boolean isSeeThrough,
                                             boolean useDefaultBackgroundColor,
                                             TextDisplayAlignment alignment,
                                             Predicate<PlayerContext> predicate,
                                             boolean hasCondition) {
        this.text = text;
        this.scale = scale;
        this.position = position;
        this.translation = translation;
        this.xRot = xRot;
        this.yRot = yRot;
        this.rotation = rotation;
        this.displayContext = displayContext;
        this.billboard = billboard;
        this.shadowRadius = shadowRadius;
        this.shadowStrength = shadowStrength;
        this.glowColor = glowColor;
        this.blockLight = blockLight;
        this.skyLight = skyLight;
        this.viewRange = viewRange;
        this.lineWidth = lineWidth;
        this.backgroundColor = backgroundColor;
        this.opacity = opacity;
        this.hasShadow = hasShadow;
        this.useDefaultBackgroundColor = useDefaultBackgroundColor;
        this.alignment = alignment;
        this.isSeeThrough = isSeeThrough;
        this.hasCondition = hasCondition;
        this.predicate = predicate;
        this.metadata = (player) -> {
            List<Object> dataValues = new ArrayList<>();
            if (glowColor != null) {
                TextDisplayEntityData.SharedFlags.addEntityData((byte) 0x40, dataValues);
                TextDisplayEntityData.GlowColorOverride.addEntityData(glowColor.color(), dataValues);
            }
            TextDisplayEntityData.Scale.addEntityDataIfNotDefaultValue(this.scale, dataValues);
            TextDisplayEntityData.RotationLeft.addEntityDataIfNotDefaultValue(this.rotation, dataValues);
            TextDisplayEntityData.BillboardConstraints.addEntityDataIfNotDefaultValue(this.billboard.id(), dataValues);
            TextDisplayEntityData.Translation.addEntityDataIfNotDefaultValue(this.translation, dataValues);
            TextDisplayEntityData.ShadowRadius.addEntityDataIfNotDefaultValue(this.shadowRadius, dataValues);
            TextDisplayEntityData.ShadowStrength.addEntityDataIfNotDefaultValue(this.shadowStrength, dataValues);
            TextDisplayEntityData.Text.addEntityData(ComponentUtils.adventureToMinecraft(AdventureHelper.miniMessage().deserialize(this.text, NetworkTextReplaceContext.of(player).tagResolvers())), dataValues);
            TextDisplayEntityData.LineWidth.addEntityDataIfNotDefaultValue(this.lineWidth, dataValues);
            TextDisplayEntityData.BackgroundColor.addEntityDataIfNotDefaultValue(this.backgroundColor, dataValues);
            TextDisplayEntityData.TextOpacity.addEntityDataIfNotDefaultValue(this.opacity, dataValues);
            TextDisplayEntityData.TextDisplayMasks.addEntityDataIfNotDefaultValue(TextDisplayEntityData.encodeMask(this.hasShadow, this.isSeeThrough, this.useDefaultBackgroundColor, this.alignment), dataValues);
            if (this.blockLight != -1 && this.skyLight != -1) {
                TextDisplayEntityData.BrightnessOverride.addEntityData(this.blockLight << 4 | this.skyLight << 20, dataValues);
            }
            TextDisplayEntityData.ViewRange.addEntityDataIfNotDefaultValue((float) (this.viewRange * player.displayEntityViewDistance()), dataValues);
            return dataValues;
        };
    }

    @Override
    public TextDisplayFurnitureElement create(@NotNull Furniture furniture) {
        return new TextDisplayFurnitureElement(furniture, this);
    }

    public static class Factory implements FurnitureElementConfigFactory<TextDisplayFurnitureElement> {

        @Override
        public TextDisplayFurnitureElementConfig create(Map<String, Object> arguments) {
            Map<String, Object> brightness = ResourceConfigUtils.getAsMap(arguments.getOrDefault("brightness", Map.of()), "brightness");
            List<Condition<PlayerContext>> conditions = ResourceConfigUtils.parseConfigAsList(arguments.get("conditions"), CommonConditions::fromMap);
            return new TextDisplayFurnitureElementConfig(
                    ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("text"), "warning.config.furniture.element.text_display.missing_text"),
                    ResourceConfigUtils.getAsVector3f(arguments.getOrDefault("scale", 1f), "scale"),
                    ResourceConfigUtils.getAsVector3f(arguments.getOrDefault("position", 0f), "position"),
                    ResourceConfigUtils.getAsVector3f(arguments.get("translation"), "translation"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("pitch", 0f), "pitch"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("yaw", 0f), "yaw"),
                    ResourceConfigUtils.getAsQuaternionf(arguments.getOrDefault("rotation", 0f), "rotation"),
                    ResourceConfigUtils.getAsEnum(ResourceConfigUtils.get(arguments, "display-context", "display-transform"), ItemDisplayContext.class, ItemDisplayContext.NONE),
                    ResourceConfigUtils.getAsEnum(arguments.get("billboard"), Billboard.class, Billboard.FIXED),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("shadow-radius", 0f), "shadow-radius"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("shadow-strength", 1f), "shadow-strength"),
                    Optional.ofNullable(arguments.get("glow-color")).map(it -> Color.fromStrings(it.toString().split(","))).orElse(null),
                    ResourceConfigUtils.getAsInt(brightness.getOrDefault("block-light", -1), "block-light"),
                    ResourceConfigUtils.getAsInt(brightness.getOrDefault("sky-light", -1), "sky-light"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("view-range", 1f), "view-range"),
                    ResourceConfigUtils.getAsInt(arguments.getOrDefault("line-width", 200), "line-width"),
                    ResourceConfigUtils.getOrDefault(arguments.get("background-color"), o -> Color.fromStrings(o.toString().split(",")).color(), 0x40000000),
                    (byte) ResourceConfigUtils.getAsInt(arguments.getOrDefault("text-opacity", -1), "text-opacity"),
                    ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("has-shadow", false), "has-shadow"),
                    ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("is-see-through", false), "is-see-through"),
                    ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("use-default-background-color", false), "use-default-background-color"),
                    ResourceConfigUtils.getAsEnum(arguments.get("alignment"), TextDisplayAlignment.class, TextDisplayAlignment.CENTER),
                    MiscUtils.allOf(conditions),
                    !conditions.isEmpty()
            );
        }
    }
}
