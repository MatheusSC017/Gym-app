package com.example.academy.ui.workout;

import com.example.academy.MainActivity;
import com.example.academy.R;
import com.example.academy.ui.base.JsonFragment;
import com.example.academy.view.EditTextDate;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import android.view.*;
import android.widget.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RegisterWorkoutFragment extends JsonFragment {
    private static List<String> TYPES_OF_MEASURES = List.of("Repetições", "Segundos", "Minutos", "Horas", "Ciclos");
    private static String WORKOUTS_FILE = "workouts.json";
    private static Integer SEQUENCE_AVAILABLE_SELECTION = 9;

    private static Boolean editable = false;

    private List<String> seriesNames = new ArrayList<>();
    private List<List<Object>> seriesList = new ArrayList<>();
    private HashMap<String, HashMap> exercisesSerie;
    private List<List<String>> sequenceGroups;

    private LinearLayout exerciseLinearLayout;
    private Spinner seriesSpinner;
    private EditTextDate workoutDate;
    private Button returnButton;
    private Button saveButton;
    private Button setLinksButton;
    private Button addSerieButton;
    private Button removeSerieButton;
    private Button addExerciseButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_workout, container, false);

        workoutDate = view.findViewById(R.id.editTextDate);
        exerciseLinearLayout = view.findViewById(R.id.exerciseLinearLayout);
        seriesSpinner = view.findViewById(R.id.seriesSpinner);

        returnButton = view.findViewById(R.id.returnButton);
        saveButton = view.findViewById(R.id.saveButton);
        setLinksButton = view.findViewById(R.id.setLinksButton);
        addSerieButton = view.findViewById(R.id.addSerieButton);
        removeSerieButton = view.findViewById(R.id.removeSerieButton);
        addExerciseButton = view.findViewById(R.id.addExerciseButton);

        returnButton.setOnClickListener(event -> {
            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle("Caso opte por retornar as informações serão perdidas")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Confirmar", (((dialogInterface, i) -> {
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).loadFragment(new WorkoutFragment());
                        }
                    }))).create();
            dialog.show();
        });
        addSerieButton.setOnClickListener(event -> showSerieRegisterDialog());
        removeSerieButton.setOnClickListener(event -> removeSerie());
        addExerciseButton.setOnClickListener(event -> showExerciseRegisterDialog());
        saveButton.setOnClickListener(event -> saveWorkout());
        setLinksButton.setOnClickListener(event -> showSetLinksDialog());

        Bundle bundle = getArguments();
        if (bundle != null) {
            editable = true;

            String workout = bundle.getString("workout");
            loadWorkoutData(workout);
        } else {
            setWorkoutDate();
        }

        return view;
    }

    private void loadWorkoutData(String workout) {
        workoutDate.setText(workout);

        HashMap<String, Object> workouts = loadJsonData(WORKOUTS_FILE);
        HashMap<String, HashMap> workoutData = (HashMap<String, HashMap>) workouts.getOrDefault(workout, null);
        if (workoutData != null) {
            HashMap<String, HashMap> seriesData = (HashMap<String, HashMap>) workoutData.getOrDefault("Series", new HashMap<String, HashMap>());
            seriesNames = seriesData.keySet().stream().collect(Collectors.toList());

            for (String serieName : seriesNames) {
                HashMap<String, HashMap> exercises = seriesData.get(serieName);

                List<Object> serie = new ArrayList<>();
                serie.add(serieName);
                serie.add(exercises);
                serie.add(loadSequenceArray(exercises));
                seriesList.add(serie);
            }

            if (!seriesNames.isEmpty()) {
                Collections.sort(seriesNames);
                seriesNames = seriesNames.stream().map(serieName -> serieName.substring(3)).collect(Collectors.toList());
                setSeriesSpinner();
            }
        }
    }

    private List<List<String>> loadSequenceArray(HashMap<String, HashMap> exercises) {
        List<List<String>> sequenceArray = new ArrayList<>();
        for (int i = 0; i < SEQUENCE_AVAILABLE_SELECTION; i++) sequenceArray.add(new ArrayList<>());

        if (exercises != null && !exercises.isEmpty()) {
            AtomicInteger sequenceCounter = new AtomicInteger(0);
            List<String> recordedSequenceExercises = new ArrayList<>();

            exercises.forEach((String exerciseName, HashMap exerciseData) -> {
                if (!recordedSequenceExercises.contains(exerciseName) &&
                        exerciseData.containsKey("Sequence") &&
                        ((List) exerciseData.get("Sequence")).size() > 0) {
                    recordedSequenceExercises.addAll((List) exerciseData.get("Sequence"));
                    int index = sequenceCounter.getAndIncrement();
                    sequenceArray.get(index).add(exerciseName);
                    sequenceArray.get(index).addAll((List) exerciseData.get("Sequence"));
                }
            });
        }
        return  sequenceArray;
    }

    private void setWorkoutDate() {
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);

        String month;
        if (calendar.get(Calendar.MONTH) <= 9)
            month = "0" + (calendar.get(Calendar.MONTH) + 1);
        else
            month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        workoutDate.setText(month + "/" + calendar.get(Calendar.YEAR));
    }

    private void showSerieRegisterDialog() {
        if (workoutDate.getText().length() != 7) {
            Toast.makeText(getContext(), "Insira a data da avaliação", Toast.LENGTH_LONG).show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_serie_register, null);

        EditText registerEditText = dialogView.findViewById(R.id.registerEditText);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button submitButton = dialogView.findViewById(R.id.submitButton);

        AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(dialogView).setCancelable(false).create();

        cancelButton.setOnClickListener(event -> dialog.dismiss());
        submitButton.setOnClickListener(event -> {
            addNewSerie(registerEditText.getText().toString());
            dialog.dismiss();
        });

        dialog.show();
    }

    private void addNewSerie(String serieName) {
        int selectedSerieIndex = seriesSpinner.getSelectedItemPosition();
        List<List<String>> sequenceArray = new ArrayList<>();
        for (int i = 0; i < SEQUENCE_AVAILABLE_SELECTION; i++) sequenceArray.add(new ArrayList<>());

        List<Object> serie = new ArrayList<>();
        serie.add(serieName);
        serie.add(new HashMap<>());
        serie.add(sequenceArray);
        seriesList.add(serie);
        seriesNames.add(serieName);

        setSeriesSpinner();
        seriesSpinner.setSelection(selectedSerieIndex);
    }

    private void removeSerie() {
        Object serieSelected = seriesSpinner.getSelectedItem();

        if (serieSelected != null) {
            seriesList.remove(seriesSpinner.getSelectedItemPosition());
            seriesNames.remove(seriesSpinner.getSelectedItemPosition());

            exerciseLinearLayout.removeAllViews();
            setSeriesSpinner();
        }
    }

    private void setSeriesSpinner() {
        List<String> seriesNamesFormatted = new ArrayList<>();
        for (int i = 0; i < seriesNames.size(); i++) {
            seriesNamesFormatted.add(getLetter(i) + "- " + seriesNames.get(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                seriesNamesFormatted);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seriesSpinner.setAdapter(adapter);

        seriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                exercisesSerie = (HashMap<String, HashMap>) seriesList.get(position).get(1);


                sequenceGroups = (List<List<String>>) seriesList.get(position).get(2);

                exerciseLinearLayout.removeAllViews();
                exercisesSerie.forEach((exerciseName, exerciseData) -> {
                    setupExerciseCard(exerciseLinearLayout, exerciseName, exerciseData);
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });

    }

    private void showExerciseRegisterDialog() {
        Object serieSelected = seriesSpinner.getSelectedItem();

        if (serieSelected != null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View dialogView = layoutInflater.inflate(R.layout.dialog_exercise_register, null);
            Button cancelButton = dialogView.findViewById(R.id.cancelButton);
            Button saveButton = dialogView.findViewById(R.id.submitButton);
            Spinner typeSpinner = dialogView.findViewById(R.id.typeSpinner);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, TYPES_OF_MEASURES);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            typeSpinner.setAdapter(adapter);

            AlertDialog registerDialog = new AlertDialog.Builder(getContext()).setView(dialogView).setCancelable(false).create();
            cancelButton.setOnClickListener(event -> registerDialog.dismiss());
            saveButton.setOnClickListener(event -> {
                addExercise(dialogView);

                registerDialog.dismiss();
            });
            registerDialog.show();
        }
    }

    private void addExercise(View dialogView) {
        EditText exerciseEditText = dialogView.findViewById(R.id.exerciseEditText);
        EditText seriesEditText = dialogView.findViewById(R.id.seriesEditText);
        Spinner typeSpinner = dialogView.findViewById(R.id.typeSpinner);
        EditText quantityEditText = dialogView.findViewById(R.id.quantityEditText);
        EditText muscleEditText = dialogView.findViewById(R.id.muscleEditText);
        EditText observationEditText = dialogView.findViewById(R.id.observationEditText);

        if (exerciseEditText.getText().length() == 0 ||
                seriesEditText.getText().length() == 0 ||
                typeSpinner.getSelectedItem() == null ||
                quantityEditText.getText().length() == 0) {
            Toast.makeText(getContext(), "Campos obrigatórios (Exercício, Series, Tipo e Quantidade)", Toast.LENGTH_LONG).show();
            return;
        }

        if (exercisesSerie.keySet().contains(exerciseEditText.getText().toString())) {
            Toast.makeText(getContext(), "Registro já existente", Toast.LENGTH_LONG).show();
            return;
        }

        HashMap<String, Object> exerciseMap = new HashMap<>();

        exerciseMap.put("Series", seriesEditText.getText().toString());
        exerciseMap.put("Type", typeSpinner.getSelectedItem().toString());
        exerciseMap.put("Quantity", quantityEditText.getText().toString());
        exerciseMap.put("Muscle", muscleEditText.getText().toString());
        exerciseMap.put("Observation", observationEditText.getText().toString());

        exercisesSerie.put(exerciseEditText.getText().toString(), exerciseMap);

        setupExerciseCard(exerciseLinearLayout, exerciseEditText.getText().toString(), exerciseMap);
    }

    private void setupExerciseCard(LinearLayout layout, String exercise, HashMap<String, Object>  exerciseData) {
        View exerciseCard = LayoutInflater.from(getContext()).inflate(R.layout.register_layout, layout, false);
        ViewStub contentViewStub = exerciseCard.findViewById(R.id.contentViewStub);
        contentViewStub.setLayoutResource(R.layout.exercise_layout);
        View contentInflated = contentViewStub.inflate();

        TextView exerciseTextView = contentInflated.findViewById(R.id.exerciseTextView);
        TextView muscleTextView = contentInflated.findViewById(R.id.muscleTextView);
        TextView seriesTextView = contentInflated.findViewById(R.id.seriesTextView);
        TextView repetitionsTextView = contentInflated.findViewById(R.id.repetitionsTextView);
        Button editExerciseButton = exerciseCard.findViewById(R.id.editExerciseButton);
        Button removeExerciseButton = exerciseCard.findViewById(R.id.removeExerciseButton);

        editExerciseButton.setOnClickListener(event -> showEditExerciseDialog(exerciseTextView.getText().toString()));
        removeExerciseButton.setOnClickListener(event -> removeExercise(exerciseTextView.getText().toString()));

        exerciseTextView.setText(exercise);
        muscleTextView.setText(exerciseData.get("Muscle").toString());
        String series = exerciseData.getOrDefault("Series", "1").toString();
        if (!series.equals("1")) seriesTextView.setText(series + " x");
        repetitionsTextView.setText(exerciseData.getOrDefault("Quantity", "").toString() + " " + exerciseData.getOrDefault("Type", "").toString());

        layout.addView(exerciseCard);
    }

    private void showEditExerciseDialog(String exercise) {
        HashMap<String, String> exerciseData = exercisesSerie.get(exercise);


        if (exerciseData != null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View dialogView = layoutInflater.inflate(R.layout.dialog_exercise_register, null);

            EditText exerciseEditText = dialogView.findViewById(R.id.exerciseEditText);
            exerciseEditText.setText(exercise);
            EditText seriesEditText = dialogView.findViewById(R.id.seriesEditText);
            if (exerciseData.containsKey("Series")) seriesEditText.setText(String.valueOf(exerciseData.get("Series")));
            Spinner typeSpinner = dialogView.findViewById(R.id.typeSpinner);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, TYPES_OF_MEASURES);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            typeSpinner.setAdapter(adapter);
            typeSpinner.setSelection(TYPES_OF_MEASURES.indexOf(exerciseData.get("Type")));
            EditText quantityEditText = dialogView.findViewById(R.id.quantityEditText);
            if (exerciseData.containsKey("Quantity")) quantityEditText.setText(String.valueOf(exerciseData.get("Quantity")));
            EditText muscleEditText = dialogView.findViewById(R.id.muscleEditText);
            if (exerciseData.containsKey("Muscle")) muscleEditText.setText(exerciseData.get("Muscle"));
            EditText observationEditText = dialogView.findViewById(R.id.observationEditText);
            if (exerciseData.containsKey("Observation")) observationEditText.setText(exerciseData.get("Observation"));

            Button cancelButton = dialogView.findViewById(R.id.cancelButton);
            Button saveButton = dialogView.findViewById(R.id.submitButton);

            AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(dialogView).setCancelable(false).create();

            cancelButton.setOnClickListener(event -> dialog.dismiss());
            saveButton.setOnClickListener(event -> {
                exerciseData.put("Series", seriesEditText.getText().toString());
                exerciseData.put("Type", typeSpinner.getSelectedItem().toString());
                exerciseData.put("Quantity", quantityEditText.getText().toString());
                exerciseData.put("Muscle", muscleEditText.getText().toString());
                exerciseData.put("Observation", observationEditText.getText().toString());

                if (!exerciseEditText.getText().toString().equals(exercise)) {
                    exercisesSerie.remove(exercise);
                    exercisesSerie.put(exerciseEditText.getText().toString(), exerciseData);
                }

                for (int i = 0; i <= exerciseLinearLayout.getChildCount(); i++) {
                    View exerciseCard = exerciseLinearLayout.getChildAt(i);
                    TextView exerciseTextView = exerciseCard.findViewById(R.id.exerciseTextView);
                    if (exerciseTextView != null && exerciseTextView.getText().toString().equals(exercise)) {
                        TextView seriesTextView = exerciseCard.findViewById(R.id.seriesTextView);
                        TextView repetitionsTextView = exerciseCard.findViewById(R.id.repetitionsTextView);

                        exerciseTextView.setText(exerciseEditText.getText().toString());
                        String series = exerciseData.getOrDefault("Series", "1").toString();
                        if (!series.equals("1")) seriesTextView.setText(series + " x");
                        repetitionsTextView.setText(exerciseData.getOrDefault("Quantity", "").toString() + " " + exerciseData.getOrDefault("Type", "").toString());

                        break;
                    }
                }

                dialog.dismiss();
            });

            dialog.show();
        }
    }

    private void removeExercise(String exercise) {
        exercisesSerie.remove(exercise);

        for (int i = 0; i <= exerciseLinearLayout.getChildCount(); i++) {
            View exerciseCard = exerciseLinearLayout.getChildAt(i);
            TextView exerciseTextView = exerciseCard.findViewById(R.id.exerciseTextView);
            if (exerciseTextView.getText().toString().equals(exercise)) {
                exerciseLinearLayout.removeView(exerciseCard);
                break;
            }
        }

    }

    private void showSetLinksDialog() {
        Context context = getContext();
        if (exercisesSerie.isEmpty()) {
            Toast.makeText(context, "Nenhum exercício cadastrado", Toast.LENGTH_LONG).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_scroll_view, null);
        LinearLayout container = dialogView.findViewById(R.id.container);

        for (String exercise: exercisesSerie.keySet()) {
            View sequenceView = inflater.inflate(R.layout.sequence_selection_layout, container, false);

            TextView exerciseTextView = sequenceView.findViewById(R.id.exerciseTextView);
            Spinner sequenceSpinner = sequenceView.findViewById(R.id.sequenceSpinner);

            exerciseTextView.setText(exercise);
            ArrayAdapter<Integer> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            int selectedSequence = 0;
            for (int i = 0; i <= SEQUENCE_AVAILABLE_SELECTION; i++) {
                adapter.add(i);
                if (i > 0 && sequenceGroups.get(i - 1).contains(exercise)) selectedSequence = i;
            }
            sequenceSpinner.setAdapter(adapter);
            sequenceSpinner.setSelection(selectedSequence);

            container.addView(sequenceView);
        }

        builder.setView(dialogView)
                .setTitle("Definir exercícios em conjunto")
                .setPositiveButton("Salvar", (dialogInterface, i) -> setLinks(container));

        builder.create().show();

    }

    private void setLinks(LinearLayout container) {
        for (int j = 0; j < SEQUENCE_AVAILABLE_SELECTION; j++) sequenceGroups.get(j).clear();

        for (int j = 0; j < container.getChildCount(); j++) {
            LinearLayout sequenceView = (LinearLayout) container.getChildAt(j);
            TextView exerciseTextView = sequenceView.findViewById(R.id.exerciseTextView);
            Spinner sequenceSpinner = sequenceView.findViewById(R.id.sequenceSpinner);

            int sequenceValue = Integer.parseInt(sequenceSpinner.getSelectedItem().toString());
            if (sequenceValue != 0) {
                sequenceGroups.get(sequenceValue - 1).add(exerciseTextView.getText().toString());
            }

        }
    }

    private void saveWorkout() {
        if (workoutDate.getText().length() != 7) {
            Toast.makeText(getContext(), "Insira a data da avaliação", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            String workoutName = workoutDate.getText().toString();
            HashMap<String, Object> workoutsExtractedMap = super.loadJsonData(WORKOUTS_FILE);

            if (workoutsExtractedMap.containsKey(workoutName)) {
                HashMap<String, HashMap> workout = (HashMap<String, HashMap>) workoutsExtractedMap.get(workoutName);
                if (!editable && workout.keySet().contains("Series")) {
                    Toast.makeText(getContext(), "Já existe treinamento com está data", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            HashMap<String, HashMap> series = new HashMap<>();

            for (int i = 0; i < seriesList.size(); i++) {
                List<Object> serieData = seriesList.get(i);
                HashMap<String, Object> exercisesMap = (HashMap) serieData.get(1);
                List<List<String>> sequenceMap = (List<List<String>>) serieData.get(2);
                for (List<String> sequence: sequenceMap) {
                    if (sequence.stream().count() > 1) {
                        for (String exercise: sequence) ((HashMap) exercisesMap.get(exercise)).put("Sequence", sequence);
                    }
                }
                series.put(getLetter(i) + "- " + seriesNames.get(i), exercisesMap);

            }

            if (workoutsExtractedMap.containsKey(workoutName)) {
                HashMap<String, HashMap> workoutData = (HashMap<String, HashMap>) workoutsExtractedMap.get(workoutName);
                workoutData.put("Series", series);
            } else {
                HashMap<String, HashMap> workoutData = new HashMap<>();
                workoutData.put("Series", series);
                workoutsExtractedMap.put(workoutName, workoutData);
            }
            saveToInternalStorage(workoutsExtractedMap, WORKOUTS_FILE);

            Toast.makeText(getContext(), "Treinamento salvo", Toast.LENGTH_LONG).show();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new WorkoutFragment());
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error extracting workouts: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static String getLetter(int number) {
        StringBuilder stringBuilder = new StringBuilder();
        while (number >= 0) {
            stringBuilder.insert(0, (char) ('A' + (number % 26)));
            number = (number / 26) - 1;
        }
        return stringBuilder.toString();
    }

}