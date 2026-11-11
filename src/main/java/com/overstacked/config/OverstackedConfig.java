package com.overstacked.config;

import com.overstacked.Overstacked;
import com.overstacked.network.ConfigSyncPayload;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TridentItem;

public final class OverstackedConfig {

    private static OverstackedConfig INSTANCE;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private int defaultMaxStackSize = 1_000;

    private boolean stackEnchantedBooks     = true;
    private boolean stackEnderPearlsSnowballs = true;
    private boolean stackBucketsPotions       = true;
    private boolean stackTotems               = false;
    private boolean stackToolsArmor           = false;

    private final Map<String, Integer> itemOverrides = new LinkedHashMap<>();
    private final Map<String, Integer> tagOverrides  = new LinkedHashMap<>();
    private final Map<Item, Integer> resolvedCache = new ConcurrentHashMap<>();

    private boolean loaded;

    private static final Set<String> ENCHANTED_BOOK_GROUP = Set.of("minecraft:enchanted_book");
    private static final Set<String> ENDER_PEARL_GROUP = Set.of(
            "minecraft:ender_pearl", "minecraft:snowball", "minecraft:egg", "minecraft:ender_eye"
    );
    private static final Set<String> BUCKET_POTION_GROUP = Set.of(
            "minecraft:bucket", "minecraft:water_bucket", "minecraft:lava_bucket",
            "minecraft:milk_bucket", "minecraft:potion", "minecraft:splash_potion",
            "minecraft:lingering_potion"
    );
    private static final Set<String> TOTEM_GROUP = Set.of("minecraft:totem_of_undying");

    private static final TagKey<Item> ENCHANTABLE_WEAPON = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("minecraft", "enchantable/weapon"));
    private static final TagKey<Item> ENCHANTABLE_DURABILITY = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("minecraft", "enchantable/durability"));
    private static final TagKey<Item> ENCHANTABLE_MINING = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("minecraft", "enchantable/mining"));

    private OverstackedConfig() {}

    public static OverstackedConfig getInstance() {
        if (INSTANCE == null) INSTANCE = new OverstackedConfig();
        return INSTANCE;
    }

    /* ---- lifecycle ----------------------------------------------------- */

    public void load() {
        Path configDir = Paths.get("config");
        reset();

        loadJsonConfig(configDir.resolve("overstacked.json"));

        loadOrCopy(configDir.resolve("overstacked-rules.xml"),
                "overstacked-rules.xml",
                path -> XmlConfigParser.parse(path, this));

        loadOrCopy(configDir.resolve("overstacked-template-overrides.json"),
                "overstacked-template-overrides.json",
                path -> TemplateOverridesParser.parse(path, this));

        resolvedCache.clear();
        loaded = true;

        Overstacked.LOGGER.info("[Overstacked] Config loaded — defaultMaxStackSize={}", defaultMaxStackSize);
    }

    public void reloadDiskRules() {
        Path configDir = Paths.get("config");
        tagOverrides.clear();
        itemOverrides.clear();

        Path xmlPath = configDir.resolve("overstacked-rules.xml");
        if (Files.exists(xmlPath)) {
            XmlConfigParser.parse(xmlPath, this);
        }

        Path jsonPath = configDir.resolve("overstacked-template-overrides.json");
        if (Files.exists(jsonPath)) {
            TemplateOverridesParser.parse(jsonPath, this);
        }

        resolvedCache.clear();
        Overstacked.LOGGER.info("[Overstacked] Disk rules reloaded — itemOverrides={}, tagOverrides={}",
                itemOverrides.size(), tagOverrides.size());
    }

    public void saveToDisk() {
        try {
            Path path = Paths.get("config", "overstacked.json");
            Files.createDirectories(path.getParent());
            JsonObject json = new JsonObject();
            json.addProperty("version", 2);
            json.addProperty("defaultMaxStackSize", defaultMaxStackSize);
            json.addProperty("stackEnchantedBooks", stackEnchantedBooks);
            json.addProperty("stackEnderPearlsSnowballs", stackEnderPearlsSnowballs);
            json.addProperty("stackBucketsPotions", stackBucketsPotions);
            json.addProperty("stackTotems", stackTotems);
            json.addProperty("stackToolsArmor", stackToolsArmor);
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(json, writer);
            }
            Overstacked.LOGGER.info("[Overstacked] Config saved to disk — defaultMaxStackSize={}", defaultMaxStackSize);
        } catch (IOException e) {
            Overstacked.LOGGER.error("[Overstacked] Failed to save config", e);
        }
    }

    public void applyFromPayload(ConfigSyncPayload payload) {
        reset();
        defaultMaxStackSize = payload.defaultMaxStackSize();
        stackEnchantedBooks = payload.stackEnchantedBooks();
        stackEnderPearlsSnowballs = payload.stackEnderPearlsSnowballs();
        stackBucketsPotions = payload.stackBucketsPotions();
        stackTotems = payload.stackTotems();
        stackToolsArmor = payload.stackToolsArmor();
        itemOverrides.putAll(payload.itemOverrides());
        tagOverrides.putAll(payload.tagOverrides());
        resolvedCache.clear();
        loaded = true;
    }

    /* ---- query --------------------------------------------------------- */

    public boolean isLoaded()              { return loaded; }
    public int getDefaultMaxStackSize()    { return defaultMaxStackSize; }
    public int getItemOverrideCount()      { return itemOverrides.size(); }
    public int getTagOverrideCount()       { return tagOverrides.size(); }
    public Map<String, Integer> getItemOverrides() { return itemOverrides; }
    public Map<String, Integer> getTagOverrides()  { return tagOverrides; }

    public boolean isStackEnchantedBooks()       { return stackEnchantedBooks; }
    public boolean isStackEnderPearlsSnowballs() { return stackEnderPearlsSnowballs; }
    public boolean isStackBucketsPotions()       { return stackBucketsPotions; }
    public boolean isStackTotems()               { return stackTotems; }
    public boolean isStackToolsArmor()           { return stackToolsArmor; }

    public int getMaxStackSize(Item item) {
        return resolvedCache.computeIfAbsent(item, this::resolveForItem);
    }

    public static int getVanillaMaxCount(Item item) {
        return item.components().getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
    }

    /* ---- network payload ----------------------------------------------- */

    public ConfigSyncPayload toPayload() {
        return new ConfigSyncPayload(defaultMaxStackSize,
                stackEnchantedBooks, stackEnderPearlsSnowballs, stackBucketsPotions,
                stackTotems, stackToolsArmor,
                new LinkedHashMap<>(itemOverrides),
                new LinkedHashMap<>(tagOverrides));
    }

    /* ---- mutators ------------------------------------------------------ */

    public void setDefaultMaxStackSize(int value)      { this.defaultMaxStackSize = Math.clamp(value, 1, 1_000_000_000); resolvedCache.clear(); }
    public void setStackEnchantedBooks(boolean v)       { this.stackEnchantedBooks = v; resolvedCache.clear(); }
    public void setStackEnderPearlsSnowballs(boolean v) { this.stackEnderPearlsSnowballs = v; resolvedCache.clear(); }
    public void setStackBucketsPotions(boolean v)       { this.stackBucketsPotions = v; resolvedCache.clear(); }
    public void setStackTotems(boolean v)               { this.stackTotems = v; resolvedCache.clear(); }
    public void setStackToolsArmor(boolean v)           { this.stackToolsArmor = v; resolvedCache.clear(); }

    public void addItemOverride(String id, int maxStack) {
        itemOverrides.put(id, Math.clamp(maxStack, 1, 1_000_000_000));
        resolvedCache.clear();
    }

    public void addTagOverride(String tag, int maxStack) {
        tagOverrides.put(tag, Math.clamp(maxStack, 1, 1_000_000_000));
        resolvedCache.clear();
    }

    public void clearItemOverrides() { itemOverrides.clear(); resolvedCache.clear(); }
    public void clearTagOverrides()  { tagOverrides.clear(); resolvedCache.clear(); }

    /* ---- internals ----------------------------------------------------- */

    private int resolveForItem(Item item) {
        String id = BuiltInRegistries.ITEM.getKey(item).toString();

        Integer exact = itemOverrides.get(id);
        if (exact != null) return exact;

        for (Map.Entry<String, Integer> entry : tagOverrides.entrySet()) {
            String rawTag = entry.getKey().startsWith("#")
                    ? entry.getKey().substring(1) : entry.getKey();
            try {
                TagKey<Item> tagKey = TagKey.create(Registries.ITEM, Identifier.parse(rawTag));
                Holder.Reference<Item> ref = item.builtInRegistryHolder();
                if (ref.is(tagKey)) return entry.getValue();
            } catch (Exception ignored) {}
        }

        if (!stackToolsArmor && isWeaponOrTool(item)) return 1;

        int vanillaMax = getVanillaMaxCount(item);

        if (vanillaMax == 1) {
            if (ENCHANTED_BOOK_GROUP.contains(id) && stackEnchantedBooks) return defaultMaxStackSize;
            if (TOTEM_GROUP.contains(id) && stackTotems) return defaultMaxStackSize;
            if (!stackToolsArmor) return vanillaMax;
            return defaultMaxStackSize;
        }

        if (ENDER_PEARL_GROUP.contains(id)) return stackEnderPearlsSnowballs ? defaultMaxStackSize : vanillaMax;
        if (BUCKET_POTION_GROUP.contains(id)) return stackBucketsPotions ? defaultMaxStackSize : vanillaMax;

        return defaultMaxStackSize;
    }

    private void reset() {
        defaultMaxStackSize = 1_000;
        stackEnchantedBooks     = true;
        stackEnderPearlsSnowballs = true;
        stackBucketsPotions       = true;
        stackTotems               = false;
        stackToolsArmor           = false;
        itemOverrides.clear();
        tagOverrides.clear();
        resolvedCache.clear();
        loaded = false;
    }

    private void loadJsonConfig(Path path) {
        if (!Files.exists(path)) return;
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            if (json == null) return;
            if (!json.has("version")) return;

            if (json.has("defaultMaxStackSize")) {
                setDefaultMaxStackSize(json.get("defaultMaxStackSize").getAsInt());
            } else if (json.has("normalMaxStack")) {
                setDefaultMaxStackSize(json.get("normalMaxStack").getAsInt());
            }

            if (json.has("stackEnchantedBooks"))        stackEnchantedBooks = json.get("stackEnchantedBooks").getAsBoolean();
            if (json.has("stackEnderPearlsSnowballs"))   stackEnderPearlsSnowballs = json.get("stackEnderPearlsSnowballs").getAsBoolean();
            if (json.has("stackBucketsPotions"))         stackBucketsPotions = json.get("stackBucketsPotions").getAsBoolean();
            if (json.has("stackTotems"))                 stackTotems = json.get("stackTotems").getAsBoolean();
            if (json.has("stackToolsArmor"))             stackToolsArmor = json.get("stackToolsArmor").getAsBoolean();
            Overstacked.LOGGER.info("[Overstacked] Loaded GUI config from {}", path.getFileName());
        } catch (Exception e) {
            Overstacked.LOGGER.error("[Overstacked] Failed to read GUI config: {}", path, e);
        }
    }

    private void loadOrCopy(Path target, String resourceName,
                            java.util.function.Consumer<Path> parser) {
        try {
            if (!Files.exists(target)) {
                Files.createDirectories(target.getParent());
                try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
                    if (is != null) Files.copy(is, target);
                }
            } else if (resourceName.endsWith(".xml") && XmlConfigParser.isOldFormat(target)) {
                Files.delete(target);
                Files.createDirectories(target.getParent());
                try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
                    if (is != null) Files.copy(is, target);
                }
            } else if (resourceName.endsWith(".json") && TemplateOverridesParser.isOldFormat(target)) {
                Files.delete(target);
                Files.createDirectories(target.getParent());
                try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
                    if (is != null) Files.copy(is, target);
                }
            }
            parser.accept(target);
        } catch (IOException e) {
            Overstacked.LOGGER.error("[Overstacked] I/O error loading {}", target, e);
        }
    }

    private static boolean isWeaponOrTool(Item item) {
        if (item instanceof SwordItem || item instanceof TieredItem ||
            item instanceof ArmorItem || item instanceof ProjectileWeaponItem ||
            item instanceof ShieldItem || item instanceof TridentItem ||
            item instanceof FishingRodItem || item instanceof ShearsItem ||
            item instanceof FlintAndSteelItem) {
            return true;
        }
        Holder.Reference<Item> ref = item.builtInRegistryHolder();
        if (ref.is(ENCHANTABLE_WEAPON) || ref.is(ENCHANTABLE_DURABILITY) || ref.is(ENCHANTABLE_MINING)) {
            return true;
        }
        if (item.components().has(DataComponents.MAX_DAMAGE)) return true;
        return false;
    }
}
