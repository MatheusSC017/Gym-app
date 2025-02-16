package com.example.academy.database;

public class FoldHelper {
    public static final String TABLE_NAME = "fold";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "fold_name";
    public static final String COLUMN_VALUE = "fold_value";
    public static final String COLUMN_PERSONAL_ID = "fold_personal_id";


    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
            " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NAME + " TEXT NOT NULL, " +
            COLUMN_VALUE + " INTEGER NOT NULL, " +
            COLUMN_PERSONAL_ID + " INTEGER NOT NULL, " +
            "FOREIGN KEY (" + COLUMN_PERSONAL_ID + ") " +
            "REFERENCES " + PersonalHelper.TABLE_NAME + "(" + PersonalHelper.COLUMN_ID + "));";
}
