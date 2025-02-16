package com.example.academy.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseManager extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Academy.db";
    private static final int DATABASE_VERSION = 3;

    public DatabaseManager(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(WorkoutHelper.CREATE_TABLE);
        sqLiteDatabase.execSQL(SerieHelper.CREATE_TABLE);
        sqLiteDatabase.execSQL(ExerciseHelper.CREATE_TABLE);
        sqLiteDatabase.execSQL(PersonalHelper.CREATE_TABLE);
        sqLiteDatabase.execSQL(FoldHelper.CREATE_TABLE);
        sqLiteDatabase.execSQL(MeasureHelper.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WorkoutHelper.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SerieHelper.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ExerciseHelper.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PersonalHelper.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FoldHelper.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MeasureHelper.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WorkoutHelper.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SerieHelper.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ExerciseHelper.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PersonalHelper.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FoldHelper.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MeasureHelper.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
