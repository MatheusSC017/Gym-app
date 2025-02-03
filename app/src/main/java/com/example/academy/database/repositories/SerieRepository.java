package com.example.academy.database.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.academy.database.DatabaseManager;
import com.example.academy.database.SerieHelper;

public class SerieRepository {
    DatabaseManager databaseManager;

    public SerieRepository(Context context) {
        databaseManager = new DatabaseManager(context);
    }

    public Long addSerie(String name, Long workout_id) {
        SQLiteDatabase sqLiteDatabase = databaseManager.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(SerieHelper.COLUMN_NAME, name);
        contentValues.put(SerieHelper.COLUMN_WORKOUT_ID, workout_id);

        Long result = sqLiteDatabase.insert(SerieHelper.TABLE_NAME, null, contentValues);
        return result;
    }

    public boolean removeSerie(Long serieId) {
        SQLiteDatabase sqLiteDatabase = databaseManager.getWritableDatabase();

        int result = sqLiteDatabase.delete(SerieHelper.TABLE_NAME, SerieHelper.COLUMN_ID + "=" + serieId, null);
        return result > 0;
    }

    public Cursor getSeries(Long workoutId) {
        String query = "SELECT * FROM " + SerieHelper.TABLE_NAME +
                " WHERE " + SerieHelper.COLUMN_WORKOUT_ID + " = " + workoutId +
                " ORDER BY " + SerieHelper.COLUMN_ID + " ASC;";
        SQLiteDatabase sqLiteDatabase = databaseManager.getReadableDatabase();

        Cursor cursor = null;
        if (sqLiteDatabase != null) {
            cursor = sqLiteDatabase.rawQuery(query, null);
        }
        return cursor;
    }
}
