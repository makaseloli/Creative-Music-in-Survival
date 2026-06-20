package io.github.makaseloli.creativemusicinsurvival.music;

import java.util.Locale;

public enum MusicChoice {
    MENU(MusicGenre.MENU, "menu", "minecraft:music.menu", "creativemusicinsurvival.music.menu"),
    SURVIVAL(MusicGenre.SURVIVAL, "survival", "minecraft:music.game", "creativemusicinsurvival.music.survival"),
    CREATIVE(MusicGenre.CREATIVE, "creative", "minecraft:music.creative", "creativemusicinsurvival.music.creative"),
    BIOME(MusicGenre.SURVIVAL, "biome", "dynamic:biome_or_environment", "creativemusicinsurvival.music.biome");

    private final MusicGenre genre;
    private final String id;
    private final String soundId;
    private final String translationKey;

    MusicChoice(MusicGenre genre, String id, String soundId, String translationKey) {
        this.genre = genre;
        this.id = id;
        this.soundId = soundId;
        this.translationKey = translationKey;
    }

    public MusicGenre genre() {
        return genre;
    }

    public String id() {
        return id;
    }

    public String soundId() {
        return soundId;
    }

    public String translationKey() {
        return translationKey;
    }

    public static MusicChoice byId(String id) {
        if (id == null) {
            return null;
        }
        String normalized = id.toLowerCase(Locale.ROOT);
        for (MusicChoice choice : values()) {
            if (choice.id.equals(normalized)) {
                return choice;
            }
        }
        return null;
    }
}
