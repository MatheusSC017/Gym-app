package com.matheus.academy.database.helpers;

public class WorkoutHelper {
    public static final String TABLE_NAME = "workouts";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "workout_date";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
            " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_DATE + " TEXT UNIQUE);";

}
