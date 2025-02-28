package com.matheus.academy.ui.history;

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

import com.matheus.academy.R;
import com.matheus.academy.database.repositories.ExerciseRepository;
import com.matheus.academy.database.repositories.HistoryRepository;
import com.matheus.academy.database.repositories.SerieRepository;
import com.matheus.academy.database.repositories.WorkoutRepository;
import com.matheus.academy.models.ExerciseModel;
import com.matheus.academy.models.HistoryModel;
import com.matheus.academy.models.SerieModel;
import com.matheus.academy.models.WorkoutModel;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HistoryFragment extends Fragment {
    private HistoryRepository historyRepository;
    private WorkoutRepository workoutRepository;
    private SerieRepository serieRepository;
    private ExerciseRepository exerciseRepository;

    private List<SerieModel>  series;
    private HistoryModel lastTrainingHistory;

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

        Button selectDateButton = view.findViewById(R.id.selectDateButton);
        selectDateButton.setOnClickListener(event -> trainingCalendarView.setVisibility(View.VISIBLE));

        trainingCalendarView = view.findViewById(R.id.trainingCalendarView);
        trainingCalendarView.setVisibility(View.GONE);
        trainingCalendarView.setOnDateChangeListener((calendarView, year, month, day) -> {
            String currentTrainingDate = twoDecimalFormatter.format(day) + "/" + twoDecimalFormatter.format(month + 1) + "/" + year;
            selectTraining(currentTrainingDate);

            dateTextView.setText(currentTrainingDate);
            trainingCalendarView.setVisibility(View.GONE);
        });

        Button saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(event -> saveHistory());

        series = getWorkoutSeries();
        if (series.isEmpty()) {
            serieTextView.setText("Não há Series Cadastradas");
            return view;
        }
        lastTrainingHistory = getAllTrainingHistory();

        if (lastTrainingHistory == null) {
            currentSerie = series.get(0);
        } else {
            for (int i = 0; i < series.size(); i++) {
                if (lastTrainingHistory.getSerieId().equals(series.get(i).getId())) {
                    if (lastTrainingHistory.getDateFormatted().equals(dateTextView.getText().toString())) {
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

    private HistoryModel getAllTrainingHistory() {
        List<HistoryModel> trainingHistory = historyRepository.getAll();
        if (trainingHistory.isEmpty()) return null;

        Comparator<HistoryModel> comparatorTrainingHistoryDate = new Comparator<HistoryModel>() {
            @Override
            public int compare(HistoryModel o1, HistoryModel o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        };

        trainingHistory.sort(comparatorTrainingHistoryDate);

        return trainingHistory.get(0);
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

        if (series.isEmpty()) {
            serieTextView.setText("Treinamento não encontrado");
            return;
        }

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
                exerciseTextView.setText(exercise.getName());

                TextView muscleTextView = exerciseCard.findViewById(R.id.muscleTextView);
                muscleTextView.setText(exercise.getMuscle());

                TextView seriesTextView = exerciseCard.findViewById(R.id.seriesTextView);
                seriesTextView.setText(exercise.getSeriesNumber() > 1 ? exercise.getSeriesNumber() + " x" : "");

                TextView repetitionsTextView = exerciseCard.findViewById(R.id.repetitionsTextView);
                repetitionsTextView.setText(exercise.getQuantity() + " " + exercise.getMeasure());

                TextView weightTextView = exerciseCard.findViewById(R.id.weightTextView);
                weightTextView.setText(exercise.getWeight() != 0 ? exercise.getWeight() + " Kg" : "");

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