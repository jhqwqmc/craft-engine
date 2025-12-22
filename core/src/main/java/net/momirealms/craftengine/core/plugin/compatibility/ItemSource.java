package net.momirealms.craftengine.core.plugin.compatibility;

import net.momirealms.craftengine.core.item.ItemBuildContext;
import org.jetbrains.annotations.Nullable;

public interface ItemSource<I> {

    String plugin();

    @Nullable
    I build(String id, ItemBuildContext context);

    String id(I item);
}