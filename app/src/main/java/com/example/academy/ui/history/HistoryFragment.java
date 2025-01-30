package com.example.academy.ui.history;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.academy.R;
import com.example.academy.ui.base.JsonFragment;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryFragment extends JsonFragment {
    private static String WORKOUTS_FILE = "workouts.json";
    private static String HISTORY_FILE = "history.json";
    private LinkedHashMap<String, HashMap> currentWorkout;
    private HashMap<String, Object> trainingHistory;

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DecimalFormat twoDecimalFormatter = new DecimalFormat("00");

    private String lastTrainingDate;
    private String currentTrainingDate;
    private String currentSerieId;

    private TextView dateTextView;
    private TextView serieTextView;
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

            historyDates.sort(Comparator.comparing(key -> LocalDate.parse(key, dateFormatter)));

            lastTrainingDate = historyDates.get(historyDates.size() - 1);
            HashMap<String, Object> lastTrainingInfo = (HashMap<String, Object>) trainingHistory.get(lastTrainingDate);
            if (lastTrainingInfo.containsKey("SerieName")) {
                String lastSerie = (String) lastTrainingInfo.get("SerieName");
                int lastSerieIndex = seriesIds.indexOf(lastSerie);
                if (lastSerieIndex >= 0) {
                    currentSerieId = String.valueOf(seriesIds.get((lastSerieIndex < seriesIds.size()) ? (lastSerieIndex + 1) : 0)) ;
                }
            }

        }

        dateTextView = view.findViewById(R.id.dateTextView);
        setCurrentDate();

        serieTextView = view.findViewById(R.id.serieTextView);
        serieTextView.setText(currentSerieId);

        selectDateButton = view.findViewById(R.id.selectDateButton);
        selectDateButton.setOnClickListener(event -> trainingCalendarView.setVisibility(View.VISIBLE));

        trainingCalendarView = view.findViewById(R.id.trainingCalendarView);
        trainingCalendarView.setVisibility(View.GONE);
        trainingCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
                currentTrainingDate = twoDecimalFormatter.format(day) + "/" + twoDecimalFormatter.format(month + 1) + "/" + year;
                selectTraining();

                dateTextView.setText(currentTrainingDate);
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

    private void setCurrentDate() {
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);

        currentTrainingDate = calendar.get(Calendar.DAY_OF_MONTH) + "/" + twoDecimalFormatter.format(calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR);

        dateTextView.setText(currentTrainingDate);
    }

    private void selectTraining() {
        LocalDate currentTrainingLocalDate = LocalDate.parse(currentTrainingDate, dateFormatter);
        LocalDate lastTrainingLocalDate = LocalDate.parse(lastTrainingDate, dateFormatter);
        if (currentTrainingLocalDate.compareTo(lastTrainingLocalDate) <= 0) {
            HashMap<String, Object> serieInfo = (HashMap<String, Object>) trainingHistory.get(currentTrainingDate);
            if (serieInfo != null) {
                serieTextView.setText((String) serieInfo.getOrDefault("SerieName", "Não houve treino nesta data."));
            } else {
                serieTextView.setText("Não houve treino nesta data.");
            }
        } else {
            serieTextView.setText(currentSerieId);
            Toast.makeText(getContext(), currentSerieId, Toast.LENGTH_LONG).show();
        }
    }

}