package com.matheus.academy.database.helpers;

public class SerieHelper {
    public static final String TABLE_NAME = "series";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "serie_name";
    public static final String COLUMN_WORKOUT_ID = "serie_workout_id";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT," +
                COLUMN_WORKOUT_ID + " INTEGER NOT NULL," +
                "FOREIGN KEY (" + COLUMN_WORKOUT_ID + ") " +
                "REFERENCES " + WorkoutHelper.TABLE_NAME + "(" + WorkoutHelper.COLUMN_ID + "));";
}
