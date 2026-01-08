package net.momirealms.craftengine.core.item.processor;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.VersionHelper;

import java.util.Map;

public class ProfileProcessor implements SimpleNetworkItemProcessor {
    public static final ItemProcessorFactory<ProfileProcessor> FACTORY = new Factory();
    private final String profileName;

    public ProfileProcessor(String profileName) {
        this.profileName = profileName;
    }

    @Override
    public <I> Item<I> apply(Item<I> item, ItemBuildContext context) {
        Component resultComponent = AdventureHelper.customMiniMessage().deserialize(this.profileName, context.tagResolvers());
        String resultString = AdventureHelper.plainTextContent(resultComponent);
        if (VersionHelper.isOrAbove1_20_5()) {
            item.setJavaComponent(DataComponentKeys.PROFILE, resultString);
        } else {
            item.setTag(resultString, "SkullOwner");
        }
        return item;
    }

    private static class Factory implements ItemProcessorFactory<ProfileProcessor> {

        @Override
        public ProfileProcessor create(Object arg) {
            Map<String, Object> profile = ResourceConfigUtils.getAsMap(arg, "profile");
            String profileName = ResourceConfigUtils.getAsStringOrNull(profile.getOrDefault("name", "<arg:player.name>"));
            return new ProfileProcessor(profileName);
        }
    }
}
