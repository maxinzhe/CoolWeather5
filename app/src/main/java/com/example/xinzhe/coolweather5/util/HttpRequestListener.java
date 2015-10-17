package com.example.xinzhe.coolweather5.util;

/**
 * Created by Xinzhe on 2015/10/15.
 */
public interface HttpRequestListener {
    void onFinish(String response);
    void onError(Exception e);
}
