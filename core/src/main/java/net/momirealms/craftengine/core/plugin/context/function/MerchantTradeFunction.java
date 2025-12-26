package net.momirealms.craftengine.core.plugin.context.function;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemKeys;
import net.momirealms.craftengine.core.item.processor.ComponentsProcessor;
import net.momirealms.craftengine.core.item.processor.TagsProcessor;
import net.momirealms.craftengine.core.item.trade.MerchantOffer;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.*;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.plugin.context.selector.PlayerSelector;
import net.momirealms.craftengine.core.plugin.context.selector.PlayerSelectors;
import net.momirealms.craftengine.core.util.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MerchantTradeFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final String title;
    private final PlayerSelector<CTX> selector;
    private final java.util.function.Function<Player, List<MerchantOffer<?>>> offers;

    public MerchantTradeFunction(List<Condition<CTX>> predicates, @Nullable PlayerSelector<CTX> selector, String title, java.util.function.Function<Player, List<MerchantOffer<?>>> offers) {
        super(predicates);
        this.title = title;
        this.selector = selector;
        this.offers = offers;
    }

    @Override
    public void runInternal(CTX ctx) {
        if (this.selector == null) {
            ctx.getOptionalParameter(DirectContextParameters.PLAYER).ifPresent(it -> {
                CraftEngine.instance().guiManager().openMerchant(it, this.title == null ? null : AdventureHelper.miniMessage().deserialize(this.title, ctx.tagResolvers()), this.offers.apply(it));
            });
        } else {
            for (Player viewer : this.selector.get(ctx)) {
                RelationalContext relationalContext = ViewerContext.of(ctx, PlayerOptionalContext.of(viewer));
                CraftEngine.instance().guiManager().openMerchant(viewer, this.title == null ? null : AdventureHelper.miniMessage().deserialize(this.title, relationalContext.tagResolvers()), this.offers.apply(viewer));
            }
        }
    }

    @Override
    public Key type() {
        return CommonFunctions.MESSAGE;
    }

    public static class FactoryImpl<CTX extends Context> extends AbstractFactory<CTX> {

        public FactoryImpl(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
            super(factory);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public Function<CTX> create(Map<String, Object> arguments) {
            String title = ResourceConfigUtils.getAsStringOrNull(arguments.get("title"));
            List<TempOffer> merchantOffers = ResourceConfigUtils.parseConfigAsList(ResourceConfigUtils.requireNonNullOrThrow(arguments.get("offers"), "warning.config.function.merchant_trade.missing_offers"), map -> {
                Object cost1 = ResourceConfigUtils.requireNonNullOrThrow(map.get("cost-1"), "warning.config.function.merchant_trade.offer.missing_cost_1");
                Object cost2 = map.get("cost-2");
                Object result = ResourceConfigUtils.requireNonNullOrThrow(map.get("result"), "warning.config.function.merchant_trade.offer.missing_result");
                int exp = ResourceConfigUtils.getAsInt(map.get("experience"), "experience");
                return new TempOffer(cost1, cost2, result, exp);
            });
            return new MerchantTradeFunction<>(getPredicates(arguments), PlayerSelectors.fromObject(arguments.get("target"), conditionFactory()), title,
                    (player) -> {
                        List<MerchantOffer<?>> offers = new ArrayList<>(merchantOffers.size());
                        for (TempOffer offer : merchantOffers) {
                            Item cost1 = parseIngredient(offer.cost1, player);
                            Optional cost2 = Optional.ofNullable(parseIngredient(offer.cost2, player));
                            Item result = parseIngredient(offer.result, player);
                            offers.add(new MerchantOffer<>(cost1, cost2, result, false, 0, Integer.MAX_VALUE, offer.exp, 0, 0, 0));
                        }
                        return offers;
                    });
        }

        public record TempOffer(Object cost1, Object cost2, Object result, int exp) {
        }

        private Item<?> parseIngredient(Object arguments, Player player) {
            if (arguments == null) return null;
            if (arguments instanceof Map<?,?> map) {
                Map<String, Object> args = MiscUtils.castToMap(map, false);
                String itemName = args.getOrDefault("item", "minecraft:stone").toString();
                Item<Object> item = createSafeItem(itemName, player);
                if (args.containsKey("count")) {
                    item.count(ResourceConfigUtils.getAsInt(args.get("count"), "count"));
                }
                if (VersionHelper.isOrAbove1_20_5() && args.containsKey("components")) {
                    item = new ComponentsProcessor<>(MiscUtils.castToMap(args.get("components"), false)).apply(item, ItemBuildContext.empty());
                }
                if (!VersionHelper.isOrAbove1_20_5() && args.containsKey("nbt")) {
                    item = new TagsProcessor<>(MiscUtils.castToMap(args.get("nbt"), false)).apply(item, ItemBuildContext.empty());
                }
                return item;
            } else {
                String itemName = arguments.toString();
                return createSafeItem(itemName, player);
            }
        }

        private Item<Object> createSafeItem(String itemName, Player player) {
            Key itemId = Key.of(itemName);
            Item<Object> item = CraftEngine.instance().itemManager().createWrappedItem(itemId, player);
            if (item == null) {
                item = CraftEngine.instance().itemManager().createWrappedItem(ItemKeys.STONE, player);
                assert item != null;
                item.itemNameComponent(Component.text(itemName).color(NamedTextColor.RED));
            }
            return item;
        }
    }
}
