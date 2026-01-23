package net.momirealms.craftengine.core.item;

import net.momirealms.craftengine.core.entity.EquipmentSlot;
import net.momirealms.craftengine.core.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface ItemWrapper<I> {

    I getItem();

    Object getLiteralObject();

    int count();

    void count(int amount);

    ItemWrapper<I> copyWithCount(int count);

    void shrink(int amount);

    void grow(int amount);

    void hurtAndBreak(int amount, @Nullable Player player, @Nullable EquipmentSlot slot);
}
