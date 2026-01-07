package net.momirealms.craftengine.bukkit.block.entity.renderer.element;

import com.google.common.base.Objects;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptors;
import net.momirealms.craftengine.bukkit.entity.data.TextDisplayEntityData;
import net.momirealms.craftengine.bukkit.util.ComponentUtils;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfig;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfigFactory;
import net.momirealms.craftengine.core.entity.display.Billboard;
import net.momirealms.craftengine.core.entity.display.TextDisplayAlignment;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.plugin.context.NetworkTextReplaceContext;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.Color;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.World;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Function;

public class TextDisplayBlockEntityElementConfig implements BlockEntityElementConfig<TextDisplayBlockEntityElement> {
    public static final Factory FACTORY = new Factory();
    public final Function<Player, List<Object>> lazyMetadataPacket;
    public final String text;
    public final Vector3f scale;
    public final Vector3f position;
    public final Vector3f translation;
    public final float xRot;
    public final float yRot;
    public final Quaternionf rotation;
    public final Billboard billboard;
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

    public TextDisplayBlockEntityElementConfig(String text,
                                               Vector3f scale,
                                               Vector3f position,
                                               Vector3f translation,
                                               float xRot,
                                               float yRot,
                                               Quaternionf rotation,
                                               Billboard billboard,
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
                                               TextDisplayAlignment alignment) {
        this.text = text;
        this.scale = scale;
        this.position = position;
        this.translation = translation;
        this.xRot = xRot;
        this.yRot = yRot;
        this.rotation = rotation;
        this.billboard = billboard;
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
        this.lazyMetadataPacket = player -> {
            List<Object> dataValues = new ArrayList<>();
            if (glowColor != null) {
                TextDisplayEntityData.SharedFlags.addEntityData((byte) 0x40, dataValues);
                TextDisplayEntityData.GlowColorOverride.addEntityData(glowColor.color(), dataValues);
            } else {
                TextDisplayEntityData.SharedFlags.addEntityData((byte) 0x0, dataValues);
                TextDisplayEntityData.GlowColorOverride.addEntityData(-1, dataValues);
            }
            TextDisplayEntityData.Text.addEntityData(ComponentUtils.adventureToMinecraft(text(player)), dataValues);
            TextDisplayEntityData.Scale.addEntityData(this.scale, dataValues);
            TextDisplayEntityData.RotationLeft.addEntityData(this.rotation, dataValues);
            TextDisplayEntityData.BillboardConstraints.addEntityData(this.billboard.id(), dataValues);
            TextDisplayEntityData.Translation.addEntityData(this.translation, dataValues);
            TextDisplayEntityData.LineWidth.addEntityData(this.lineWidth, dataValues);
            TextDisplayEntityData.BackgroundColor.addEntityData(this.backgroundColor, dataValues);
            TextDisplayEntityData.TextOpacity.addEntityData(this.opacity, dataValues);
            TextDisplayEntityData.TextDisplayMasks.addEntityData(TextDisplayEntityData.encodeMask(this.hasShadow, this.isSeeThrough, this.useDefaultBackgroundColor, this.alignment), dataValues);
            if (this.blockLight != -1 && this.skyLight != -1) {
                TextDisplayEntityData.BrightnessOverride.addEntityData(this.blockLight << 4 | this.skyLight << 20, dataValues);
            } else {
                TextDisplayEntityData.BrightnessOverride.addEntityData(-1, dataValues);
            }
            TextDisplayEntityData.ViewRange.addEntityData((float) (this.viewRange * player.displayEntityViewDistance()), dataValues);
            return dataValues;
        };
    }

    @Override
    public TextDisplayBlockEntityElement create(World world, BlockPos pos) {
        return new TextDisplayBlockEntityElement(this, pos);
    }

    @Override
    public TextDisplayBlockEntityElement create(World world, BlockPos pos, TextDisplayBlockEntityElement previous) {
        return new TextDisplayBlockEntityElement(this, pos, previous.entityId,
                previous.config.yRot != this.yRot ||
                        previous.config.xRot != this.xRot ||
                        !previous.config.position.equals(this.position)
        );
    }

    @Override
    public TextDisplayBlockEntityElement createExact(World world, BlockPos pos, TextDisplayBlockEntityElement previous) {
        if (!previous.config.isSamePosition(this)) {
            return null;
        }
        return new TextDisplayBlockEntityElement(this, pos, previous.entityId, false);
    }

    @Override
    public Class<TextDisplayBlockEntityElement> elementClass() {
        return TextDisplayBlockEntityElement.class;
    }

    public String text() {
        return text;
    }

    public Component text(Player player) {
        return AdventureHelper.miniMessage().deserialize(this.text, NetworkTextReplaceContext.of(player).tagResolvers());
    }

    public Vector3f scale() {
        return this.scale;
    }

    public Vector3f translation() {
        return this.translation;
    }

    public Vector3f position() {
        return this.position;
    }

    public float yRot() {
        return this.yRot;
    }

    public float xRot() {
        return this.xRot;
    }

    public Billboard billboard() {
        return billboard;
    }

    public Quaternionf rotation() {
        return rotation;
    }

    public List<Object> metadataValues(Player player) {
        return this.lazyMetadataPacket.apply(player);
    }

    public boolean isSamePosition(TextDisplayBlockEntityElementConfig that) {
        return Float.compare(xRot, that.xRot) == 0 &&
                Float.compare(yRot, that.yRot) == 0 &&
                Objects.equal(position, that.position) &&
                Objects.equal(translation, that.translation) &&
                Objects.equal(rotation, that.rotation);
    }

    public static class Factory implements BlockEntityElementConfigFactory<TextDisplayBlockEntityElement> {

        @Override
        public TextDisplayBlockEntityElementConfig create(Map<String, Object> arguments) {
            String text = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("text"), "warning.config.block.state.entity_renderer.text_display.missing_text");
            Map<String, Object> brightness = ResourceConfigUtils.getAsMap(arguments.getOrDefault("brightness", Map.of()), "brightness");
            return new TextDisplayBlockEntityElementConfig(
                    text,
                    ResourceConfigUtils.getAsVector3f(arguments.getOrDefault("scale", 1f), "scale"),
                    ResourceConfigUtils.getAsVector3f(arguments.getOrDefault("position", 0.5f), "position"),
                    ResourceConfigUtils.getAsVector3f(arguments.get("translation"), "translation"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("pitch", 0f), "pitch"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("yaw", 0f), "yaw"),
                    ResourceConfigUtils.getAsQuaternionf(arguments.getOrDefault("rotation", 0f), "rotation"),
                    Billboard.valueOf(arguments.getOrDefault("billboard", "fixed").toString().toUpperCase(Locale.ROOT)),
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
                    ResourceConfigUtils.getAsEnum(arguments.get("alignment"), TextDisplayAlignment.class, TextDisplayAlignment.CENTER)
            );
        }
    }
}
