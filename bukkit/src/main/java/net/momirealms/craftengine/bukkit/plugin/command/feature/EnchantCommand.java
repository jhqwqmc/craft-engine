package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.bukkit.util.EnchantmentUtils;
import net.momirealms.craftengine.bukkit.util.ItemStackUtils;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.plugin.command.FlagKeys;
import net.momirealms.craftengine.core.plugin.locale.MessageConstants;
import net.momirealms.craftengine.core.util.Pair;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.data.MultipleEntitySelector;
import org.incendo.cloud.bukkit.parser.NamespacedKeyParser;
import org.incendo.cloud.bukkit.parser.selector.MultipleEntitySelectorParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.standard.EnumParser;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public final class EnchantCommand extends BukkitCommandFeature<CommandSender> {

    public EnchantCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .required("entity", MultipleEntitySelectorParser.multipleEntitySelectorParser(false))
                .required("enchantment", NamespacedKeyParser.namespacedKeyComponent().suggestionProvider(new SuggestionProvider<>() {
                    @Override
                    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<Object> context, @NonNull CommandInput input) {
                        return CompletableFuture.completedFuture(Registry.ENCHANTMENT.stream()
                                .map(enchantment -> Suggestion.suggestion(enchantment.getKey().asString()))
                                .toList());
                    }
                }))
                .optional("level", IntegerParser.integerParser(1))
                .flag(FlagKeys.SILENT_FLAG)
                .flag(manager.flagBuilder("ignore-level"))
                .flag(manager.flagBuilder("ignore-conflict"))
                .flag(manager.flagBuilder("ignore-incompatible"))
                .flag(manager.flagBuilder("slot").withComponent(EnumParser.enumComponent(EquipmentSlot.class).name("slot").parser()))
                .handler(context -> {
                    NamespacedKey enchantmentKey = context.get("enchantment");
                    Enchantment enchantment = Registry.ENCHANTMENT.get(enchantmentKey);
                    if (enchantment == null) {
                        handleFeedback(context, MessageConstants.COMMAND_ENCHANT_FAILURE_NOT_EXIST, Component.text(KeyUtils.namespacedKeyToKey(enchantmentKey).asString()));
                        return;
                    }

                    int level = context.getOrDefault("level", 1);
                    EquipmentSlot slot = context.flags().<EquipmentSlot>getValue("slot").orElse(EquipmentSlot.HAND);

                    if (!context.flags().hasFlag("ignore-level") && enchantment.getMaxLevel() < level) {
                        handleFeedback(context, MessageConstants.COMMAND_ENCHANT_FAILURE_LEVEL, Component.text(level), Component.text(enchantment.getMaxLevel()));
                        return;
                    }

                    boolean ignoreIncompatible = context.flags().hasFlag("ignore-incompatible");
                    boolean ignoreConflict = context.flags().hasFlag("ignore-conflict");
                    MultipleEntitySelector selector = context.get("entity");
                    Collection<Entity> targets = selector.values();

                    int successCount = 0;
                    for (Entity entity : targets) {
                        if (!(entity instanceof LivingEntity livingEntity)) {
                            if (targets.size() != 1) continue;
                            handleFeedback(context, MessageConstants.COMMAND_ENCHANT_FAILURE_ENTITY, Component.text(entity.getName()));
                            return;
                        }

                        EntityEquipment equipment = livingEntity.getEquipment();
                        if (equipment == null) {
                            if (targets.size() != 1) continue;
                            handleFeedback(context, MessageConstants.COMMAND_ENCHANT_FAILURE_ENTITY, Component.text(entity.getName()));
                            return;
                        }

                        ItemStack itemStack = equipment.getItem(slot);
                        if (ItemStackUtils.isEmpty(itemStack)) {
                            if (targets.size() != 1) continue;
                            handleFeedback(context, MessageConstants.COMMAND_ENCHANT_FAILURE_ITEMLESS, Component.text(livingEntity.getName()));
                            return;
                        }

                        if ((!enchantment.canEnchantItem(itemStack) && !ignoreIncompatible)
                                || (hasConflicts(enchantment, itemStack) && !ignoreConflict)) {
                            if (targets.size() != 1) continue;
                            handleFeedback(context, MessageConstants.COMMAND_ENCHANT_FAILURE_INCOMPATIBLE, Component.translatable(ItemStackUtils.getDescriptionId(itemStack)));
                            return;
                        }

                        itemStack.addUnsafeEnchantment(enchantment, level);
                        equipment.setItem(slot, itemStack);
                        successCount++;
                    }

                    if (successCount == 0) {
                        handleFeedback(context, MessageConstants.COMMAND_ENCHANT_FAILURE_NO_TARGET);
                        return;
                    }

                    Pair<TranslatableComponent.Builder, Component> pair = resolveSelector(selector,
                            MessageConstants.COMMAND_ENCHANT_SUCCESS_SINGLE,
                            MessageConstants.COMMAND_ENCHANT_SUCCESS_MULTIPLE);
                    handleFeedback(context, pair.left(), EnchantmentUtils.getFullName(enchantment, level), pair.right());
                });
    }

    private boolean hasConflicts(Enchantment enchantment, ItemStack itemStack) {
        for (Enchantment applied : itemStack.getEnchantments().keySet()) {
            if (applied.conflictsWith(enchantment)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getFeatureID() {
        return "enchant";
    }
}
