package dev.tigr.ares.core.setting;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;

/**
 * Provides functions for saving and reading configs
 *
 * @author Tigermouthbear 7/5/20
 */
public class SettingsManager {
    public static final File SAVE_FILE = new File("Ares/config.json");
    public static JSONObject SAVE_OBJECT = read();

    public static JSONObject read() {
        SAVE_FILE.getParentFile().mkdirs();
        try {
            return read(SAVE_FILE);
        } catch(IOException e) {
            return new JSONObject();
        }
    }

    public static JSONObject read(File file) throws IOException {
        if(!file.exists()) {
            file.createNewFile();
            return new JSONObject();
        }

        try {
            return new JSONObject(new JSONTokener(new FileInputStream(file)));
        } catch(JSONException e) {
            return new JSONObject();
        }
    }

    public static void save() {
        try {
            save(SAVE_FILE);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void save(File file) throws IOException {
        SettingCategory.forEach(settingCategory ->
                settingCategory.getSettings().forEach(setting ->
                        setting.save(settingCategory.getValue())));

        PrintWriter printWriter = new PrintWriter(new FileWriter(file));
        printWriter.print(SAVE_OBJECT.toString(4));
        printWriter.close();
    }

    public static void load(File file) throws IOException {
        SAVE_OBJECT = read(file);

        SettingCategory.forEach(settingCategory -> {
            if(settingCategory.getParent() == null) {
                if(SAVE_OBJECT.has(settingCategory.getName()))
                    settingCategory.setValue(SAVE_OBJECT.getJSONObject(settingCategory.getName()));
            } else if(settingCategory.getParent().getValue().has(settingCategory.getName()))
                settingCategory.setValue(settingCategory.getParent().getValue().getJSONObject(settingCategory.getName()));
        });

        Setting.getAll().forEach(setting -> {
            try {
                setting.setValue(setting.read(setting.getParent().getValue()));
            } catch(Exception ignored) {
            }
        });
    }
}
