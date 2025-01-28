package com.example.academy.ui.history;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.academy.R;
import com.example.academy.ui.base.JsonFragment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    private String currentSerieId;

    private Button selectDateButton;
    private CalendarView trainingCalendarView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        currentWorkout = loadWorkoutData();
        trainingHistory = loadTrainingHistoryData();

        List<String> seriesIds = currentWorkout.keySet().stream().collect(Collectors.toList());
        currentSerieId = String.valueOf(seriesIds.get(0));
        if (trainingHistory.size() != 0) {
            List<String> historyDates = new ArrayList<>(trainingHistory.keySet());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            historyDates.sort(Comparator.comparing(key -> LocalDate.parse(key, formatter)));

            String lastTrainingDate = historyDates.get(historyDates.size() - 1);
            HashMap<String, Object> lastTrainingInfo = (HashMap<String, Object>) trainingHistory.get(lastTrainingDate);
            if (lastTrainingInfo.containsKey("SerieName")) {
                String lastSerie = (String) lastTrainingInfo.get("SerieName");
                int lastSerieIndex = seriesIds.indexOf(lastSerie);
                if (lastSerieIndex >= 0) {
                    currentSerieId = String.valueOf(seriesIds.get((lastSerieIndex < seriesIds.size()) ? (lastSerieIndex + 1) : 0)) ;
                }
            }
        }

        selectDateButton = view.findViewById(R.id.selectDateButton);
        trainingCalendarView = view.findViewById(R.id.trainingCalendarView);
        trainingCalendarView.setVisibility(View.GONE);

        selectDateButton.setOnClickListener(event -> trainingCalendarView.setVisibility(View.VISIBLE));
        trainingCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
                trainingCalendarView.setVisibility(View.GONE);
            }
        });

        return view;
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