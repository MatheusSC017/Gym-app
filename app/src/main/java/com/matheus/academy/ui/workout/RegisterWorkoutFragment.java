package com.matheus.academy.ui.workout;

import com.matheus.academy.ui.MainActivity;
import com.matheus.academy.R;
import com.matheus.academy.database.repositories.ExerciseRepository;
import com.matheus.academy.database.repositories.SerieRepository;
import com.matheus.academy.models.ExerciseModel;
import com.matheus.academy.models.SerieModel;
import com.matheus.academy.utils.Utils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import android.text.InputType;
import android.view.*;
import android.widget.*;

import androidx.fragment.app.Fragment;

import java.util.*;
import java.util.stream.Collectors;

public class RegisterWorkoutFragment extends Fragment {
    private static List<String> TYPES_OF_MEASURES = List.of("Repetições", "Segundos", "Minutos", "Horas", "Ciclos");
    private static Integer SEQUENCE_AVAILABLE_SELECTION = 9;

    private SerieRepository serieRepository = null;
    private ExerciseRepository exerciseRepository = null;

    private Long workout_id;

    private LinkedHashMap<Long, String> seriesIds = new LinkedHashMap<>();
    private LinkedHashMap<Long, ExerciseModel> exercises = new LinkedHashMap<>();

    private LinearLayout exerciseLinearLayout;
    private Spinner seriesSpinner;
    private TextView workoutDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_workout, container, false);

        serieRepository = new SerieRepository(getContext());
        exerciseRepository = new ExerciseRepository(getContext());

        workoutDate = view.findViewById(R.id.dateTextView);
        exerciseLinearLayout = view.findViewById(R.id.exerciseLinearLayout);
        seriesSpinner = view.findViewById(R.id.seriesSpinner);

        Button returnButton = view.findViewById(R.id.returnButton);
        returnButton.setOnClickListener(event -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new WorkoutFragment());
            }
        });

        Button addSerieButton = view.findViewById(R.id.addSerieButton);
        addSerieButton.setOnClickListener(event -> showSerieRegisterDialog());

        Button removeSerieButton = view.findViewById(R.id.removeSerieButton);
        removeSerieButton.setOnClickListener(event -> removeSerie());

        Button addExerciseButton = view.findViewById(R.id.addExerciseButton);
        addExerciseButton.setOnClickListener(event -> showExerciseRegisterDialog());

        Button setLinksButton = view.findViewById(R.id.setLinksButton);
        setLinksButton.setOnClickListener(event -> showSetLinksDialog());

        Bundle bundle = getArguments();
        if (bundle == null || !bundle.containsKey("workout_id") || !bundle.containsKey("workout_date"))
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).loadFragment(new WorkoutFragment());

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
                setupExercisesCards(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });

    }

    private void setupExercisesCards(int position) {
        Long serieId = seriesIds.keySet().stream().collect(Collectors.toList()).get(position);
        List<ExerciseModel> exercisesList = exerciseRepository.getAll(serieId);

        exercises.clear();
        exerciseLinearLayout.removeAllViews();
        for (ExerciseModel exercise:exercisesList) {
            exercises.put(exercise.getId(), exercise);
            setupExerciseCard(exerciseLinearLayout, exercise);
        }
    }

    private void showExerciseRegisterDialog() {
        int serieIndex = seriesSpinner.getSelectedItemPosition();
        if (serieIndex == -1) return;

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View dialogView = layoutInflater.inflate(R.layout.dialog_exercise_register, null);

        Spinner typeSpinner = dialogView.findViewById(R.id.typeSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, TYPES_OF_MEASURES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);

        AlertDialog registerDialog = new AlertDialog.Builder(getContext()).setView(dialogView).setCancelable(false).create();

        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(event -> registerDialog.dismiss());

        Button saveButton = dialogView.findViewById(R.id.submitButton);
        saveButton.setOnClickListener(event -> {
            addExercise(dialogView, seriesIds.keySet().stream().collect(Collectors.toList()).get(serieIndex));
            registerDialog.dismiss();
        });

        registerDialog.show();
    }

    private void addExercise(View dialogView, Long serieId) {
        EditText exerciseEditText = dialogView.findViewById(R.id.exerciseEditText);
        String exerciseName = exerciseEditText.getText().toString();

        EditText seriesEditText = dialogView.findViewById(R.id.seriesEditText);
        int seriesNumber = seriesEditText.getText().toString().equals("") ? 0 : Integer.valueOf(seriesEditText.getText().toString());

        Spinner typeSpinner = dialogView.findViewById(R.id.typeSpinner);
        String measure = typeSpinner.getSelectedItem() == null ? null : typeSpinner.getSelectedItem().toString();

        EditText quantityEditText = dialogView.findViewById(R.id.quantityEditText);
        int quantity = quantityEditText.getText().toString().equals("") ? 0 : Integer.valueOf(quantityEditText.getText().toString());

        EditText muscleEditText = dialogView.findViewById(R.id.muscleEditText);
        String muscle = muscleEditText.getText().toString();

        EditText weightEditText = dialogView.findViewById(R.id.weightEditText);
        int weight = weightEditText.getText().toString().equals("") ? 0 : Integer.valueOf(weightEditText.getText().toString());

        EditText observationEditText = dialogView.findViewById(R.id.observationEditText);
        String observation = observationEditText.getText().toString();

        if (exerciseName.isEmpty() || seriesNumber <= 0 || typeSpinner.getSelectedItem() == null || quantity <= 0) {
            Toast.makeText(getContext(), "Campos obrigatórios (Exercício, Series, Tipo e Quantidade)", Toast.LENGTH_LONG).show();
            return;
        }

        ExerciseModel exercise = new ExerciseModel(null, exerciseName, seriesNumber, measure,
                quantity, muscle, 0, observation, weight, serieId);

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
        exerciseTextView.setText(exercise.getName());

        TextView muscleTextView = contentInflated.findViewById(R.id.muscleTextView);
        muscleTextView.setText(exercise.getMuscle());

        TextView seriesTextView = contentInflated.findViewById(R.id.seriesTextView);
        seriesTextView.setText(exercise.getSeriesNumber() > 1 ? exercise.getSeriesNumber() + " x" : "");

        TextView repetitionsTextView = contentInflated.findViewById(R.id.repetitionsTextView);
        repetitionsTextView.setText(exercise.getQuantity() + " " + exercise.getMeasure());

        TextView weightTextView = contentInflated.findViewById(R.id.weightTextView);
        weightTextView.setText(exercise.getWeight() != 0 ? exercise.getWeight() + " Kg" : "");

        Button editExerciseButton = exerciseCard.findViewById(R.id.editExerciseButton);
        editExerciseButton.setOnClickListener(event -> showEditExerciseDialog(exercise.getId()));

        Button removeExerciseButton = exerciseCard.findViewById(R.id.removeExerciseButton);
        removeExerciseButton.setOnClickListener(event -> removeExercise(exercise.getId()));

        layout.addView(exerciseCard);
    }

    private void showEditExerciseDialog(Long exercise) {
        ExerciseModel exerciseModel = exercises.get(exercise);
        if (exerciseModel == null) return;

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

        EditText weightEditText = dialogView.findViewById(R.id.weightEditText);
        weightEditText.setText(String.valueOf(exerciseModel.getWeight()));

        EditText observationEditText = dialogView.findViewById(R.id.observationEditText);
        observationEditText.setText(exerciseModel.getObservation());

        AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(dialogView).setCancelable(false).create();

        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(event -> dialog.dismiss());

        Button saveButton = dialogView.findViewById(R.id.submitButton);
        saveButton.setOnClickListener(event -> {
            String oldExerciseName = exerciseModel.getName();

            String exerciseName = exerciseEditText.getText().toString();
            int seriesNumber = seriesEditText.getText().toString().equals("") ? 0 : Integer.valueOf(seriesEditText.getText().toString());
            int quantity = quantityEditText.getText().toString().equals("") ? 0 : Integer.valueOf(quantityEditText.getText().toString());
            String measure = typeSpinner.getSelectedItem().toString();
            String muscle = muscleEditText.getText().toString();
            int weight = weightEditText.getText().toString().equals("") ? 0 : Integer.valueOf(weightEditText.getText().toString());
            String observation = observationEditText.getText().toString();

            exerciseModel.setName(exerciseName);
            exerciseModel.setSeriesNumber(seriesNumber);
            exerciseModel.setMeasure(measure);
            exerciseModel.setQuantity(quantity);
            exerciseModel.setMuscle(muscle);
            exerciseModel.setWeight(weight);
            exerciseModel.setObservation(observation);

            editExercise(oldExerciseName, exerciseModel);

            dialog.dismiss();
        });

        dialog.show();
    }

    private void editExercise(String oldExerciseName, ExerciseModel exerciseModel) {
        boolean result = exerciseRepository.update(exerciseModel.getId(), exerciseModel);
        if (!result) return;

        for (int i = 0; i <= exerciseLinearLayout.getChildCount(); i++) {
            View exerciseCard = exerciseLinearLayout.getChildAt(i);
            TextView exerciseTextView = exerciseCard.findViewById(R.id.exerciseTextView);
            if (exerciseTextView != null && exerciseTextView.getText().toString().equals(oldExerciseName)) {
                exerciseTextView.setText(exerciseModel.getName());

                TextView seriesTextView = exerciseCard.findViewById(R.id.seriesTextView);
                seriesTextView.setText(exerciseModel.getSeriesNumber() + " x");

                TextView repetitionsTextView = exerciseCard.findViewById(R.id.repetitionsTextView);
                repetitionsTextView.setText(exerciseModel.getQuantity() + " " + exerciseModel.getMeasure());

                TextView muscleTextView = exerciseCard.findViewById(R.id.muscleTextView);
                muscleTextView.setText(exerciseModel.getMuscle());

                TextView weightEditText = exerciseCard.findViewById(R.id.weightTextView);
                weightEditText.setText(exerciseModel.getWeight() != 0 ? exerciseModel.getWeight() + " Kg" : "");
                break;
            }
        }
    }

    private void removeExercise(Long exercise) {
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Deseja confirmar a exclusão deste exercício?")
                .setPositiveButton("Confirmar", ((dialogInterface, i) -> {
                    boolean result = exerciseRepository.delete(exercise);
                    if (!result) {
                        Toast.makeText(getContext(), "Erro excluindo exercício", Toast.LENGTH_LONG).show();
                        return;
                    }

                    ExerciseModel exerciseModel = exercises.get(exercise);
                    if (exerciseModel != null) {
                        exercises.remove(exercise);

                        for (int j = 0; j <= exerciseLinearLayout.getChildCount(); j++) {
                            View exerciseCard = exerciseLinearLayout.getChildAt(j);
                            TextView exerciseTextView = exerciseCard.findViewById(R.id.exerciseTextView);
                            if (exerciseTextView.getText().toString().equals(exerciseModel.getName())) {
                                exerciseLinearLayout.removeView(exerciseCard);
                                break;
                            }
                        }
                    }
                })).setNegativeButton("Cancelar", ((dialogInterface, i) -> {
                    // Do nothing
                })).create();
        dialog.show();
    }

    private void showSetLinksDialog() {
        if (exercises.isEmpty()) {
            Toast.makeText(getContext(), "Nenhum exercício cadastrado", Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_scroll_view, null);
        LinearLayout container = dialogView.findViewById(R.id.container);

        for (ExerciseModel exercise: exercises.values()) {
            View sequenceView = inflater.inflate(R.layout.sequence_selection_layout, container, false);

            TextView exerciseTextView = sequenceView.findViewById(R.id.exerciseTextView);
            exerciseTextView.setText(exercise.getName());

            ArrayAdapter<Integer> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            for (int i = 0; i <= SEQUENCE_AVAILABLE_SELECTION; i++) {
                adapter.add(i);
            }
            Spinner sequenceSpinner = sequenceView.findViewById(R.id.sequenceSpinner);
            sequenceSpinner.setAdapter(adapter);
            sequenceSpinner.setSelection(exercise.getSequence());

            container.addView(sequenceView);
        }

        builder.setView(dialogView)
                .setTitle("Definir exercícios em conjunto")
                .setPositiveButton("Salvar", (dialogInterface, i) -> setLinks(container));

        builder.create().show();

    }

    private void setLinks(LinearLayout container) {
        for (int j = 0; j < container.getChildCount(); j++) {
            LinearLayout sequenceView = (LinearLayout) container.getChildAt(j);
            Spinner sequenceSpinner = sequenceView.findViewById(R.id.sequenceSpinner);

            int sequenceValue = Integer.parseInt(sequenceSpinner.getSelectedItem().toString());
            ExerciseModel exerciseModel = exercises.values().stream().collect(Collectors.toList()).get(j);
            exerciseModel.setSequence(sequenceValue);
            exerciseRepository.update(exerciseModel.getId(), exerciseModel);

        }
    }

}