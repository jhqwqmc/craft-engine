package net.momirealms.craftengine.bukkit.item;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.*;
import net.momirealms.craftengine.core.item.modifier.ArgumentsModifier;
import net.momirealms.craftengine.core.item.modifier.ItemDataModifier;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.ContextKey;
import net.momirealms.craftengine.core.plugin.context.NetworkTextReplaceContext;
import net.momirealms.craftengine.core.plugin.text.component.ComponentProvider;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.ListTag;
import net.momirealms.sparrow.nbt.StringTag;
import net.momirealms.sparrow.nbt.Tag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@SuppressWarnings("DuplicatedCode")
public final class ModernNetworkItemHandler implements NetworkItemHandler<ItemStack> {

    @Override
    public Optional<Item<ItemStack>> c2s(Item<ItemStack> wrapped) {
        boolean forceReturn = false;

        // 处理收纳袋
        if (wrapped.hasComponent(ComponentTypes.BUNDLE_CONTENTS)) {
            Object bundleContents = wrapped.getExactComponent(ComponentTypes.BUNDLE_CONTENTS);
            List<Object> newItems = new ArrayList<>();
            boolean changed = false;
            for (Object previousItem : FastNMS.INSTANCE.method$BundleContents$items(bundleContents)) {
                Optional<ItemStack> itemStack = BukkitItemManager.instance().c2s(FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(previousItem));
                if (itemStack.isPresent()) {
                    newItems.add(FastNMS.INSTANCE.field$CraftItemStack$handle(itemStack.get()));
                    changed = true;
                } else {
                    newItems.add(previousItem);
                }
            }
            if (changed) {
                wrapped.setExactComponent(ComponentTypes.BUNDLE_CONTENTS, FastNMS.INSTANCE.constructor$BundleContents(newItems));
                forceReturn = true;
            }
        }

        // 处理潜影盒等
        if (wrapped.hasComponent(ComponentTypes.CONTAINER)) {
            Object containerContents = wrapped.getExactComponent(ComponentTypes.CONTAINER);
            List<Object> newItems = new ArrayList<>();
            boolean changed = false;
            for (Object previousItem : FastNMS.INSTANCE.field$ItemContainerContents$items(containerContents)) {
                Optional<ItemStack> itemStack = BukkitItemManager.instance().c2s(FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(previousItem));
                if (itemStack.isPresent()) {
                    newItems.add(FastNMS.INSTANCE.field$CraftItemStack$handle(itemStack.get()));
                    changed = true;
                } else {
                    newItems.add(previousItem);
                }
            }
            if (changed) {
                wrapped.setExactComponent(ComponentTypes.CONTAINER, FastNMS.INSTANCE.method$ItemContainerContents$fromItems(newItems));
                forceReturn = true;
            }
        }

        // 先尝试恢复client-bound-material
        Optional<CustomItem<ItemStack>> optionalCustomItem = wrapped.getCustomItem();
        if (optionalCustomItem.isPresent()) {
            BukkitCustomItem customItem = (BukkitCustomItem) optionalCustomItem.get();
            if (customItem.item() != FastNMS.INSTANCE.method$ItemStack$getItem(wrapped.getLiteralObject())) {
                wrapped = wrapped.unsafeTransmuteCopy(customItem.item(), wrapped.count());
                forceReturn = true;
            }
        }

        // 获取custom data
        Tag customData = wrapped.getSparrowNBTComponent(ComponentTypes.CUSTOM_DATA);
        if (customData instanceof CompoundTag compoundTag) {
            CompoundTag networkData = compoundTag.getCompound(NETWORK_ITEM_TAG);
            if (networkData != null) {
                forceReturn = true;
                // 移除此tag
                compoundTag.remove(NETWORK_ITEM_TAG);

                // 恢复物品
                for (Map.Entry<String, Tag> entry : networkData.entrySet()) {
                    if (entry.getValue() instanceof CompoundTag tag) {
                        NetworkItemHandler.apply(entry.getKey(), tag, wrapped);
                    }
                }

                // 如果清空了，则直接移除这个组件
                if (compoundTag.isEmpty()) wrapped.resetComponent(ComponentTypes.CUSTOM_DATA);
                // 否则设置为新的
                else wrapped.setNBTComponent(ComponentTypes.CUSTOM_DATA, compoundTag);
            }
        }

        return forceReturn ? Optional.of(wrapped) : Optional.empty();
    }

    @Override
    public Optional<Item<ItemStack>> s2c(Item<ItemStack> wrapped, Player player) {
        boolean forceReturn = false;

        // 处理收纳袋
        if (wrapped.hasComponent(ComponentTypes.BUNDLE_CONTENTS)) {
            Object bundleContents = wrapped.getExactComponent(ComponentTypes.BUNDLE_CONTENTS);
            List<Object> newItems = new ArrayList<>();
            boolean changed = false;
            for (Object previousItem : FastNMS.INSTANCE.method$BundleContents$items(bundleContents)) {
                Optional<ItemStack> itemStack = BukkitItemManager.instance().s2c(FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(previousItem), player);
                if (itemStack.isPresent()) {
                    newItems.add(FastNMS.INSTANCE.field$CraftItemStack$handle(itemStack.get()));
                    changed = true;
                } else {
                    newItems.add(previousItem);
                }
            }
            if (changed) {
                wrapped.setExactComponent(ComponentTypes.BUNDLE_CONTENTS, FastNMS.INSTANCE.constructor$BundleContents(newItems));
                forceReturn = true;
            }
        }

        // 处理潜影盒等
        if (wrapped.hasComponent(ComponentTypes.CONTAINER)) {
            Object containerContents = wrapped.getExactComponent(ComponentTypes.CONTAINER);
            List<Object> newItems = new ArrayList<>();
            for (Object previousItem : FastNMS.INSTANCE.field$ItemContainerContents$items(containerContents)) {
                boolean changed = false;
                Optional<ItemStack> itemStack = BukkitItemManager.instance().s2c(FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(previousItem), player);
                if (itemStack.isPresent()) {
                    newItems.add(FastNMS.INSTANCE.field$CraftItemStack$handle(itemStack.get()));
                    changed = true;
                } else {
                    newItems.add(previousItem);
                }
                if (changed) {
                    wrapped.setExactComponent(ComponentTypes.CONTAINER, FastNMS.INSTANCE.method$ItemContainerContents$fromItems(newItems));
                    forceReturn = true;
                }
            }
        }

        // todo 处理book

        // 不是自定义物品或修改过的原版物品
        Optional<CustomItem<ItemStack>> optionalCustomItem = wrapped.getCustomItem();
        if (optionalCustomItem.isEmpty()) {
            if (!Config.interceptItem()) {
                return forceReturn ? Optional.of(wrapped) : Optional.empty();
            }
            return new OtherItem(wrapped, forceReturn).process(NetworkTextReplaceContext.of(player));
        }

        BukkitCustomItem customItem = (BukkitCustomItem) optionalCustomItem.get();
        // 提前复制，这和物品类型相关
        Item<ItemStack> original = wrapped;
        // 应用 client-bound-material前提是服务端侧物品类型和客户端侧的不同
        if (customItem.hasClientboundMaterial() && FastNMS.INSTANCE.method$ItemStack$getItem(wrapped.getLiteralObject()) != customItem.clientItem()) {
            wrapped = wrapped.unsafeTransmuteCopy(customItem.clientItem(), wrapped.count());
            forceReturn = true;
        }
        // 没有 client-bound-data
        if (!customItem.hasClientBoundDataModifier()) {
            if (!Config.interceptItem()) {
                return forceReturn ? Optional.of(wrapped) : Optional.empty();
            }
            return new OtherItem(wrapped, forceReturn).process(NetworkTextReplaceContext.of(player));
        }
        // 获取custom data
        CompoundTag customData = Optional.ofNullable(wrapped.getSparrowNBTComponent(ComponentTypes.CUSTOM_DATA))
                .map(CompoundTag.class::cast)
                .orElseGet(CompoundTag::new);
        CompoundTag arguments = customData.getCompound(ArgumentsModifier.ARGUMENTS_TAG);
        // 创建context
        ItemBuildContext context;
        if (arguments == null) {
            context = ItemBuildContext.of(player);
        } else {
            ContextHolder.Builder builder = ContextHolder.builder();
            for (Map.Entry<String, Tag> entry : arguments.entrySet()) {
                builder.withParameter(ContextKey.direct(entry.getKey()), entry.getValue().getAsString());
            }
            context = ItemBuildContext.of(player, builder);
        }
        // 准备阶段
        CompoundTag tag = new CompoundTag();
        for (ItemDataModifier<ItemStack> modifier : customItem.clientBoundDataModifiers()) {
            modifier.prepareNetworkItem(original, context, tag);
        }
        // 应用阶段
        for (ItemDataModifier<ItemStack> modifier : customItem.clientBoundDataModifiers()) {
            modifier.apply(wrapped, context);
        }
        // 如果拦截物品的描述名称等
        if (Config.interceptItem()) {
            if (!tag.containsKey(ComponentIds.ITEM_NAME)) {
                if (VersionHelper.isOrAbove1_21_5()) processModernItemName(wrapped, () -> tag, context);
                else processLegacyItemName(wrapped, () -> tag, context);
            }
            if (!tag.containsKey(ComponentIds.CUSTOM_NAME)) {
                if (VersionHelper.isOrAbove1_21_5()) processModernCustomName(wrapped, () -> tag, context);
                else processLegacyCustomName(wrapped, () -> tag, context);
            }
            if (!tag.containsKey(ComponentIds.LORE)) {
                if (VersionHelper.isOrAbove1_21_5()) processModernLore(wrapped, () -> tag, context);
                else processLegacyLore(wrapped, () -> tag, context);
            }
        }
        // 如果tag不空，则需要返回
        if (!tag.isEmpty()) {
            customData.put(NETWORK_ITEM_TAG, tag);
            wrapped.setNBTComponent(ComponentTypes.CUSTOM_DATA, customData);
            forceReturn = true;
        }
        return forceReturn ? Optional.of(wrapped) : Optional.empty();
    }

    public static boolean processLegacyLore(Item<ItemStack> item, Supplier<CompoundTag> tag, Context context) {
        Optional<List<String>> optionalLore = item.loreJson();
        if (optionalLore.isPresent()) {
            boolean changed = false;
            List<String> lore = optionalLore.get();
            List<String> newLore = new ArrayList<>(lore.size());
            for (String line : lore) {
                Map<String, ComponentProvider> tokens = CraftEngine.instance().fontManager().matchTags(line);
                if (tokens.isEmpty()) {
                    newLore.add(line);
                } else {
                    newLore.add(AdventureHelper.componentToJson(AdventureHelper.replaceText(AdventureHelper.jsonToComponent(line), tokens, context)));
                    changed = true;
                }
            }
            if (changed) {
                item.loreJson(newLore);
                ListTag listTag = new ListTag();
                for (String line : lore) {
                    listTag.add(new StringTag(line));
                }
                tag.get().put(ComponentIds.LORE, NetworkItemHandler.pack(Operation.ADD, listTag));
                return true;
            }
        }
        return false;
    }
    
    public static boolean processLegacyCustomName(Item<ItemStack> item, Supplier<CompoundTag> tag, Context context) {
        Optional<String> optionalCustomName = item.customNameJson();
        if (optionalCustomName.isPresent()) {
            String line = optionalCustomName.get();
            Map<String, ComponentProvider> tokens = CraftEngine.instance().fontManager().matchTags(line);
            if (!tokens.isEmpty()) {
                item.customNameJson(AdventureHelper.componentToJson(AdventureHelper.replaceText(AdventureHelper.jsonToComponent(line), tokens, context)));
                tag.get().put(ComponentIds.CUSTOM_NAME, NetworkItemHandler.pack(Operation.ADD, new StringTag(line)));
                return true;
            }
        }
        return false;
    }

    public static boolean processLegacyItemName(Item<ItemStack> item, Supplier<CompoundTag> tag, Context context) {
        Optional<String> optionalItemName = item.itemNameJson();
        if (optionalItemName.isPresent()) {
            String line = optionalItemName.get();
            Map<String, ComponentProvider> tokens = CraftEngine.instance().fontManager().matchTags(line);
            if (!tokens.isEmpty()) {
                item.itemNameJson(AdventureHelper.componentToJson(AdventureHelper.replaceText(AdventureHelper.jsonToComponent(line), tokens, context)));
                tag.get().put(ComponentIds.ITEM_NAME, NetworkItemHandler.pack(Operation.ADD, new StringTag(line)));
                return true;
            }
        }
        return false;
    }

    public static boolean processModernItemName(Item<ItemStack> item, Supplier<CompoundTag> tag, Context context) {
        Tag nameTag = item.getSparrowNBTComponent(ComponentTypes.ITEM_NAME);
        if (nameTag == null) return false;
        Map<String, ComponentProvider> tokens = CraftEngine.instance().fontManager().matchTags(nameTag);
        if (!tokens.isEmpty()) {
            item.setNBTComponent(ComponentKeys.ITEM_NAME, AdventureHelper.componentToNbt(AdventureHelper.replaceText(AdventureHelper.nbtToComponent(nameTag), tokens, context)));
            tag.get().put(ComponentIds.ITEM_NAME, NetworkItemHandler.pack(Operation.ADD, nameTag));
            return true;
        }
        return false;
    }

    public static boolean processModernCustomName(Item<ItemStack> item, Supplier<CompoundTag> tag, Context context) {
        Tag nameTag = item.getSparrowNBTComponent(ComponentTypes.CUSTOM_NAME);
        if (nameTag == null) return false;
        Map<String, ComponentProvider> tokens = CraftEngine.instance().fontManager().matchTags(nameTag);
        if (!tokens.isEmpty()) {
            item.setNBTComponent(ComponentKeys.CUSTOM_NAME, AdventureHelper.componentToNbt(AdventureHelper.replaceText(AdventureHelper.nbtToComponent(nameTag), tokens, context)));
            tag.get().put(ComponentIds.CUSTOM_NAME, NetworkItemHandler.pack(Operation.ADD, nameTag));
            return true;
        }
        return false;
    }

    public static boolean processModernLore(Item<ItemStack> item, Supplier<CompoundTag> tagSupplier, Context context) {
        Tag loreTag = item.getSparrowNBTComponent(ComponentTypes.LORE);
        boolean changed = false;
        if (!(loreTag instanceof ListTag listTag)) {
            return false;
        }
        ListTag newLore = new ListTag();
        for (Tag tag : listTag) {
            Map<String, ComponentProvider> tokens = CraftEngine.instance().fontManager().matchTags(tag);
            if (tokens.isEmpty()) {
                newLore.add(tag);
            } else {
                newLore.add(AdventureHelper.componentToNbt(AdventureHelper.replaceText(AdventureHelper.nbtToComponent(tag), tokens, context)));
                changed = true;
            }
        }
        if (changed) {
            item.setNBTComponent(ComponentKeys.LORE, newLore);
            tagSupplier.get().put(ComponentIds.LORE, NetworkItemHandler.pack(Operation.ADD, listTag));
            return true;
        }
        return false;
    }

    static class OtherItem {
        private final Item<ItemStack> item;
        private final boolean forceReturn;
        private boolean globalChanged = false;
        private CompoundTag tag;

        public OtherItem(Item<ItemStack> item, boolean forceReturn) {
            this.item = item;
            this.forceReturn = forceReturn;
        }

        public Optional<Item<ItemStack>> process(Context context) {
            if (VersionHelper.isOrAbove1_21_5()) {
                if (processModernLore(this.item, this::getOrCreateTag, context))
                    this.globalChanged = true;
                if (processModernCustomName(this.item, this::getOrCreateTag, context))
                    this.globalChanged = true;
                if (processModernItemName(this.item, this::getOrCreateTag, context))
                    this.globalChanged = true;
            } else {
                if (processLegacyLore(this.item, this::getOrCreateTag, context))
                    this.globalChanged = true;
                if (processLegacyCustomName(this.item, this::getOrCreateTag, context))
                    this.globalChanged = true;
                if (processLegacyItemName(this.item, this::getOrCreateTag, context))
                    this.globalChanged = true;
            }
            if (this.globalChanged) {
                CompoundTag customData = Optional.ofNullable(this.item.getSparrowNBTComponent(ComponentTypes.CUSTOM_DATA))
                        .map(CompoundTag.class::cast)
                        .orElseGet(CompoundTag::new);
                customData.put(NETWORK_ITEM_TAG, getOrCreateTag());
                this.item.setNBTComponent(ComponentKeys.CUSTOM_DATA, customData);
                return Optional.of(this.item);
            } else if (this.forceReturn) {
                return Optional.of(this.item);
            } else {
                return Optional.empty();
            }
        }

        private CompoundTag getOrCreateTag() {
            if (this.tag == null) {
                this.tag = new CompoundTag();
            }
            return this.tag;
        }
    }
}
