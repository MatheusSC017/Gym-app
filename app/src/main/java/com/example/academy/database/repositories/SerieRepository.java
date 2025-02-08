package com.example.academy.database.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.academy.database.DatabaseManager;
import com.example.academy.database.SerieHelper;
import com.example.academy.database.WorkoutHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        sqLiteDatabase.close();
        return result;
    }

    public boolean removeSerie(Long serieId) {
        SQLiteDatabase sqLiteDatabase = databaseManager.getWritableDatabase();

        int result = sqLiteDatabase.delete(SerieHelper.TABLE_NAME, SerieHelper.COLUMN_ID + "=?", new String[]{String.valueOf(serieId)});
        sqLiteDatabase.close();
        return result > 0;
    }

    public List<HashMap<String, Object>> getSeries(Long workoutId) {
        String query = "SELECT * FROM " + SerieHelper.TABLE_NAME +
                " WHERE " + SerieHelper.COLUMN_WORKOUT_ID + " = ?" +
                " ORDER BY " + SerieHelper.COLUMN_ID + " ASC;";
        SQLiteDatabase sqLiteDatabase = databaseManager.getReadableDatabase();

        List<HashMap<String, Object>> seriesList = new ArrayList<>();

        if (sqLiteDatabase != null) {
            Cursor cursor = sqLiteDatabase.rawQuery(query, new String[]{String.valueOf(workoutId)});

            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, Object> serieMap = new HashMap<>();
                    serieMap.put(SerieHelper.COLUMN_ID, cursor.getLong(0));
                    serieMap.put(SerieHelper.COLUMN_NAME, cursor.getString(1));
                    seriesList.add(serieMap);
                } while (cursor.moveToNext());
            }

            cursor.close();
        }
        return seriesList;
    }
}
