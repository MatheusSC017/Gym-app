package com.example.academy.ui.base;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class ConvertFromJson {
    public static Object convert(Object json) throws JSONException {
        if (json == JSONObject.NULL) {
            return new HashMap<>();
        } else if (json instanceof JSONObject) {
            return toMap((JSONObject) json);
        } else if (json instanceof JSONArray) {
            return toList((JSONArray) json);
        } else {
            return json;
        }
    }

    private static LinkedHashMap<String, Object> toMap(JSONObject object) throws JSONException {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        Iterator<String> keys = object.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            map.put(key, convert(object.get(key)));
        }
        return map;
    }

    private static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            list.add(convert(array.get(i)));
        }
        return list;
    }
}
