package net.momirealms.craftengine.core.plugin.context.number;

import net.momirealms.craftengine.core.plugin.context.Context;

public interface NumberProvider {

    float getFloat(Context context);

    double getDouble(Context context);

    default int getInt(Context context) {
        return Math.round(this.getFloat(context));
    }
}
