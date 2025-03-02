package com.matheus.academy.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.matheus.academy.R;
import com.matheus.academy.database.helpers.ExerciseHelper;
import com.matheus.academy.database.helpers.FoldHelper;
import com.matheus.academy.database.helpers.HistoryHelper;
import com.matheus.academy.database.helpers.MeasureHelper;
import com.matheus.academy.database.helpers.PersonalHelper;
import com.matheus.academy.database.helpers.SerieHelper;
import com.matheus.academy.database.helpers.WorkoutHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class DatabaseManager extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Academy.db";
    public static final int DATABASE_VERSION = 5;
    
    private Context context;

    public DatabaseManager(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(WorkoutHelper.CREATE_TABLE);
        sqLiteDatabase.execSQL(SerieHelper.CREATE_TABLE);
        sqLiteDatabase.execSQL(ExerciseHelper.CREATE_TABLE);
        sqLiteDatabase.execSQL(PersonalHelper.CREATE_TABLE);
        sqLiteDatabase.execSQL(FoldHelper.CREATE_TABLE);
        sqLiteDatabase.execSQL(MeasureHelper.CREATE_TABLE);
        sqLiteDatabase.execSQL(HistoryHelper.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WorkoutHelper.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SerieHelper.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ExerciseHelper.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PersonalHelper.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FoldHelper.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MeasureHelper.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + HistoryHelper.TABLE_NAME);
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
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + HistoryHelper.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void backup(String outFileName) {

        final String inFileName = context.getDatabasePath(DATABASE_NAME).toString();

        try {
            File dbFile = new File(inFileName);
            FileInputStream fis = new FileInputStream(dbFile);

            File backupFile = new File(Environment.getExternalStorageDirectory() + File.separator + context.getResources().getString(R.string.app_name), outFileName);
            OutputStream output = new FileOutputStream(backupFile);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            
            output.flush();
            output.close();
            fis.close();

            Toast.makeText(context, "Backup Completed", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(context, "Unable to backup database. Retry", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void importDB(String inFileName) {

        final String outFileName = context.getDatabasePath(DATABASE_NAME).toString();

        try {
            File dbFile = new File(inFileName);
            FileInputStream fis = new FileInputStream(dbFile);

            OutputStream output = new FileOutputStream(outFileName);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            output.flush();
            output.close();
            fis.close();

            Toast.makeText(context, "Import Completed", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Unable to import database. Retry", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
