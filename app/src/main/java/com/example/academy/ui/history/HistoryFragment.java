package com.example.academy.ui.history;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.academy.R;
import com.example.academy.database.repositories.ExerciseRepository;
import com.example.academy.database.repositories.HistoryRepository;
import com.example.academy.database.repositories.SerieRepository;
import com.example.academy.database.repositories.WorkoutRepository;
import com.example.academy.models.ExerciseModel;
import com.example.academy.models.HistoryModel;
import com.example.academy.models.SerieModel;
import com.example.academy.models.WorkoutModel;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class HistoryFragment extends Fragment {
    private HistoryRepository historyRepository;
    private WorkoutRepository workoutRepository;
    private SerieRepository serieRepository;
    private ExerciseRepository exerciseRepository;

    private List<SerieModel>  series;
    private List<HistoryModel> trainingHistory;

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DecimalFormat twoDecimalFormatter = new DecimalFormat("00");

    private SerieModel currentSerie;

    private TextView dateTextView;
    private TextView serieTextView;
    private CalendarView trainingCalendarView;
    private LinearLayout exercisesLinearLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        historyRepository = new HistoryRepository(getContext());
        workoutRepository = new WorkoutRepository(getContext());
        serieRepository = new SerieRepository(getContext());
        exerciseRepository = new ExerciseRepository(getContext());


        exercisesLinearLayout = view.findViewById(R.id.exercisesLinearLayout);

        serieTextView = view.findViewById(R.id.serieTextView);

        dateTextView = view.findViewById(R.id.dateTextView);
        setCurrentDate();

        series = getWorkoutSeries();
        if (series.isEmpty()) {
            serieTextView.setText("Não há Series Cadastradas");
            return view;
        }
        trainingHistory = getAllTrainingHistory();

        if (trainingHistory.isEmpty()) {
            currentSerie = series.get(0);
        } else {
            HistoryModel history = trainingHistory.get(0);

            for (int i = 0; i < series.size(); i++) {
                if (history.getSerieId().equals(series.get(i).getId())) {
                    if (history.getDateFormatted().equals(dateTextView.getText().toString())) {
                        currentSerie = series.get(i);
                    } else {
                        currentSerie = series.get(i + 1);
                    }
                    break;
                }
            }

        }

        serieTextView.setText(currentSerie.getName());
        setExercises(currentSerie.getId());

        Button selectDateButton = view.findViewById(R.id.selectDateButton);
        selectDateButton.setOnClickListener(event -> trainingCalendarView.setVisibility(View.VISIBLE));

        trainingCalendarView = view.findViewById(R.id.trainingCalendarView);
        trainingCalendarView.setVisibility(View.GONE);
        trainingCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
                String currentTrainingDate = twoDecimalFormatter.format(day) + "/" + twoDecimalFormatter.format(month + 1) + "/" + year;
                selectTraining(currentTrainingDate);

                dateTextView.setText(currentTrainingDate);
                trainingCalendarView.setVisibility(View.GONE);
            }
        });

        Button saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(event -> saveHistory());

        return view;
    }

    private List<SerieModel> getWorkoutSeries() {
        try {
            List<WorkoutModel> workoutsList = workoutRepository.getAll();
            if (workoutsList.isEmpty())
                return new ArrayList<>();

            Comparator<WorkoutModel> comparatorWorkoutDate = new Comparator<WorkoutModel>() {
                @Override
                public int compare(WorkoutModel o1, WorkoutModel o2) {
                    String[] parts1 = o1.getDate().split("/");
                    String[] parts2 = o2.getDate().split("/");

                    int year1 = Integer.parseInt(parts1[1]);
                    int month1 = Integer.parseInt(parts1[0]);
                    int year2 = Integer.parseInt(parts2[1]);
                    int month2 = Integer.parseInt(parts2[0]);

                    if (year1 != year2) {
                        return Integer.compare(year2, year1);
                    } else {
                        return Integer.compare(month2, month1);
                    }
                }
            };

            workoutsList.sort(comparatorWorkoutDate);
            Long workoutId = workoutsList.get(0).getId();
            return serieRepository.getAll(workoutId);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error extracting workouts: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return new ArrayList<>();
        }
    }

    // Review (It's necessary to get All training data or only the last?
    private List<HistoryModel> getAllTrainingHistory() {
        List<HistoryModel> trainingHistory = historyRepository.getAll();

        Comparator<HistoryModel> comparatorTrainingHistoryDate = new Comparator<HistoryModel>() {
            @Override
            public int compare(HistoryModel o1, HistoryModel o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        };

        trainingHistory.sort(comparatorTrainingHistoryDate);
        return trainingHistory;
    }

    private void setCurrentDate() {
        LocalDate today = LocalDate.now();
        String currentTrainingDate = twoDecimalFormatter.format(today.getDayOfMonth()) + "/" + twoDecimalFormatter.format(today.getMonthValue()) + "/" + today.getYear();
        dateTextView.setText(currentTrainingDate);
    }

    private void selectTraining(String currentTrainingDate) {
        HistoryModel history = historyRepository.getByDate(currentTrainingDate);
        LocalDate today = LocalDate.now();
        String todayDate = twoDecimalFormatter.format(today.getDayOfMonth()) + "/" + twoDecimalFormatter.format(today.getMonthValue()) + "/" + today.getYear();


        exercisesLinearLayout.removeAllViews();
        if (history != null) {
            SerieModel serie = getSerie(history.getSerieId());
            if (serie != null) {
                serieTextView.setText(serie.getName());
                setExercises(serie.getId());
            }
        } else if (currentTrainingDate.equals(todayDate)) {
            serieTextView.setText(currentSerie.getName());
            setExercises(currentSerie.getId());
        } else {
            serieTextView.setText("Treinamento não encontrado");
        }
    }

    private SerieModel getSerie(Long serieId) {
        for (SerieModel serie: series) {
            if (serie.getId().equals(serieId)) {
                return serie;
            }
        }
        return null;
    }

    private void setExercises(Long serieId) {
        List<ExerciseModel> exercises = exerciseRepository.getAll(serieId);
        if (!exercises.isEmpty()) {
            exercises.forEach((ExerciseModel exercise) -> {
                View exerciseCard = LayoutInflater.from(getContext()).inflate(R.layout.exercise_layout, exercisesLinearLayout, false);

                TextView exerciseTextView = exerciseCard.findViewById(R.id.exerciseTextView);
                TextView muscleTextView = exerciseCard.findViewById(R.id.muscleTextView);
                TextView seriesTextView = exerciseCard.findViewById(R.id.seriesTextView);
                TextView repetitionsTextView = exerciseCard.findViewById(R.id.repetitionsTextView);

                exerciseTextView.setText(exercise.getName());
                muscleTextView.setText(exercise.getMuscle());

                String series = exercise.getSeriesNumber().toString();
                if (!series.equals("1")) seriesTextView.setText(series + " x");

                repetitionsTextView.setText(exercise.getQuantity() + " " + exercise.getMeasure());
                exercisesLinearLayout.addView(exerciseCard);
            });
        }
    }

    private void saveHistory() {
        LocalDate today = LocalDate.now();
        String currentDate = twoDecimalFormatter.format(today.getDayOfMonth()) + "/" + twoDecimalFormatter.format(today.getMonthValue()) + "/" + today.getYear();

        if (dateTextView.getText().toString().equals(currentDate)) {
            historyRepository.add(currentDate, currentSerie.getId());
            Toast.makeText(getContext(), "Treino salvo", Toast.LENGTH_LONG).show();
        }
    }

}