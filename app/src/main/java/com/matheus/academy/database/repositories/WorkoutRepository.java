package com.matheus.academy.database.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.matheus.academy.database.DatabaseManager;
import com.matheus.academy.database.helpers.WorkoutHelper;
import com.matheus.academy.models.WorkoutModel;

import java.util.ArrayList;
import java.util.List;

public class WorkoutRepository {
    private DatabaseManager databaseManager;

    public WorkoutRepository(Context context) {
        databaseManager = new DatabaseManager(context);
    }

    public long add(String date) {
        SQLiteDatabase sqLiteDatabase = databaseManager.getWritableDatabase();
        if (sqLiteDatabase == null) return -1;

        ContentValues contentValues = new ContentValues();

        contentValues.put(WorkoutHelper.COLUMN_DATE, date);
        long result = sqLiteDatabase.insert(WorkoutHelper.TABLE_NAME, null, contentValues);
        sqLiteDatabase.close();
        return result;
    }

    public boolean delete(Long id) {
        SQLiteDatabase sqLiteDatabase = databaseManager.getWritableDatabase();
        if (sqLiteDatabase == null) return false;

        int result = sqLiteDatabase.delete(WorkoutHelper.TABLE_NAME, WorkoutHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        sqLiteDatabase.close();
        return result > 0;
    }

    public List<WorkoutModel> getAll() {
        String query = "SELECT * FROM " + WorkoutHelper.TABLE_NAME;
        SQLiteDatabase sqLiteDatabase = databaseManager.getReadableDatabase();

        List<WorkoutModel> workoutsList = new ArrayList<>();

        if (sqLiteDatabase != null) {
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    WorkoutModel workout = new WorkoutModel(cursor.getLong(0), cursor.getString(1));
                    workoutsList.add(workout);
                } while (cursor.moveToNext());
            }

            cursor.close();
        }
        return workoutsList;
    }
}
