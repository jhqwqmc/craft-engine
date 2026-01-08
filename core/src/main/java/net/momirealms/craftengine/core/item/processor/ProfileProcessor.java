package net.momirealms.craftengine.core.item.processor;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.util.*;

import java.util.Map;

public class ProfileProcessor implements SimpleNetworkItemProcessor {
    public static final ItemProcessorFactory<ProfileProcessor> FACTORY = new Factory();
    private final String profileName;
    private final String base64Data;

    public ProfileProcessor(String profileName, String base64Data) {
        this.profileName = profileName;
        this.base64Data = base64Data;
    }

    @Override
    public <I> Item<I> apply(Item<I> item, ItemBuildContext context) {
        if (this.base64Data == null) {
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
            Map<String, Object> profile = ResourceConfigUtils.getAsMap(arg, "profile");
            String profileName = ResourceConfigUtils.getAsStringOrNull(profile.getOrDefault("name", "<arg:player.name>"));
            Object raw = profile.get("data");
            String base64Data = ResourceConfigUtils.getAsStringOrNull(raw);
            if (base64Data == null && profile.containsKey("url")) {
                String url = ResourceConfigUtils.getAsStringOrNull(profile.get("url"));
                base64Data = Base64Utils.encode("{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}");
            } else if (base64Data != null && base64Data.indexOf('{') == 0) {
                base64Data = Base64Utils.encode(GsonHelper.get().toJson(raw));
            }
            return new ProfileProcessor(profileName, base64Data);
        }
    }
}
