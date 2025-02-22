package com.example.academy.database.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.academy.database.DatabaseManager;
import com.example.academy.database.FoldHelper;
import com.example.academy.database.MeasureHelper;
import com.example.academy.database.WorkoutHelper;
import com.example.academy.models.ExerciseModel;
import com.example.academy.models.FoldModel;
import com.example.academy.models.MeasureModel;

import java.util.ArrayList;
import java.util.List;

public class FoldRepository {
    private DatabaseManager databaseManager;

    public FoldRepository(Context context) { databaseManager = new DatabaseManager(context); }

    public FoldModel add(FoldModel fold) {
        SQLiteDatabase sqLiteDatabase = databaseManager.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(FoldHelper.COLUMN_NAME, fold.getName());
        contentValues.put(FoldHelper.COLUMN_VALUE, fold.getValue());
        contentValues.put(FoldHelper.COLUMN_PERSONAL_ID, fold.getPersonalId());

        Long result = sqLiteDatabase.insert(FoldHelper.TABLE_NAME, null, contentValues);
        sqLiteDatabase.close();
        if(result != -1) {
            fold.setId(result);
            return fold;
        }
        return null;
    }

    public boolean update(Long foldId, FoldModel fold) {
        SQLiteDatabase sqLiteDatabase = databaseManager.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(FoldHelper.COLUMN_NAME, fold.getName());
        contentValues.put(FoldHelper.COLUMN_VALUE, fold.getValue());
        contentValues.put(FoldHelper.COLUMN_PERSONAL_ID, fold.getPersonalId());

        int result = sqLiteDatabase.update(FoldHelper.TABLE_NAME, contentValues,
                FoldHelper.COLUMN_ID + "=?", new String[]{String.valueOf(foldId)});
        sqLiteDatabase.close();

        return result > 0;
    }

    public boolean delete(Long id) {
        SQLiteDatabase sqLiteDatabase = databaseManager.getWritableDatabase();
        if (sqLiteDatabase == null) return false;

        int result = sqLiteDatabase.delete(FoldHelper.TABLE_NAME, FoldHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        sqLiteDatabase.close();
        return result > 0;
    }


    public List<FoldModel> getAll(Long personalId) {
        String query = "SELECT * FROM " + FoldHelper.TABLE_NAME + " WHERE " + FoldHelper.COLUMN_PERSONAL_ID + "=?;";
        SQLiteDatabase sqLiteDatabase = databaseManager.getReadableDatabase();

        List<FoldModel> foldList = new ArrayList<>();

        if (sqLiteDatabase != null) {
            Cursor cursor = sqLiteDatabase.rawQuery(query,  new String[]{String.valueOf(personalId)});

            if (cursor.moveToFirst()) {
                do {
                    FoldModel fold = new FoldModel(cursor.getLong(0), cursor.getString(1),
                            cursor.getInt(2), cursor.getLong(3));
                    foldList.add(fold);
                } while (cursor.moveToNext());
            }
        }

        return foldList;
    }

}
