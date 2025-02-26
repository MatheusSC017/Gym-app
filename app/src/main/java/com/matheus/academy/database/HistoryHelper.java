package com.matheus.academy.database;

public class HistoryHelper {
    public static final String TABLE_NAME = "history";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "history_date";
    public static final String COLUMN_SERIE_ID = "history_serie_id";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
            " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_DATE + " TEXT UNIQUE, " +
            COLUMN_SERIE_ID + " INTEGER, " +
            "FOREIGN KEY (" + COLUMN_SERIE_ID + ") " +
            "REFERENCES " + SerieHelper.TABLE_NAME + "(" + SerieHelper.COLUMN_ID + "));";
}
