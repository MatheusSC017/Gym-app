package com.example.academy.database.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.academy.database.DatabaseManager;
import com.example.academy.database.WorkoutHelper;

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
        return result;
    }

    public Cursor getAllWorkouts() {
        String query = "SELECT * FROM " + WorkoutHelper.TABLE_NAME;
        SQLiteDatabase sqLiteDatabase = databaseManager.getReadableDatabase();

        Cursor cursor = null;
        if (sqLiteDatabase != null) {
            cursor = sqLiteDatabase.rawQuery(query, null);
        }
        return cursor;
    }
}
