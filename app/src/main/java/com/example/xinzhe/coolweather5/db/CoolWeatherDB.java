package com.example.xinzhe.coolweather5.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.xinzhe.coolweather5.model.Area;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xinzhe on 2015/10/15.
 */
public class CoolWeatherDB {
    private static CoolWeatherDB coolWeatherDB;
    private final int VERSION=1;
    private SQLiteDatabase db;
    private CoolWeatherDBHelper coolWeatherDBHelper;
    public synchronized static CoolWeatherDB getInstance(Context context){
        if (coolWeatherDB==null){
            coolWeatherDB=new CoolWeatherDB(context);
        }
        return coolWeatherDB;
    }
    private CoolWeatherDB(Context context) {
        coolWeatherDBHelper=new CoolWeatherDBHelper(context,"CoolWeatherDB",null,VERSION);
        db=coolWeatherDBHelper.getWritableDatabase();
    }

    public List<Area> loadList(String parentCode){//把code代表的地区的子地区加载到areaList中
        ArrayList<Area> areaList=new ArrayList<>();
        Cursor cursor;
         int  currentLevel=0;
         final int PROVINCE_LEVEL=0;
         final int CITY_LEVEL=1;
         final int COUNTY_LEVEL=2;

        String tableName="Province";
        int codeLength=parentCode.length();
        switch (codeLength){
            case 0:
                currentLevel=PROVINCE_LEVEL;
                tableName="Province";
                cursor=db.query(coolWeatherDBHelper.PROVINCE_TABLE_NAME,null,null,null,null,null,null);
                break;
            case 2:
                currentLevel=CITY_LEVEL;
                tableName="City";
                cursor=db.query(coolWeatherDBHelper.CITY_TABLE_NAME,null,"parent_code=?",new String[]{String.valueOf(parentCode)},null,null,null);
                break;
            case 4:
                currentLevel=COUNTY_LEVEL;
                tableName="County";
                cursor=db.query(coolWeatherDBHelper.COUNTY_TABLE_NAME,null,"parent_code=?",new String[]{String.valueOf(parentCode)},null,null,null);

                break;
            default://空指针按省份处理
                currentLevel=PROVINCE_LEVEL;
                tableName="Province";
                cursor=db.query(coolWeatherDBHelper.PROVINCE_TABLE_NAME,null,null,null,null, null, null);

        }
        if(cursor.moveToFirst()) {
            areaList.clear();//这里面三种结构公用一个容器，所以需要先清空再添加
            do {
                Area area = new Area();
                area.setCode(cursor.getString(cursor.getColumnIndex("code")));
                area.setName(cursor.getString(cursor.getColumnIndex("name")));
                area.setParentCode(cursor.getString(cursor.getColumnIndex("parent_code")));
                areaList.add(area);

            }while(cursor.moveToNext());
            cursor.close();

        }
        return areaList ;
    }

    public void save(Area area,String code) {
        String tableName = "Province";
        ContentValues values=new ContentValues();
        values.put("code",area.getCode());
        values.put("name",area.getName());
        values.put("parent_code",area.getParentCode());
        switch (code.length()){
            case 2:
                tableName="City";//逻辑错误。这时候的code是parentArea的code
                break;
            case 4:
                tableName="County";
                break;
            case 6:
                tableName="";
                break;
        }
        db.insert(tableName,null,values);
    }
}
