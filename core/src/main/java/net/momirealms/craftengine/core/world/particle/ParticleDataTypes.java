package net.momirealms.craftengine.core.world.particle;

import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.util.Color;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.LazyReference;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ParticleDataTypes {
    public static final Map<Key, java.util.function.Function<Map<String, Object>, ParticleData>> TYPES = new HashMap<>();

    static {
        registerParticleData(map -> new BlockStateData(
                        LazyReference.lazyReference(new Supplier<>() {
                            final String blockState = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("block-state"), "warning.config.function.particle.missing_block_state");
                            @Override
                            public BlockStateWrapper get() {
                                return CraftEngine.instance().blockManager().createBlockState(this.blockState);
                            }
                        })),
                ParticleTypes.BLOCK, ParticleTypes.FALLING_DUST, ParticleTypes.DUST_PILLAR, ParticleTypes.BLOCK_CRUMBLE, ParticleTypes.BLOCK_MARKER);
        registerParticleData(map -> new ColorData(
                        Color.fromStrings(ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("color"), "warning.config.function.particle.missing_color").split(","))),
                ParticleTypes.ENTITY_EFFECT, ParticleTypes.TINTED_LEAVES);
        registerParticleData(map -> new JavaTypeData(
                        ResourceConfigUtils.getAsFloat(map.get("charge"), "charge")),
                ParticleTypes.SCULK_CHARGE);
        registerParticleData(map -> new JavaTypeData(
                        ResourceConfigUtils.getAsInt(map.get("shriek"), "shriek")),
                ParticleTypes.SHRIEK);
        registerParticleData(map -> new DustData(
                        Color.fromStrings(ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("color"), "warning.config.function.particle.missing_color").split(",")),
                        ResourceConfigUtils.getAsFloat(map.getOrDefault("scale", 1), "scale")),
                ParticleTypes.DUST);
        registerParticleData(map -> new DustTransitionData(
                        Color.fromStrings(ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("from"), "warning.config.function.particle.missing_from").split(",")),
                        Color.fromStrings(ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("to"), "warning.config.function.particle.missing_to").split(",")),
                        ResourceConfigUtils.getAsFloat(map.getOrDefault("scale", 1), "scale")),
                ParticleTypes.DUST_COLOR_TRANSITION);
        registerParticleData(map -> new ItemStackData(
                        LazyReference.lazyReference(new Supplier<>() {
                            final Key itemId = Key.of(ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("item"), "warning.config.function.particle.missing_item"));
                            @Override
                            public Item<?> get() {
                                return CraftEngine.instance().itemManager().createWrappedItem(this.itemId, null);
                            }
                        })
                ),
                ParticleTypes.ITEM);
        registerParticleData(map -> new VibrationData(
                        NumberProviders.fromObject(map.getOrDefault("target-x", 0)),
                        NumberProviders.fromObject(map.getOrDefault("target-y", 0)),
                        NumberProviders.fromObject(map.getOrDefault("target-z", 0)),
                        NumberProviders.fromObject(map.getOrDefault("arrival-time", 10))),
                ParticleTypes.VIBRATION);
        registerParticleData(map -> new TrailData(
                        NumberProviders.fromObject(map.getOrDefault("target-x", 0)),
                        NumberProviders.fromObject(map.getOrDefault("target-y", 0)),
                        NumberProviders.fromObject(map.getOrDefault("target-z", 0)),
                        Color.fromStrings(ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("color"), "warning.config.function.particle.missing_color").split(",")),
                        NumberProviders.fromObject(map.getOrDefault("duration", 10))),
                ParticleTypes.TRAIL);
    }

    public static void registerParticleData(java.util.function.Function<Map<String, Object>, ParticleData> function, Key... types) {
        for (Key type : types) {
            TYPES.put(type, function);
        }
    }
}
