package com.qiyi.video.injector.util;

import java.lang.reflect.Field;

public class ReflectUtil {

    public static void setField(Object obj, String fieldName, Object fieldValue) throws IllegalAccessException {
        Field field = getFieldByName(obj.getClass(), fieldName);
        field.setAccessible(true);
        field.set(obj, fieldValue);
    }

    public static Field getFieldByName(Class<?> clazz, String name) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }
}