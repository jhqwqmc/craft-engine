package net.momirealms.craftengine.core.plugin.locale;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

public interface MessageConstants {
    TranslatableComponent.Builder COMMAND_RELOAD_FAILURE_IS_LOADING = Component.translatable().key("command.reload.failure.is_reloading");
    TranslatableComponent.Builder COMMAND_RELOAD_CONFIG_SUCCESS = Component.translatable().key("command.reload.config.success");
    TranslatableComponent.Builder COMMAND_RELOAD_CONFIG_FAILURE = Component.translatable().key("command.reload.config.failure");
    TranslatableComponent.Builder COMMAND_RELOAD_PACK_SUCCESS = Component.translatable().key("command.reload.pack.success");
    TranslatableComponent.Builder COMMAND_RELOAD_PACK_FAILURE = Component.translatable().key("command.reload.pack.failure");
    TranslatableComponent.Builder COMMAND_RELOAD_ALL_SUCCESS = Component.translatable().key("command.reload.all.success");
    TranslatableComponent.Builder COMMAND_RELOAD_ALL_FAILURE = Component.translatable().key("command.reload.all.failure");
    TranslatableComponent.Builder COMMAND_ITEM_GET_SUCCESS = Component.translatable().key("command.item.get.success");
    TranslatableComponent.Builder COMMAND_ITEM_GET_FAILURE_NOT_EXIST = Component.translatable().key("command.item.get.failure.not_exist");
    TranslatableComponent.Builder COMMAND_ITEM_GIVE_SUCCESS_SINGLE = Component.translatable().key("command.item.give.success.single");
    TranslatableComponent.Builder COMMAND_ITEM_GIVE_SUCCESS_MULTIPLE = Component.translatable().key("command.item.give.success.multiple");
    TranslatableComponent.Builder COMMAND_ITEM_GIVE_FAILURE_NOT_EXIST = Component.translatable().key("command.item.give.failure.not_exist");
    TranslatableComponent.Builder COMMAND_SEARCH_RECIPE_NOT_FOUND = Component.translatable().key("command.search_recipe.not_found");
    TranslatableComponent.Builder COMMAND_SEARCH_RECIPE_NO_ITEM = Component.translatable().key("command.search_recipe.no_item");
    TranslatableComponent.Builder COMMAND_SEARCH_USAGE_NOT_FOUND = Component.translatable().key("command.search_usage.not_found");
    TranslatableComponent.Builder COMMAND_SEARCH_USAGE_NO_ITEM = Component.translatable().key("command.search_usage.no_item");
    TranslatableComponent.Builder COMMAND_TOTEM_NOT_TOTEM = Component.translatable().key("command.totem_animation.failure.not_totem");
    TranslatableComponent.Builder COMMAND_RESOURCE_ENABLE_SUCCESS = Component.translatable().key("command.resource.enable.success");
    TranslatableComponent.Builder COMMAND_RESOURCE_ENABLE_FAILURE = Component.translatable().key("command.resource.enable.failure.unknown");
    TranslatableComponent.Builder COMMAND_RESOURCE_DISABLE_SUCCESS = Component.translatable().key("command.resource.disable.success");
    TranslatableComponent.Builder COMMAND_RESOURCE_DISABLE_FAILURE = Component.translatable().key("command.resource.disable.failure.unknown");
    TranslatableComponent.Builder COMMAND_RESOURCE_LIST = Component.translatable().key("command.resource.list");
    TranslatableComponent.Builder COMMAND_UPLOAD_FAILURE_NOT_SUPPORTED = Component.translatable().key("command.upload.failure.not_supported");
    TranslatableComponent.Builder COMMAND_UPLOAD_ON_PROGRESS = Component.translatable().key("command.upload.on_progress");
    TranslatableComponent.Builder COMMAND_SEND_RESOURCE_PACK_SUCCESS_SINGLE = Component.translatable().key("command.send_resource_pack.success.single");
    TranslatableComponent.Builder COMMAND_SEND_RESOURCE_PACK_SUCCESS_MULTIPLE = Component.translatable().key("command.send_resource_pack.success.multiple");
    TranslatableComponent.Builder COMMAND_TOTEM_SUCCESS_SINGLE = Component.translatable().key("command.totem_animation.success.single");
    TranslatableComponent.Builder COMMAND_TOTEM_SUCCESS_MULTIPLE = Component.translatable().key("command.totem_animation.success.multiple");
    TranslatableComponent.Builder COMMAND_LOCALE_SET_FAILURE = Component.translatable().key("command.locale.set.failure");
    TranslatableComponent.Builder COMMAND_LOCALE_SET_SUCCESS = Component.translatable().key("command.locale.set.success");
    TranslatableComponent.Builder COMMAND_LOCALE_UNSET_SUCCESS = Component.translatable().key("command.locale.unset.success");
    TranslatableComponent.Builder COMMAND_ITEM_CLEAR_SUCCESS_SINGLE = Component.translatable().key("command.item.clear.success.single");
    TranslatableComponent.Builder COMMAND_ITEM_CLEAR_SUCCESS_MULTIPLE = Component.translatable().key("command.item.clear.success.multiple");
    TranslatableComponent.Builder COMMAND_ITEM_CLEAR_FAILED_SINGLE = Component.translatable().key("command.item.clear.failed.single");
    TranslatableComponent.Builder COMMAND_ITEM_CLEAR_FAILED_MULTIPLE = Component.translatable().key("command.item.clear.failed.multiple");
    TranslatableComponent.Builder COMMAND_ITEM_CLEAR_TEST_SINGLE = Component.translatable().key("command.item.clear.test.single");
    TranslatableComponent.Builder COMMAND_ITEM_CLEAR_TEST_MULTIPLE = Component.translatable().key("command.item.clear.test.multiple");
}
