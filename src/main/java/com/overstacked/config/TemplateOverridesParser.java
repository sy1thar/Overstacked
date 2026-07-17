package com.overstacked.config;

import com.overstacked.Overstacked;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.file.Files;
import java.nio.file.Path;

public final class TemplateOverridesParser {

    private static final int CURRENT_VERSION = 2;

    private TemplateOverridesParser() {}

    public static boolean isOldFormat(Path path) {
        try {
            String json = Files.readString(path);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            return !root.has("version") || root.get("version").getAsInt() < CURRENT_VERSION;
        } catch (Exception e) {
            return true;
        }
    }

    public static void parse(Path path, OverstackedConfig config) {
        try {
            String json = Files.readString(path);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            if (!root.has("overrides")) {
                Overstacked.LOGGER.warn("[Overstacked] JSON overrides file has no 'overrides' array");
                return;
            }

            JsonArray overrides = root.getAsJsonArray("overrides");
            for (JsonElement elem : overrides) {
                if (!elem.isJsonObject()) continue;
                JsonObject obj = elem.getAsJsonObject();

                if (!obj.has("stackSize")) {
                    Overstacked.LOGGER.warn("[Overstacked] Skipping override without 'stackSize': {}", obj);
                    continue;
                }

                int stackSize = obj.get("stackSize").getAsInt();
                if (stackSize <= 0) {
                    Overstacked.LOGGER.warn("[Overstacked] Skipping non-positive stackSize: {}", stackSize);
                    continue;
                }

                if (obj.has("id")) {
                    config.addItemOverride(obj.get("id").getAsString(), stackSize);
                } else if (obj.has("tag")) {
                    config.addTagOverride("#" + obj.get("tag").getAsString(), stackSize);
                } else {
                    Overstacked.LOGGER.warn("[Overstacked] Override missing 'id' or 'tag': {}", obj);
                }
            }

            Overstacked.LOGGER.info("[Overstacked] Parsed JSON template overrides from {}", path.getFileName());
        } catch (Exception e) {
            Overstacked.LOGGER.error("[Overstacked] Failed to parse JSON overrides: {}", path, e);
        }
    }
}
