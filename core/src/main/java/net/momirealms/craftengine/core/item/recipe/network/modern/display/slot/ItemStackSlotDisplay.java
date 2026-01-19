package net.momirealms.craftengine.core.item.recipe.network.modern.display.slot;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;

import java.util.function.Function;

public class ItemStackSlotDisplay<I> implements SlotDisplay<I> {
    private Item<I> item;

    public ItemStackSlotDisplay(Item<I> item) {
        this.item = item;
    }

    public static <I> ItemStackSlotDisplay<I> read(FriendlyByteBuf buf, FriendlyByteBuf.Reader<Item<I>> reader) {
        Item<I> itemStack = reader.apply(buf);
        return new ItemStackSlotDisplay<>(itemStack);
    }

    @Override
    public void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer) {
        buf.writeVarInt(3);
        writer.accept(buf, item);
    }

    @Override
    public void applyClientboundData(Function<Item<I>, Item<I>> function) {
        this.item = function.apply(this.item);
    }

    public Item<I> item() {
        return item;
    }

    @Override
    public String toString() {
        return "ItemStackSlotDisplay{" +
                "item=" + this.item.getLiteralObject() +
                '}';
    }
}
