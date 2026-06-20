package io.github.makaseloli.creativemusicinsurvival.music;

import java.util.Locale;

public enum DimensionMusicSource {
    OVERWORLD("overworld", "dynamic:general_selection", "creativemusicinsurvival.source.overworld"),
    NETHER("nether", "dynamic:music.nether.*", "creativemusicinsurvival.source.nether"),
    END("end", "dynamic:music.end", "creativemusicinsurvival.source.end");

    private final String id;
    private final String soundId;
    private final String translationKey;

    DimensionMusicSource(String id, String soundId, String translationKey) {
        this.id = id;
        this.soundId = soundId;
        this.translationKey = translationKey;
    }

    public String id() { return id; }
    public String soundId() { return soundId; }
    public String translationKey() { return translationKey; }

    public static DimensionMusicSource byId(String id) {
        if (id == null) return null;
        String normalized = id.toLowerCase(Locale.ROOT);
        for (DimensionMusicSource value : values()) {
            if (value.id.equals(normalized)) return value;
        }
        return null;
    }
}
