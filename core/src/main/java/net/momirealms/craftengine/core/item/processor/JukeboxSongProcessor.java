package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.item.data.JukeboxPlayable;

public class JukeboxSongProcessor<I> implements ItemProcessor<I> {
    public static final ItemProcessorFactory<?> FACTORY = new Factory<>();
    private final JukeboxPlayable song;

    public JukeboxSongProcessor(JukeboxPlayable song) {
        this.song = song;
    }

    public JukeboxPlayable song() {
        return song;
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        item.jukeboxSong(this.song);
        return item;
    }

    private static class Factory<I> implements ItemProcessorFactory<I> {

        @Override
        public ItemProcessor<I> create(Object arg) {
            String song = arg.toString();
            return new JukeboxSongProcessor<>(new JukeboxPlayable(song, true));
        }
    }
}
