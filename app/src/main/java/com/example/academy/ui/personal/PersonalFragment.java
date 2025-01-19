package com.example.academy.ui.personal;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.academy.MainActivity;
import com.example.academy.R;
import com.example.academy.ui.base.JsonFragment;
import com.example.academy.ui.workout.RegisterWorkoutFragment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class PersonalFragment extends JsonFragment {
    private static String WORKOUTS_FILE = "workouts.json";

    private Spinner personalDateSpinner;
    private TextView imcTextView;
    private TextView heightTextView;
    private TextView weightTextView;
    private TextView fatPercentageTextView;
    private TextView leanBodyMassTextView;
    private TextView fatWeightTextView;
    private LinearLayout measurementLayout;
    private LinearLayout foldsMeasurementsLayout;
    private Button insertButton;
    private Button editButton;
    private Button deleteButton;

    private List<String> personalIds = new ArrayList<>();
    private HashMap<String, Object> personalDataMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal, container, false);

        personalDateSpinner = view.findViewById(R.id.personalDateSpinner);
        imcTextView = view.findViewById(R.id.imcTextView);
        heightTextView = view.findViewById(R.id.heightTextView);
        weightTextView = view.findViewById(R.id.weightTextView);
        fatPercentageTextView = view.findViewById(R.id.fatPercentageTextView);
        leanBodyMassTextView = view.findViewById(R.id.leanBodyMassTextView);
        fatWeightTextView = view.findViewById(R.id.fatWeightTextView);
        measurementLayout = view.findViewById(R.id.measurementLayout);
        foldsMeasurementsLayout = view.findViewById(R.id.foldsMeasurementsLayout);
        insertButton = view.findViewById(R.id.insertButton);
        editButton = view.findViewById(R.id.editButton);
        deleteButton = view.findViewById(R.id.deleteButton);

        insertButton.setOnClickListener(event -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new RegisterPersonalFragment());
            }
        });

        editButton.setOnClickListener(event -> navigateEditPersonalData());

        deleteButton.setOnClickListener(event -> deletePersonalRegister());

        personalDataMap = loadJsonData(WORKOUTS_FILE);
        setupWorkoutSpinner();

        return view;

    }

    public void navigateEditPersonalData() {
        String personalDate = personalDateSpinner.getSelectedItem().toString();

        Bundle bundle = new Bundle();
        bundle.putString("personalDate", personalDate);

        RegisterPersonalFragment fragment = new RegisterPersonalFragment();
        fragment.setArguments(bundle);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).loadFragment(fragment);
        }
    }

    public HashMap<String, Object> loadJsonData(String filePath) {
        try {
            HashMap<String, Object> workoutsExtractedMap = super.loadJsonData(filePath);
            if (workoutsExtractedMap != null) {
                personalIds = workoutsExtractedMap.keySet().stream().collect(Collectors.toList());

                Comparator<String> comparatorWorkoutsIds = new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        String[] parts1 = o1.split("/");
                        String[] parts2 = o2.split("/");

                        int year1 = Integer.parseInt(parts1[1]);
                        int month1 = Integer.parseInt(parts1[0]);
                        int year2 = Integer.parseInt(parts2[1]);
                        int month2 = Integer.parseInt(parts2[0]);

                        if (year1 != year2) {
                            return Integer.compare(year2, year1); // Descending order by year
                        } else {
                            return Integer.compare(month2, month1); // Descending order by month
                        }
                    }
                };
                personalIds.sort(comparatorWorkoutsIds);
            }
            return workoutsExtractedMap;
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error extracting workouts: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private void setupWorkoutSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, personalIds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        personalDateSpinner.setAdapter(adapter);

        personalDateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                setupPersonalData(personalIds.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
    }

    private void deletePersonalRegister() {
        String personalDate = personalDateSpinner.getSelectedItem().toString();

        if (!personalDate.equals("")) {
            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle("Deseja confirmar a exclusão deste treinamento?")
                    .setPositiveButton("Confirmar", ((dialogInterface, i) -> {
                        personalIds.remove(personalIds.indexOf(personalDate));
                        setupWorkoutSpinner();
                        HashMap<String, Object> registerData = (HashMap<String, Object>) personalDataMap.get(personalDate);
                        registerData.remove("Personal");
                        if (registerData.size() == 0) personalDataMap.remove(personalDate);
                        saveToInternalStorage(personalDataMap, WORKOUTS_FILE);
                    })).setNegativeButton("Cancelar", ((dialogInterface, i) -> {
                        // Do nothing
                    })).create();
            dialog.show();
        }
    }

    private void setupPersonalData(String personalDate) {
        HashMap<String, Object> registerData = (HashMap<String, Object>) personalDataMap.get(personalDate);
        HashMap<String, Object> personalData = (HashMap<String, Object>) registerData.get("Personal");

        if (personalData != null) {
            imcTextView.setText("IMC: " + personalData.getOrDefault("IMC", "-").toString());
            heightTextView.setText("Altura: " + personalData.getOrDefault("Height", "-").toString() + "m");
            weightTextView.setText("Peso: " + personalData.getOrDefault("Weight", "-").toString() + "Kg");
            fatPercentageTextView.setText("Percentual de Gordura: " + personalData.getOrDefault("Fat percentage", "-").toString() + "%");
            leanBodyMassTextView.setText("Massa magra: " + personalData.getOrDefault("Lean mass", "-").toString() + "Kg");
            fatWeightTextView.setText("Peso gordo: " + personalData.getOrDefault("Fat weight", "-").toString() + "Kg");

            setupPersonalDataSubItems((HashMap<String, Object>) personalData.getOrDefault("Measures", new HashMap<>()), measurementLayout);
            setupPersonalDataSubItems((HashMap<String, Object>) personalData.getOrDefault("Folds", new HashMap<>()), foldsMeasurementsLayout);
        }

    }

    private void setupPersonalDataSubItems(HashMap<String, Object> personalData, LinearLayout layout) {
        Context context = getContext();

        layout.removeAllViews();
        personalData.forEach((String measure, Object value) -> {
            TextView personalValueTextView = new TextView(context);
            personalValueTextView.setText(measure + ": " + String.valueOf(value));
            layout.addView(personalValueTextView);
        });
    }

}