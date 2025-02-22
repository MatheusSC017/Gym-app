package com.example.academy.ui.personal;

import android.app.AlertDialog;
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
import com.example.academy.ui.workout.WorkoutFragment;

import java.util.*;

public class RegisterPersonalFragment extends Fragment {
    private Long personalId;

    private PersonalRepository personalRepository = null;
    private MeasureRepository measureRepository = null;
    private FoldRepository foldRepository = null;

    List<Long> deletedMeasuresList = new ArrayList<>();
    List<Long> deletedFoldsList = new ArrayList<>();

    HashMap<String, MeasureModel> measuresMap = new HashMap<>();
    HashMap<String, FoldModel> foldsMap = new HashMap<>();

    private TextView personalTextView;
    private EditText imcEditText;
    private EditText heightEditText;
    private EditText weightEditText;
    private EditText fatPercentageEditText;
    private EditText leanBodyMassEditText;
    private EditText fatWeightEditText;
    private LinearLayout measuresLayout;
    private LinearLayout foldsLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_personal, container, false);

        personalRepository = new PersonalRepository(getContext());
        measureRepository = new MeasureRepository(getContext());
        foldRepository = new FoldRepository(getContext());

        personalTextView = view.findViewById(R.id.personalTextView);
        imcEditText = view.findViewById(R.id.imcEditText);
        heightEditText = view.findViewById(R.id.heightEditText);
        weightEditText = view.findViewById(R.id.weightEditText);
        fatPercentageEditText = view.findViewById(R.id.fatPercentageEditText);
        leanBodyMassEditText = view.findViewById(R.id.leanBodyMassEditText);
        fatWeightEditText = view.findViewById(R.id.fatWeightEditText);
        measuresLayout = view.findViewById(R.id.measuresLayout);
        foldsLayout = view.findViewById(R.id.foldsLayout);

        Button returnButton = view.findViewById(R.id.returnButton);
        returnButton.setOnClickListener(event -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).loadFragment(new PersonalFragment());
        });

        Button saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(event -> savePersonalData());

        Button insertMeasureButton = view.findViewById(R.id.insertMeasureButton);
        insertMeasureButton.setOnClickListener(event -> insertMeasure());

        Button insertFoldButton = view.findViewById(R.id.insertFoldButton);
        insertFoldButton.setOnClickListener(event -> insertFold());

        Bundle bundle = getArguments();
        if (bundle == null || !bundle.containsKey("personal_id"))
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).loadFragment(new WorkoutFragment());

        personalId = bundle.getLong("personal_id");

        loadPersonalData();

        return view;
    }

    private void loadPersonalData() {
        PersonalModel personalModel = personalRepository.get(personalId);

        personalTextView.setText(personalModel.getDate());
        imcEditText.setText(personalModel.getImc().toString());
        heightEditText.setText(personalModel.getHeight().toString());
        weightEditText.setText(personalModel.getWeight().toString());
        fatPercentageEditText.setText(personalModel.getFatPercentage().toString());
        leanBodyMassEditText.setText(personalModel.getLeanMass().toString());
        fatWeightEditText.setText(personalModel.getFatWeight().toString());

        List<MeasureModel> measuresList = measureRepository.getAll(personalModel.getId());
        measuresLayout.removeAllViews();
        measuresList.forEach((MeasureModel measure) -> {
            measuresMap.put(measure.getName(), measure);
            setupMeasureLayout(measure.getName(), measure.getValue().toString());
        });
        List<FoldModel> foldsList = foldRepository.getAll(personalModel.getId());
        foldsLayout.removeAllViews();
        foldsList.forEach((FoldModel fold) -> {
            foldsMap.put(fold.getName(), fold);
            setupFoldLayout(fold.getName(), fold.getValue().toString());
        });
    }

    private void savePersonalData(){
        PersonalModel personal = new PersonalModel(personalId, personalTextView.getText().toString(),
                Float.valueOf(weightEditText.getText().toString()), Float.valueOf(heightEditText.getText().toString()),
                Float.valueOf(leanBodyMassEditText.getText().toString()), Float.valueOf(fatWeightEditText.getText().toString()),
                Float.valueOf(fatPercentageEditText.getText().toString()), Float.valueOf(imcEditText.getText().toString()));

        boolean result = personalRepository.update(personalId, personal);
        if (!result) {
            Toast.makeText(getContext(), "Erro ao salvar os dados pessoais", Toast.LENGTH_LONG).show();
            return;
        }

        measuresMap.forEach((String measureName, MeasureModel measure) -> {
            if (measure.getId().equals(0L)) {
                measureRepository.add(measure);
            } else {
                measureRepository.update(measure.getId(), measure);
            }
        });

        for (Long id: deletedMeasuresList) measureRepository.delete(id);

        foldsMap.forEach((String foldName, FoldModel fold) -> {
            if (fold.getId().equals(0L)) {
                foldRepository.add(fold);
            } else {
                foldRepository.update(fold.getId(), fold);
            }
        });

        for (Long id: deletedFoldsList) foldRepository.delete(id);

        Toast.makeText(getContext(), "Dados Pessoais salvos", Toast.LENGTH_LONG).show();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).loadFragment(new PersonalFragment());
        }
    }

    private void insertFold() {
        EditText foldNameEditText = getView().findViewById(R.id.foldNameEditText);
        String name = foldNameEditText.getText().toString();
        foldNameEditText.setText("");

        EditText foldValueEditText = getView().findViewById(R.id.foldValueEditText);
        String value = foldValueEditText.getText().toString();
        foldValueEditText.setText("");

        if (name.isEmpty() || value.isEmpty()) {
            return;
        }

        if (foldsMap.containsKey(name)) {
            Toast.makeText(getContext(), "Já existe registro com esse nome", Toast.LENGTH_LONG).show();
            return;
        }

        FoldModel fold = new FoldModel(0L, name, Integer.valueOf(value), personalId);

        foldsMap.put(name, fold);
        setupFoldLayout(name, value);
    }

    private void setupFoldLayout(String name, String value) {
        View personalCard = LayoutInflater.from(getContext()).inflate(R.layout.register_layout, foldsLayout, false);
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
            valueEditText.setText(value);
            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle("Insira um novo valor para " + name)
                    .setView(valueEditText)
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Confirmar", (((dialogInterface, i) -> {
                        String newValue = valueEditText.getText().toString();
                        if (!newValue.isEmpty()) {
                            FoldModel fold = foldsMap.get(name);
                            fold.setValue(Integer.valueOf(newValue));
                            textView.setText(name + ": " + newValue);
                        }
                    }))).create();
            dialog.show();
        });
        removeExerciseButton.setOnClickListener(event -> {
            Long id = foldsMap.get(name).getId();

            if (!id.equals(0L)) {
                deletedFoldsList.add(id);
            }

            foldsLayout.removeView(personalCard);
            foldsMap.remove(name);
        });

        foldsLayout.addView(personalCard);
    }

    private void insertMeasure() {
        EditText measureNameEditText = getView().findViewById(R.id.measureNameEditText);
        String name = measureNameEditText.getText().toString();
        measureNameEditText.setText("");

        EditText measureValueEditText = getView().findViewById(R.id.measureValueEditText);
        String value = measureValueEditText.getText().toString();
        measureValueEditText.setText("");

        if (name.isEmpty() || value.isEmpty()) {
            return;
        }

        if (measuresMap.containsKey(name)) {
            Toast.makeText(getContext(), "Já existe registro com esse nome", Toast.LENGTH_LONG).show();
            return;
        }

        MeasureModel measure = new MeasureModel(0L, name, Integer.valueOf(value), personalId);

        measuresMap.put(name, measure);
        setupMeasureLayout(name, value);

    }

    private void setupMeasureLayout(String name, String value) {
        View personalCard = LayoutInflater.from(getContext()).inflate(R.layout.register_layout, measuresLayout, false);
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
            valueEditText.setText(value);
            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle("Insira um novo valor para " + name)
                    .setView(valueEditText)
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Confirmar", (((dialogInterface, i) -> {
                        String newValue = valueEditText.getText().toString();
                        if (!newValue.isEmpty()) {
                            MeasureModel measure = measuresMap.get(name);
                            measure.setValue(Integer.valueOf(newValue));
                            textView.setText(name + ": " + newValue);
                        }
                    }))).create();
            dialog.show();
        });
        removeExerciseButton.setOnClickListener(event -> {
            Long id = measuresMap.get(name).getId();

            if (!id.equals(0L)) {
                deletedMeasuresList.add(id);
            }

            measuresLayout.removeView(personalCard);
            measuresMap.remove(name);
        });

        measuresLayout.addView(personalCard);
    }

}