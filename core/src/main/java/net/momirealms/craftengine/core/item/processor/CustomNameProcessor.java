package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.text.minimessage.FormattedLine;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

public class CustomNameProcessor<I> implements SimpleNetworkItemProcessor<I> {
    public static final ItemProcessorFactory<?> FACTORY = new Factory<>();
    private static final Object[] NBT_PATH = new Object[]{"display", "Name"};
    private final String argument;
    private final FormattedLine line;

    public CustomNameProcessor(String argument) {
        if (Config.addNonItalicTag()) {
            if (argument.startsWith("<!i>")) {
                this.argument = argument;
            } else {
                this.argument  = "<!i>" + argument;
            }
        } else {
            this.argument = argument;
        }
        this.line = FormattedLine.create(this.argument);
    }

    public String customName() {
        return argument;
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        item.customNameComponent(this.line.parse(context));
        return item;
    }

    @Override
    public @Nullable Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.CUSTOM_NAME;
    }

    @Override
    public @Nullable Object[] nbtPath(Item<I> item, ItemBuildContext context) {
        return NBT_PATH;
    }

    @Override
    public String nbtPathString(Item<I> item, ItemBuildContext context) {
        return "display.Name";
    }

    private static class Factory<I> implements ItemProcessorFactory<I> {

        @Override
        public ItemProcessor<I> create(Object arg) {
            String name = arg.toString();
            return new CustomNameProcessor<>(name);
        }
    }
}
