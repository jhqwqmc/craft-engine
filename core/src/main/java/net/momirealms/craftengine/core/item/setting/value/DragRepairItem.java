package net.momirealms.craftengine.core.item.setting.value;

import net.momirealms.craftengine.core.sound.SoundData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record DragRepairItem(List<String> targets, int amount, double percent, @Nullable SoundData sound) {
}
