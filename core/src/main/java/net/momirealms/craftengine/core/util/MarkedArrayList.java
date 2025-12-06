package net.momirealms.craftengine.core.util;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;

public class MarkedArrayList<T> extends ArrayList<T> {
    @Serial
    private static final long serialVersionUID = 1L;

    public MarkedArrayList() {
    }

    public MarkedArrayList(@NotNull Collection<? extends T> c) {
        super(c);
    }
}
