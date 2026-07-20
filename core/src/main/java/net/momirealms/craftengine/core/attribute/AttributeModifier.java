package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.Key;

import java.util.function.Predicate;

public interface AttributeModifier {
    Predicate<Context> condition();

    Key id();

    double amount(Context context);

    Key operation();
}
