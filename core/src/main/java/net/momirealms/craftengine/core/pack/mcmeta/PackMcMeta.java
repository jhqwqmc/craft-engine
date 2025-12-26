package net.momirealms.craftengine.core.pack.mcmeta;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public final class PackMcMeta {
    private final List<Overlay> overlays;

    public PackMcMeta(JsonObject mcmeta) {
        this.overlays = getOverlays(mcmeta);
    }

    private List<Overlay> getOverlays(JsonObject mcmeta) {
        List<Overlay> overlays = new ArrayList<>();
        JsonObject overlaysJson = mcmeta.getAsJsonObject("overlays");
        if (overlaysJson != null) {
            JsonArray entries = overlaysJson.getAsJsonArray("entries");
            if (entries != null) {
                for (JsonElement overlayJson : entries) {
                    if (overlayJson instanceof JsonObject overlayJsonObj) {
                        overlays.add(new Overlay(overlayJsonObj));
                    }
                }
            }
        }
        return overlays;
    }

    public List<Overlay> overlays() {
        return this.overlays;
    }
}
