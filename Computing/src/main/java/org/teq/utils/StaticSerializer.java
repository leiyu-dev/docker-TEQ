package org.teq.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class StaticSerializer {

    public static String serializeToJson(Class<?> clazz) throws Exception {
        Map<String, Object> staticFields = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                staticFields.put(field.getName(), field.get(null));
            }
        }
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(staticFields);
    }

    // 从JSON反序列化为静态变量
    public static void deserializeFromJson(Class<?> clazz, String json) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> staticFields = objectMapper.readValue(json, Map.class);

        for (Field field : clazz.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                if (staticFields.containsKey(field.getName())) {
                    Object value = staticFields.get(field.getName());
                    // 如果字段是基本数据类型，需要特殊处理
                    if (field.getType().isPrimitive()) {
                        field.set(null, convertToPrimitive(value, field.getType()));
                    } else {
                        field.set(null, value);
                    }
                }
            }
        }
    }

    // 将Object转换为基本数据类型
    public static Object convertToPrimitive(Object value, Class<?> type) {
        if (type == int.class) {
            return ((Number) value).intValue();
        } else if (type == long.class) {
            return ((Number) value).longValue();
        } else if (type == float.class) {
            return ((Number) value).floatValue();
        } else if (type == double.class) {
            return ((Number) value).doubleValue();
        } else if (type == boolean.class) {
            return value;
        } else if (type == char.class) {
            return ((String) value).charAt(0);
        } else if (type == byte.class) {
            return ((Number) value).byteValue();
        } else if (type == short.class) {
            return ((Number) value).shortValue();
        }
        throw new IllegalArgumentException("Unsupported primitive type: " + type);
    }

    public static Object convertStringToObject(String value, Class<?> type) {
        if (type == int.class) {
            return Integer.parseInt(value);
        } else if (type == long.class) {
            return Long.parseLong(value);
        } else if (type == float.class) {
            return Float.parseFloat(value);
        } else if (type == double.class) {
            return Double.parseDouble(value);
        } else if (type == boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type == char.class) {
            return value.charAt(0);
        } else if (type == byte.class) {
            return Byte.parseByte(value);
        } else if (type == short.class) {
            return Short.parseShort(value);
        } else if (type == String.class) {
            return value;
        }
        throw new IllegalArgumentException("Unsupported type: " + type);
    }
}
