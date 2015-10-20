package com.example.xinzhe.coolweather5.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Xinzhe on 2015/10/15.
 */
public class HttpUtil {

    private String address;

    public HttpUtil(String code) {
        switch (code.length()) {
            case 9:
                address="http://www.weather.com.cn/adat/cityinfo/" +
                        code +
                        ".html";
                break;
            default:
                address = "http://www.weather.com.cn/data/list3/city" +
                    code +
                    ".xml";
        }
    }

    public void sendHttpRequest(final HttpRequestListener listener){

        new Thread(new Runnable() {
            HttpURLConnection connection=null;
            @Override
            public void run() {
                try{
                    StringBuilder response =new StringBuilder();
                    String temp;
                    URL url=new URL(address);
                    connection=(HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream is=connection.getInputStream();
                    BufferedReader br=new BufferedReader(new InputStreamReader(is));
                    while((temp=br.readLine())!=null){
                        response.append(temp);
                    }
                   if (listener!=null){
                        listener.onFinish(response.toString());
                    }
                }catch (Exception e){
                        if(listener!=null){
                            listener.onError(e);
                        }
                }finally {
                    if(connection!=null){
                        connection.disconnect();
                    }
                }
            }
        }).start();

    }
}
