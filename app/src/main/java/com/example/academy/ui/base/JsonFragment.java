package com.example.academy.ui.base;

import com.google.gson.Gson;
import android.content.Context;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.json.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class JsonFragment extends Fragment {
    private HashMap<String, Object> dataMap;

    public HashMap<String, Object> loadJsonData(String filePath) {
        try (FileInputStream fis = getContext().openFileInput(filePath)) {
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONObject dataJson = new JSONObject(json);
            dataMap = (HashMap<String, Object>) convertFromJson(dataJson);
            return dataMap;
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error loading JSON: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return dataMap;
        }
    }

    private static Object convertFromJson(Object json) throws JSONException {
        if (json == JSONObject.NULL) {
            return null;
        } else if (json instanceof JSONObject) {
            return toMap((JSONObject) json);
        } else if (json instanceof JSONArray) {
            return toList((JSONArray) json);
        } else {
            return json;
        }
    }

    private static HashMap<String, Object> toMap(JSONObject object) throws JSONException {
        HashMap<String, Object> map = new HashMap<>();
        Iterator<String> keys = object.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            map.put(key, convertFromJson(object.get(key)));
        }
        return map;
    }

    private static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            list.add(convertFromJson(array.get(i)));
        }
        return list;
    }

    public void saveToInternalStorage(HashMap<String, Object> mapData, String filePath) {
        Context context = getContext();
        Gson gson = new Gson();

        try (FileOutputStream fos = context.openFileOutput(filePath, Context.MODE_PRIVATE)) {
            String jsonData = gson.toJson(mapData);
            fos.write(jsonData.toString().getBytes());
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error saving file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
