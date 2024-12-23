package com.example.academy;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

public class MainActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;

    private LinearLayout workoutLayout;
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
        setUpToolbar();
        navigationView = (NavigationView) findViewById(R.id.navigation_menu);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        workoutLayout = findViewById(R.id.workoutLayout);
        workoutsSpinner = findViewById(R.id.workoutsSpinner);
        exerciseSeriesSpinner = findViewById(R.id.exerciseSeriesSpinner);

        loadJsonData();
        saveToInternalStorage("workouts", workoutsJson);
        setupWorkoutSpinner();
    }

    public void setUpToolbar() {
        drawerLayout = findViewById(R.id.drawerLayout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    public void loadJsonData() {
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

    public static Object convertFromJson(Object json) throws JSONException {
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
            HashMap<String, Object> exercisesCopy = new HashMap<String, Object>(exercises);

            workoutLayout.removeAllViews();
            while (exercisesCopy.keySet().stream().count() > 0){
                String exercise = exercisesCopy.keySet().stream().findFirst().orElse(null).toString();
                HashMap<String, Object> exerciseData = (HashMap<String, Object>) exercisesCopy.get(exercise);
                exercisesCopy.remove(exercise);

                View exerciseCard = LayoutInflater.from(this).inflate(R.layout.item_card, workoutLayout, false);
                LinearLayout exerciseCardLayout = exerciseCard.findViewById(R.id.exercisesLayout);

                setupExerciseCard(exercise, exerciseData, exerciseCardLayout);
                if (exerciseData.containsKey("Simultaneo")) {
                    List<String> chainedExercises = (List<String>) exerciseData.get("Simultaneo");
                    for (String chainedExercise : chainedExercises) {
                        if (!exercisesCopy.containsKey(chainedExercise)) continue;

                        HashMap<String, Object> chainedExerciseData = (HashMap<String, Object>) exercisesCopy.get(chainedExercise);
                        exercisesCopy.remove(chainedExercise);

                        ImageView chainImage = new ImageView(this);
                        chainImage.setImageResource(R.drawable.ic_link_16);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        chainImage.setLayoutParams(params);
                        exerciseCardLayout.addView(chainImage);
                        setupExerciseCard(chainedExercise, chainedExerciseData, exerciseCardLayout);
                    }
                }
                workoutLayout.addView(exerciseCard);
            }
        }
    }

    private void setupExerciseCard(String exercise, HashMap<String, Object> exerciseData, LinearLayout layout) {
        View exerciseCard = LayoutInflater.from(this).inflate(R.layout.exercise_card, layout, false);

        TextView exerciseTextView = exerciseCard.findViewById(R.id.exerciseTextView);
        TextView seriesTextView = exerciseCard.findViewById(R.id.seriesTextView);
        TextView repetitionsTextView = exerciseCard.findViewById(R.id.repetitionsTextView);

        exerciseTextView.setText(exercise);
        seriesTextView.setText(exerciseData.get("Series").toString());
        repetitionsTextView.setText(exerciseData.get("Quantidade").toString() + " " + exerciseData.get("Tipo").toString());

        layout.addView(exerciseCard);
    }

}