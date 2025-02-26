package com.matheus.academy.database;

public class ExerciseHelper {
    public static final String TABLE_NAME = "exercises";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "exercise_name";
    public static final String COLUMN_SERIES_NUMBER = "exercise_series_number";
    public static final String COLUMN_MEASURE = "exercise_measure";
    public static final String COLUMN_QUANTITY = "exercise_quantity";
    public static final String COLUMN_MUSCLE = "exercise_muscle";
    public static final String COLUMN_SEQUENCE = "exercise_sequence";
    public static final String COLUMN_OBSERVATION = "exercise_observation";
    public static final String COLUMN_WEIGHT = "exercise_weight";
    public static final String COLUMN_SERIE_ID = "exercise_serie_id";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
            " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NAME + " TEXT NOT NULL," +
            COLUMN_SERIES_NUMBER + " INTEGER," +
            COLUMN_MEASURE + " TEXT NOT NULL," +
            COLUMN_QUANTITY + " INTEGER NOT NULL," +
            COLUMN_MUSCLE + " TEXT," +
            COLUMN_SEQUENCE + " INTEGER," +
            COLUMN_OBSERVATION + " TEXT," +
            COLUMN_WEIGHT + " INTEGER, " +
            COLUMN_SERIE_ID + " INTEGER NOT NULL," +
            "FOREIGN KEY (" + COLUMN_SERIE_ID + ") " +
            "REFERENCES " + SerieHelper.TABLE_NAME + "(" + SerieHelper.COLUMN_ID + "));";
}
