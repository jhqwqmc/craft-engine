package net.momirealms.craftengine.bukkit.compatibility.model.bettermodel;

import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.data.renderer.ModelRenderer;
import net.momirealms.craftengine.bukkit.block.entity.renderer.element.BukkitBlockEntityElementConfigs;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.entity.Entity;

public class BetterModelUtils {

    public static void bindModel(Entity base, String id) {
        ModelRenderer renderer = BetterModel.plugin().modelManager().model(id);
        if (renderer == null) {
            throw new NullPointerException("Could not find BetterModel blueprint " + id);
        }
        renderer.create(base);
    }

    public static void registerConstantBlockEntityRender() {
        BukkitBlockEntityElementConfigs.register(Key.of("craftengine:better_model"), new BetterModelBlockEntityElementConfig.Factory());
    }
}
