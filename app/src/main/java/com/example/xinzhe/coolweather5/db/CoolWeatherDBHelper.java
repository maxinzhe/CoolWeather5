package com.example.xinzhe.coolweather5.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLOutput;

/**
 * Created by Xinzhe on 2015/10/15.
 */
public class CoolWeatherDBHelper extends SQLiteOpenHelper {
    public final String PROVINCE_TABLE_NAME="Province";
    public final String CITY_TABLE_NAME="City";
    public final String COUNTY_TABLE_NAME="County";

    private final String CREATE_DB_HEAD="create table " ;



    private final String CREATE_DB_TAIL=
            "(id integer primary key autoincrement," +
            "code text," +
            "name text," +
            "parent_code)";//待测试""
    public CoolWeatherDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
         db.execSQL(CREATE_DB_HEAD+"Province"+CREATE_DB_TAIL);
        db.execSQL(CREATE_DB_HEAD+"City"+CREATE_DB_TAIL);
        db.execSQL(CREATE_DB_HEAD+"County"+CREATE_DB_TAIL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
