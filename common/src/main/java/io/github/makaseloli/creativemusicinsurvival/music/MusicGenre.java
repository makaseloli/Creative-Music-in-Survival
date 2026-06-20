package io.github.makaseloli.creativemusicinsurvival.music;

import java.util.Locale;

public enum MusicGenre {
    MENU("menu", "creativemusicinsurvival.genre.menu"),
    SURVIVAL("survival", "creativemusicinsurvival.genre.survival"),
    CREATIVE("creative", "creativemusicinsurvival.genre.creative");

    private final String id;
    private final String translationKey;

    MusicGenre(String id, String translationKey) {
        this.id = id;
        this.translationKey = translationKey;
    }

    public String id() {
        return id;
    }

    public String translationKey() {
        return translationKey;
    }

    public static MusicGenre byId(String id) {
        if (id == null) {
            return null;
        }
        String normalized = id.toLowerCase(Locale.ROOT);
        for (MusicGenre genre : values()) {
            if (genre.id.equals(normalized)) {
                return genre;
            }
        }
        return null;
    }
}
