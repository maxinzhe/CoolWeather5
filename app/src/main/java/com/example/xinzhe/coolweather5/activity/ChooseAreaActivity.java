package com.example.xinzhe.coolweather5.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xinzhe.coolweather5.R;
import com.example.xinzhe.coolweather5.db.CoolWeatherDB;
import com.example.xinzhe.coolweather5.model.Area;

import com.example.xinzhe.coolweather5.util.HttpRequestListener;
import com.example.xinzhe.coolweather5.util.HttpUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class ChooseAreaActivity extends Activity {

    ProgressDialog progressDialog;
    List<Area>  areaList=new ArrayList<>();
    Stack<String> nameStack=new Stack<>();


    CoolWeatherDB db;

    ArrayAdapter adapter;
    ArrayList <String> dataArray=new ArrayList<>();

    ListView listView;
    TextView textView;

    private int  currentLevel=0;
    private final int PROVINCE_LEVEL=0;
    private final int CITY_LEVEL=1;
    private final int COUNTY_LEVEL=2;


    private Area selectedArea=new Area();
    String currentCode=null;//用于返回

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("city_selected",false)){//如果没你有诶设置过就默认为false
            Intent  intent=new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_choose_area_acitvity);

        db=CoolWeatherDB.getInstance(this);

        listView=(ListView)findViewById(R.id.listView);
        textView=(TextView)findViewById(R.id.textView);

        adapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,dataArray);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedArea = areaList.get(position);
                currentCode = selectedArea.getCode();
                textView.setText(selectedArea.getName());
                nameStack.push(selectedArea.getName());//名字入栈
                query(selectedArea.getCode());
            }
        });
        String name="中国";
        textView.setText(name);
        nameStack.push(name);
        query("");

    }


    void query(String code) {
        //若为县级的Code则跳转到另一个Activity
        if(code.length()==6){
            Intent intent=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
            intent.putExtra("county_code",code);
            startActivity(intent);
            finish();
        }

        areaList = db.loadList(code);
    if (areaList.size() > 0) {
        dataArray.clear();
        for (Area area : areaList) {
            dataArray.add(area.getName());
        }
        adapter.notifyDataSetChanged();
        listView.setSelection(0);
    } else {
        queryFromServer(code);
    }
}

    void queryFromServer(final String  code){
        startProgressDialog();
        HttpUtil httpUtil=new HttpUtil(code);
        httpUtil.sendHttpRequest(new HttpRequestListener() {
            @Override
            public void onFinish(String response) {
                String[] piece = response.split(",");
                if (piece != null && piece.length > 0) {
                    for (String p : piece) {
                        String[] array = p.split("\\|");
                        {
                            Area area = new Area();
                            area.setCode(array[0]);
                            area.setName(array[1]);
                            area.setParentCode(code);
                            db.save(area, code);
                        }
                    }
                }
                closeProgressDialog();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        query(code);
                    }
                });

            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                Log.e("My",e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
       // query(code);//sendHttpRequest语句中会建立新的线程，使得数据库还没有填充的时候就再次查询，导致再次进行网络加载。最后结果随机产生多个随机的重复对象
    }

    void startProgressDialog(){
        if (progressDialog==null){
            progressDialog=new ProgressDialog(this);
            progressDialog.setMessage("加载中");
            progressDialog.setCanceledOnTouchOutside(false);
        } progressDialog.show();
    }
    void closeProgressDialog(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }

    }

    @Override
    public void onBackPressed() {
        int length=currentCode.length();
        if(length==0){
            super.onBackPressed();
        }
        currentCode=currentCode.substring(0,length-2);
        nameStack.pop();//把本级别的弹出
        textView.setText(nameStack.pop());
        query(currentCode);

    }
}
