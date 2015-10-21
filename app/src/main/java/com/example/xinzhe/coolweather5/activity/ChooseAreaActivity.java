package com.example.xinzhe.coolweather5.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.SettingInjectorService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


public class ChooseAreaActivity extends Activity {

    ProgressDialog progressDialog;
    List<Area>  areaList=new ArrayList<>();
    Stack<String> nameStack=new Stack<>();

    Map<String,String> nameMap=new HashMap<>();

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

    Boolean fromWeatherInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fromWeatherInfo=getIntent().getBooleanExtra("from_weather_activity", false);//如果没取到则默认返回false
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        if((fromWeatherInfo==false)&&prefs.getBoolean("city_selected",false)){//如果不是为了重新选择地点，并且没你有诶设置过地点就默认为false
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

                nameMap.put(selectedArea.getCode(), selectedArea.getName());


                //nameStack.push(selectedArea.getName());//名字入栈
                query(selectedArea.getCode());
               // textView.setText(selectedArea.getName());//这时候置位若加载失败会产生错误
            }
        });
        String name="中国";
        textView.setText(name);
        nameMap.put("", "中国");
        //nameStack.push(name);
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


        ArrayList<Area>tempList=(ArrayList<Area>)db.loadList(code);
        /*if(tempList.size()>0){

        }*/
        ///areaList = //这里如果查询失败，areaList被赋值为空表，若访问网络同时失败，再次点击列表则产生空表溢出
    if (tempList.size() > 0) {//tempList不为空在再赋值给原来的
        areaList =tempList;
        dataArray.clear();
        for (Area area : areaList) {
            dataArray.add(area.getName());
        }
        textView.setText(selectedArea.getName());//////
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
            finish();
        }
        if(length>0){
            currentCode=currentCode.substring(0,length-2);
        }

        String currentTitle=nameMap.get(currentCode);
        nameMap.remove(currentCode);//清理否则map会记录所有的地区的代码和姓名，其实这个数据结构用盏最合适可是不会啊
        //nameStack.pop();//把本级别的弹出
        //textView.setText(currentTitle);
        selectedArea.setName(currentTitle);
        query(currentCode);

    }
}
