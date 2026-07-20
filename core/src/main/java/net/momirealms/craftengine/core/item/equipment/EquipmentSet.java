package net.momirealms.craftengine.core.item.equipment;

import net.momirealms.craftengine.core.attribute.AttributeModifierConfig;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EquipmentSet {
    private final Entry[] entries;
    private final boolean accumulate;

    public EquipmentSet(Entry[] entries, boolean accumulate) {
        this.entries = entries;
        this.accumulate = accumulate;
    }

    public static EquipmentSet fromConfig(ConfigSection section) {
        boolean accumulate = section.getBoolean("accumulate", true);
        ConfigSection piecesSection = section.getNonNullSection("pieces");
        int maxPieces = 0;
        Map<Integer, Entry> entries = new HashMap<>();
        for (String pieceStr : piecesSection.keySet()) {
            int pieceCount = Integer.parseInt(pieceStr);
            maxPieces = Math.max(maxPieces, pieceCount);
            entries.put(pieceCount, Entry.fromConfig(piecesSection.getSection(pieceStr)));
        }
        Entry[] entriesArray = new Entry[maxPieces];
        for (Map.Entry<Integer, Entry> entry : entries.entrySet()) {
            entriesArray[entry.getKey() - 1] = entry.getValue();
        }
        return new EquipmentSet(entriesArray, accumulate);
    }

    public List<AttributeModifierConfig> getAttributeModifiers(int pieces) {
        if (pieces == 0) return List.of();
        if (this.accumulate) {
            List<AttributeModifierConfig> list = new ArrayList<>(4);
            for (int i = 0; i <= pieces - 1 && i < this.entries.length; i++) {
                Entry entry = this.entries[i];
                list.addAll(entry.modifiers);
            }
            return list;
        } else {
            if (pieces > this.entries.length) return List.of();
            Entry entry = this.entries[pieces - 1];
            return entry == null ? List.of() : entry.modifiers;
        }
    }

    public static class Entry {
        private final List<AttributeModifierConfig> modifiers;

        public Entry(List<AttributeModifierConfig> modifiers) {
            this.modifiers = modifiers;
        }

        public static Entry fromConfig(ConfigSection section) {
            List<AttributeModifierConfig> modifiers = section.getSectionList("attribute", AttributeModifierConfig::fromConfig);
            return new Entry(modifiers);
        }
    }
}
