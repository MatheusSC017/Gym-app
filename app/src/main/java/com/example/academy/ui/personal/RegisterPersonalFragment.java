package com.example.academy.ui.personal;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.academy.MainActivity;
import com.example.academy.R;
import com.example.academy.ui.base.JsonFragment;
import com.example.academy.ui.workout.WorkoutFragment;
import com.example.academy.view.EditTextDate;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class RegisterPersonalFragment extends JsonFragment {
    private static String WORKOUTS_FILE = "workouts.json";
    private static Boolean editable = false;

    HashMap<String, Integer> measuresMap = new HashMap<>();
    HashMap<String, Integer> foldsMap = new HashMap<>();

    private EditTextDate personalEditTextDate;
    private EditText imcEditText;
    private EditText heightEditText;
    private EditText weightEditText;
    private EditText fatPercentageEditText;
    private EditText leanBodyMassEditText;
    private EditText fatWeightEditText;
    private LinearLayout measuresLayout;
    private LinearLayout foldsLayout;
    private EditText measureNameEditText;
    private EditText measureValueEditText;
    private EditText foldNameEditText;
    private EditText foldValueEditText;
    private Button insertMeasureButton;
    private Button insertFoldButton;
    private Button returnButton;
    private Button saveButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_personal, container, false);

        personalEditTextDate = view.findViewById(R.id.personalEditTextDate);
        imcEditText = view.findViewById(R.id.imcEditText);
        heightEditText = view.findViewById(R.id.heightEditText);
        weightEditText = view.findViewById(R.id.weightEditText);
        fatPercentageEditText = view.findViewById(R.id.fatPercentageEditText);
        leanBodyMassEditText = view.findViewById(R.id.leanBodyMassEditText);
        fatWeightEditText = view.findViewById(R.id.fatWeightEditText);
        measuresLayout = view.findViewById(R.id.measuresLayout);
        foldsLayout = view.findViewById(R.id.foldsLayout);
        measureNameEditText = view.findViewById(R.id.measureNameEditText);
        measureValueEditText = view.findViewById(R.id.measureValueEditText);
        foldNameEditText = view.findViewById(R.id.foldNameEditText);
        foldValueEditText = view.findViewById(R.id.foldValueEditText);
        insertMeasureButton = view.findViewById(R.id.insertMeasureButton);
        insertFoldButton = view.findViewById(R.id.insertFoldButton);
        returnButton = view.findViewById(R.id.returnButton);
        saveButton = view.findViewById(R.id.saveButton);

        returnButton.setOnClickListener(event -> {
            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle("Caso opte por retornar as informações serão perdidas")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Confirmar", (((dialogInterface, i) -> {
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).loadFragment(new PersonalFragment());
                        }
                    }))).create();
            dialog.show();
        });

        saveButton.setOnClickListener(event -> savePersonalData());

        insertMeasureButton.setOnClickListener(event -> {
            insertValueSubList(measureNameEditText, measureValueEditText, measuresLayout, measuresMap);
        });

        insertFoldButton.setOnClickListener(event -> {
            insertValueSubList(foldNameEditText, foldValueEditText, foldsLayout, foldsMap);
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            editable = true;

            String personalDate = bundle.getString("personalDate");
            loadPersonalData(personalDate);
        } else {
            setPersonalDate();
        }

        return view;
    }

    private void loadPersonalData(String personalDate) {
        HashMap<String, Object> storedData = loadJsonData(WORKOUTS_FILE);
        if (storedData.containsKey(personalDate)) {
            HashMap<String, Object> registerData = (HashMap<String, Object>) storedData.get(personalDate);
            HashMap<String, Object> personalData = (HashMap<String, Object>) registerData.getOrDefault("Personal", new HashMap<>());

            personalEditTextDate.setText(personalDate);
            imcEditText.setText(personalData.getOrDefault("IMC", "").toString());
            heightEditText.setText(personalData.getOrDefault("Height", "").toString());
            weightEditText.setText(personalData.getOrDefault("Weight", "").toString());
            fatPercentageEditText.setText(personalData.getOrDefault("Fat percentage", "").toString());
            leanBodyMassEditText.setText(personalData.getOrDefault("Lean mass", "").toString());
            fatWeightEditText.setText(personalData.getOrDefault("Fat weight", "").toString());

            measuresMap = (HashMap<String, Integer>) personalData.getOrDefault("Measures", new HashMap<>());
            foldsMap = (HashMap<String, Integer>) personalData.getOrDefault("Folds", new HashMap<>());

            measuresMap.forEach((name, value) -> setupSubItemLayout(name, value.toString(), measuresLayout, measuresMap));
            foldsMap.forEach((name, value) -> setupSubItemLayout(name, value.toString(), foldsLayout, foldsMap));

        }
    }

    private void setPersonalDate() {
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);

        String month;
        if (calendar.get(Calendar.MONTH) <= 9)
            month = "0" + (calendar.get(Calendar.MONTH) + 1);
        else
            month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        personalEditTextDate.setText(month + "/" + calendar.get(Calendar.YEAR));
    }

    private void savePersonalData(){
        String personalDate = personalEditTextDate.getText().toString();
        if (personalDate.length() != 7) {
            Toast.makeText(getContext(), "Insira a data da avaliação", Toast.LENGTH_LONG).show();
            return;
        }

        HashMap<String, Object> storedData = loadJsonData(WORKOUTS_FILE);

        if (!storedData.containsKey(personalDate)) storedData.put(personalDate, new HashMap<>());
        HashMap<String, HashMap> registerDate = (HashMap<String, HashMap>) storedData.get(personalDate);

        if (!editable && registerDate != null && registerDate.containsKey("Personal")) {
            Toast.makeText(getContext(), "Já existe registro com esta data", Toast.LENGTH_LONG).show();
            return;
        }

        if (!registerDate.containsKey("Personal")) registerDate.put("Personal", new HashMap<>());
        HashMap<String, Object> personalData = registerDate.get("Personal");

        personalData.put("IMC", imcEditText.getText().toString());
        personalData.put("Height", heightEditText.getText().toString());
        personalData.put("Weight", weightEditText.getText().toString());
        personalData.put("Fat percentage", fatPercentageEditText.getText().toString());
        personalData.put("Lean mass", leanBodyMassEditText.getText().toString());
        personalData.put("Fat weight", fatWeightEditText.getText().toString());

        personalData.put("Measures", measuresMap);
        personalData.put("Folds", foldsMap);

        saveToInternalStorage(storedData, WORKOUTS_FILE);
        Toast.makeText(getContext(), "Dados Pessoais salvos", Toast.LENGTH_LONG).show();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).loadFragment(new PersonalFragment());
        }
    }

    private void insertValueSubList(EditText nameEditText, EditText valueEditText,
                                    LinearLayout layout, HashMap<String, Integer> subList) {
        String name = nameEditText.getText().toString();
        String value = valueEditText.getText().toString();

        if (name.isEmpty() || value.isEmpty()) {
            return;
        }

        if (subList.containsKey(name)) {
            Toast.makeText(getContext(), "Já existe registro com esse nome", Toast.LENGTH_LONG).show();
            return;
        }

        nameEditText.setText("");
        valueEditText.setText("");

        subList.put(name, Integer.valueOf(value));
        setupSubItemLayout(name, value, layout, subList);
    }

    private void setupSubItemLayout(String name, String value, LinearLayout layout, HashMap<String, Integer> subList) {
        View personalCard = LayoutInflater.from(getContext()).inflate(R.layout.register_layout, layout, false);
        ViewStub contentViewStub = personalCard.findViewById(R.id.contentViewStub);
        contentViewStub.setLayoutResource(R.layout.personal_layout);
        View contentInflated = contentViewStub.inflate();

        TextView textView = contentInflated.findViewById(R.id.personalTextView);
        textView.setText(name + ": " + value);

        Button editExerciseButton = personalCard.findViewById(R.id.editExerciseButton);
        Button removeExerciseButton = personalCard.findViewById(R.id.removeExerciseButton);

        editExerciseButton.setOnClickListener(event -> {
            EditText valueEditText = new EditText(getContext());
            valueEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
            valueEditText.setText(String.valueOf(subList.get(name)));
            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle("Insira um novo valor para " + name)
                    .setView(valueEditText)
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Confirmar", (((dialogInterface, i) -> {
                        String newValue = valueEditText.getText().toString();
                        if (!newValue.isEmpty()) {
                            subList.put(name, Integer.valueOf(newValue));
                            textView.setText(name + ": " + newValue);
                        }
                    }))).create();
            dialog.show();
        });
        removeExerciseButton.setOnClickListener(event -> {
            layout.removeView(personalCard);
            subList.remove(name);
        });

        layout.addView(personalCard);
    }

}