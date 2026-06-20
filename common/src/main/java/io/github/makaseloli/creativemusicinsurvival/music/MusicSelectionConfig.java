package io.github.makaseloli.creativemusicinsurvival.music;

import io.github.makaseloli.creativemusicinsurvival.Constants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MusicSelectionConfig {
    private static final Pattern ARRAY_PATTERN = Pattern.compile("\"(menu|survival|creative)(?:\\.(overworld|nether|end))?\"\\s*:\\s*\\[(.*?)]", Pattern.DOTALL);
    private static final Pattern STRING_PATTERN = Pattern.compile("\"([^\"]+)\"");
    private static final Path CONFIG_PATH = Path.of("config", Constants.MODID + "-webui.json");
    private static final Random RANDOM = new Random();
    private static final AtomicLong REVISION = new AtomicLong();
    private static final Map<MusicGenre, EnumSet<MusicChoice>> GENERAL = new EnumMap<>(MusicGenre.class);
    private static final Map<MusicGenre, Map<MusicDimension, EnumSet<DimensionMusicSource>>> DIMENSIONS = new EnumMap<>(MusicGenre.class);

    static { resetDefaults(); }
    private MusicSelectionConfig() {}

    public static synchronized void load() {
        resetDefaults();
        if (!Files.isRegularFile(CONFIG_PATH)) {
            save();
            return;
        }
        try {
            Matcher arrays = ARRAY_PATTERN.matcher(Files.readString(CONFIG_PATH, StandardCharsets.UTF_8));
            while (arrays.find()) {
                MusicGenre target = MusicGenre.byId(arrays.group(1));
                MusicDimension dimension = MusicDimension.byId(arrays.group(2));
                Matcher values = STRING_PATTERN.matcher(arrays.group(3));
                if (dimension == null) {
                    EnumSet<MusicChoice> selected = EnumSet.noneOf(MusicChoice.class);
                    while (values.find()) {
                        MusicChoice choice = MusicChoice.byId(values.group(1));
                        if (choice != null) selected.add(choice);
                    }
                    GENERAL.put(target, selected);
                } else {
                    EnumSet<DimensionMusicSource> selected = EnumSet.noneOf(DimensionMusicSource.class);
                    while (values.find()) {
                        DimensionMusicSource source = DimensionMusicSource.byId(values.group(1));
                        if (source != null) selected.add(source);
                    }
                    DIMENSIONS.get(target).put(dimension, selected);
                }
            }
        } catch (IOException e) {
            Constants.LOGGER.warn("Failed to read music Web UI config", e);
        }
    }

    public static synchronized void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, toJson(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Constants.LOGGER.warn("Failed to write music Web UI config", e);
        }
    }

    public static synchronized boolean isSelected(MusicGenre target, MusicChoice choice) {
        return GENERAL.get(target).contains(choice);
    }

    public static synchronized boolean isSelected(MusicGenre target, MusicDimension dimension, DimensionMusicSource source) {
        return DIMENSIONS.get(target).get(dimension).contains(source);
    }

    public static synchronized void setSelected(MusicGenre target, MusicChoice choice, boolean selected) {
        setMembership(GENERAL.get(target), choice, selected);
        changed();
    }

    public static synchronized void setSelected(MusicGenre target, MusicDimension dimension, DimensionMusicSource source, boolean selected) {
        setMembership(DIMENSIONS.get(target).get(dimension), source, selected);
        changed();
    }

    public static synchronized void reset(MusicGenre target, MusicDimension dimension) {
        if (dimension == null) {
            GENERAL.put(target, defaultGeneral(target));
        } else {
            DIMENSIONS.get(target).put(dimension, defaultDimension(dimension));
        }
        changed();
    }

    public static long revision() { return REVISION.get(); }

    public static synchronized boolean hasEmptySelection() {
        if (GENERAL.values().stream().anyMatch(Set::isEmpty)) return true;
        return DIMENSIONS.values().stream().flatMap(map -> map.values().stream()).anyMatch(Set::isEmpty);
    }

    public static synchronized MusicPick pickReplacement(MusicGenre target, MusicDimension dimension, boolean biomeAvailable) {
        DimensionMusicSource source = random(DIMENSIONS.get(target).get(dimension));
        if (source == null) return null;
        if (source != DimensionMusicSource.OVERWORLD) return MusicPick.dimension(source);

        EnumSet<MusicChoice> selected = EnumSet.copyOf(GENERAL.get(target));
        if (!biomeAvailable) selected.remove(MusicChoice.BIOME);
        MusicChoice choice = random(selected);
        return choice == null ? null : MusicPick.general(choice);
    }

    public static synchronized boolean isDefaultSelection(MusicGenre target, MusicDimension dimension) {
        return GENERAL.get(target).equals(defaultGeneral(target))
                && DIMENSIONS.get(target).get(dimension).equals(defaultDimension(dimension));
    }

    private static <T extends Enum<T>> void setMembership(Set<T> values, T value, boolean selected) {
        if (selected) values.add(value); else values.remove(value);
    }

    private static void changed() {
        save();
        REVISION.incrementAndGet();
    }

    private static <T> T random(Set<T> values) {
        if (values.isEmpty()) return null;
        int index = RANDOM.nextInt(values.size());
        for (T value : values) if (index-- == 0) return value;
        return null;
    }

    private static void resetDefaults() {
        GENERAL.clear();
        DIMENSIONS.clear();
        for (MusicGenre target : MusicGenre.values()) {
            GENERAL.put(target, defaultGeneral(target));
            Map<MusicDimension, EnumSet<DimensionMusicSource>> dimensions = new EnumMap<>(MusicDimension.class);
            for (MusicDimension dimension : MusicDimension.values()) dimensions.put(dimension, defaultDimension(dimension));
            DIMENSIONS.put(target, dimensions);
        }
    }

    private static EnumSet<MusicChoice> defaultGeneral(MusicGenre target) {
        return switch (target) {
            case MENU -> EnumSet.of(MusicChoice.MENU);
            case SURVIVAL -> EnumSet.of(MusicChoice.SURVIVAL, MusicChoice.BIOME);
            case CREATIVE -> EnumSet.of(MusicChoice.CREATIVE, MusicChoice.BIOME);
        };
    }

    private static EnumSet<DimensionMusicSource> defaultDimension(MusicDimension dimension) {
        return EnumSet.of(switch (dimension) {
            case OVERWORLD -> DimensionMusicSource.OVERWORLD;
            case NETHER -> DimensionMusicSource.NETHER;
            case END -> DimensionMusicSource.END;
        });
    }

    private static String toJson() {
        StringBuilder json = new StringBuilder("{\n");
        int index = 0;
        for (MusicGenre target : MusicGenre.values()) {
            append(json, index++, target.id(), GENERAL.get(target));
            for (MusicDimension dimension : MusicDimension.values()) {
                append(json, index++, target.id() + "." + dimension.id(), DIMENSIONS.get(target).get(dimension));
            }
        }
        return json.append("\n}\n").toString();
    }

    private static void append(StringBuilder json, int index, String key, Set<? extends Enum<?>> values) {
        if (index > 0) json.append(",\n");
        json.append("  \"").append(key).append("\": [");
        int valueIndex = 0;
        for (Enum<?> value : values) {
            if (valueIndex++ > 0) json.append(", ");
            String id = value instanceof MusicChoice choice ? choice.id() : ((DimensionMusicSource) value).id();
            json.append('"').append(id).append('"');
        }
        json.append(']');
    }
}
