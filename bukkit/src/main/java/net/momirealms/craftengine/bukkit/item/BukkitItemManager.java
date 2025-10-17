package net.momirealms.craftengine.bukkit.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.momirealms.craftengine.bukkit.item.behavior.AxeItemBehavior;
import net.momirealms.craftengine.bukkit.item.behavior.FlintAndSteelItemBehavior;
import net.momirealms.craftengine.bukkit.item.factory.BukkitItemFactory;
import net.momirealms.craftengine.bukkit.item.listener.ArmorEventListener;
import net.momirealms.craftengine.bukkit.item.listener.DebugStickListener;
import net.momirealms.craftengine.bukkit.item.listener.ItemEventListener;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.*;
import net.momirealms.craftengine.bukkit.util.ItemStackUtils;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.*;
import net.momirealms.craftengine.core.item.recipe.DatapackRecipeResult;
import net.momirealms.craftengine.core.item.recipe.UniqueIdItem;
import net.momirealms.craftengine.core.pack.AbstractPackManager;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.GsonHelper;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.UniqueKey;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.incendo.cloud.suggestion.Suggestion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

public class BukkitItemManager extends AbstractItemManager<ItemStack> {
    static {
        registerVanillaItemExtraBehavior(FlintAndSteelItemBehavior.INSTANCE, ItemKeys.FLINT_AND_STEEL);
        registerVanillaItemExtraBehavior(AxeItemBehavior.INSTANCE, ItemKeys.AXES);
    }

    private static BukkitItemManager instance;
    private final BukkitItemFactory<? extends ItemWrapper<ItemStack>> factory;
    private final BukkitCraftEngine plugin;
    private final ItemEventListener itemEventListener;
    private final DebugStickListener debugStickListener;
    private final ArmorEventListener armorEventListener;
    private final NetworkItemHandler<ItemStack> networkItemHandler;
    private final Object bedrockItemHolder;
    private final Item<ItemStack> emptyItem;
    private final UniqueIdItem<ItemStack> emptyUniqueItem;
    private final Function<Object, Integer> decoratedHashOpsGenerator;
    private Set<Key> lastRegisteredPatterns = Set.of();

    @SuppressWarnings("unchecked")
    public BukkitItemManager(BukkitCraftEngine plugin) {
        super(plugin);
        instance = this;
        this.plugin = plugin;
        this.factory = BukkitItemFactory.create(plugin);
        this.itemEventListener = new ItemEventListener(plugin, this);
        this.debugStickListener = new DebugStickListener(plugin);
        this.armorEventListener = new ArmorEventListener();
        this.networkItemHandler = VersionHelper.isOrAbove1_20_5() ? new ModernNetworkItemHandler() : new LegacyNetworkItemHandler();
        this.registerAllVanillaItems();
        this.bedrockItemHolder = FastNMS.INSTANCE.method$Registry$getHolderByResourceKey(MBuiltInRegistries.ITEM, FastNMS.INSTANCE.method$ResourceKey$create(MRegistries.ITEM, KeyUtils.toResourceLocation(Key.of("minecraft:bedrock")))).get();
        this.registerCustomTrimMaterial();
        this.loadLastRegisteredPatterns();
        ItemStack emptyStack = FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(CoreReflections.instance$ItemStack$EMPTY);
        this.emptyItem = this.factory.wrap(emptyStack);
        this.emptyUniqueItem = UniqueIdItem.of(this.emptyItem);
        this.decoratedHashOpsGenerator = VersionHelper.isOrAbove1_21_5() ? (Function<Object, Integer>) FastNMS.INSTANCE.createDecoratedHashOpsGenerator(MRegistryOps.HASHCODE) : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void delayedLoad() {
        super.delayedLoad();
        List<ExternalItemSource<ItemStack>> sources = new ArrayList<>();
        for (String externalSource : Config.recipeIngredientSources()) {
            String sourceId = externalSource.toLowerCase(Locale.ENGLISH);
            ExternalItemSource<ItemStack> provider = getExternalItemSource(sourceId);
            if (provider != null) {
                sources.add(provider);
            }
        }
        this.factory.resetRecipeIngredientSources(sources.isEmpty() ? null : sources.toArray(new ExternalItemSource[0]));
    }

    @Override
    public UniqueIdItem<ItemStack> uniqueEmptyItem() {
        return this.emptyUniqueItem;
    }

    @Override
    public void delayedInit() {
        Bukkit.getPluginManager().registerEvents(this.itemEventListener, this.plugin.javaPlugin());
        Bukkit.getPluginManager().registerEvents(this.debugStickListener, this.plugin.javaPlugin());
        Bukkit.getPluginManager().registerEvents(this.armorEventListener, this.plugin.javaPlugin());
    }

    @Override
    public NetworkItemHandler<ItemStack> networkItemHandler() {
        return this.networkItemHandler;
    }

    public static BukkitItemManager instance() {
        return instance;
    }

    @Override
    public Optional<Item<ItemStack>> s2c(Item<ItemStack> item, Player player) {
        if (item.isEmpty()) return Optional.empty();
        return this.networkItemHandler.s2c(item, player);
    }

    @Override
    public Optional<Item<ItemStack>> c2s(Item<ItemStack> item) {
        if (item.isEmpty()) return Optional.empty();
        return this.networkItemHandler.c2s(item);
    }

    public Optional<ItemStack> s2c(ItemStack item, Player player) {
        if (item.isEmpty()) return Optional.empty();
        return this.networkItemHandler.s2c(wrap(item), player).map(Item::getItem);
    }

    public Optional<ItemStack> c2s(ItemStack item) {
        if (item.isEmpty()) return Optional.empty();
        return this.networkItemHandler.c2s(wrap(item)).map(Item::getItem);
    }

    @Override
    public Item<ItemStack> build(DatapackRecipeResult result) {
        if (result.components() == null) {
            ItemStack itemStack = createVanillaItemStack(Key.of(result.id()));
            return wrap(itemStack).count(result.count());
        } else {
            // 低版本无法应用nbt或组件,所以这里是1.20.5+
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", result.id());
            jsonObject.addProperty("count", result.count());
            jsonObject.add("components", result.components());
            Object nmsStack = CoreReflections.instance$ItemStack$CODEC.parse(MRegistryOps.JSON, jsonObject)
                    .resultOrPartial((itemId) -> plugin.logger().severe("Tried to load invalid item: '" + itemId + "'")).orElse(null);
            if (nmsStack == null) {
                return this.emptyItem;
            }
            return wrap(FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(nmsStack));
        }
    }

    @Override
    public Optional<BuildableItem<ItemStack>> getVanillaItem(Key key) {
        ItemStack vanilla = createVanillaItemStack(key);
        if (vanilla == null) {
            return Optional.empty();
        }
        return Optional.of(CloneableConstantItem.of(this.wrap(vanilla)));
    }

    @Override
    public int fuelTime(ItemStack itemStack) {
        if (ItemStackUtils.isEmpty(itemStack)) return 0;
        Optional<CustomItem<ItemStack>> customItem = wrap(itemStack).getCustomItem();
        return customItem.map(it -> it.settings().fuelTime()).orElse(0);
    }

    @Override
    public int fuelTime(Key id) {
        return getCustomItem(id).map(it -> it.settings().fuelTime()).orElse(0);
    }

    @Override
    public void disable() {
        this.unload();
        HandlerList.unregisterAll(this.itemEventListener);
        HandlerList.unregisterAll(this.debugStickListener);
        HandlerList.unregisterAll(this.armorEventListener);
        this.persistLastRegisteredPatterns();
    }

    @Override
    protected void registerArmorTrimPattern(Collection<Key> equipments) {
        if (equipments.isEmpty()) return;
        this.lastRegisteredPatterns = new HashSet<>(equipments);
        // 可能还没加载
        if (Config.sacrificedAssetId() != null)
            this.lastRegisteredPatterns.add(Config.sacrificedAssetId());
        Object registry = FastNMS.INSTANCE.method$RegistryAccess$lookupOrThrow(FastNMS.INSTANCE.registryAccess(), MRegistries.TRIM_PATTERN);
        try {
            CoreReflections.field$MappedRegistry$frozen.set(registry, false);
            for (Key assetId : this.lastRegisteredPatterns) {
                Object resourceLocation = KeyUtils.toResourceLocation(assetId);
                Object previous = FastNMS.INSTANCE.method$Registry$getValue(registry, resourceLocation);
                if (previous == null) {
                    Object trimPattern = createTrimPattern(assetId);
                    Object holder = CoreReflections.method$Registry$registerForHolder.invoke(null, registry, resourceLocation, trimPattern);
                    CoreReflections.method$Holder$Reference$bindValue.invoke(holder, trimPattern);
                    CoreReflections.field$Holder$Reference$tags.set(holder, Set.of());
                }
            }
        } catch (Exception e) {
            this.plugin.logger().warn("Failed to register armor trim pattern.", e);
        } finally {
            try {
                CoreReflections.field$MappedRegistry$frozen.set(registry, true);
            } catch (ReflectiveOperationException ignored) {
            }
        }
    }

    private void persistLastRegisteredPatterns() {
        Path persistTrimPatternPath = this.plugin.dataFolderPath()
                .resolve("cache")
                .resolve("trim_patterns.json");
        try {
            Files.createDirectories(persistTrimPatternPath.getParent());
            JsonObject json = new JsonObject();
            JsonArray jsonElements = new JsonArray();
            for (Key key : this.lastRegisteredPatterns) {
                jsonElements.add(new JsonPrimitive(key.toString()));
            }
            json.add("patterns", jsonElements);
            if (jsonElements.isEmpty()) {
                if (Files.exists(persistTrimPatternPath)) {
                    Files.delete(persistTrimPatternPath);
                }
            } else {
                GsonHelper.writeJsonFile(json, persistTrimPatternPath);
            }
        } catch (IOException e) {
            this.plugin.logger().warn("Failed to persist registered trim patterns.", e);
        }
    }

    // 需要持久化存储上一次注册的新trim类型，如果注册晚了，加载世界可能导致一些物品损坏
    private void loadLastRegisteredPatterns() {
        Path persistTrimPatternPath = this.plugin.dataFolderPath()
                .resolve("cache")
                .resolve("trim_patterns.json");
        if (Files.exists(persistTrimPatternPath) && Files.isRegularFile(persistTrimPatternPath)) {
            try {
                JsonObject cache = GsonHelper.readJsonFile(persistTrimPatternPath).getAsJsonObject();
                JsonArray patterns = cache.getAsJsonArray("patterns");
                Set<Key> trims = new HashSet<>();
                for (JsonElement element : patterns) {
                    if (element instanceof JsonPrimitive primitive) {
                        trims.add(Key.of(primitive.getAsString()));
                    }
                }
                this.registerArmorTrimPattern(trims);
                this.lastRegisteredPatterns = trims;
            } catch (IOException e) {
                this.plugin.logger().warn("Failed to load registered trim patterns.", e);
            }
        }
    }

    private void registerCustomTrimMaterial() {
        Object registry = FastNMS.INSTANCE.method$RegistryAccess$lookupOrThrow(FastNMS.INSTANCE.registryAccess(), MRegistries.TRIM_MATERIAL);
        Object resourceLocation = KeyUtils.toResourceLocation(Key.of("minecraft", AbstractPackManager.NEW_TRIM_MATERIAL));
        Object previous = FastNMS.INSTANCE.method$Registry$getValue(registry, resourceLocation);
        if (previous == null) {
            try {
                CoreReflections.field$MappedRegistry$frozen.set(registry, false);
                Object trimMaterial = createTrimMaterial();
                Object holder = CoreReflections.method$Registry$registerForHolder.invoke(null, registry, resourceLocation, trimMaterial);
                CoreReflections.method$Holder$Reference$bindValue.invoke(holder, trimMaterial);
                CoreReflections.field$Holder$Reference$tags.set(holder, Set.of());
            } catch (Exception e) {
                this.plugin.logger().warn("Failed to register trim material.", e);
            } finally {
                try {
                    CoreReflections.field$MappedRegistry$frozen.set(registry, true);
                } catch (ReflectiveOperationException ignored) {
                }
            }
        }
    }

    private Object createTrimPattern(Key key) throws ReflectiveOperationException {
        if (VersionHelper.isOrAbove1_21_5()) {
            return CoreReflections.constructor$TrimPattern.newInstance(KeyUtils.toResourceLocation(key), CoreReflections.instance$Component$empty, false);
        } else if (VersionHelper.isOrAbove1_20_2()) {
            return CoreReflections.constructor$TrimPattern.newInstance(KeyUtils.toResourceLocation(key), this.bedrockItemHolder, CoreReflections.instance$Component$empty, false);
        } else {
            return CoreReflections.constructor$TrimPattern.newInstance(KeyUtils.toResourceLocation(key), this.bedrockItemHolder, CoreReflections.instance$Component$empty);
        }
    }

    private Object createTrimMaterial() throws ReflectiveOperationException {
        if (VersionHelper.isOrAbove1_21_5()) {
            Object assetGroup = CoreReflections.method$MaterialAssetGroup$create.invoke(null, "custom");
            return CoreReflections.constructor$TrimMaterial.newInstance(assetGroup, CoreReflections.instance$Component$empty);
        } else if (VersionHelper.isOrAbove1_21_4()) {
            return CoreReflections.constructor$TrimMaterial.newInstance("custom", this.bedrockItemHolder, Map.of(), CoreReflections.instance$Component$empty);
        } else {
            return CoreReflections.constructor$TrimMaterial.newInstance("custom", this.bedrockItemHolder, 0f, Map.of(), CoreReflections.instance$Component$empty);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public Item<ItemStack> fromByteArray(byte[] bytes) {
        return this.factory.wrap(Bukkit.getUnsafe().deserializeItem(bytes));
    }

    @Override
    public ItemStack buildCustomItemStack(Key id, Player player) {
        return Optional.ofNullable(this.customItemsById.get(id)).map(it -> it.buildItemStack(ItemBuildContext.of(player), 1)).orElse(null);
    }

    @Override
    public ItemStack buildItemStack(Key id, @Nullable Player player) {
        ItemStack customItem = buildCustomItemStack(id, player);
        if (customItem != null) {
            return customItem;
        }
        return createVanillaItemStack(id);
    }

    @Override
    public Item<ItemStack> createCustomWrappedItem(Key id, Player player) {
        return Optional.ofNullable(customItemsById.get(id)).map(it -> it.buildItem(player)).orElse(null);
    }

    @Override
    public Item<ItemStack> createWrappedItem(Key id, @Nullable Player player) {
        CustomItem<ItemStack> customItem = this.customItemsById.get(id);
        if (customItem != null) {
            return customItem.buildItem(player);
        }
        ItemStack itemStack = this.createVanillaItemStack(id);
        if (itemStack != null) {
            return wrap(itemStack);
        }
        return null;
    }

    @Nullable
    private ItemStack createVanillaItemStack(Key id) {
        Object item = FastNMS.INSTANCE.method$Registry$getValue(MBuiltInRegistries.ITEM, KeyUtils.toResourceLocation(id));
        if (item == MItems.AIR && !id.equals(ItemKeys.AIR)) {
            return null;
        }
        return FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(FastNMS.INSTANCE.constructor$ItemStack(item, 1));
    }

    @Override
    public @NotNull Item<ItemStack> wrap(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) return this.emptyItem;
        return this.factory.wrap(itemStack);
    }

    @Override
    protected CustomItem.Builder<ItemStack> createPlatformItemBuilder(UniqueKey id, Key materialId, Key clientBoundMaterialId) {
        Object item = FastNMS.INSTANCE.method$Registry$getValue(MBuiltInRegistries.ITEM, KeyUtils.toResourceLocation(materialId));
        Object clientBoundItem = materialId == clientBoundMaterialId ? item : FastNMS.INSTANCE.method$Registry$getValue(MBuiltInRegistries.ITEM, KeyUtils.toResourceLocation(clientBoundMaterialId));
        if (item == MItems.AIR) {
            throw new LocalizedResourceConfigException("warning.config.item.invalid_material", materialId.toString());
        }
        if (clientBoundItem == MItems.AIR) {
            throw new LocalizedResourceConfigException("warning.config.item.invalid_material", clientBoundMaterialId.toString());
        }
        return BukkitCustomItem.builder(item, clientBoundItem)
                .id(id)
                .material(materialId)
                .clientBoundMaterial(clientBoundMaterialId);
    }

    @SuppressWarnings("unchecked")
    private void registerAllVanillaItems() {
        try {
            for (Object item : (Iterable<?>) MBuiltInRegistries.ITEM) {
                Object resourceLocation = FastNMS.INSTANCE.method$Registry$getKey(MBuiltInRegistries.ITEM, item);
                Key itemKey = KeyUtils.resourceLocationToKey(resourceLocation);
                VANILLA_ITEMS.add(itemKey);
                super.cachedVanillaItemSuggestions.add(Suggestion.suggestion(itemKey.asString()));
                UniqueKey uniqueKey = UniqueKey.create(itemKey);
                Object mcHolder = FastNMS.INSTANCE.method$Registry$getHolderByResourceKey(MBuiltInRegistries.ITEM, FastNMS.INSTANCE.method$ResourceKey$create(MRegistries.ITEM, resourceLocation)).get();
                Set<Object> tags = (Set<Object>) CoreReflections.field$Holder$Reference$tags.get(mcHolder);
                for (Object tag : tags) {
                    Key tagId = Key.of(CoreReflections.field$TagKey$location.get(tag).toString());
                    VANILLA_ITEM_TAGS.computeIfAbsent(tagId, (key) -> new ArrayList<>()).add(uniqueKey);
                }
            }
        } catch (ReflectiveOperationException e) {
            plugin.logger().warn("Failed to init vanilla items", e);
        }
    }

    // 1.20-1.21.4 template 不为空
    // 1.21.5+ pattern 不为空
    @Override
    public Item<ItemStack> applyTrim(Item<ItemStack> base, Item<ItemStack> addition, Item<ItemStack> template, Key pattern) {
        Optional<?> optionalMaterial = FastNMS.INSTANCE.method$TrimMaterials$getFromIngredient(addition.getLiteralObject());
        Optional<?> optionalPattern = VersionHelper.isOrAbove1_21_5() ?
                FastNMS.INSTANCE.method$Registry$getHolderByResourceLocation(FastNMS.INSTANCE.method$RegistryAccess$lookupOrThrow(FastNMS.INSTANCE.registryAccess(), MRegistries.TRIM_PATTERN), KeyUtils.toResourceLocation(pattern)) :
                FastNMS.INSTANCE.method$TrimPatterns$getFromTemplate(template.getLiteralObject());
        if (optionalMaterial.isPresent() && optionalPattern.isPresent()) {
            Object armorTrim = FastNMS.INSTANCE.constructor$ArmorTrim(optionalMaterial.get(), optionalPattern.get());
            Object previousTrim;
            if (VersionHelper.isOrAbove1_20_5()) {
                previousTrim = base.getExactComponent(DataComponentKeys.TRIM);
            } else {
                try {
                    previousTrim = VersionHelper.isOrAbove1_20_2() ?
                    ((Optional<?>) CoreReflections.method$ArmorTrim$getTrim.invoke(null, FastNMS.INSTANCE.registryAccess(), base.getLiteralObject(), true)).orElse(null) :
                    ((Optional<?>) CoreReflections.method$ArmorTrim$getTrim.invoke(null, FastNMS.INSTANCE.registryAccess(), base.getLiteralObject())).orElse(null);
                } catch (ReflectiveOperationException e) {
                    this.plugin.logger().warn("Failed to get armor trim", e);
                    return this.emptyItem;
                }
            }
            if (armorTrim.equals(previousTrim)) {
                return this.emptyItem;
            }
            Item<ItemStack> newItem = base.copyWithCount(1);
            if (VersionHelper.isOrAbove1_20_5()) {
                newItem.setExactComponent(DataComponentKeys.TRIM, armorTrim);
            } else {
                try {
                    CoreReflections.method$ArmorTrim$setTrim.invoke(null, FastNMS.INSTANCE.registryAccess(), newItem.getLiteralObject(), armorTrim);
                } catch (ReflectiveOperationException e) {
                    this.plugin.logger().warn("Failed to set armor trim", e);
                    return this.emptyItem;
                }
            }
            return newItem;
        }
        return this.emptyItem;
    }

    @Nullable // 1.21.5+
    public Function<Object, Integer> decoratedHashOpsGenerator() {
        return decoratedHashOpsGenerator;
    }
}
