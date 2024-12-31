package com.example.academy.ui.workout;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.academy.MainActivity;
import com.example.academy.R;
import com.example.academy.ui.base.JsonFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;


public class WorkoutFragment extends JsonFragment {
    private static String WORKOUTS_FILE = "workouts.json";

    private LinearLayout workoutLayout;
    private Spinner workoutsSpinner;
    private Spinner exerciseSeriesSpinner;

    private List<String> workoutsIds = new ArrayList<>();
    private List<String> seriesIds = new ArrayList<>();
    private Button insertButton;
    private Button editButton;
    private Button loadButton;
    private HashMap<String, Object> workoutsMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout, container, false);

        insertButton = view.findViewById(R.id.insertButton);
        editButton = view.findViewById(R.id.editButton);
        loadButton = view.findViewById(R.id.loadButton);
        workoutLayout = view.findViewById(R.id.workoutLayout);
        workoutsSpinner = view.findViewById(R.id.workoutsSpinner);
        exerciseSeriesSpinner = view.findViewById(R.id.exerciseSeriesSpinner);

        insertButton.setOnClickListener(event -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new RegisterWorkoutFragment());
            }
        });

        workoutsMap = loadJsonData(WORKOUTS_FILE);
        setupWorkoutSpinner();

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public HashMap<String, Object> loadJsonData(String filePath) {
        try {
            HashMap<String, Object> workoutsExtractedMap = super.loadJsonData(filePath);
            if (workoutsExtractedMap != null) {
                workoutsIds = workoutsExtractedMap.keySet().stream().collect(Collectors.toList());

                Comparator<String> comparatorWorkoutsIds = new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        String[] parts1 = o1.split("/");
                        String[] parts2 = o2.split("/");

                        int year1 = Integer.parseInt(parts1[1]);
                        int month1 = Integer.parseInt(parts1[0]);
                        int year2 = Integer.parseInt(parts2[1]);
                        int month2 = Integer.parseInt(parts2[0]);

                        if (year1 != year2) {
                            return Integer.compare(year2, year1); // Descending order by year
                        } else {
                            return Integer.compare(month2, month1); // Descending order by month
                        }
                    }
                };
                workoutsIds.sort(comparatorWorkoutsIds);
            }
            return workoutsExtractedMap;
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error extracting workouts: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private void setupWorkoutSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, workoutsIds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        workoutsSpinner.setAdapter(adapter);

        workoutsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setupExerciseSeriesSpinner(workoutsIds.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
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
                    Collections.sort(seriesIds);
                }
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error loading Series: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, seriesIds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exerciseSeriesSpinner.setAdapter(adapter);

        exerciseSeriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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

                View exerciseCard = LayoutInflater.from(getContext()).inflate(R.layout.workout_card, workoutLayout, false);
                LinearLayout exerciseCardLayout = exerciseCard.findViewById(R.id.exercisesLayout);

                setupExerciseCard(exercise, exerciseData, exerciseCardLayout);
                if (exerciseData.containsKey("Simultaneo")) {
                    List<String> chainedExercises = (List<String>) exerciseData.get("Simultaneo");
                    for (String chainedExercise : chainedExercises) {
                        if (!exercisesCopy.containsKey(chainedExercise)) continue;

                        HashMap<String, Object> chainedExerciseData = (HashMap<String, Object>) exercisesCopy.get(chainedExercise);
                        exercisesCopy.remove(chainedExercise);

                        ImageView chainImage = new ImageView(getContext());
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
        View exerciseCard = LayoutInflater.from(getContext()).inflate(R.layout.exercise_layout, layout, false);

        TextView exerciseTextView = exerciseCard.findViewById(R.id.exerciseTextView);
        TextView seriesTextView = exerciseCard.findViewById(R.id.seriesTextView);
        TextView repetitionsTextView = exerciseCard.findViewById(R.id.repetitionsTextView);

        exerciseTextView.setText(exercise);
        String series = exerciseData.getOrDefault("Series", "1").toString();
        if (!series.equals("1")) seriesTextView.setText(series);

        repetitionsTextView.setText(exerciseData.getOrDefault("Quantidade", "").toString() + " " + exerciseData.getOrDefault("Tipo", "").toString());
        layout.addView(exerciseCard);
    }

}