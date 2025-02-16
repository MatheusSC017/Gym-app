package com.example.academy.database.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.academy.database.DatabaseManager;
import com.example.academy.database.ExerciseHelper;
import com.example.academy.database.SerieHelper;
import com.example.academy.models.ExerciseModel;

import java.util.ArrayList;
import java.util.List;

public class ExerciseRepository {
    DatabaseManager databaseManager;

    public ExerciseRepository(Context context) {
        databaseManager = new DatabaseManager(context);
    }

    public ExerciseModel add(ExerciseModel exercise) {
        SQLiteDatabase sqLiteDatabase = databaseManager.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(ExerciseHelper.COLUMN_NAME, exercise.getName());
        contentValues.put(ExerciseHelper.COLUMN_SERIES_NUMBER, exercise.getSeriesNumber());
        contentValues.put(ExerciseHelper.COLUMN_MEASURE, exercise.getMeasure());
        contentValues.put(ExerciseHelper.COLUMN_QUANTITY, exercise.getQuantity());
        contentValues.put(ExerciseHelper.COLUMN_MUSCLE, exercise.getMuscle());
        contentValues.put(ExerciseHelper.COLUMN_OBSERVATION, exercise.getObservation());
        contentValues.put(ExerciseHelper.COLUMN_SERIE_ID, exercise.getSerieId());

        Long result = sqLiteDatabase.insert(ExerciseHelper.TABLE_NAME, null, contentValues);
        sqLiteDatabase.close();
        if (result != -1) {
            exercise.setId(result);
            return exercise;
        }
        return null;
    }

    public boolean update(Long exerciseId, ExerciseModel exercise) {
        SQLiteDatabase sqLiteDatabase = databaseManager.getWritableDatabase();
        if (sqLiteDatabase == null) return false;

        ContentValues contentValues = new ContentValues();
        contentValues.put(ExerciseHelper.COLUMN_NAME, exercise.getName());
        contentValues.put(ExerciseHelper.COLUMN_SERIES_NUMBER, exercise.getSeriesNumber());
        contentValues.put(ExerciseHelper.COLUMN_MEASURE, exercise.getMeasure());
        contentValues.put(ExerciseHelper.COLUMN_QUANTITY, exercise.getQuantity());
        contentValues.put(ExerciseHelper.COLUMN_MUSCLE, exercise.getMuscle());
        contentValues.put(ExerciseHelper.COLUMN_SEQUENCE, exercise.getSequence());
        contentValues.put(ExerciseHelper.COLUMN_OBSERVATION, exercise.getObservation());

        int result = sqLiteDatabase.update(ExerciseHelper.TABLE_NAME, contentValues,
                ExerciseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(exerciseId)});
        sqLiteDatabase.close();
        return result > 0;
    }

    public boolean delete(Long exerciseId) {
        SQLiteDatabase sqLiteDatabase = databaseManager.getWritableDatabase();
        if (sqLiteDatabase == null) return false;

        int result = sqLiteDatabase.delete(ExerciseHelper.TABLE_NAME,
                ExerciseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(exerciseId)});
        sqLiteDatabase.close();
        return result > 0 ;
    }

    public List<ExerciseModel> getAll(Long serieId) {
        String query = "SELECT * FROM " + ExerciseHelper.TABLE_NAME +
                " WHERE " + ExerciseHelper.COLUMN_SERIE_ID + " = ?" +
                " ORDER BY " + SerieHelper.COLUMN_ID + " ASC;";
        SQLiteDatabase sqLiteDatabase = databaseManager.getReadableDatabase();

        List<ExerciseModel> exercisesList = new ArrayList<>();

        if (sqLiteDatabase != null) {
            Cursor cursor = sqLiteDatabase.rawQuery(query, new String[]{String.valueOf(serieId)});

            if (cursor.moveToFirst()) {
                do {
                    ExerciseModel exercise = new ExerciseModel(cursor.getLong(0), cursor.getString(1),
                            cursor.getInt(2), cursor.getString(3), cursor.getInt(4), cursor.getString(5),
                            cursor.getInt(6), cursor.getString(7), cursor.getLong(8));
                    exercisesList.add(exercise);
                } while (cursor.moveToNext());
            }

            cursor.close();
        }
        return exercisesList;
    }
}
