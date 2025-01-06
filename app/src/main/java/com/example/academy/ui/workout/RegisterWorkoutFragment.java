package com.example.academy.ui.workout;

import com.example.academy.MainActivity;
import com.example.academy.R;
import com.example.academy.ui.base.JsonFragment;
import com.example.academy.view.EditTextDate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class RegisterWorkoutFragment extends JsonFragment {
    Integer sequenceAvailableSelection = 9;

    ArrayList<String> seriesNames = new ArrayList<>();
    ArrayList<ArrayList<Object>> seriesList = new ArrayList<>();
    HashMap<String, HashMap> exercisesSerie;
    List<ArrayList<String>> sequenceGroups;

    LinearLayout exerciseLinearLayout;

    Spinner seriesSpinner;
    EditTextDate workoutDate;
    Button returnButton;
    Button saveButton;
    Button setLinksButton;
    Button addSerieButton;
    Button removeSerieButton;
    Button addExerciseButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_workout, container, false);

        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);

        workoutDate = view.findViewById(R.id.editTextDate);
        String month;
        if (calendar.get(Calendar.MONTH) <= 9)
            month = "0" + (calendar.get(Calendar.MONTH) + 1);
        else
            month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        workoutDate.setText(month + "/" + calendar.get(Calendar.YEAR));

        exerciseLinearLayout = view.findViewById(R.id.exerciseLinearLayout);
        seriesSpinner = view.findViewById(R.id.seriesSpinner);

        returnButton = view.findViewById(R.id.returnButton);
        saveButton = view.findViewById(R.id.saveButton);
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
        saveButton.setOnClickListener(event -> saveWorkout());
        setLinksButton.setOnClickListener(event -> showSetLinksDialog());

        return view;
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
            int selectedSerieIndex = seriesSpinner.getSelectedItemPosition();
            String serieName = registerEditText.getText().toString();
            ArrayList<ArrayList<String>> sequenceArray = new ArrayList<>();
            for (int i = 0; i < sequenceAvailableSelection; i++) sequenceArray.add(new ArrayList<>());

            ArrayList<Object> serie = new ArrayList<>();
            serie.add(serieName);
            serie.add(new HashMap<>());
            serie.add(sequenceArray);
            seriesList.add(serie);
            seriesNames.add(serieName);

            setSeriesSpinner();
            seriesSpinner.setSelection(selectedSerieIndex);

            dialog.dismiss();
        });

        dialog.show();
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
        ArrayList<String> seriesNamesAdapter = new ArrayList<>();
        for (int i = 0; i < seriesNames.size(); i++) {
            seriesNamesAdapter.add(getLetter(i) + "- " + seriesNames.get(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                seriesNamesAdapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seriesSpinner.setAdapter(adapter);

        seriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                exercisesSerie = (HashMap<String, HashMap>) seriesList.get(position).get(1);
                sequenceGroups = (List<ArrayList<String>>) seriesList.get(position).get(2);

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

            AlertDialog registerDialog = new AlertDialog.Builder(getContext()).setView(dialogView).setCancelable(false).create();

            cancelButton.setOnClickListener(event -> registerDialog.dismiss());

            saveButton.setOnClickListener(event -> {
                EditText exerciseEditText = dialogView.findViewById(R.id.exerciseEditText);
                EditText seriesEditText = dialogView.findViewById(R.id.seriesEditText);
                EditText typeEditText = dialogView.findViewById(R.id.typeEditText);
                EditText quantityEditText = dialogView.findViewById(R.id.quantityEditText);
                EditText muscleEditText = dialogView.findViewById(R.id.muscleEditText);
                EditText observationEditText = dialogView.findViewById(R.id.observationEditText);

                if (exerciseEditText.getText().length() == 0 ||
                        seriesEditText.getText().length() == 0 ||
                        typeEditText.getText().length() == 0 ||
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
                exerciseMap.put("Type", typeEditText.getText().toString());
                exerciseMap.put("Quantity", quantityEditText.getText().toString());
                exerciseMap.put("Muscle", muscleEditText.getText().toString());
                exerciseMap.put("Observation", observationEditText.getText().toString());

                exercisesSerie.put(exerciseEditText.getText().toString(), exerciseMap);

                setupExerciseCard(exerciseLinearLayout, exerciseEditText.getText().toString(), exerciseMap);

                registerDialog.dismiss();
            });

            registerDialog.show();
        }
    }

    private void setupExerciseCard(LinearLayout layout, String exercise, HashMap<String, Object>  exerciseData) {
        View exerciseCard = LayoutInflater.from(getContext()).inflate(R.layout.register_exercise_layout, layout, false);

        TextView exerciseTextView = exerciseCard.findViewById(R.id.exerciseTextView);
        TextView seriesTextView = exerciseCard.findViewById(R.id.seriesTextView);
        TextView repetitionsTextView = exerciseCard.findViewById(R.id.repetitionsTextView);
        Button editExerciseButton = exerciseCard.findViewById(R.id.editExerciseButton);
        Button removeExerciseButton = exerciseCard.findViewById(R.id.removeExerciseButton);

        editExerciseButton.setOnClickListener(event -> editExercise(exerciseTextView.getText().toString()));
        removeExerciseButton.setOnClickListener(event -> removeExercise(exerciseTextView.getText().toString()));

        exerciseTextView.setText(exercise);
        String series = exerciseData.getOrDefault("Series", "1").toString();
        if (!series.equals("1")) seriesTextView.setText(series + " x");
        repetitionsTextView.setText(exerciseData.getOrDefault("Quantity", "").toString() + " " + exerciseData.getOrDefault("Type", "").toString());

        layout.addView(exerciseCard);
    }

    private void editExercise(String exercise) {
        HashMap<String, String> exerciseData = exercisesSerie.get(exercise);

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View dialogView = layoutInflater.inflate(R.layout.dialog_exercise_register, null);

        EditText exerciseEditText = dialogView.findViewById(R.id.exerciseEditText);
        exerciseEditText.setText(exercise);
        EditText seriesEditText = dialogView.findViewById(R.id.seriesEditText);
        seriesEditText.setText(exerciseData.getOrDefault("Series", ""));
        EditText typeEditText = dialogView.findViewById(R.id.typeEditText);
        typeEditText.setText(exerciseData.getOrDefault("Type", ""));
        EditText quantityEditText = dialogView.findViewById(R.id.quantityEditText);
        quantityEditText.setText(exerciseData.getOrDefault("Quantity", ""));
        EditText muscleEditText = dialogView.findViewById(R.id.muscleEditText);
        muscleEditText.setText(exerciseData.getOrDefault("Muscle", ""));
        EditText observationEditText = dialogView.findViewById(R.id.observationEditText);
        observationEditText.setText(exerciseData.getOrDefault("Observation", ""));

        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button saveButton = dialogView.findViewById(R.id.submitButton);

        AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(dialogView).setCancelable(false).create();

        cancelButton.setOnClickListener(event -> dialog.dismiss());
        saveButton.setOnClickListener(event -> {
            exerciseData.put("Series", seriesEditText.getText().toString());
            exerciseData.put("Type", typeEditText.getText().toString());
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
            for (int i = 0; i <= sequenceAvailableSelection; i++) {
                adapter.add(i);
                if (i > 0 && sequenceGroups.get(i - 1).contains(exercise)) selectedSequence = i;
            }
            sequenceSpinner.setAdapter(adapter);
            sequenceSpinner.setSelection(selectedSequence);

            container.addView(sequenceView);
        }

        builder.setView(dialogView)
                .setTitle("Definir exercícios em conjunto")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for (int j = 0; j < sequenceAvailableSelection; j++) sequenceGroups.get(j).clear();

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
                });

        builder.create().show();

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
                if (workout.keySet().contains("Series")) {
                    Toast.makeText(getContext(), "Já existe treinamento com está data", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            HashMap<String, HashMap> series = new HashMap<>();

            for (int i = 0; i < seriesList.size(); i++) {
                ArrayList<Object> serieData = seriesList.get(i);
                HashMap<String, Object> exercisesMap = (HashMap) serieData.get(1);
                ArrayList<ArrayList<String>> sequenceMap = (ArrayList<ArrayList<String>>) serieData.get(2);
                for (ArrayList<String> sequence: sequenceMap) {
                    if (sequence.stream().count() > 1) {
                        for (String exercise: sequence) ((HashMap) exercisesMap.get(exercise)).put("Sequence", sequence);
                    }
                }
                series.put((String) serieData.get(0), exercisesMap);

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