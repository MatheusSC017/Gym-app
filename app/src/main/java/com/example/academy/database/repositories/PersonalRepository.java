package com.example.academy.database.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.academy.database.DatabaseManager;
import com.example.academy.database.MeasureHelper;
import com.example.academy.database.PersonalHelper;
import com.example.academy.models.PersonalModel;

import java.util.ArrayList;
import java.util.List;

public class PersonalRepository {
    private DatabaseManager databaseManager;

    public PersonalRepository(Context context) { databaseManager = new DatabaseManager(context); }

    public long add(String date) {
        SQLiteDatabase sqLiteDatabase = databaseManager.getWritableDatabase();
        if (sqLiteDatabase == null) return -1;

        ContentValues contentValues = new ContentValues();

        contentValues.put(PersonalHelper.COLUMN_DATE, date);
        long result = sqLiteDatabase.insert(PersonalHelper.TABLE_NAME, null, contentValues);
        sqLiteDatabase.close();
        return result;
    }

    public boolean update(Long personalId, PersonalModel personal) {
        SQLiteDatabase sqLiteDatabase = databaseManager.getWritableDatabase();
        if (sqLiteDatabase == null) return false;

        ContentValues contentValues = new ContentValues();
        contentValues.put(PersonalHelper.COLUMN_HEIGHT, personal.getHeight());
        contentValues.put(PersonalHelper.COLUMN_WEIGHT, personal.getWeight());
        contentValues.put(PersonalHelper.COLUMN_FAT_PERCENTAGE, personal.getFatPercentage());
        contentValues.put(PersonalHelper.COLUMN_FAT_WEIGHT, personal.getFatWeight());
        contentValues.put(PersonalHelper.COLUMN_LEAN_MASS, personal.getLeanMass());
        contentValues.put(PersonalHelper.COLUMN_IMC, personal.getImc());

        int result = sqLiteDatabase.update(PersonalHelper.TABLE_NAME, contentValues,
                PersonalHelper.COLUMN_ID + "=?", new String[]{String.valueOf(personalId)});
        sqLiteDatabase.close();

        return result > 0;
    }

    public boolean delete(Long id) {
        SQLiteDatabase sqLiteDatabase = databaseManager.getWritableDatabase();
        if (sqLiteDatabase == null) return false;

        int result = sqLiteDatabase.delete(PersonalHelper.TABLE_NAME, PersonalHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        sqLiteDatabase.close();
        return result > 0;
    }


    public PersonalModel get(Long personalId) {
        String query = "SELECT * FROM " + PersonalHelper.TABLE_NAME + " WHERE " + PersonalHelper.COLUMN_ID + "=?;";
        SQLiteDatabase sqLiteDatabase = databaseManager.getReadableDatabase();

        if (sqLiteDatabase != null) {
            Cursor cursor = sqLiteDatabase.rawQuery(query, new String[]{String.valueOf(personalId)});

            if (cursor.moveToFirst() && cursor.getCount() > 0) {
                PersonalModel personalModel = new PersonalModel(cursor.getLong(0), cursor.getString(1),
                        cursor.getFloat(2), cursor.getFloat(3), cursor.getFloat(4),
                        cursor.getFloat(5), cursor.getFloat(6), cursor.getFloat(7));
                return personalModel;
            }
        }

        return null;
    }

    public List<PersonalModel> getAll() {
        String query = "SELECT * FROM " + PersonalHelper.TABLE_NAME;
        SQLiteDatabase sqLiteDatabase = databaseManager.getReadableDatabase();

        List<PersonalModel> personalModelList = new ArrayList<>();

        if (sqLiteDatabase != null) {
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    PersonalModel personalModel = new PersonalModel(cursor.getLong(0), cursor.getString(1),
                            cursor.getFloat(2), cursor.getFloat(3), cursor.getFloat(4),
                            cursor.getFloat(5), cursor.getFloat(6), cursor.getFloat(7));
                    personalModelList.add(personalModel);
                } while (cursor.moveToNext());
            }
        }

        return personalModelList;
    }
}
