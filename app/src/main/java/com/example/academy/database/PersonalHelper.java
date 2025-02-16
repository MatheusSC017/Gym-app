package com.example.academy.database;

public class PersonalHelper {
    public static final String TABLE_NAME = "personal";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "personal_date";
    public static final String COLUMN_WEIGHT = "personal_weight";
    public static final String COLUMN_HEIGHT = "personal_height";
    public static final String COLUMN_LEAN_MASS = "personal_lean_mass";
    public static final String COLUMN_FAT_WEIGHT = "personal_fat_weight";
    public static final String COLUMN_FAT_PERCENTAGE = "personal_fat_percentage";
    public static final String COLUMN_IMC = "personal_imc";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
            " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_DATE + " TEXT UNIQUE, " +
            COLUMN_WEIGHT + " REAL, " +
            COLUMN_HEIGHT + " REAL, " +
            COLUMN_LEAN_MASS + " REAL, " +
            COLUMN_FAT_WEIGHT + " REAL, " +
            COLUMN_FAT_PERCENTAGE + " REAL, " +
            COLUMN_IMC + " REAL);";
}
