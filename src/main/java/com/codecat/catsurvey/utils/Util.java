package com.codecat.catsurvey.utils;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.util.BeanUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Util {
    public static Map<String, Object> objectToMap(Object object) {
        TreeMap<String, Object> ans = new TreeMap<>();

        Class<?> clazz = object.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            try {
                if (!field.trySetAccessible())
                    continue;

                field.setAccessible(true);
                ans.put(field.getName(), field.get(object));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return ans;
    }

    public static <T> T mapToObject(Map<String, Object> source, Class<T> target) {
        if (source == null)
            return null;

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(source, target);
    }

    public static Set<String> getObjectFiledName(Object object) {
        return objectToMap(object).keySet();
    }

    protected Type getType(int index) {
        Type superClass = getClass().getGenericSuperclass();
        return ((ParameterizedType) superClass).getActualTypeArguments()[index];
    }

    @SuppressWarnings("unchecked")
    public static List<Object> objectToList(Object object) {
        return (List<Object>) object;
    }
}
