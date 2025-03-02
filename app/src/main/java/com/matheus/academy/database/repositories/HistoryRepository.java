package com.matheus.academy.database.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.matheus.academy.database.DatabaseManager;
import com.matheus.academy.database.helpers.HistoryHelper;
import com.matheus.academy.models.HistoryModel;

import java.util.ArrayList;
import java.util.List;

public class HistoryRepository {
    private DatabaseManager databaseManager;

    public HistoryRepository(Context context) { databaseManager = new DatabaseManager(context); }

    public long add(String date, Long serieId) {
        SQLiteDatabase sqLiteDatabase = databaseManager.getWritableDatabase();
        if (sqLiteDatabase == null) return -1;

        ContentValues contentValues = new ContentValues();

        contentValues.put(HistoryHelper.COLUMN_DATE, date);
        contentValues.put(HistoryHelper.COLUMN_SERIE_ID, serieId);

        long result = sqLiteDatabase.insert(HistoryHelper.TABLE_NAME, null, contentValues);
        sqLiteDatabase.close();
        return result;
    }

    public List<HistoryModel> getAll() {
        String query = "SELECT * FROM " + HistoryHelper.TABLE_NAME;
        SQLiteDatabase sqLiteDatabase = databaseManager.getReadableDatabase();

        List<HistoryModel> historyList = new ArrayList<>();

        if (sqLiteDatabase != null) {
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    HistoryModel history = new HistoryModel(cursor.getLong(0), cursor.getString(1), cursor.getLong(2));
                    historyList.add(history);
                } while (cursor.moveToNext());
            }
        }

        return historyList;
    }

    public HistoryModel getByDate(String date) {
        String query = "SELECT * FROM " + HistoryHelper.TABLE_NAME + " WHERE " + HistoryHelper.COLUMN_DATE + "=?";
        SQLiteDatabase sqLiteDatabase = databaseManager.getReadableDatabase();

        if (sqLiteDatabase == null) return null;

        Cursor cursor = sqLiteDatabase.rawQuery(query, new String[]{date});
        if (cursor.moveToFirst() && cursor.getCount() > 0) {
            HistoryModel history = new HistoryModel(cursor.getLong(0), cursor.getString(1), cursor.getLong(2));
            return history;
        }
        return null;
    }

}
