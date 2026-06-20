package io.github.makaseloli.creativemusicinsurvival.web;

import java.util.concurrent.atomic.AtomicBoolean;

public final class NowPlayingState {
    private static final AtomicBoolean SKIP_REQUESTED = new AtomicBoolean();
    private static volatile boolean supported;
    private static volatile Track track;

    private NowPlayingState() {}

    public static void enable() {
        supported = true;
    }

    public static boolean isSupported() {
        return supported;
    }

    public static void update(String translatedName, String fallbackKey) {
        if (translatedName == null || translatedName.isBlank()) {
            track = null;
            return;
        }
        String display = translatedName.equals(fallbackKey) ? titleFromKey(fallbackKey) : translatedName;
        int separator = display.indexOf(" - ");
        track = separator < 0
                ? new Track("", display)
                : new Track(display.substring(0, separator), display.substring(separator + 3));
    }

    public static void clear() {
        track = null;
    }

    public static Track current() {
        return track;
    }

    public static void requestSkip() {
        if (supported) {
            SKIP_REQUESTED.set(true);
        }
    }

    public static boolean consumeSkipRequest() {
        return SKIP_REQUESTED.getAndSet(false);
    }

    private static String titleFromKey(String key) {
        if (key == null || key.isBlank()) {
            return "Unknown track";
        }
        String name = key.substring(key.lastIndexOf('.') + 1).replace('_', ' ');
        StringBuilder title = new StringBuilder(name.length());
        boolean capitalize = true;
        for (int i = 0; i < name.length(); i++) {
            char character = name.charAt(i);
            title.append(capitalize ? Character.toUpperCase(character) : character);
            capitalize = character == ' ';
        }
        return title.toString();
    }

    public record Track(String artist, String title) {
        public String display() {
            return artist.isBlank() ? title : artist + " - " + title;
        }
    }
}
