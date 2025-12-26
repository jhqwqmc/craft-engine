package net.momirealms.craftengine.core.plugin.context;

import net.momirealms.craftengine.core.plugin.context.function.*;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.*;

public class CommonFunctions {

    static {
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.COMMAND, new CommandFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.MESSAGE, new MessageFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.ACTIONBAR, new ActionBarFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.TITLE, new TitleFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.OPEN_WINDOW, new OpenWindowFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.CANCEL_EVENT, new CancelEventFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.RUN, new RunFunction.Factory<>(CommonFunctions::fromMap, CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.PLACE_BLOCK, new PlaceBlockFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.UPDATE_BLOCK_PROPERTY, new UpdateBlockPropertyFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.TRANSFORM_BLOCK, new TransformBlockFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.BREAK_BLOCK, new BreakBlockFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.UPDATE_INTERACTION_TICK, new UpdateInteractionFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.SET_COUNT, new SetCountFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.DROP_LOOT, new DropLootFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.SWING_HAND, new SwingHandFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.SET_FOOD, new SetFoodFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.SET_SATURATION, new SetSaturationFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.PLAY_SOUND, new PlaySoundFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.PARTICLE, new ParticleFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.POTION_EFFECT, new PotionEffectFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.REMOVE_POTION_EFFECT, new RemovePotionEffectFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.LEVELER_EXP, new LevelerExpFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.SET_COOLDOWN, new SetCooldownFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.REMOVE_COOLDOWN, new RemoveCooldownFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.SPAWN_FURNITURE, new SpawnFurnitureFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.REMOVE_FURNITURE, new RemoveFurnitureFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.REPLACE_FURNITURE, new ReplaceFurnitureFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.ROTATE_FURNITURE, new RotateFurnitureFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.MYTHIC_MOBS_SKILL, new MythicMobsSkillFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.TELEPORT, new TeleportFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.SET_VARIABLE, new SetVariableFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.TOAST, new ToastFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.DAMAGE, new DamageFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.MERCHANT_TRADE, new MerchantTradeFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.REMOVE_ENTITY, new RemoveEntityFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.IF_ELSE, new IfElseFunction.Factory<>(CommonConditions::fromMap, CommonFunctions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.ALTERNATIVES, new IfElseFunction.Factory<>(CommonConditions::fromMap, CommonFunctions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.WHEN, new WhenFunction.Factory<>(CommonConditions::fromMap, CommonFunctions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.DAMAGE_ITEM, new DamageItemFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.CYCLE_BLOCK_PROPERTY, new CycleBlockPropertyFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.SET_EXP, new SetExpFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.SET_LEVEL, new SetLevelFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.PLAY_TOTEM_ANIMATION, new PlayTotemAnimationFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.CLOSE_INVENTORY, new CloseInventoryFunction.Factory<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.CLEAR_ITEM, new ClearItemFunction.Factory<>(CommonConditions::fromMap));
    }

    public static void register(Key key, FunctionFactory<Context> factory) {
        ((WritableRegistry<FunctionFactory<Context>>) BuiltInRegistries.EVENT_FUNCTION_FACTORY)
                .register(ResourceKey.create(Registries.EVENT_FUNCTION_FACTORY.location(), key), factory);
    }

    public static Function<Context> fromMap(Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), "warning.config.function.missing_type");
        Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
        FunctionFactory<Context> factory = BuiltInRegistries.EVENT_FUNCTION_FACTORY.getValue(key);
        if (factory == null) {
            throw new LocalizedResourceConfigException("warning.config.function.invalid_type", type);
        }
        return factory.create(map);
    }

    public static Map<EventTrigger, List<Function<Context>>> parseEvents(Object eventsObj) {
        if (eventsObj == null) return Map.of();
        EnumMap<EventTrigger, List<Function<Context>>> events = new EnumMap<>(EventTrigger.class);
        if (eventsObj instanceof Map<?, ?> eventsSection) {
            Map<String, Object> eventsSectionMap = MiscUtils.castToMap(eventsSection, false);
            for (Map.Entry<String, Object> eventEntry : eventsSectionMap.entrySet()) {
                try {
                    EventTrigger eventTrigger = EventTrigger.byName(eventEntry.getKey());
                    events.put(eventTrigger, ResourceConfigUtils.parseConfigAsList(eventEntry.getValue(), CommonFunctions::fromMap));
                } catch (IllegalArgumentException e) {
                    throw new LocalizedResourceConfigException("warning.config.event.invalid_trigger", eventEntry.getKey());
                }
            }
        } else if (eventsObj instanceof List<?> list) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> eventsList = (List<Map<String, Object>>) list;
            for (Map<String, Object> eventSection : eventsList) {
                String on = ResourceConfigUtils.requireNonEmptyStringOrThrow(eventSection.get("on"), "warning.config.event.missing_trigger");
                try {
                    EventTrigger eventTrigger = EventTrigger.byName(on);
                    if (eventSection.containsKey("type")) {
                        Function<Context> function = CommonFunctions.fromMap(eventSection);
                        events.computeIfAbsent(eventTrigger, k -> new ArrayList<>(4)).add(function);
                    } else if (eventSection.containsKey("functions")) {
                        events.computeIfAbsent(eventTrigger, k -> new ArrayList<>(4)).add(Objects.requireNonNull(BuiltInRegistries.EVENT_FUNCTION_FACTORY.getValue(net.momirealms.craftengine.core.plugin.context.function.CommonFunctions.RUN)).create(eventSection));
                    }
                } catch (IllegalArgumentException e) {
                    throw new LocalizedResourceConfigException("warning.config.event.invalid_trigger", on);
                }
            }
        }
        return events;
    }
}
