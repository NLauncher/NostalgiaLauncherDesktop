package net.eqozqq.nostalgialauncherdesktop;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class LocaleManager {
    private static LocaleManager instance;
    private Map<String, String> translations;
    private String currentLanguage;

    private LocaleManager() {
        translations = new HashMap<>();
        currentLanguage = "en";
    }

    public static synchronized LocaleManager getInstance() {
        if (instance == null) {
            instance = new LocaleManager();
        }
        return instance;
    }

    public void loadLanguage(String language) {
        if (language == null || language.isEmpty()) {
            language = "en";
        }
        this.currentLanguage = language;
        String fileName = "/locales/" + language + ".json";
        try (InputStream inputStream = getClass().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                if (!"en".equals(language)) {
                    loadLanguage("en");
                }
                return;
            }
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            translations = gson.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), type);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String get(String key) {
        return translations.getOrDefault(key, key);
    }
    
    public String get(String key, Object... args) {
        return String.format(get(key), args);
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }
    
    public void init(Properties settings) {
        loadLanguage(settings.getProperty("language", "en"));
    }
}