package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.item.data.JukeboxPlayable;

public final class JukeboxSongProcessor implements ItemProcessor {
    public static final ItemProcessorFactory<JukeboxSongProcessor> FACTORY = new Factory();
    private final JukeboxPlayable song;

    public JukeboxSongProcessor(JukeboxPlayable song) {
        this.song = song;
    }

    public JukeboxPlayable song() {
        return song;
    }

    @Override
    public <I> Item<I> apply(Item<I> item, ItemBuildContext context) {
        item.jukeboxSong(this.song);
        return item;
    }

    private static class Factory implements ItemProcessorFactory<JukeboxSongProcessor> {

        @Override
        public JukeboxSongProcessor create(Object arg) {
            String song = arg.toString();
            return new JukeboxSongProcessor(new JukeboxPlayable(song, true));
        }
    }
}
