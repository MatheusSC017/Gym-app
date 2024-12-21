package com.example.academy;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainActivity extends AppCompatActivity {
    private Spinner workoutsSpinner;
    private Spinner exerciseSeriesSpinner;

    private List<String> workoutsIds = new ArrayList<>();
    private List<String> seriesIds = new ArrayList<>();
    private JSONObject workoutsJson;
    private HashMap<String, Object> workoutsMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        workoutsSpinner = findViewById(R.id.workoutsSpinner);
        exerciseSeriesSpinner = findViewById(R.id.exerciseSeriesSpinner);

        loadJsonData();
        saveToInternalStorage("workouts", workoutsJson);
        setupWorkoutSpinner();
    }

    private void loadJsonData() {
        try {
            InputStream inputStream = getAssets().open("workout.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            workoutsJson = new JSONObject(json);
            workoutsMap = (HashMap<String, Object>) convertFromJson(workoutsJson);
            if (workoutsMap != null) {
                workoutsIds = workoutsMap.keySet().stream().collect(Collectors.toList());
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading JSON: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

    public static HashMap<String, Object> toMap(JSONObject object) throws JSONException {
        HashMap<String, Object> map = new HashMap<>();
        Iterator<String> keys = object.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            map.put(key, convertFromJson(object.get(key)));
        }
        return map;
    }

    public static ArrayList<Object> toList(JSONArray array) throws JSONException {
        ArrayList<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            list.add(convertFromJson(array.get(i)));
        }
        return list;
    }

    private void saveToInternalStorage(String fileName, JSONObject jsonData) {
        try (FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE)) {
            fos.write(jsonData.toString().getBytes());
            Toast.makeText(this, "File saved!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupWorkoutSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, workoutsIds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        workoutsSpinner.setAdapter(adapter);

        workoutsSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                 setupExerciseSeriesSpinner(workoutsIds.get(position));
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupExerciseSeriesSpinner(String workoutId) {
        try {
            seriesIds.clear();
            HashMap<String, Object> workout = (HashMap<String, Object>) workoutsMap.get(workoutId);

            if (workout != null) {
                HashMap<String, Object> series = (HashMap<String, Object>) workout.get("Series");
                if (series != null) {
                    seriesIds = series.keySet().stream().collect(Collectors.toList());
                    Collections.reverse(seriesIds);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading Series: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, seriesIds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exerciseSeriesSpinner.setAdapter(adapter);

        exerciseSeriesSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                setupExercisesCards(workoutId, seriesIds.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
    }

    private void setupExercisesCards(String workoutId, String serieId) {
        HashMap<String, Object> workout = (HashMap<String, Object>) workoutsMap.get(workoutId);
        if (workout == null) return;

        HashMap<String, Object> series = (HashMap<String, Object>) workout.get("Series");
        if (series == null) return;

        HashMap<String, Object> exercises = (HashMap<String, Object>) series.get(serieId);

        if (exercises != null) {
            Toast.makeText(this, exercises.toString(), Toast.LENGTH_LONG).show();
        }
    }

}