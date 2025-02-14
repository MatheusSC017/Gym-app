package com.example.academy.database;

public class MeasureHelper {
    public static final String TABLE_NAME = "measure";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "measure_name";
    public static final String COLUMN_VALUE = "measure_value";
    public static final String COLUMN_PERSONAL_ID = "measure_personal_id";


    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
            " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NAME + " TEXT NOT NULL, " +
            COLUMN_VALUE + " INTEGER NOT NULL, " +
            "FOREIGN KEY (" + COLUMN_PERSONAL_ID + ") " +
            "REFERENCES " + PersonalHelper.TABLE_NAME + "(" + PersonalHelper.COLUMN_ID + "));";

}
