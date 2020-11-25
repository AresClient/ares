package dev.tigr.ares.core.util.global;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Tigermouthbear 8/28/20
 */
public class ReflectionHelper {
    public static <T> T getPrivateValue(Class clazz, Object object, String... names) {
        Field field = getField(clazz, names);
        field.setAccessible(true);

        try {
            return (T) field.get(object);
        } catch(IllegalAccessException ignored) {
        }
        return null;
    }

    public static <T> boolean setPrivateValue(Class clazz, Object object, T value, String... names) {
        Field field = getField(clazz, names);
        field.setAccessible(true);

        try {
            field.set(object, value);
        } catch(IllegalAccessException ignored) {
            return false;
        }
        return true;
    }

    public static Field getField(Class clazz, String... names) {
        Field field = null;

        for(String name: names) {
            if(field != null) break;
            try {
                field = clazz.getDeclaredField(name);
            } catch(NoSuchFieldException ignored) {
            }
        }

        return field;
    }

    public static <T> T callPrivateMethod(Class<?> clazz, Object object, String[] names, Object... args) {
        // grab classes of args
        Class<?>[] classes = new Class<?>[]{};
        for(int i = 0; i < args.length; i++) classes[i] = args[i].getClass();

        Method method = getMethod(clazz, names, classes);
        method.setAccessible(true);

        try {
            return (T) method.invoke(object, args);
        } catch(IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Method getMethod(Class<?> clazz, String[] names, Class<?>[] args) {
        Method method = null;

        for(String name: names) {
            if(method != null) break;
            try {
                method = clazz.getDeclaredMethod(name, args);
            } catch(NoSuchMethodException ignored) {
            }
        }

        return method;
    }
}
