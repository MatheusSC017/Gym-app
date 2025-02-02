package com.example.academy.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SerieHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "Academy.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "series";
    public static final String COLUMN_ID = "_id";
    private static final String COLUMN_NAME = "serie_name";
    private static final String COLUMN_WORKOUT_ID = "serie_workout_id";

    public SerieHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + TABLE_NAME +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT," +
                COLUMN_WORKOUT_ID + " INTEGER NOT NULL," +
                "FOREIGN KEY (" + COLUMN_WORKOUT_ID + ") " +
                "REFERENCES " + WorkoutHelper.TABLE_NAME + "(" + WorkoutHelper.COLUMN_ID + "));";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }
}
