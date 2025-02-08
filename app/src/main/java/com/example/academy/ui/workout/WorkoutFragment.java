package com.example.academy.ui.workout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;

import android.text.InputType;
import android.view.*;
import android.widget.*;

import com.example.academy.MainActivity;
import com.example.academy.R;
import com.example.academy.database.SerieHelper;
import com.example.academy.database.WorkoutHelper;
import com.example.academy.database.repositories.SerieRepository;
import com.example.academy.database.repositories.WorkoutRepository;
import com.example.academy.ui.base.JsonFragment;
import com.example.academy.utils.Utils;
import com.example.academy.view.EditTextDate;
import com.google.gson.internal.TroubleshootingGuide;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


public class WorkoutFragment extends JsonFragment {
    private static String WORKOUTS_FILE = "workouts.json";

    WorkoutRepository workoutRepository = null;
    SerieRepository serieRepository = null;

    DecimalFormat twoDecimalFormatter = new DecimalFormat("00");
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

    private LinkedHashMap<String, Long> workoutDates = new LinkedHashMap<>();
    private List<String> workoutsIds = new ArrayList<>(); // Delete variable
    private HashMap<String, Object> workoutsMap; // Delete variable

    private LinearLayout workoutLayout;
    private Spinner workoutsSpinner;
    private Spinner exerciseSeriesSpinner;
    private Button insertButton;
    private Button editButton;
    private Button deleteButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout, container, false);

        workoutRepository = new WorkoutRepository(getContext());
        serieRepository = new SerieRepository(getContext());

        insertButton = view.findViewById(R.id.insertButton);
        editButton = view.findViewById(R.id.editButton);
        deleteButton = view.findViewById(R.id.deleteButton);
        workoutLayout = view.findViewById(R.id.workoutLayout);
        workoutsSpinner = view.findViewById(R.id.personalDateSpinner);
        exerciseSeriesSpinner = view.findViewById(R.id.exerciseSeriesSpinner);

        insertButton.setOnClickListener(event -> insertWorkout());

        editButton.setOnClickListener(event -> navigateEditWorkout());

        deleteButton.setOnClickListener(event -> deleteWorkout());

//        workoutsMap = loadJsonData(WORKOUTS_FILE);
        setupWorkoutSpinner();

        return view;
    }

    // Deprecated
    public HashMap<String, Object> loadJsonData(String filePath) {
        try {
            HashMap<String, Object> workoutsExtractedMap = super.loadJsonData(filePath);
            if (workoutsExtractedMap != null) {
                workoutsIds = workoutsExtractedMap.keySet().stream().collect(Collectors.toList());

                workoutsIds.sort(comparatorWorkoutDate);
            }
            return workoutsExtractedMap;
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error extracting workouts: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    // Review
    public void navigateEditWorkout() {
        String workout = workoutsSpinner.getSelectedItem().toString();

        if (workout == null) return;

        Bundle bundle = new Bundle();
        bundle.putString("workout", workout);

        RegisterWorkoutFragment fragment = new RegisterWorkoutFragment();
        fragment.setArguments(bundle);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).loadFragment(fragment);
        }
    }

    public void insertWorkout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Adicionar Treino");

        LocalDate today = LocalDate.now();

        final EditTextDate workoutEditTextDate = new EditTextDate(getContext());
        workoutEditTextDate.setText(twoDecimalFormatter.format(today.getMonthValue()) + "/" + today.getYear());
        workoutEditTextDate.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(workoutEditTextDate);

        builder.setPositiveButton("Cadastrar", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (workoutEditTextDate.getText().length() != 7) {
                    Toast.makeText(getContext(), "Insira uma data válida", Toast.LENGTH_LONG).show();
                    return;
                }
                String workoutDate = workoutEditTextDate.getText().toString();
                long result = workoutRepository.addWorkout(workoutDate);
                if (result == -1) {
                    Toast.makeText(getContext(), "Erro: Verifique se um treino nesta data já existe.", Toast.LENGTH_LONG).show();
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putLong("workout_id", result);
                    bundle.putString("workout_date", workoutDate);

                    RegisterWorkoutFragment fragment = new RegisterWorkoutFragment();
                    fragment.setArguments(bundle);

                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).loadFragment(fragment);
                    }
                }
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

    private void deleteWorkout() {
        Object workout = workoutsSpinner.getSelectedItem();

        if (workout != null) {
            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle("Deseja confirmar a exclusão deste treinamento?")
                    .setPositiveButton("Confirmar", ((dialogInterface, i) -> {
                        boolean result = workoutRepository.deleteWorkout(workoutDates.get(workout.toString()));
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
        List<HashMap<String, Object>> workoutsList = workoutRepository.getAllWorkouts();

        workoutDates.clear();
        for (HashMap<String, Object> workout: workoutsList) {
            workoutDates.put((String) workout.get(WorkoutHelper.COLUMN_DATE), (Long) workout.get(WorkoutHelper.COLUMN_ID));
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
    }

    private void setupSeriesSpinner(Long workoutId) {
        List<String> seriesNames = new ArrayList<>();

        try {
            workoutLayout.removeAllViews();

            List<HashMap<String, Object>> seriesList = serieRepository.getSeries(workoutId);
            for (HashMap<String, Object> serie: seriesList) {
                seriesNames.add(Utils.getLetter(seriesNames.size()) + "- " + serie.get(SerieHelper.COLUMN_NAME));
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error loading Series: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, seriesNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exerciseSeriesSpinner.setAdapter(adapter);

        exerciseSeriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                setupExercisesCards(workoutId, seriesIds.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
    }

    // Review
    private void setupExercisesCards(String workoutId, String serieId) {
        HashMap<String, Object> workout = (HashMap<String, Object>) workoutsMap.get(workoutId);
        if (workout == null) return;

        HashMap<String, Object> series = (HashMap<String, Object>) workout.get("Series");
        if (series == null) return;

        LinkedHashMap<String, Object> exercises = (LinkedHashMap<String, Object>) series.get(serieId);

        if (exercises != null) {
            LinkedHashMap<String, Object> exercisesCopy = new LinkedHashMap<String, Object>(exercises);

            workoutLayout.removeAllViews();
            while (exercisesCopy.keySet().stream().count() > 0){
                String exercise = exercisesCopy.keySet().stream().findFirst().orElse(null).toString();
                HashMap<String, Object> exerciseData = (HashMap<String, Object>) exercisesCopy.get(exercise);
                exercisesCopy.remove(exercise);

                View exerciseCard = LayoutInflater.from(getContext()).inflate(R.layout.workout_card, workoutLayout, false);
                LinearLayout exerciseCardLayout = exerciseCard.findViewById(R.id.exercisesLayout);

                setupExerciseCard(exercise, exerciseData, exerciseCardLayout);
                if (exerciseData.containsKey("Sequence")) {
                    List<String> chainedExercises = (List<String>) exerciseData.get("Sequence");
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

    // Review
    private void setupExerciseCard(String exercise, HashMap<String, Object> exerciseData, LinearLayout layout) {
        View exerciseCard = LayoutInflater.from(getContext()).inflate(R.layout.exercise_layout, layout, false);

        TextView exerciseTextView = exerciseCard.findViewById(R.id.exerciseTextView);
        TextView muscleTextView = exerciseCard.findViewById(R.id.muscleTextView);
        TextView seriesTextView = exerciseCard.findViewById(R.id.seriesTextView);
        TextView repetitionsTextView = exerciseCard.findViewById(R.id.repetitionsTextView);

        exerciseTextView.setText(exercise);
        muscleTextView.setText(exerciseData.get("Muscle").toString());

        String series = exerciseData.getOrDefault("Series", "1").toString();
        if (!series.equals("1")) seriesTextView.setText(series + " x");

        repetitionsTextView.setText(exerciseData.getOrDefault("Quantity", "").toString() + " " + exerciseData.getOrDefault("Type", "").toString());
        layout.addView(exerciseCard);
    }

}