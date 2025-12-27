package net.momirealms.craftengine.core.pack.host;

import net.momirealms.craftengine.core.pack.host.impl.*;
import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;

public final class ResourcePackHosts {
    public static final ResourcePackHostType NONE = register(Key.ce("none"), NoneHost.FACTORY);
    public static final ResourcePackHostType SELF = register(Key.ce("self"), SelfHost.FACTORY);
    public static final ResourcePackHostType EXTERNAL = register(Key.ce("external"), ExternalHost.FACTORY);
    public static final ResourcePackHostType LOBFILE = register(Key.ce("lobfile"), LobFileHost.FACTORY);
    public static final ResourcePackHostType S3 = register(Key.ce("s3"), S3HostFactory.INSTANCE);
    public static final ResourcePackHostType ALIST = register(Key.ce("alist"), AlistHost.FACTORY);
    public static final ResourcePackHostType DROPBOX = register(Key.ce("dropbox"), DropboxHost.FACTORY);
    public static final ResourcePackHostType ONEDRIVE = register(Key.ce("onedrive"), OneDriveHost.FACTORY);
    public static final ResourcePackHostType GITLAB = register(Key.ce("gitlab"), GitLabHost.FACTORY);

    private ResourcePackHosts() {}

    public static ResourcePackHostType register(Key key, ResourcePackHostFactory factory) {
        ResourcePackHostType type = new ResourcePackHostType(key, factory);
        ((WritableRegistry<ResourcePackHostType>) BuiltInRegistries.RESOURCE_PACK_HOST_TYPE)
                .register(ResourceKey.create(Registries.RESOURCE_PACK_HOST_TYPE.location(), key), type);
        return type;
    }

    public static ResourcePackHost fromMap(Map<String, Object> map) {
        String type = (String) map.get("type");
        if (type == null) {
            throw new LocalizedException("warning.config.host.missing_type");
        }
        Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
        ResourcePackHostType hostType = BuiltInRegistries.RESOURCE_PACK_HOST_TYPE.getValue(key);
        if (hostType == null) {
            throw new LocalizedException("warning.config.host.invalid_type", type);
        }
        return hostType.factory().create(map);
    }
}
