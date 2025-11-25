package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.selector.PlayerSelector;
import net.momirealms.craftengine.core.plugin.context.selector.PlayerSelectors;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PlayTotemAnimationFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final PlayerSelector<CTX> selector;
    private final Key item;
    @Nullable
    private final Key sound;
    private final float volume;
    private final float pitch;
    private final float minVolume;
    private final float minPitch;
    private final boolean noSound;

    public PlayTotemAnimationFunction(
            List<Condition<CTX>> predicates,
            PlayerSelector<CTX> selector,
            Key item,
            @Nullable Key sound,
            float volume,
            float pitch,
            float minVolume,
            float minPitch,
            boolean noSound
    ) {
        super(predicates);
        this.selector = selector;
        this.item = item;
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.minVolume = minVolume;
        this.minPitch = minPitch;
        this.noSound = noSound;
    }

    @Override
    protected void runInternal(CTX ctx) {
        CustomItem<?> customItem = CraftEngine.instance().itemManager().getCustomItem(this.item).orElse(null);
        if (customItem == null) {
            return;
        }
        SoundData soundData = null;
        if (this.sound != null) {
            soundData = SoundData.of(
                    this.sound,
                    SoundData.SoundValue.ranged(this.minVolume, this.volume),
                    SoundData.SoundValue.ranged(this.minPitch, this.pitch)
            );
        }
        for (Player player : this.selector.get(ctx)) {
            Item<?> buildItem = customItem.buildItem(player);
            if (VersionHelper.isOrAbove1_21_2()) {
                buildItem.setJavaComponent(DataComponentKeys.DEATH_PROTECTION, Map.of());
            }
            player.sendTotemAnimation(buildItem, soundData, this.noSound);
        }
    }

    @Override
    public Key type() {
        return CommonFunctions.PLAY_TOTEM_ANIMATION;
    }

    public static class FactoryImpl<CTX extends Context> extends AbstractFactory<CTX> {

        public FactoryImpl(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public Function<CTX> create(Map<String, Object> arguments) {
            PlayerSelector<CTX> selector = PlayerSelectors.fromObject(arguments.getOrDefault("target", "self"), conditionFactory());
            Key item = Key.of(ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("item"), "warning.config.function.play_totem_animation.missing_item"));
            @Nullable Key sound = Optional.ofNullable(arguments.get("sound")).map(String::valueOf).map(Key::of).orElse(null);
            float volume = Math.max(ResourceConfigUtils.getAsFloat(arguments.getOrDefault("volume", 1f), "volume"), 0f);
            float pitch = MiscUtils.clamp(ResourceConfigUtils.getAsFloat(arguments.getOrDefault("pitch", 1f), "pitch"), 0f, 2f);
            float minVolume = Math.max(ResourceConfigUtils.getAsFloat(arguments.getOrDefault("min-volume", 1f), "min-volume"), 0f);
            float minPitch = MiscUtils.clamp(ResourceConfigUtils.getAsFloat(arguments.getOrDefault("min-pitch", 1f), "min-pitch"), 0f, 2f);
            boolean noSound = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("no-sound", false), "no-sound");
            return new PlayTotemAnimationFunction<>(getPredicates(arguments), selector, item, sound, volume, pitch, minVolume, minPitch, noSound);
        }
    }
}
