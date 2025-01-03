package com.example.academy.ui.workout;

import com.example.academy.MainActivity;
import com.example.academy.R;
import com.example.academy.view.EditTextDate;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class RegisterWorkoutFragment extends Fragment {
    ArrayList<String> seriesNames = new ArrayList<>();
    HashMap<String, HashMap> seriesMap = new HashMap<>();

    Spinner seriesSpinner;
    EditTextDate workoutDate;
    Button returnButton;
    Button saveButton;
    Button addSerieButton;
    Button removeSerieButton;
    Button addExerciseButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_workout, container, false);

        workoutDate = view.findViewById(R.id.editTextDate);

        seriesSpinner = view.findViewById(R.id.seriesSpinner);

        returnButton = view.findViewById(R.id.returnButton);
        saveButton = view.findViewById(R.id.saveButton);
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
            String serieName = registerEditText.getText().toString();
            seriesMap.put(serieName, new HashMap());
            seriesNames.add(serieName);

            setSeriesSpinner();

            dialog.dismiss();
        });

        dialog.show();
    }

    private void removeSerie() {
        Object serieSelected = seriesSpinner.getSelectedItem();

        if (serieSelected != null) {
            String serie = serieSelected.toString().substring(3);
            seriesMap.remove(serie);
            seriesNames.remove(seriesNames.indexOf(serie));

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
    }

    private static String getLetter(int number) {
        StringBuilder stringBuilder = new StringBuilder();
        while (number >= 0) {
            stringBuilder.insert(0, (char) ('A' + (number % 26)));
            number = (number / 26) - 1;
        }
        return stringBuilder.toString();
    }

    private void showExerciseRegisterDialog() {
        Object serieSelected = seriesSpinner.getSelectedItem();

        if (serieSelected != null) {
            // Do nothing
        }
    }

    private void saveWorkout() {
        if (workoutDate.getText().length() != 7) {
            Toast.makeText(getContext(), "Insira a data da avaliação", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(getContext(), "Treinamento salvo", Toast.LENGTH_LONG).show();
    }

}