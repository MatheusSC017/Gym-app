package com.example.academy;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Console;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Spinner workoutsSpinner;
    private Spinner exerciseSeriesSpinner;

    private List<String> workoutsIds = new ArrayList<>();
    private JSONObject workoutsJson;

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
            JSONArray workouts = workoutsJson.getJSONArray("workouts");

            for (int i = 0; i < workouts.length(); i++) {
                JSONObject workout = workouts.getJSONObject(i);
                workoutsIds.add(workout.getString("id"));
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error loading JSON: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupWorkoutSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, workoutsIds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        workoutsSpinner.setAdapter(adapter);
        workoutsSpinner.setSelection(workoutsIds.size() - 1);

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

    private void setupExerciseSeriesSpinner(String workoutId) {}
}