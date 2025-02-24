package com.example.academy.ui.personal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import android.text.InputType;
import android.view.*;
import android.widget.*;

import androidx.fragment.app.Fragment;

import com.example.academy.MainActivity;
import com.example.academy.R;
import com.example.academy.database.repositories.FoldRepository;
import com.example.academy.database.repositories.MeasureRepository;
import com.example.academy.database.repositories.PersonalRepository;
import com.example.academy.models.FoldModel;
import com.example.academy.models.MeasureModel;
import com.example.academy.models.PersonalModel;
import com.example.academy.view.EditTextDate;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class PersonalFragment extends Fragment {
    private PersonalRepository personalRepository = null;
    private MeasureRepository measureRepository = null;
    private FoldRepository foldRepository = null;

    private Map<String, PersonalModel> personalModelMap = new HashMap<>();

    private Spinner personalDateSpinner;
    private TextView imcTextView;
    private TextView heightTextView;
    private TextView weightTextView;
    private TextView fatPercentageTextView;
    private TextView leanBodyMassTextView;
    private TextView fatWeightTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal, container, false);

        personalRepository = new PersonalRepository(getContext());
        measureRepository = new MeasureRepository(getContext());
        foldRepository = new FoldRepository(getContext());

        personalDateSpinner = view.findViewById(R.id.personalDateSpinner);
        imcTextView = view.findViewById(R.id.imcTextView);
        heightTextView = view.findViewById(R.id.heightTextView);
        weightTextView = view.findViewById(R.id.weightTextView);
        fatPercentageTextView = view.findViewById(R.id.fatPercentageTextView);
        leanBodyMassTextView = view.findViewById(R.id.leanBodyMassTextView);
        fatWeightTextView = view.findViewById(R.id.fatWeightTextView);

        Button insertButton = view.findViewById(R.id.insertButton);
        insertButton.setOnClickListener(event -> showInsertPersonalDialog());

        Button editButton = view.findViewById(R.id.editButton);
        editButton.setOnClickListener(event -> navigateEditPersonalData());

        Button deleteButton = view.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(event -> deletePersonalRegister());

        setupPersonalSpinner();

        return view;

    }

    public void navigateEditPersonalData() {
        if (personalDateSpinner.getSelectedItem() != null) {
            String personalDate = personalDateSpinner.getSelectedItem().toString();
            PersonalModel personalObjectSelected = personalModelMap.get(personalDate);

            Bundle bundle = new Bundle();
            bundle.putLong("personal_id", personalObjectSelected.getId());

            RegisterPersonalFragment fragment = new RegisterPersonalFragment();
            fragment.setArguments(bundle);

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(fragment);
            }
        }
    }

    private void setupPersonalSpinner() {
        List<PersonalModel> personalList = personalRepository.getAll();
        personalModelMap.clear();
        personalList.forEach(personalMap -> personalModelMap.put(personalMap.getDate(), personalMap));

        Comparator<String> comparatorWorkoutDate = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String[] parts1 = o1.split("/");
                String[] parts2 = o2.split("/");

                int year1 = Integer.parseInt(parts1[1]);
                int month1 = Integer.parseInt(parts1[0]);
                int year2 = Integer.parseInt(parts2[1]);
                int month2 = Integer.parseInt(parts2[0]);

                if (year1 != year2) {
                    return Integer.compare(year2, year1);
                } else {
                    return Integer.compare(month2, month1);
                }
            }
        };

        List<String> personalDates = personalModelMap.keySet().stream().collect(Collectors.toList());
        personalDates.sort(comparatorWorkoutDate);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, personalDates);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        personalDateSpinner.setAdapter(adapter);

        personalDateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                setupPersonalData(personalModelMap.get(personalDateSpinner.getSelectedItem().toString()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });

        if (personalDates.size() == 0) {
            imcTextView.setText("IMC: - " );
            heightTextView.setText("Altura: - ");
            weightTextView.setText("Peso: - ");
            fatPercentageTextView.setText("Percentual de Gordura: - ");
            leanBodyMassTextView.setText("Massa magra: - ");
            fatWeightTextView.setText("Peso gordo: - ");
        }
    }

    private void showInsertPersonalDialog() {
        DecimalFormat twoDecimalFormatter = new DecimalFormat("00");
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Adicionar avaliação física");

        LocalDate today = LocalDate.now();

        EditTextDate personalEditTextDate = new EditTextDate(getContext());
        personalEditTextDate.setText(twoDecimalFormatter.format(today.getMonthValue()) + "/" + today.getYear());
        personalEditTextDate.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(personalEditTextDate);

        builder.setPositiveButton("Cadastrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                insertPersonalRegister(personalEditTextDate.getText().toString());
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

    private void insertPersonalRegister(String personalDate) {
        if (personalDate.length() != 7) {
            Toast.makeText(getContext(), "Insira uma data válida", Toast.LENGTH_LONG).show();
            return;
        }
        long result = personalRepository.add(personalDate);
        if (result == -1) {
            Toast.makeText(getContext(), "Erro: Verifique se uma avaliação nesta data já existe", Toast.LENGTH_LONG).show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putLong("personal_id", result);

        RegisterPersonalFragment fragment = new RegisterPersonalFragment();
        fragment.setArguments(bundle);

        if (getActivity() instanceof  MainActivity) {
            ((MainActivity) getActivity()).loadFragment(fragment);
        }
    }

    private void deletePersonalRegister() {
        if (personalDateSpinner.getSelectedItem() != null) {
            String personalDate = personalDateSpinner.getSelectedItem().toString();
            PersonalModel personal = personalModelMap.get(personalDate);
            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle("Deseja confirmar a exclusão deste treinamento?")
                    .setPositiveButton("Confirmar", ((dialogInterface, i) -> {
                        boolean result = personalRepository.delete(personal.getId());
                        if (result) {
                            Toast.makeText(getContext(), "Dados pessoais deletados", Toast.LENGTH_LONG).show();
                            setupPersonalSpinner();
                        } else {
                            Toast.makeText(getContext(), "Erro ao deletar dados pessoais", Toast.LENGTH_LONG).show();
                        }
                    })).setNegativeButton("Cancelar", ((dialogInterface, i) -> {
                        // Do nothing
                    })).create();
            dialog.show();
        }
    }

    private void setupPersonalData(PersonalModel personalModel) {
        imcTextView.setText("IMC: " + personalModel.getImc());
        heightTextView.setText("Altura: " + personalModel.getHeight() + "m");
        weightTextView.setText("Peso: " + personalModel.getWeight() + "Kg");
        fatPercentageTextView.setText("Percentual de Gordura: " + personalModel.getFatPercentage() + "%");
        leanBodyMassTextView.setText("Massa magra: " + personalModel.getLeanMass() + "Kg");
        fatWeightTextView.setText("Peso gordo: " + personalModel.getFatWeight() + "Kg");

        LinearLayout measurementLayout = getView().findViewById(R.id.measurementLayout);
        List<MeasureModel> measureModelList = measureRepository.getAll(personalModel.getId());
        measurementLayout.removeAllViews();
        measureModelList.forEach((MeasureModel measureModel) -> {
            TextView personalValueTextView = new TextView(getContext());
            personalValueTextView.setText(measureModel.getName() + ": " + measureModel.getValue());
            measurementLayout.addView(personalValueTextView);
        });
        LinearLayout foldsMeasurementsLayout = getView().findViewById(R.id.foldsMeasurementsLayout);
        List<FoldModel> foldModelList = foldRepository.getAll(personalModel.getId());
        foldsMeasurementsLayout.removeAllViews();
        foldModelList.forEach((FoldModel foldModel) -> {
            TextView personalValueTextView = new TextView(getContext());
            personalValueTextView.setText(foldModel.getName() + ": " + foldModel.getValue());
            foldsMeasurementsLayout.addView(personalValueTextView);
        });

    }

}