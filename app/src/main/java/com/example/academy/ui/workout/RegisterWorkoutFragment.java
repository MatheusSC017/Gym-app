package com.example.academy.ui.workout;

import com.example.academy.MainActivity;
import com.example.academy.R;
import com.example.academy.database.repositories.ExerciseRepository;
import com.example.academy.database.repositories.SerieRepository;
import com.example.academy.models.ExerciseModel;
import com.example.academy.models.SerieModel;
import com.example.academy.ui.base.JsonFragment;
import com.example.academy.utils.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
    private LinkedHashMap<Long, ExerciseModel> exercises = new LinkedHashMap<>();

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
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new WorkoutFragment());
            }
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
        List<SerieModel> seriesList = serieRepository.getAll(workout_id);

        for (SerieModel serie: seriesList) {
            seriesIds.put(serie.getId(), serie.getName());
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
        Long result = serieRepository.add(serieName, workout_id);

        if (result != -1) {
            seriesIds.put(result, serieName);
            setSeriesSpinner();
        }
    }

    private void removeSerie() {
        int position = seriesSpinner.getSelectedItemPosition();

        if (position != -1) {
            long serieId = seriesIds.keySet().stream().collect(Collectors.toList()).get(position);
            boolean result = serieRepository.remove(serieId);
            if (result) {
                seriesIds.remove(serieId);
                setSeriesSpinner();
            } else {
                Toast.makeText(getContext(), "Erro removendo Serie", Toast.LENGTH_LONG).show();
            }
        }
    }

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
                List<ExerciseModel> exercisesList = exerciseRepository.getAll(serieId);

                exerciseLinearLayout.removeAllViews();
                for (ExerciseModel exercise:exercisesList) {
                    exercises.put(exercise.getId(), exercise);
                    setupExerciseCard(exerciseLinearLayout, exercise);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });

    }

    private void showExerciseRegisterDialog() {
        int serieIndex = seriesSpinner.getSelectedItemPosition();

        if (serieIndex != -1) {
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
                addExercise(dialogView, seriesIds.keySet().stream().collect(Collectors.toList()).get(serieIndex));

                registerDialog.dismiss();
            });

            registerDialog.show();
        }
    }

    private void addExercise(View dialogView, Long serieId) {
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

        ExerciseModel exercise = new ExerciseModel(null, exerciseEditText.getText().toString(), Integer.valueOf(seriesEditText.getText().toString()),
                typeSpinner.getSelectedItem().toString(), Integer.valueOf(quantityEditText.getText().toString()),
                muscleEditText.getText().toString(), 0, observationEditText.getText().toString(), serieId);

        exercise = exerciseRepository.add(exercise);

        if (exercise == null) {
            Toast.makeText(getContext(), "Erro ao adicionar exercício", Toast.LENGTH_LONG).show();
            return;
        }

        exercises.put(exercise.getId(), exercise);
        setupExerciseCard(exerciseLinearLayout, exercise);
    }

    private void setupExerciseCard(LinearLayout layout, ExerciseModel  exercise) {
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

        editExerciseButton.setOnClickListener(event -> showEditExerciseDialog(exercise.getId()));
        removeExerciseButton.setOnClickListener(event -> removeExercise(exercise.getId()));

        exerciseTextView.setText(exercise.getName());
        muscleTextView.setText(exercise.getMuscle());
        seriesTextView.setText(exercise.getSeriesNumber() + " x");
        repetitionsTextView.setText(exercise.getQuantity() + " " + exercise.getMeasure());

        layout.addView(exerciseCard);
    }

    // Review
    private void showEditExerciseDialog(Long exercise) {
        ExerciseModel exerciseModel = exercises.get(exercise);


        if (exerciseModel != null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View dialogView = layoutInflater.inflate(R.layout.dialog_exercise_register, null);

            EditText exerciseEditText = dialogView.findViewById(R.id.exerciseEditText);
            exerciseEditText.setText(exerciseModel.getName());
            EditText seriesEditText = dialogView.findViewById(R.id.seriesEditText);
            seriesEditText.setText(String.valueOf(exerciseModel.getSeriesNumber()));
            Spinner typeSpinner = dialogView.findViewById(R.id.typeSpinner);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, TYPES_OF_MEASURES);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            typeSpinner.setAdapter(adapter);
            typeSpinner.setSelection(TYPES_OF_MEASURES.indexOf(exerciseModel.getMeasure()));
            EditText quantityEditText = dialogView.findViewById(R.id.quantityEditText);
            quantityEditText.setText(String.valueOf(exerciseModel.getQuantity()));
            EditText muscleEditText = dialogView.findViewById(R.id.muscleEditText);
            muscleEditText.setText(exerciseModel.getMuscle());
            EditText observationEditText = dialogView.findViewById(R.id.observationEditText);
            observationEditText.setText(exerciseModel.getObservation());

            Button cancelButton = dialogView.findViewById(R.id.cancelButton);
            Button saveButton = dialogView.findViewById(R.id.submitButton);

            AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(dialogView).setCancelable(false).create();

            cancelButton.setOnClickListener(event -> dialog.dismiss());
            saveButton.setOnClickListener(event -> {
                String oldExerciseName = exerciseModel.getName();

                exerciseModel.setName(exerciseEditText.getText().toString());
                exerciseModel.setSeriesNumber(Integer.valueOf(seriesEditText.getText().toString()));
                exerciseModel.setMeasure(typeSpinner.getSelectedItem().toString());
                exerciseModel.setQuantity(Integer.valueOf(quantityEditText.getText().toString()));
                exerciseModel.setMuscle(muscleEditText.getText().toString());
                exerciseModel.setObservation(observationEditText.getText().toString());

                boolean result = exerciseRepository.update(exerciseModel.getId(), exerciseModel);

                if (result) {
                    for (int i = 0; i <= exerciseLinearLayout.getChildCount(); i++) {
                        View exerciseCard = exerciseLinearLayout.getChildAt(i);
                        TextView exerciseTextView = exerciseCard.findViewById(R.id.exerciseTextView);
                        if (exerciseTextView != null && exerciseTextView.getText().toString().equals(oldExerciseName)) {
                            TextView seriesTextView = exerciseCard.findViewById(R.id.seriesTextView);
                            TextView repetitionsTextView = exerciseCard.findViewById(R.id.repetitionsTextView);
                            TextView muscleTextView = exerciseCard.findViewById(R.id.muscleTextView);

                            exerciseTextView.setText(exerciseModel.getName());
                            muscleTextView.setText(exerciseModel.getMuscle());
                            seriesTextView.setText(exerciseModel.getSeriesNumber() + " x");
                            repetitionsTextView.setText(exerciseModel.getQuantity() + " " + exerciseModel.getMeasure());

                            break;
                        }
                    }
                }

                dialog.dismiss();
            });

            dialog.show();
        }
    }

    private void removeExercise(Long exercise) {
        boolean result = exerciseRepository.delete(exercise);
        if (!result) {
            Toast.makeText(getContext(), "Erro excluindo exercício", Toast.LENGTH_LONG).show();
            return;
        }

        ExerciseModel exerciseModel = exercises.get(exercise);
        if (exerciseModel != null) {
            exercises.remove(exercise);

            for (int i = 0; i <= exerciseLinearLayout.getChildCount(); i++) {
                View exerciseCard = exerciseLinearLayout.getChildAt(i);
                TextView exerciseTextView = exerciseCard.findViewById(R.id.exerciseTextView);
                if (exerciseTextView.getText().toString().equals(exerciseModel.getName())) {
                    exerciseLinearLayout.removeView(exerciseCard);
                    break;
                }
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