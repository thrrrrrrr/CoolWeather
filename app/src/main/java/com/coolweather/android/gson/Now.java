package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Now {

    public String obsTime;

    public String temp;

    public String feelsLike;

    public String text;

    public String windDir;

    public String windScale;

    public String humidity;

    @Override
    public String toString() {
        return "Now{" +
                "obsTime='" + obsTime + '\'' +
                ", temp='" + temp + '\'' +
                ", feelsLike='" + feelsLike + '\'' +
                ", text='" + text + '\'' +
                ", windDir='" + windDir + '\'' +
                ", windScale='" + windScale + '\'' +
                ", humidity='" + humidity + '\'' +
                '}';
    }
}
