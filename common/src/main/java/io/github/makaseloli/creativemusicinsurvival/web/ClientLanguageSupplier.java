package io.github.makaseloli.creativemusicinsurvival.web;

public final class ClientLanguageSupplier {
    private ClientLanguageSupplier() {}

    public static String get() {
        try {
            Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
            Object minecraft = minecraftClass.getMethod("getInstance").invoke(null);
            if (minecraft == null) {
                return "en_us";
            }
            Object languageManager = minecraftClass.getMethod("getLanguageManager").invoke(minecraft);
            if (languageManager == null) {
                return "en_us";
            }
            Object selected = languageManager.getClass().getMethod("getSelected").invoke(languageManager);
            return selected instanceof String value && !value.isBlank() ? value : "en_us";
        } catch (ReflectiveOperationException | LinkageError e) {
            return "en_us";
        }
    }
}
