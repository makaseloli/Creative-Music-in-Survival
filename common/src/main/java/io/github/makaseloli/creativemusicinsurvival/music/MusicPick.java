package io.github.makaseloli.creativemusicinsurvival.music;

public record MusicPick(MusicChoice choice, DimensionMusicSource source) {
    public static MusicPick general(MusicChoice choice) {
        return new MusicPick(choice, DimensionMusicSource.OVERWORLD);
    }

    public static MusicPick dimension(DimensionMusicSource source) {
        return new MusicPick(null, source);
    }
}
