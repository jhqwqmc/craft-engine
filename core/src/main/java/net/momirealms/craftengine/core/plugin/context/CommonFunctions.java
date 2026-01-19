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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class CommonFunctions {
    public static final CommonFunctionType<CommandFunction<Context>> COMMAND = register(Key.ce("command"), CommandFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<MessageFunction<Context>> MESSAGE = register(Key.ce("message"), MessageFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<ActionBarFunction<Context>> ACTIONBAR = register(Key.ce("actionbar"), ActionBarFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<TitleFunction<Context>> TITLE = register(Key.ce("title"), TitleFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<OpenWindowFunction<Context>> OPEN_WINDOW = register(Key.ce("open_window"), OpenWindowFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<CancelEventFunction<Context>> CANCEL_EVENT = register(Key.ce("cancel_event"), CancelEventFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<RunFunction<Context>> RUN = register(Key.ce("run"), RunFunction.factory(CommonFunctions::fromMap, CommonConditions::fromMap));
    public static final CommonFunctionType<PlaceBlockFunction<Context>> PLACE_BLOCK = register(Key.ce("place_block"), PlaceBlockFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<UpdateBlockPropertyFunction<Context>> UPDATE_BLOCK_PROPERTY = register(Key.ce("update_block_property"), UpdateBlockPropertyFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<TransformBlockFunction<Context>> TRANSFORM_BLOCK = register(Key.ce("transform_block"), TransformBlockFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<BreakBlockFunction<Context>> BREAK_BLOCK = register(Key.ce("break_block"), BreakBlockFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<UpdateInteractionFunction<Context>> UPDATE_INTERACTION_TICK = register(Key.ce("update_interaction_tick"), UpdateInteractionFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<SetCountFunction<Context>> SET_COUNT = register(Key.ce("set_count"), SetCountFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<DropLootFunction<Context>> DROP_LOOT = register(Key.ce("drop_loot"), DropLootFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<SwingHandFunction<Context>> SWING_HAND = register(Key.ce("swing_hand"), SwingHandFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<SetFoodFunction<Context>> SET_FOOD = register(Key.ce("set_food"), SetFoodFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<SetSaturationFunction<Context>> SET_SATURATION = register(Key.ce("set_saturation"), SetSaturationFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<PlaySoundFunction<Context>> PLAY_SOUND = register(Key.ce("play_sound"), PlaySoundFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<ParticleFunction<Context>> PARTICLE = register(Key.ce("particle"), ParticleFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<PotionEffectFunction<Context>> POTION_EFFECT = register(Key.ce("potion_effect"), PotionEffectFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<RemovePotionEffectFunction<Context>> REMOVE_POTION_EFFECT = register(Key.ce("remove_potion_effect"), RemovePotionEffectFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<LevelerExpFunction<Context>> LEVELER_EXP = register(Key.ce("leveler_exp"), LevelerExpFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<SetCooldownFunction<Context>> SET_COOLDOWN = register(Key.ce("set_cooldown"), SetCooldownFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<RemoveCooldownFunction<Context>> REMOVE_COOLDOWN = register(Key.ce("remove_cooldown"), RemoveCooldownFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<SpawnFurnitureFunction<Context>> SPAWN_FURNITURE = register(Key.ce("spawn_furniture"), SpawnFurnitureFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<RemoveFurnitureFunction<Context>> REMOVE_FURNITURE = register(Key.ce("remove_furniture"), RemoveFurnitureFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<ReplaceFurnitureFunction<Context>> REPLACE_FURNITURE = register(Key.ce("replace_furniture"), ReplaceFurnitureFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<RotateFurnitureFunction<Context>> ROTATE_FURNITURE = register(Key.ce("rotate_furniture"), RotateFurnitureFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<MythicMobsSkillFunction<Context>> MYTHIC_MOBS_SKILL = register(Key.ce("mythic_mobs_skill"), MythicMobsSkillFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<TeleportFunction<Context>> TELEPORT = register(Key.ce("teleport"), TeleportFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<SetVariableFunction<Context>> SET_VARIABLE = register(Key.ce("set_variable"), SetVariableFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<ToastFunction<Context>> TOAST = register(Key.ce("toast"), ToastFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<DamageFunction<Context>> DAMAGE = register(Key.ce("damage"), DamageFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<MerchantTradeFunction<Context>> MERCHANT_TRADE = register(Key.ce("merchant_trade"), MerchantTradeFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<RemoveEntityFunction<Context>> REMOVE_ENTITY = register(Key.ce("remove_entity"), RemoveEntityFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<IfElseFunction<Context>> IF_ELSE = register(Key.ce("if_else"), IfElseFunction.factory(CommonFunctions::fromMap, CommonConditions::fromMap));
    public static final CommonFunctionType<IfElseFunction<Context>> ALTERNATIVES = register(Key.ce("alternatives"), IfElseFunction.factory(CommonFunctions::fromMap, CommonConditions::fromMap));
    public static final CommonFunctionType<WhenFunction<Context>> WHEN = register(Key.ce("when"), WhenFunction.factory(CommonFunctions::fromMap, CommonConditions::fromMap));
    public static final CommonFunctionType<DamageItemFunction<Context>> DAMAGE_ITEM = register(Key.ce("damage_item"), DamageItemFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<CycleBlockPropertyFunction<Context>> CYCLE_BLOCK_PROPERTY = register(Key.ce("cycle_block_property"), CycleBlockPropertyFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<SetExpFunction<Context>> SET_EXP = register(Key.ce("set_exp"), SetExpFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<SetLevelFunction<Context>> SET_LEVEL = register(Key.ce("set_level"), SetLevelFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<PlayTotemAnimationFunction<Context>> PLAY_TOTEM_ANIMATION = register(Key.ce("play_totem_animation"), PlayTotemAnimationFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<CloseInventoryFunction<Context>> CLOSE_INVENTORY = register(Key.ce("close_inventory"), CloseInventoryFunction.factory(CommonConditions::fromMap));
    public static final CommonFunctionType<ClearItemFunction<Context>> CLEAR_ITEM = register(Key.ce("clear_item"), ClearItemFunction.factory(CommonConditions::fromMap));

    private CommonFunctions() {}

    public static <T extends Function<Context>> CommonFunctionType<T> register(Key key, FunctionFactory<Context, T> factory) {
        CommonFunctionType<T> type = new CommonFunctionType<>(key, factory);
        ((WritableRegistry<CommonFunctionType<?>>) BuiltInRegistries.COMMON_FUNCTION_TYPE)
                .register(ResourceKey.create(Registries.COMMON_FUNCTION_TYPE.location(), key), type);
        return type;
    }

    public static Function<Context> fromMap(Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), "warning.config.function.missing_type");
        Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
        CommonFunctionType<? extends Function<Context>> functionType = BuiltInRegistries.COMMON_FUNCTION_TYPE.getValue(key);
        if (functionType == null) {
            throw new LocalizedResourceConfigException("warning.config.function.invalid_type", type);
        }
        return functionType.factory().create(map);
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
                        events.computeIfAbsent(eventTrigger, k -> new ArrayList<>(4)).add(RUN.factory().create(eventSection));
                    }
                } catch (IllegalArgumentException e) {
                    throw new LocalizedResourceConfigException("warning.config.event.invalid_trigger", on);
                }
            }
        }
        return events;
    }
}
