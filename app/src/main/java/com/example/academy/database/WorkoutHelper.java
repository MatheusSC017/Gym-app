package com.example.academy.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class WorkoutHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "Academy.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "workouts";
    public static final String COLUMN_ID = "_id";
    private static final String COLUMN_DATE = "workout_date";

    public WorkoutHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + TABLE_NAME +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATE + " TEXT);";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public void addWorkout(String date) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_DATE, date);
        long result = sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        if (result == -1) {
            Toast.makeText(context, "Failed", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Added Successfully!", Toast.LENGTH_LONG).show();
        }

    }
}
