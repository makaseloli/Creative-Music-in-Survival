package io.github.makaseloli.creativemusicinsurvival.music;

import java.util.Locale;

public enum MusicDimension {
    OVERWORLD("overworld", "creativemusicinsurvival.dimension.overworld"),
    NETHER("nether", "creativemusicinsurvival.dimension.nether"),
    END("end", "creativemusicinsurvival.dimension.end");

    private final String id;
    private final String translationKey;

    MusicDimension(String id, String translationKey) {
        this.id = id;
        this.translationKey = translationKey;
    }

    public String id() { return id; }
    public String translationKey() { return translationKey; }

    public static MusicDimension byId(String id) {
        if (id == null) return null;
        String normalized = id.toLowerCase(Locale.ROOT);
        for (MusicDimension value : values()) {
            if (value.id.equals(normalized)) return value;
        }
        return null;
    }
}
