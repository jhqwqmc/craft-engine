package net.momirealms.craftengine.bukkit.block.entity.renderer.element;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.entity.data.TextDisplayEntityData;
import net.momirealms.craftengine.bukkit.util.ComponentUtils;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElement;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfig;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfigFactory;
import net.momirealms.craftengine.core.entity.Billboard;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public class TextDisplayBlockEntityElementConfig implements BlockEntityElementConfig<TextDisplayBlockEntityElement> {
    public static final Factory FACTORY = new Factory();
    private final Function<Player, List<Object>> lazyMetadataPacket;
    private final String text;
    private final Vector3f scale;
    private final Vector3f position;
    private final Vector3f translation;
    private final float xRot;
    private final float yRot;
    private final Quaternionf rotation;
    private final Billboard billboard;

    public TextDisplayBlockEntityElementConfig(String text,
                                               Vector3f scale,
                                               Vector3f position,
                                               Vector3f translation,
                                               float xRot,
                                               float yRot,
                                               Quaternionf rotation,
                                               Billboard billboard) {
        this.text = text;
        this.scale = scale;
        this.position = position;
        this.translation = translation;
        this.xRot = xRot;
        this.yRot = yRot;
        this.rotation = rotation;
        this.billboard = billboard;
        this.lazyMetadataPacket = player -> {
            List<Object> dataValues = new ArrayList<>();
            TextDisplayEntityData.Text.addEntityData(ComponentUtils.adventureToMinecraft(text(player)), dataValues);
            TextDisplayEntityData.Scale.addEntityData(this.scale, dataValues);
            TextDisplayEntityData.RotationLeft.addEntityData(this.rotation, dataValues);
            TextDisplayEntityData.BillboardConstraints.addEntityData(this.billboard.id(), dataValues);
            TextDisplayEntityData.Translation.addEntityData(this.translation, dataValues);
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
    public Class<TextDisplayBlockEntityElement> elementClass() {
        return TextDisplayBlockEntityElement.class;
    }

    public Component text(Player player) {
        return AdventureHelper.miniMessage().deserialize(this.text, PlayerOptionalContext.of(player).tagResolvers());
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

    public static class Factory implements BlockEntityElementConfigFactory {

        @SuppressWarnings("unchecked")
        @Override
        public <E extends BlockEntityElement> BlockEntityElementConfig<E> create(Map<String, Object> arguments) {
            String text = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("text"), "warning.config.block.state.entity_renderer.text_display.missing_text");
            return (BlockEntityElementConfig<E>) new TextDisplayBlockEntityElementConfig(
                    text,
                    ResourceConfigUtils.getAsVector3f(arguments.getOrDefault("scale", 1f), "scale"),
                    ResourceConfigUtils.getAsVector3f(arguments.getOrDefault("position", 0.5f), "position"),
                    ResourceConfigUtils.getAsVector3f(arguments.get("translation"), "translation"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("pitch", 0f), "pitch"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("yaw", 0f), "yaw"),
                    ResourceConfigUtils.getAsQuaternionf(arguments.getOrDefault("rotation", 0f), "rotation"),
                    Billboard.valueOf(arguments.getOrDefault("billboard", "fixed").toString().toUpperCase(Locale.ROOT))
            );
        }
    }
}
