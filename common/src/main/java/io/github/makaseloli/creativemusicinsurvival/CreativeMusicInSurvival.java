package io.github.makaseloli.creativemusicinsurvival;

import io.github.makaseloli.creativemusicinsurvival.music.MusicSelectionConfig;
import io.github.makaseloli.creativemusicinsurvival.web.WebUiServer;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public final class CreativeMusicInSurvival {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean();

    private CreativeMusicInSurvival() {}

    public static void init() {
        init(() -> "en_us");
    }

    public static void init(Supplier<String> languageSupplier) {
        if (!INITIALIZED.compareAndSet(false, true)) {
            return;
        }

        MusicSelectionConfig.load();
        WebUiServer.start(Objects.requireNonNullElseGet(languageSupplier, () -> () -> "en_us"));
    }
}
