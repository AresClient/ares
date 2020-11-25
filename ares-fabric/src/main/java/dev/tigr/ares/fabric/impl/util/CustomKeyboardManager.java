package dev.tigr.ares.fabric.impl.util;

import dev.tigr.ares.core.util.IKeyboardManager;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tigermouthbear 11/24/20
 * based off lwjgl keyboard helper
 */
public class CustomKeyboardManager implements IKeyboardManager {
    /** Key names */
    private static final Map<Integer, String> nameMap = new HashMap<>();
    private static final Map<String, Integer> keyMap = new HashMap<>();

    static {
        // Use reflection to find out key names
        Field[] fields = GLFW.class.getFields();
        try {
            for(Field field: fields) {
                if(Modifier.isStatic(field.getModifiers())
                        && Modifier.isPublic(field.getModifiers())
                        && Modifier.isFinal(field.getModifiers())
                        && field.getType().equals(int.class)
                        && field.getName().startsWith("GLFW_KEY_")) {
                    int key = field.getInt(null);
                    String name = field.getName().substring(9);
                    name = transformName(name);
                    nameMap.put(key, name);
                    keyMap.put(name, key);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // rename keys to work with previous versions of ares
    private static String transformName(String key) {
        if(key.startsWith("KP_")) key = "NUMPAD" + key.substring(3);

        switch(key) {
            case "LEFT_SHIFT":
                return "LSHIFT";
            case "RIGHT_SHIFT":
                return "RSHIFT";
            case "LEFT_ALT":
                return "LMENU";
            case "RIGHT_ALT":
                return "RMENU";
            case "LEFT_CONTROL":
                return "LCONTROL";
            case "RIGHT_CONTROL":
                return "RCONTROL";
            case "LEFT_SUPER":
                return "LMETA";
            case "RIGHT_SUPER":
                return "RMETA";

            case "LEFT_BRACKET":
                return "LBRACKET";
            case "RIGHT_BRACKET":
                return "RBRACKET";

            case "NUMPADADD":
                return "ADD";
            case "NUMPADSUBTRACT":
                return "SUBTRACT";
            case "NUMPADDECIMAL":
                return "DECIMAL";
            case "NUMPADMULTIPLY":
                return "MULTIPLY";
            case "NUMPADDIVIDE":
                return "DIVIDE";
            case "NUMPADEQUAL":
                return "NUMPADEQUALS";
        }

        return key;
    }

    @Override
    public synchronized String getKeyName(int key) {
        return nameMap.getOrDefault(key, "NONE");
    }
}
