package com.example.antismartphoneaddictionapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.antismartphoneaddictionapp.Models.LocalAppModel;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "anti_smart_phone";
    private static final String TABLE_APPS = "apps";

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "packageName";
    private static final String KEY_DATE_TIME = "dateTime";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_APP_TABLE = "CREATE TABLE " + TABLE_APPS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_DATE_TIME + " TEXT" + ")";


        db.execSQL(CREATE_APP_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPS);
        onCreate(db);
    }

    void addApp(LocalAppModel appModel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, appModel.getPackageName());
        values.put(KEY_DATE_TIME, appModel.getDateTime());


        db.insert(TABLE_APPS, null, values);
        db.close();
    }

    public ArrayList<LocalAppModel> getAllApps() {
        ArrayList<LocalAppModel> appModels = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_APPS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                LocalAppModel appModel = new LocalAppModel();
                appModel.setId(Integer.parseInt(cursor.getString(0)));
                appModel.setPackageName(cursor.getString(1));
                appModel.setDateTime(cursor.getString(2));
                appModels.add(appModel);
            } while (cursor.moveToNext());
        }
        return appModels;
    }

    public int updateApp(LocalAppModel appModel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, appModel.getPackageName());
        values.put(KEY_DATE_TIME, appModel.getDateTime());

        return db.update(TABLE_APPS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(appModel.getId())});
    }

    public void deleteApp(LocalAppModel appModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_APPS, KEY_ID + " = ?",
                new String[]{String.valueOf(appModel.getId())});
        db.close();
    }


}
