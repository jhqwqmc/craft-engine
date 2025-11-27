package net.momirealms.craftengine.core.world.chunk.client;

import net.momirealms.craftengine.core.world.SectionPos;
import net.momirealms.craftengine.core.world.WorldHeight;
import org.jetbrains.annotations.Nullable;

public class ClientChunk {
    @Nullable
    public final ClientSection[] sections;
    private final WorldHeight worldHeight;

    public ClientChunk(ClientSection[] sections, WorldHeight worldHeight) {
        this.sections = sections;
        this.worldHeight = worldHeight;
    }

    @Nullable
    public ClientSection[] sections() {
        return sections;
    }

    public boolean isOccluding(int x, int y, int z) {
        if (this.sections == null) return false;
        int index = sectionIndex(SectionPos.blockToSectionCoord(y));
        ClientSection section = this.sections[index];
        if (section == null) return false;
        return section.isOccluding((y & 15) << 8 | (z & 15) << 4 | x & 15);
    }

    public int sectionIndex(int sectionId) {
        return sectionId - this.worldHeight.getMinSection();
    }
}
