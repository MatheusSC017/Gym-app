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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterWorkoutFragment extends Fragment {
    AlertDialog dialog;
    View dialogView;
    EditTextDate workoutDate;

    Button returnButton;
    Button saveButton;
    Button addSerieButton;
    Button addExerciseButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_workout, container, false);

        dialogView = inflater.inflate(R.layout.dialog_register, null);
        workoutDate = view.findViewById(R.id.editTextDate);

        returnButton = view.findViewById(R.id.returnButton);
        saveButton = view.findViewById(R.id.saveButton);
        addSerieButton = view.findViewById(R.id.addSerieButton);
        addExerciseButton = view.findViewById(R.id.addExerciseButton);

        returnButton.setOnClickListener(event -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new WorkoutFragment());
            }
        });

        addSerieButton.setOnClickListener(event -> showRegisterDialog());

        return view;
    }

    private void showRegisterDialog() {
        if (workoutDate.getText().length() != 7) {
            Toast.makeText(getContext(), "Insira a data da avaliação", Toast.LENGTH_LONG).show();
            return;
        }

        EditText registerEditText = dialogView.findViewById(R.id.registerEditText);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button submitButton = dialogView.findViewById(R.id.submitButton);

        dialog = new AlertDialog.Builder(getContext()).setView(dialogView).setCancelable(false).create();

        cancelButton.setOnClickListener(event -> dialog.dismiss());

        submitButton.setOnClickListener(event -> {
            Toast.makeText(getContext(), registerEditText.getText().toString(), Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });

        dialog.show();

    }

    private void addSerie() {
        // Do nothing
    }

}