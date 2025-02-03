package com.example.academy.ui.workout;

import com.example.academy.MainActivity;
import com.example.academy.R;
import com.example.academy.database.ExerciseHelper;
import com.example.academy.database.repositories.ExerciseRepository;
import com.example.academy.database.repositories.SerieRepository;
import com.example.academy.ui.base.JsonFragment;
import com.example.academy.utils.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;

import android.text.InputType;
import android.view.*;
import android.widget.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RegisterWorkoutFragment extends JsonFragment {
    private static List<String> TYPES_OF_MEASURES = List.of("Repetições", "Segundos", "Minutos", "Horas", "Ciclos");
    private static Integer SEQUENCE_AVAILABLE_SELECTION = 9;

    private SerieRepository serieRepository = null;
    private ExerciseRepository exerciseRepository = null;

    private Long workout_id;

    private LinkedHashMap<Long, String> seriesIds = new LinkedHashMap<>();
    private LinkedHashMap<Long, HashMap> exercises = new LinkedHashMap<>();

    private LinkedHashMap<String, HashMap> exercisesSerie; // Remove Variable
    private List<List<String>> sequenceGroups; // Remove Variable

    private LinearLayout exerciseLinearLayout;
    private Spinner seriesSpinner;
    private TextView workoutDate;
    private Button returnButton;
    private Button setLinksButton;
    private Button addSerieButton;
    private Button removeSerieButton;
    private Button addExerciseButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_workout, container, false);

        serieRepository = new SerieRepository(getContext());
        exerciseRepository = new ExerciseRepository(getContext());

        workoutDate = view.findViewById(R.id.dateTextView);
        exerciseLinearLayout = view.findViewById(R.id.exerciseLinearLayout);
        seriesSpinner = view.findViewById(R.id.seriesSpinner);

        returnButton = view.findViewById(R.id.returnButton);
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
        setLinksButton.setOnClickListener(event -> showSetLinksDialog());

        Bundle bundle = getArguments();
        if (bundle == null || !bundle.containsKey("workout_id") || !bundle.containsKey("workout_date")) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new WorkoutFragment());
            }
        }

        workout_id = bundle.getLong("workout_id");
        workoutDate.setText(bundle.getString("workout_date"));

        loadSeries();
        setSeriesSpinner();

        return view;
    }

    private void loadSeries() {
        Cursor cursor = serieRepository.getSeries(workout_id);
        if (cursor.getCount() == 0) return;

        while (cursor.moveToNext()) {
            seriesIds.put(cursor.getLong(0), cursor.getString(1));
        }
    }

    // Deprecated
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

    private void showSerieRegisterDialog() {
        if (workoutDate.getText().length() != 7) {
            Toast.makeText(getContext(), "Insira a data da avaliação", Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Adicionar Serie");

        final EditText serieEditText = new EditText(getContext());
        serieEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(serieEditText);

        builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                addNewSerie(serieEditText.getText().toString());
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

    private void addNewSerie(String serieName) {
        Long result = serieRepository.addSerie(serieName, workout_id);

        if (result != -1) {
            seriesIds.put(result, serieName);
            setSeriesSpinner();
        }
    }

    private void removeSerie() {
        int position = seriesSpinner.getSelectedItemPosition();

        if (position != -1) {
            long serieId = seriesIds.keySet().stream().collect(Collectors.toList()).get(position);
            boolean result = serieRepository.removeSerie(serieId);
            if (result) {
                seriesIds.remove(serieId);
                setSeriesSpinner();
            } else {
                Toast.makeText(getContext(), "Erro removendo Serie", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Review
    private void setSeriesSpinner() {
        List<String> seriesNamesFormatted = seriesIds.values().stream().collect(Collectors.toList());
        for (int i = 0; i < seriesIds.size(); i++) {
            seriesNamesFormatted.set(i, Utils.getLetter(i) + "- " + seriesNamesFormatted.get(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                seriesNamesFormatted);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seriesSpinner.setAdapter(adapter);

        seriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Long serieId = seriesIds.keySet().stream().collect(Collectors.toList()).get(position);
                Cursor cursor = exerciseRepository.getExercises(serieId);

                exerciseLinearLayout.removeAllViews();
                while (cursor.moveToNext()) {
                    HashMap<String, Object> exercise = new HashMap<>();
                    exercise.put(ExerciseHelper.COLUMN_NAME, cursor.getString(1));
                    exercise.put(ExerciseHelper.COLUMN_SERIES_NUMBER, cursor.getInt(2));
                    exercise.put(ExerciseHelper.COLUMN_MEASURE, cursor.getString(3));
                    exercise.put(ExerciseHelper.COLUMN_QUANTITY, cursor.getInt(4));
                    exercise.put(ExerciseHelper.COLUMN_MUSCLE, cursor.getString(5));
                    exercise.put(ExerciseHelper.COLUMN_SEQUENCE, cursor.getString(6));
                    exercise.put(ExerciseHelper.COLUMN_OBSERVATION, cursor.getString(7));

                    exercises.put(cursor.getLong(0), exercise);
                    setupExerciseCard(exerciseLinearLayout, exercise);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });

    }

    // Review
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

    // Review
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

        exerciseMap.put("Exercise", exerciseEditText.getText().toString());
        exerciseMap.put("Series", seriesEditText.getText().toString());
        exerciseMap.put("Type", typeSpinner.getSelectedItem().toString());
        exerciseMap.put("Quantity", quantityEditText.getText().toString());
        exerciseMap.put("Muscle", muscleEditText.getText().toString());
        exerciseMap.put("Observation", observationEditText.getText().toString());

        exercisesSerie.put(exerciseEditText.getText().toString(), exerciseMap);

        setupExerciseCard(exerciseLinearLayout, exerciseMap);
    }

    // Review
    private void setupExerciseCard(LinearLayout layout, HashMap<String, Object>  exercise) {
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

        exerciseTextView.setText(exercise.get(ExerciseHelper.COLUMN_NAME).toString());
        muscleTextView.setText(exercise.get(ExerciseHelper.COLUMN_MUSCLE).toString());
        Object series = exercise.get(ExerciseHelper.COLUMN_SERIES_NUMBER);
        if (series != null) seriesTextView.setText(series + " x");
        repetitionsTextView.setText(exercise.get(ExerciseHelper.COLUMN_QUANTITY).toString() + " " + exercise.get(ExerciseHelper.COLUMN_MEASURE).toString());

        layout.addView(exerciseCard);
    }

    // Review
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
                    for (List<String> sequenceGroup: sequenceGroups) {
                        if (sequenceGroup.contains(exercise)) {
                            sequenceGroup.remove(sequenceGroup.indexOf(exercise));
                            sequenceGroup.add(exerciseEditText.getText().toString());
                        }
                    }
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

    // Review
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

    // Review
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

    // Review
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

}