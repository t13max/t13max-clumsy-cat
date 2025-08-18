package com.t13max.cc.utils;
import java.lang.reflect.*;
import java.util.*;

public class DeepCopyUtil {

    public static <T> T deepCopy(T original) {
        if (original == null) {
            return null;
        }
        try {
            return (T) copyObject(original, new IdentityHashMap<>());
        } catch (Exception e) {
            throw new RuntimeException("Deep copy failed", e);
        }
    }

    private static Object copyObject(Object original, Map<Object, Object> visited) throws Exception {
        if (original == null) return null;

        // 防止循环引用
        if (visited.containsKey(original)) {
            return visited.get(original);
        }

        Class<?> clazz = original.getClass();

        // 基本类型 包装类 String 都直接返回
        if (clazz.isPrimitive() || clazz.equals(String.class) || Number.class.isAssignableFrom(clazz)
                || clazz.equals(Boolean.class) || clazz.equals(Character.class)) {
            return original;
        }

        // 数组
        if (clazz.isArray()) {
            int length = Array.getLength(original);
            Object copy = Array.newInstance(clazz.getComponentType(), length);
            visited.put(original, copy);
            for (int i = 0; i < length; i++) {
                Array.set(copy, i, copyObject(Array.get(original, i), visited));
            }
            return copy;
        }

        // 集合
        if (original instanceof Collection) {
            Collection<?> src = (Collection<?>) original;
            Collection<Object> copy = src instanceof List ? new ArrayList<>() :
                    src instanceof Set ? new HashSet<>() : new ArrayList<>();
            visited.put(original, copy);
            for (Object o : src) {
                copy.add(copyObject(o, visited));
            }
            return copy;
        }

        // Map
        if (original instanceof Map) {
            Map<?, ?> src = (Map<?, ?>) original;
            Map<Object, Object> copy = new HashMap<>();
            visited.put(original, copy);
            for (Map.Entry<?, ?> e : src.entrySet()) {
                copy.put(copyObject(e.getKey(), visited), copyObject(e.getValue(), visited));
            }
            return copy;
        }

        // 普通对象
        Object copy = clazz.getDeclaredConstructor().newInstance();
        visited.put(original, copy);
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(original);
            field.set(copy, copyObject(value, visited));
        }
        return copy;
    }
}