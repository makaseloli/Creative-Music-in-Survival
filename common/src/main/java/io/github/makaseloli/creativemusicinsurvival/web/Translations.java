package io.github.makaseloli.creativemusicinsurvival.web;

import io.github.makaseloli.creativemusicinsurvival.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Translations {
    private static final Pattern ENTRY_PATTERN = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");

    private Translations() {}

    static Translator load(String languageCode) {
        Map<String, String> values = new HashMap<>();
        values.putAll(loadFile("en_us"));
        String normalized = normalize(languageCode);
        if (!"en_us".equals(normalized)) {
            values.putAll(loadFile(normalized));
        }
        return key -> values.getOrDefault(key, key);
    }

    private static Map<String, String> loadFile(String languageCode) {
        String path = "/assets/" + Constants.MODID + "/lang/" + languageCode + ".json";
        try (InputStream stream = Translations.class.getResourceAsStream(path)) {
            if (stream == null) {
                return Map.of();
            }
            StringBuilder json = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line).append('\n');
                }
            }
            Map<String, String> values = new HashMap<>();
            Matcher matcher = ENTRY_PATTERN.matcher(json);
            while (matcher.find()) {
                values.put(unescape(matcher.group(1)), unescape(matcher.group(2)));
            }
            return values;
        } catch (IOException e) {
            return Map.of();
        }
    }

    private static String normalize(String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            return "en_us";
        }
        return languageCode.toLowerCase(Locale.ROOT).replace('-', '_');
    }

    private static String unescape(String value) {
        return value.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    interface Translator {
        String translate(String key);
    }
}
