package net.eqozqq.nostalgialauncherdesktop;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.eqozqq.nostalgialauncherdesktop.Instances.InstanceManager;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
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
            parseJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void loadCustomLanguage(String path) {
        this.currentLanguage = "custom";
        try (InputStream inputStream = new FileInputStream(path)) {
            parseJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            loadLanguage("en");
        }
    }

    public void loadFromUrl(String urlString, String name) {
        this.currentLanguage = "github";
        File cacheDir = new File(InstanceManager.getInstance().resolvePath("cache/locales"));
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        File cacheFile = new File(cacheDir, name + ".json");

        try {
            if (urlString != null && !urlString.isEmpty()) {
                URL url = new URL(urlString);
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                try (InputStream in = connection.getInputStream();
                     FileOutputStream out = new FileOutputStream(cacheFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
            }
            
            if (cacheFile.exists()) {
                try (InputStream inputStream = new FileInputStream(cacheFile)) {
                    parseJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                }
            } else {
                loadLanguage("en");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cacheFile.exists()) {
                try (InputStream inputStream = new FileInputStream(cacheFile)) {
                    parseJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                } catch (IOException ex) {
                    loadLanguage("en");
                }
            } else {
                loadLanguage("en");
            }
        }
    }

    private void parseJson(Reader reader) {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>() {}.getType();
        translations = gson.fromJson(reader, type);
    }

    public String get(String key) {
        return translations.getOrDefault(key, key);
    }
    
    public String get(String key, Object... args) {
        return String.format(get(key), args);
    }

    public boolean has(String key) {
        return translations != null && translations.containsKey(key);
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }
    
    public void init(Properties settings) {
        String lang = settings.getProperty("language", "en");
        if ("custom".equals(lang)) {
            loadCustomLanguage(settings.getProperty("customTranslationPath"));
        } else if ("github".equals(lang)) {
            loadFromUrl(settings.getProperty("githubTranslationUrl"), settings.getProperty("githubTranslationName", "unknown"));
        } else {
            loadLanguage(lang);
        }
    }
}