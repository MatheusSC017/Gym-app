package com.example.academy.ui.history;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.academy.R;
import com.example.academy.ui.base.JsonFragment;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryFragment extends JsonFragment {
    private static String WORKOUTS_FILE = "workouts.json";
    private static String HISTORY_FILE = "history.json";
    private LinkedHashMap<String, HashMap> currentWorkout;
    private HashMap<String, Object> trainingHistory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        currentWorkout = loadWorkoutData();
        trainingHistory = loadTrainingHistoryData();
        Toast.makeText(getContext(), currentWorkout.keySet().toString(), Toast.LENGTH_LONG).show();
        Toast.makeText(getContext(), trainingHistory.keySet().toString(), Toast.LENGTH_LONG).show();

        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    private LinkedHashMap<String, HashMap> loadWorkoutData() {
        try {
            HashMap<String, Object> workoutsExtractedMap = loadJsonData(WORKOUTS_FILE);
            if (workoutsExtractedMap != null) {
                List<String> workoutsIds = workoutsExtractedMap.keySet().stream().collect(Collectors.toList());

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
                for (int i=0; i < workoutsIds.size(); i++) {
                    HashMap<String, HashMap> registerData = (HashMap<String, HashMap>) workoutsExtractedMap.get(workoutsIds.get(i));

                    if (registerData.containsKey("Series")) {
                        return (LinkedHashMap<String, HashMap>) registerData.get("Series");
                    }
                }
            }
            return new LinkedHashMap<>();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error extracting workouts: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return new LinkedHashMap<>();
        }
    }

    private HashMap<String, Object> loadTrainingHistoryData() {
        try {
            HashMap<String, Object> historyExtractedMap = loadJsonData(HISTORY_FILE);
            return historyExtractedMap;
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error extracting history: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return new HashMap<>();
        }
    }

}