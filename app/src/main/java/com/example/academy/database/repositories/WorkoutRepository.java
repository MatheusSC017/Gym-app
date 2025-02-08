package com.example.academy.database.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.academy.database.DatabaseManager;
import com.example.academy.database.WorkoutHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WorkoutRepository {
    private DatabaseManager databaseManager;

    public WorkoutRepository(Context context) {
        databaseManager = new DatabaseManager(context);
    }

    public long addWorkout(String date) {
        SQLiteDatabase sqLiteDatabase = databaseManager.getWritableDatabase();
        if (sqLiteDatabase == null) return -1;

        ContentValues contentValues = new ContentValues();

        contentValues.put(WorkoutHelper.COLUMN_DATE, date);
        long result = sqLiteDatabase.insert(WorkoutHelper.TABLE_NAME, null, contentValues);
        sqLiteDatabase.close();
        return result;
    }

    public boolean deleteWorkout(Long id) {
        SQLiteDatabase sqLiteDatabase = databaseManager.getWritableDatabase();
        if (sqLiteDatabase == null) return false;

        int result = sqLiteDatabase.delete(WorkoutHelper.TABLE_NAME, WorkoutHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        sqLiteDatabase.close();
        return result > 0;
    }

    public List<HashMap<String, Object>> getAllWorkouts() {
        String query = "SELECT * FROM " + WorkoutHelper.TABLE_NAME;
        SQLiteDatabase sqLiteDatabase = databaseManager.getReadableDatabase();

        List<HashMap<String, Object>> workoutsList = new ArrayList<>();

        if (sqLiteDatabase != null) {
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, Object> workoutMap = new HashMap<>();
                    workoutMap.put(WorkoutHelper.COLUMN_ID, cursor.getLong(0));
                    workoutMap.put(WorkoutHelper.COLUMN_DATE, cursor.getString(1));
                    workoutsList.add(workoutMap);
                } while (cursor.moveToNext());
            }

            cursor.close();
        }
        return workoutsList;
    }
}
