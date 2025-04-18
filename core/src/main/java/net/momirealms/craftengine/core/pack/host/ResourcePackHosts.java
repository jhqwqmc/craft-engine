package net.momirealms.craftengine.core.pack.host;

import net.momirealms.craftengine.core.pack.host.impl.*;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;

public class ResourcePackHosts {
    public static final Key NONE = Key.of("craftengine:none");
    public static final Key SELF_HOST = Key.of("craftengine:self_host");
    public static final Key EXTERNAL_HOST = Key.of("craftengine:external_host");
    public static final Key LOBFILE = Key.of("craftengine:lobfile");
    public static final Key S3_HOST = Key.of("craftengine:s3_host");
    public static final Key CUSTOM_API_HOST = Key.of("craftengine:custom_api_host");
    public static final Key ALIST_HOST = Key.of("craftengine:alist_host");
    public static final Key DROPBOX_HOST = Key.of("craftengine:dropbox_host");

    static {
        register(NONE, NoneHost.FACTORY);
        register(SELF_HOST, SelfHost.FACTORY);
        register(EXTERNAL_HOST, ExternalHost.FACTORY);
        register(LOBFILE, LobFileHost.FACTORY);
        register(S3_HOST, S3Host.FACTORY);
        register(CUSTOM_API_HOST, CustomApiHost.FACTORY);
        register(ALIST_HOST, AlistHost.FACTORY);
        register(DROPBOX_HOST, DropboxHost.FACTORY);
    }

    public static void register(Key key, ResourcePackHostFactory factory) {
        Holder.Reference<ResourcePackHostFactory> holder = ((WritableRegistry<ResourcePackHostFactory>) BuiltInRegistries.RESOURCE_PACK_HOST_FACTORY)
                .registerForHolder(new ResourceKey<>(Registries.RESOURCE_PACK_HOST_FACTORY.location(), key));
        holder.bindValue(factory);
    }

    public static ResourcePackHost fromMap(Map<String, Object> map) {
        String type = (String) map.get("type");
        if (type == null) {
            throw new NullPointerException("host type cannot be null");
        }
        Key key = Key.withDefaultNamespace(type, "craftengine");
        ResourcePackHostFactory factory = BuiltInRegistries.RESOURCE_PACK_HOST_FACTORY.getValue(key);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown resource pack host type: " + type);
        }
        return factory.create(map);
    }
}
