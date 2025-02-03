package com.example.academy.database.repositories;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.academy.database.DatabaseManager;
import com.example.academy.database.ExerciseHelper;
import com.example.academy.database.SerieHelper;

public class ExerciseRepository {
    DatabaseManager databaseManager;

    public ExerciseRepository(Context context) {
        databaseManager = new DatabaseManager(context);
    }

    public Cursor getExercises(Long serieId) {
        String query = "SELECT * FROM " + ExerciseHelper.TABLE_NAME +
                " WHERE " + ExerciseHelper.COLUMN_SERIE_ID + " = " + serieId +
                " ORDER BY " + SerieHelper.COLUMN_ID + " ASC;";
        SQLiteDatabase sqLiteDatabase = databaseManager.getReadableDatabase();

        Cursor cursor = null;
        if (sqLiteDatabase != null) {
            cursor = sqLiteDatabase.rawQuery(query, null);
        }
        return cursor;
    }
}
