package com.example.academy.database.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.academy.database.DatabaseManager;
import com.example.academy.database.FoldHelper;
import com.example.academy.database.MeasureHelper;
import com.example.academy.database.PersonalHelper;
import com.example.academy.models.FoldModel;
import com.example.academy.models.MeasureModel;

import java.util.ArrayList;
import java.util.List;

public class MeasureRepository {
    private DatabaseManager databaseManager;

    public MeasureRepository(Context context) { databaseManager = new DatabaseManager(context); }

    public MeasureModel add(MeasureModel measure) {
        SQLiteDatabase sqLiteDatabase = databaseManager.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(MeasureHelper.COLUMN_NAME, measure.getName());
        contentValues.put(MeasureHelper.COLUMN_VALUE, measure.getValue());
        contentValues.put(MeasureHelper.COLUMN_PERSONAL_ID, measure.getPersonalId());

        Long result = sqLiteDatabase.insert(MeasureHelper.TABLE_NAME, null, contentValues);
        sqLiteDatabase.close();
        if(result != -1) {
            measure.setId(result);
            return measure;
        }
        return null;
    }

    public boolean update(Long measureId, MeasureModel measure) {
        SQLiteDatabase sqLiteDatabase = databaseManager.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(MeasureHelper.COLUMN_NAME, measure.getName());
        contentValues.put(MeasureHelper.COLUMN_VALUE, measure.getValue());
        contentValues.put(MeasureHelper.COLUMN_PERSONAL_ID, measure.getPersonalId());

        int result = sqLiteDatabase.update(MeasureHelper.TABLE_NAME, contentValues,
                MeasureHelper.COLUMN_ID + "=?", new String[]{String.valueOf(measureId)});
        sqLiteDatabase.close();

        return result > 0;
    }

    public boolean delete(Long id) {
        SQLiteDatabase sqLiteDatabase = databaseManager.getWritableDatabase();
        if (sqLiteDatabase == null) return false;

        int result = sqLiteDatabase.delete(MeasureHelper.TABLE_NAME, MeasureHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        sqLiteDatabase.close();
        return result > 0;
    }

    public List<MeasureModel> getAll(Long personalId) {
        String query = "SELECT * FROM " + MeasureHelper.TABLE_NAME + " WHERE " + MeasureHelper.COLUMN_PERSONAL_ID + "=?;";
        SQLiteDatabase sqLiteDatabase = databaseManager.getReadableDatabase();

        List<MeasureModel> measureModelList = new ArrayList<>();

        if (sqLiteDatabase != null) {
            Cursor cursor = sqLiteDatabase.rawQuery(query, new String[]{String.valueOf(personalId)});

            if (cursor.moveToFirst()) {
                do {
                    MeasureModel measureModel = new MeasureModel(cursor.getLong(0), cursor.getString(1),
                            cursor.getInt(2), cursor.getLong(3));
                    measureModelList.add(measureModel);
                } while (cursor.moveToNext());
            }
        }

        return measureModelList;
    }
}
