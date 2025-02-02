package com.example.academy.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class ExerciseHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "Academy.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "exercises";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_NAME = "exercise_name";
    private static final String COLUMN_SERIES_NUMBER = "exercise_series_number";
    private static final String COLUMN_MEASURE = "exercise_measure";
    private static final String COLUMN_QUANTITY = "exercise_quantity";
    private static final String COLUMN_MUSCLE = "exercise_muscle";
    private static final String COLUMN_SEQUENCE = "exercise_sequence";
    private static final String COLUMN_ORDER = "exercise_order";
    private static final String COLUMN_OBSERVATION = "exercise_observation";
    private static final String COLUMN_SERIE_ID = "exercise_serie_id";

    public ExerciseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + TABLE_NAME +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL," +
                COLUMN_SERIES_NUMBER + " INTEGER," +
                COLUMN_MEASURE + " TEXT NOT NULL," +
                COLUMN_QUANTITY + " INTEGER NOT NULL," +
                COLUMN_MUSCLE + " TEXT," +
                COLUMN_SEQUENCE + " INTEGER," +
                COLUMN_ORDER + " INTEGER NOT NULL," +
                COLUMN_OBSERVATION + " TEXT," +
                COLUMN_SERIE_ID + " INTEGER NOT NULL," +
                "FOREIGN KEY (" + COLUMN_SERIE_ID + ") " +
                "REFERENCES " + SerieHelper.TABLE_NAME + "(" + SerieHelper.COLUMN_ID + "));";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }
}
