package com.codecat.catsurvey.commcon.utils;

import com.alibaba.fastjson2.JSONObject;

import java.lang.reflect.Field;
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

    public static <T> T mapToObject(Map source, Class<T> target) {
        if (source == null)
            return null;

        return JSONObject.parseObject(JSONObject.toJSONString(source), target);
    }

    public static Set<String> getObjectFiledName(Object object) {
        return objectToMap(object).keySet();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> objectToList(Object object) {
        return (List<Object>) object;
    }
}
