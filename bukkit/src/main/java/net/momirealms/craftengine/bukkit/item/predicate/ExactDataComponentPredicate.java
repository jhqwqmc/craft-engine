package net.momirealms.craftengine.bukkit.item.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.bukkit.util.RegistryOps;
import net.momirealms.craftengine.bukkit.util.RegistryUtils;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.component.predicate.DataComponentPredicate;
import net.momirealms.craftengine.core.item.component.predicate.DataComponentPredicateFactory;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.proxy.minecraft.core.component.DataComponentTypeProxy;
import net.momirealms.craftengine.proxy.minecraft.core.registries.BuiltInRegistriesProxy;
import net.momirealms.sparrow.nbt.Tag;

public final class ExactDataComponentPredicate implements DataComponentPredicate {
    public static final DataComponentPredicateFactory<ExactDataComponentPredicate> FACTORY = new Factory();
    private final Key dataComponentType;
    private final Object exactData;

    public ExactDataComponentPredicate(Key dataComponentType, Object exactData) {
        this.dataComponentType = dataComponentType;
        this.exactData = exactData;
    }

    @Override
    public void apply(Item item) {
        item.setExactComponent(this.dataComponentType, this.exactData);
    }

    @Override
    public boolean test(Item item) {
        Object exactComponent = item.getExactComponent(this.dataComponentType);
        if (exactComponent == null) {
            exactComponent = item.type().getExactComponent(this.dataComponentType);
        }
        if (exactComponent == null) {
            return false;
        }
        return exactComponent.equals(this.exactData);
    }

    private static class Factory implements DataComponentPredicateFactory<ExactDataComponentPredicate> {

        @Override
        public ExactDataComponentPredicate create(ConfigSection section) {
            Key componentType = section.getNonNullKey("component");
            Tag componentValue = section.getNonNullValue("value", ConfigConstants.ARGUMENT_ANY, ConfigValue::getAsTag);
            Object dataComponentType = RegistryUtils.getRegistryValue(BuiltInRegistriesProxy.DATA_COMPONENT_TYPE, KeyUtils.toIdentifier(componentType));
            if (dataComponentType == null) {
                throw new KnownResourceException("resource.item.unknown_data_component_type", section.assemblePath("component"), componentType.asString());
            }
            Codec<Object> codec = DataComponentTypeProxy.INSTANCE.codecOrThrow(dataComponentType);
            DataResult<Object> result = codec.parse(RegistryOps.SPARROW_NBT, componentValue);
            if (result.isError()) {
                throw new IllegalArgumentException(result.toString());
            }
            return new ExactDataComponentPredicate(componentType, result.result().get());
        }
    }
}
