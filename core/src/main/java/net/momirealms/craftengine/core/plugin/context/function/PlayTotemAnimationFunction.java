package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
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
    private final NumberProvider volume;
    private final NumberProvider pitch;
    private final boolean silent;

    public PlayTotemAnimationFunction(
            List<Condition<CTX>> predicates,
            PlayerSelector<CTX> selector,
            Key item,
            @Nullable Key sound,
            NumberProvider volume,
            NumberProvider pitch,
            boolean silent
    ) {
        super(predicates);
        this.selector = selector;
        this.item = item;
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.silent = silent;
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
                    SoundData.SoundValue.fixed(Math.max(this.volume.getFloat(ctx), 0f)),
                    SoundData.SoundValue.fixed(MiscUtils.clamp(this.pitch.getFloat(ctx), 0f, 2f))
            );
        }
        for (Player player : this.selector.get(ctx)) {
            Item<?> buildItem = customItem.buildItem(player);
            if (VersionHelper.isOrAbove1_21_2()) {
                buildItem.setJavaComponent(DataComponentKeys.DEATH_PROTECTION, Map.of());
            }
            player.sendTotemAnimation(buildItem, soundData, this.silent);
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
            NumberProvider volume = NumberProviders.fromObject(arguments.getOrDefault("volume", 1f));
            NumberProvider pitch = NumberProviders.fromObject(arguments.getOrDefault("pitch", 1f));
            boolean silent = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("silent", false), "silent");
            return new PlayTotemAnimationFunction<>(getPredicates(arguments), selector, item, sound, volume, pitch, silent);
        }
    }
}
