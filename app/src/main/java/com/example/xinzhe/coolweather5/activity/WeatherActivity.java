package com.example.xinzhe.coolweather5.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.xinzhe.coolweather5.R;
import com.example.xinzhe.coolweather5.util.HttpRequestListener;
import com.example.xinzhe.coolweather5.util.HttpUtil;
import com.example.xinzhe.coolweather5.util.Utility;

/**
 * Created by Xinzhe on 2015/10/18.
 */
public class WeatherActivity extends Activity {


    private LinearLayout weatherInfoLayout;

    private TextView publishText;
    private TextView cityNameText;
    private TextView temp1Text;
    private TextView temp2Text;
    private TextView weatherDespText;
    private TextView getPublishText;
    private TextView currentDateText;

    private Button homeButton;
    private Button refreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);


         weatherInfoLayout=(LinearLayout)findViewById(R.id.weather_info_layout);

          publishText=(TextView)findViewById(R.id.publish_text);
          cityNameText=(TextView)findViewById(R.id.city_name);
          temp1Text=(TextView)findViewById(R.id.temp1);
          temp2Text=(TextView)findViewById(R.id.temp2);
          weatherDespText=(TextView)findViewById(R.id.weather_desp);
          getPublishText=(TextView)findViewById(R.id.publish_text);
          currentDateText=(TextView)findViewById(R.id.current_date);

        homeButton=(Button)findViewById(R.id.home_button);
        refreshButton=(Button)findViewById(R.id.refresh_button);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(WeatherActivity.this,ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity",true);//用于ChooseAreaActivity界面判定是否为重新选择地区
                startActivity(intent);
                finish();
            }
        });
        refreshButton.setOnClickListener(new View.OnClickListener() {//这里的刷新就是指重新查询
            @Override
            public void onClick(View v) {
                publishText.setText("同步中");
                SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                String weatherCode=prefs.getString("weather_code","");
                if(!TextUtils.isEmpty(weatherCode)){
                    queryWeatherInfo(weatherCode);
                }

            }
        });

        String countyCode=getIntent().getStringExtra("county_code");
        if(!TextUtils.isEmpty(countyCode)){
            publishText.setText("同步中");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        }else{
            //没有省级代号就直接显示天气
            showWeather();
        }
    }
    private void queryWeatherCode(String countyCode){

        queryFromServer(countyCode);
    }

    private void queryWeatherInfo(String infoCode){
        queryFromServer(infoCode);
    }

    private void showWeather(){
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(prefs.getString("city_name",""));
        temp1Text.setText(prefs.getString("temp1",""));
        temp2Text.setText(prefs.getString("temp2",""));
        weatherDespText.setText(prefs.getString("weather_desp",""));
        publishText.setText(prefs.getString("publish_time","")+"发布");
        currentDateText.setText(prefs.getString("current_date",""));

        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
    }
    private void queryFromServer(final String code){

        new HttpUtil(code).sendHttpRequest(new HttpRequestListener() {
            @Override
            public void onFinish(String response) {
                switch (code.length()){
                    case 6 ://处理"区县代码|天气代码"
                        if (!TextUtils.isEmpty(response)){
                            String[] piece=response.split("\\|");
                            if (piece!=null&&piece.length==2){
                                String weatherCode=piece[1];
                                queryWeatherInfo(weatherCode);
                            }
                        }
                    break;
                    case 9://处理天气Json数据
                        Utility.handleWeatherResponse(WeatherActivity.this,response);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showWeather();
                            }
                        });
                    break;

                    default:
                        ;

                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                    }
                });
            }
        });
    }
}
