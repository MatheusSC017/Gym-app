package com.example.academy;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.ObjectListKt;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.academy.ui.base.ConvertFromJson;
import com.example.academy.ui.finance.FinanceFragment;
import com.example.academy.ui.history.HistoryFragment;
import com.example.academy.ui.personal.PersonalFragment;
import com.example.academy.ui.workout.WorkoutFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private static String JSON_FILE = "workouts.json";

    private DrawerLayout drawerLayout;

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        setUpToolbar();

        navigationView = findViewById(R.id.navigation_menu);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId() == R.id.history) {
                    loadFragment(new HistoryFragment());
                } else if (item.getItemId() == R.id.personal) {
                    loadFragment(new PersonalFragment());
                } else if (item.getItemId() == R.id.workouts) {
                    loadFragment(new WorkoutFragment());
                } else if (item.getItemId() == R.id.finance) {
                    loadFragment(new FinanceFragment());
                } else if (item.getItemId() == R.id.upload) {
                    uploadData();
                } else if (item.getItemId() == R.id.download) {
                    downloadData();
                } else {
                    loadFragment(new WorkoutFragment());
                }
                return false;
            }
        });

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            Uri uri = data.getData();
                            handleFileUri(uri);
                        }
                    }
                }
        );

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void setUpToolbar() {
        drawerLayout = findViewById(R.id.drawerLayout);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.nav_open,
                R.string.nav_close
        );

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        loadFragment(new HistoryFragment());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
        drawerLayout.closeDrawers();
    }

    private void uploadData() {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            try {
                filePickerLauncher.launch(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Erro ao acessar o armazenamento de dados", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void handleFileUri(Uri uri) {
        try {
            ContentResolver resolver = getContentResolver();
            InputStream inputStream = resolver.openInputStream(uri);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder jsonBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
                reader.close();
                inputStream.close();

                JSONObject jsonData = new JSONObject(jsonBuilder.toString());
                Object loadedData = ConvertFromJson.convert(jsonData);

                if (loadedData != null) {
                    HashMap<String, Object> validDateMap = validateJsonData(loadedData);
                    JSONObject dataJson = loadInternalJson();
                    HashMap<String, Object> internalDataJson = (HashMap<String, Object>) ConvertFromJson.convert(dataJson);
                    internalDataJson.putAll(validDateMap);
                    saveToInternalStorage(internalDataJson);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Falha ao ler o arquivo", Toast.LENGTH_LONG).show();
        }
    }

    private HashMap<String, Object> validateJsonData(Object loadedData) {
        if (loadedData instanceof HashMap) {
            HashMap<String, Object> dataMap = (HashMap<String, Object>) loadedData;
            HashMap<String, Object> validDateMap = getValidDates(dataMap);
            Toast.makeText(this, loadedData.toString(), Toast.LENGTH_LONG).show();
            return validDateMap;
        }
        return new HashMap<>();
    }

    private HashMap<String, Object> getValidDates(HashMap<String, Object> dataMap) {
        HashMap<String, Object> validDateMap = new HashMap<>();
        for (String date: dataMap.keySet()) {
            if (isValidDate(date)) {
                validDateMap.put(date, getValidSubItems((HashMap<String, Object>) dataMap.get(date)));
            }
        }
        return validDateMap;
    }

    private boolean isValidDate(String date) {
        String regex = "\\d{2}/\\d{4}";

        return date != null && date.length() == 7 && Pattern.matches(regex, date);
    }

    private HashMap<String, Object> getValidSubItems(HashMap<String, Object> subItems) {
        HashMap<String, Object> validSubItems = new HashMap<>();
        for (String item: subItems.keySet()) {
            switch (item) {
                case "Personal":
                    validSubItems.put(item, getValidPersonalData((HashMap<String, Object>) subItems.get(item)));
                    break;
                case "Series":
                    validSubItems.put(item, getValidSeries((HashMap<String, Object>) subItems.get(item)));
                    break;
            }
        }
        return validSubItems;
    }

    private HashMap<String, Object> getValidSeries(HashMap<String, Object> series) {
        HashMap<String, Object> validSeries = new HashMap<>();
        series.forEach((serieName, value) -> {
            if (value instanceof LinkedHashMap) {
                LinkedHashMap<String, HashMap> exercises = (LinkedHashMap<String, HashMap>) value;
                validSeries.put(serieName, getValidExercises(exercises));
            }
        });
        return validSeries;
    }

    private LinkedHashMap<String, HashMap> getValidExercises(LinkedHashMap<String, HashMap> exercises) {
        List<String> exerciseInfo = Arrays.asList("Series", "Type", "Quantity", "Muscle", "Sequence", "Observation");
        LinkedHashMap<String, HashMap> validExercises = new LinkedHashMap<>();
        exercises.forEach((exerciseName, exerciseData) -> {
            HashMap<String, Object> validExerciseDate = new HashMap<>();
            exerciseData.forEach((key, value) -> {
                if (exerciseInfo.contains((String) key)) {
                    validExerciseDate.put((String) key, value);
                }
            });
            if (hasRequiredExerciseKeys(validExerciseDate, exerciseInfo.subList(0, 3))) {
                validExercises.put(exerciseName, exerciseData);
            }

        });
        return validExercises;
    }

    private static boolean hasRequiredExerciseKeys(HashMap<String, Object> exerciseMap, List<String> keys) {
        for (String key: keys) {
            if (!exerciseMap.containsKey(key)) return false;
        }
        return true;
    }

    private HashMap<String, Object> getValidPersonalData(HashMap<String, Object> personalData) {
        HashMap<String, Object> validPersonalData = new HashMap<>();
        List<String> allowedKeys = Arrays.asList("Weight", "Height", "Lean mass", "Fat weight", "Fat percentage", "IMC", "Folds", "Measures");
        personalData.forEach((key, value) -> {
            if (allowedKeys.subList(0, 6).contains(key) &&
                    value != null &&
                    (value instanceof Double ||
                    (value instanceof String) && isNumeric((String) value))) {
                validPersonalData.put(key, value);
            }
            if (allowedKeys.subList(6, 8).contains(key) &&
                value instanceof HashMap){
                validPersonalData.put(key, value);
            }
        });
        return validPersonalData;
    }

    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void saveToInternalStorage(HashMap<String, Object> mapData) {
        Gson gson = new Gson();

        try (FileOutputStream fos = openFileOutput(JSON_FILE, Context.MODE_PRIVATE)) {
            String jsonData = gson.toJson(mapData);
            fos.write(jsonData.toString().getBytes());
        } catch (Exception e) {
            Toast.makeText(this, "Error saving file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void downloadData() {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            JSONObject dataJson = loadInternalJson();
            saveExternalJson("treinamento.json", dataJson);
        }
    }

    private JSONObject loadInternalJson() {
        try (FileInputStream fis = this.openFileInput(JSON_FILE)) {
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            return new JSONObject(json);

        } catch (Exception e) {
            Toast.makeText(this, "Error loading JSON: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return new JSONObject();
        }
    }

    private void saveExternalJson(String name, JSONObject dataJson) {
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(folder, name);

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(dataJson.toString().getBytes());
            Toast.makeText(this, "Treinamento baixado", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}