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
        dataMap = new HashMap<>();

        try (FileInputStream fis = getContext().openFileInput(filePath)) {
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONObject dataJson = new JSONObject(json);
            dataMap = (HashMap<String, Object>) ConvertFromJson.convert(dataJson);
            return dataMap;
        } catch (Exception e) {
            return dataMap;
        }
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
