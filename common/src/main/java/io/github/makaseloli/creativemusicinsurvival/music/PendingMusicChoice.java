package io.github.makaseloli.creativemusicinsurvival.music;

public final class PendingMusicChoice {
    private static final ThreadLocal<DimensionMusicSource> CHOICE = new ThreadLocal<>();

    private PendingMusicChoice() {}

    public static void set(DimensionMusicSource choice) {
        if (choice == DimensionMusicSource.NETHER || choice == DimensionMusicSource.END) {
            CHOICE.set(choice);
        } else {
            CHOICE.remove();
        }
    }

    public static DimensionMusicSource consume() {
        DimensionMusicSource choice = CHOICE.get();
        CHOICE.remove();
        return choice;
    }
}
