package net.momirealms.craftengine.core.item.processor;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class ProfileProcessor implements SimpleNetworkItemProcessor {
    public static final ItemProcessorFactory<ProfileProcessor> FACTORY = new Factory();
    private static final Object[] NBT_PATH = new Object[] {"SkullOwner"};
    private final String profileName;
    private final String base64Data;

    public ProfileProcessor(String profileName, String base64Data) {
        this.profileName = profileName;
        this.base64Data = base64Data;
    }

    @Override
    public <I> Item<I> apply(Item<I> item, ItemBuildContext context) {
        if (this.profileName != null) {
            Component resultComponent = AdventureHelper.customMiniMessage().deserialize(this.profileName, context.tagResolvers());
            String resultString = AdventureHelper.plainTextContent(resultComponent);
            if (VersionHelper.isOrAbove1_20_5()) {
                item.setJavaComponent(DataComponentKeys.PROFILE, resultString);
            } else {
                item.setTag(resultString, "SkullOwner");
            }
        } else {
            item.skull(this.base64Data);
        }
        return item;
    }

    private static class Factory implements ItemProcessorFactory<ProfileProcessor> {

        @Override
        public ProfileProcessor create(Object arg) {
            if (arg instanceof String guess) {
                String base64Data = null;
                if (guess.startsWith("http://") || guess.startsWith("https://")) {
                    base64Data = Base64Utils.encode("{\"textures\":{\"SKIN\":{\"url\":\"" + guess + "\"}}}");
                } else if (guess.length() > 16 && guess.matches("^[-A-Za-z0-9+/]*={0,3}$")) {
                    base64Data = guess;
                }
                if (base64Data != null) {
                    return new ProfileProcessor(null, base64Data);
                } else {
                    return new ProfileProcessor(guess, null);
                }
            } else {
                Map<String, Object> profile = ResourceConfigUtils.getAsMap(arg, "profile");
                String base64Data = ResourceConfigUtils.getAsStringOrNull(profile.get("base64"));
                if (base64Data == null && profile.containsKey("url")) {
                    String url = ResourceConfigUtils.getAsStringOrNull(profile.get("url"));
                    base64Data = Base64Utils.encode("{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}");
                }
                if (base64Data != null) {
                    return new ProfileProcessor(null, base64Data);
                }
                String profileName = ResourceConfigUtils.getAsStringOrNull(profile.getOrDefault("name", "<arg:player.name>"));
                return new ProfileProcessor(profileName, null);
            }
        }
    }

    @Override
    public <I> String nbtPathString(Item<I> item, ItemBuildContext context) {
        return "SkullOwner";
    }

    @Override
    public <I> @NotNull Object[] nbtPath(Item<I> item, ItemBuildContext context) {
        return NBT_PATH;
    }

    @Override
    public @NotNull <I> Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.PROFILE;
    }
}
