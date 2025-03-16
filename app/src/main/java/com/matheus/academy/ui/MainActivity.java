package com.matheus.academy.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.matheus.academy.R;
import com.matheus.academy.database.DatabaseManager;
import com.matheus.academy.database.LocalBackup;
import com.matheus.academy.ui.history.HistoryFragment;
import com.matheus.academy.ui.personal.PersonalFragment;
import com.matheus.academy.ui.workout.WorkoutFragment;
import com.google.android.material.navigation.NavigationView;

import java.time.LocalDate;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private LocalBackup localBackup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        setUpToolbar();

        final DatabaseManager db = new DatabaseManager(getApplicationContext());

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
                } else if (item.getItemId() == R.id.upload) {
                    LocalDate today = LocalDate.now();
                    localBackup.performBackup(db, "workouts_backup_");
                } else if (item.getItemId() == R.id.download) {
                    localBackup.performRestore(db);
                } else {
                    loadFragment(new WorkoutFragment());
                }
                return false;
            }
        });

        localBackup = new LocalBackup(this);

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

}