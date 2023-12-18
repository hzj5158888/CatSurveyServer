package com.codecat.catsurvey.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.util.BeanUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class Util {
    public static Map<String, Object> objectToMap(Object object) {
        return JSONObject.parseObject(JSON.toJSONString(object));
    }

    public static <T> T mapToObject(Map<String, Object> source, Class<T> target) {
        if (source == null)
            return null;

        return JSONObject.parseObject(JSON.toJSONString(source), target);
    }

    public static Set<String> getObjectFiledName(Object object) {
        return objectToMap(object).keySet();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> objectToList(Object object) {
        return (List<Object>) object;
    }
}
