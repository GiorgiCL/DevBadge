package com.devbadge.devbadge.service;

import java.lang.reflect.Field;

public final class TestUtils {

    private TestUtils() {}

    public static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
