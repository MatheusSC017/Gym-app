package com.example.academy;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.ObjectListKt;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
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

import com.example.academy.database.DatabaseManager;
import com.example.academy.ui.finance.FinanceFragment;
import com.example.academy.ui.history.HistoryFragment;
import com.example.academy.ui.personal.PersonalFragment;
import com.example.academy.ui.workout.WorkoutFragment;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.time.LocalDate;

public class MainActivity extends AppCompatActivity {
    private final ActivityResultLauncher<Intent> filePickerLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri selectedFileUri = result.getData().getData();
                if (selectedFileUri != null) {
                    uploadData(selectedFileUri);
                }
            }
        });


    private DrawerLayout drawerLayout;

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;

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
                    selectBackupFile();
                } else if (item.getItemId() == R.id.download) {
                    downloadData();
                } else {
                    loadFragment(new WorkoutFragment());
                }
                return false;
            }
        });


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

    public void selectBackupFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        filePickerLauncher.launch(intent);
    }

    private void uploadData(Uri fileUri) {
        try {
            File dbFile = getApplicationContext().getDatabasePath(DatabaseManager.DATABASE_NAME);

            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            OutputStream outputStream = new FileOutputStream(dbFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            Toast.makeText(this, "Base de dados restaurada!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Upload falhou: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadData() {
        try {
            LocalDate today = LocalDate.now();
            File dbFile = getApplicationContext().getDatabasePath(DatabaseManager.DATABASE_NAME);
            String backupFilePath = "workouts_backup_" + today.getDayOfMonth() + "_" + today.getMonth() + "_" + today.getYear() + ".db";
            File backupFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), backupFilePath);

            FileChannel dbFileChannel = new FileInputStream(dbFile).getChannel();
            FileChannel backupFileChannel = new FileOutputStream(backupFile).getChannel();
            backupFileChannel.transferFrom(dbFileChannel, 0, dbFileChannel.size());
            dbFileChannel.close();
            backupFileChannel.close();

            Toast.makeText(this, "Backup completo!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Backup falhou: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}