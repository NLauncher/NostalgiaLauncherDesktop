package net.eqozqq.nostalgialauncherdesktop.Proxy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.eqozqq.nostalgialauncherdesktop.Instances.InstanceManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ServerStorage {
    private static final String SERVERS_FILE = "servers.json";

    public static List<Server> loadServers() {
        File file = new File(InstanceManager.getInstance().resolvePath(SERVERS_FILE));
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Server>>() {
                }.getType();
                return gson.fromJson(reader, listType);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    public static void saveServers(List<Server> servers) {
        File file = new File(InstanceManager.getInstance().resolvePath(SERVERS_FILE));
        try (Writer writer = new FileWriter(file)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(servers, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}