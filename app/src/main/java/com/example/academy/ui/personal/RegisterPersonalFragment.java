package com.example.academy.ui.personal;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.academy.R;

import java.util.HashMap;

public class RegisterPersonalFragment extends Fragment {
    HashMap<String, Integer> measuresMap = new HashMap<>();
    HashMap<String, Integer> foldsMap = new HashMap<>();

    private LinearLayout measuresLayout;
    private LinearLayout foldsLayout;
    private EditText measureNameEditText;
    private EditText measureValueEditText;
    private EditText foldNameEditText;
    private EditText foldValueEditText;
    private Button insertMeasureButton;
    private Button insertFoldButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_personal, container, false);

        measuresLayout = view.findViewById(R.id.measuresLayout);
        foldsLayout = view.findViewById(R.id.foldsLayout);
        measureNameEditText = view.findViewById(R.id.measureNameEditText);
        measureValueEditText = view.findViewById(R.id.measureValueEditText);
        foldNameEditText = view.findViewById(R.id.foldNameEditText);
        foldValueEditText = view.findViewById(R.id.foldValueEditText);
        insertMeasureButton = view.findViewById(R.id.insertMeasureButton);
        insertFoldButton = view.findViewById(R.id.insertFoldButton);

        insertMeasureButton.setOnClickListener(event -> {
            insertValueSubList(measureNameEditText, measureValueEditText, measuresLayout, measuresMap);
        });

        insertFoldButton.setOnClickListener(event -> {
            insertValueSubList(foldNameEditText, foldValueEditText, foldsLayout, foldsMap);
        });

        return view;
    }

    private void insertValueSubList(EditText nameEditText, EditText valueEditText,
                                    LinearLayout layout, HashMap<String, Integer> subList) {
        String name = nameEditText.getText().toString();
        String value = valueEditText.getText().toString();

        if (name.isEmpty() || value.isEmpty()) {
            return;
        }
        nameEditText.setText("");
        valueEditText.setText("");

        subList.put(name, Integer.valueOf(value));

        TextView textView = new TextView(getContext());
        textView.setText(name + ": " + value);
        layout.addView(textView);
    }
}