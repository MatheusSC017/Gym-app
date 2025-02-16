package com.example.academy.ui.workout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import android.text.InputType;
import android.view.*;
import android.widget.*;

import com.example.academy.MainActivity;
import com.example.academy.R;
import com.example.academy.database.repositories.ExerciseRepository;
import com.example.academy.database.repositories.SerieRepository;
import com.example.academy.database.repositories.WorkoutRepository;
import com.example.academy.models.ExerciseModel;
import com.example.academy.models.SerieModel;
import com.example.academy.models.WorkoutModel;
import com.example.academy.ui.base.JsonFragment;
import com.example.academy.utils.Utils;
import com.example.academy.view.EditTextDate;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


public class WorkoutFragment extends JsonFragment {
    WorkoutRepository workoutRepository = null;
    SerieRepository serieRepository = null;
    ExerciseRepository exerciseRepository = null;

    private LinkedHashMap<String, Long> workoutDates = new LinkedHashMap<>();
    private LinkedHashMap<String, Long> seriesMap = new LinkedHashMap<>();

    private LinearLayout workoutLayout;
    private Spinner workoutsSpinner;
    private Spinner exerciseSeriesSpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout, container, false);

        workoutRepository = new WorkoutRepository(getContext());
        serieRepository = new SerieRepository(getContext());
        exerciseRepository = new ExerciseRepository(getContext());

        workoutLayout = view.findViewById(R.id.workoutLayout);
        workoutsSpinner = view.findViewById(R.id.personalDateSpinner);
        exerciseSeriesSpinner = view.findViewById(R.id.exerciseSeriesSpinner);

        Button insertButton = view.findViewById(R.id.insertButton);
        insertButton.setOnClickListener(event -> showInsertWorkoutDialog());

        Button editButton = view.findViewById(R.id.editButton);
        editButton.setOnClickListener(event -> navigateEditWorkout());

        Button deleteButton = view.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(event -> deleteWorkout());

        setupWorkoutSpinner();

        return view;
    }

    public void navigateEditWorkout() {
        if (workoutsSpinner.getSelectedItem() != null) {
            String workout = workoutsSpinner.getSelectedItem().toString();

            if (workout == null && workoutDates.containsKey(workout)) return;

            Bundle bundle = new Bundle();
            bundle.putLong("workout_id", workoutDates.get(workout));
            bundle.putString("workout_date", workout);

            RegisterWorkoutFragment fragment = new RegisterWorkoutFragment();
            fragment.setArguments(bundle);

            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).loadFragment(fragment);
        }
    }

    public void showInsertWorkoutDialog() {
        DecimalFormat twoDecimalFormatter = new DecimalFormat("00");
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Adicionar Treino");

        LocalDate today = LocalDate.now();

        EditTextDate workoutEditTextDate = new EditTextDate(getContext());
        workoutEditTextDate.setText(twoDecimalFormatter.format(today.getMonthValue()) + "/" + today.getYear());
        workoutEditTextDate.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(workoutEditTextDate);

        builder.setPositiveButton("Cadastrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                insertWorkout(workoutEditTextDate.getText().toString());
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    private void insertWorkout(String workout) {
        if (workout.length() != 7) {
            Toast.makeText(getContext(), "Insira uma data válida", Toast.LENGTH_LONG).show();
            return;
        }
        long result = workoutRepository.add(workout);
        if (result == -1) {
            Toast.makeText(getContext(), "Erro: Verifique se um treino nesta data já existe.", Toast.LENGTH_LONG).show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putLong("workout_id", result);
        bundle.putString("workout_date", workout);

        RegisterWorkoutFragment fragment = new RegisterWorkoutFragment();
        fragment.setArguments(bundle);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).loadFragment(fragment);
        }
    }

    private void deleteWorkout() {
        Object workout = workoutsSpinner.getSelectedItem();

        if (workout != null) {
            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle("Deseja confirmar a exclusão deste treinamento?")
                    .setPositiveButton("Confirmar", ((dialogInterface, i) -> {
                        boolean result = workoutRepository.delete(workoutDates.get(workout.toString()));
                        setupWorkoutSpinner();
                        if (!result) {
                            Toast.makeText(getContext(), "Erro ao deletar Treinamento", Toast.LENGTH_LONG).show();
                        }
                    })).setNegativeButton("Cancelar", ((dialogInterface, i) -> {
                        // Do nothing
                    })).create();
            dialog.show();
        }
    }

    private void setupWorkoutSpinner() {
        List<WorkoutModel> workoutsList = workoutRepository.getAll();
        Comparator<String> comparatorWorkoutDate = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String[] parts1 = o1.split("/");
                String[] parts2 = o2.split("/");

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

        workoutDates.clear();
        for (WorkoutModel workout: workoutsList) {
            workoutDates.put(workout.getDate(), workout.getId());
        }

        List<String> orderedWorkoutDates = workoutDates.keySet().stream().collect(Collectors.toList());
        orderedWorkoutDates.sort(comparatorWorkoutDate);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, orderedWorkoutDates);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        workoutsSpinner.setAdapter(adapter);

        workoutsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setupSeriesSpinner(workoutDates.get(workoutsSpinner.getSelectedItem().toString()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        if (orderedWorkoutDates.size() == 0)
            setupSeriesSpinner(0L);
    }

    private void setupSeriesSpinner(Long workoutId) {
        List<String> seriesNames = loadSeriesNames(workoutId);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, seriesNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exerciseSeriesSpinner.setAdapter(adapter);

        exerciseSeriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setupExercisesCards(seriesMap.get(exerciseSeriesSpinner.getSelectedItem().toString()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
    }

    private List<String> loadSeriesNames(Long workoutId) {
        List<String> seriesNames = new ArrayList<>();
        try {
            workoutLayout.removeAllViews();
            seriesMap.clear();

            List<SerieModel> seriesList = serieRepository.getAll(workoutId);
            for (SerieModel serie: seriesList) {
                String serieName = Utils.getLetter(seriesNames.size()) + "- " + serie.getName();
                seriesNames.add(serieName);
                seriesMap.put(serieName, serie.getId());
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error loading Series: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            return seriesNames;
        }
    }

    private void setupExercisesCards(Long serieId) {
        List<ExerciseModel> exercisesList = exerciseRepository.getAll(serieId);

        workoutLayout.removeAllViews();
        while (exercisesList.size() > 0) {
            ExerciseModel exercise = exercisesList.get(0);
            exercisesList.remove(0);
            View exerciseCard = LayoutInflater.from(getContext()).inflate(R.layout.workout_card, workoutLayout, false);
            LinearLayout exerciseCardLayout = exerciseCard.findViewById(R.id.exercisesLayout);
            setupExerciseCard(exerciseCardLayout, exercise);
            if (exercise.getSequence() != 0) {
                int i = 0;
                while (exercisesList.size() > i) {
                    ExerciseModel exerciseSequence = exercisesList.get(i);
                    if (exercise.getSequence().equals(exerciseSequence.getSequence())) {
                        exercisesList.remove(i);

                        ImageView chainImage = new ImageView(getContext());
                        chainImage.setImageResource(R.drawable.ic_link_16);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        chainImage.setLayoutParams(params);
                        exerciseCardLayout.addView(chainImage);
                        setupExerciseCard(exerciseCardLayout, exerciseSequence);
                        continue;
                    }
                    i = i + 1;
                }
            }
            workoutLayout.addView(exerciseCard);
        }
    }

    private void setupExerciseCard(LinearLayout layout, ExerciseModel  exercise) {
        View exerciseCard = LayoutInflater.from(getContext()).inflate(R.layout.exercise_layout, layout, false);

        TextView exerciseTextView = exerciseCard.findViewById(R.id.exerciseTextView);
        TextView muscleTextView = exerciseCard.findViewById(R.id.muscleTextView);
        TextView seriesTextView = exerciseCard.findViewById(R.id.seriesTextView);
        TextView repetitionsTextView = exerciseCard.findViewById(R.id.repetitionsTextView);


        exerciseTextView.setText(exercise.getName());
        muscleTextView.setText(exercise.getMuscle());
        if (exercise.getSeriesNumber() > 1) seriesTextView.setText(exercise.getSeriesNumber() + " x");
        repetitionsTextView.setText(exercise.getQuantity() + " " + exercise.getMeasure());

        layout.addView(exerciseCard);
    }

}