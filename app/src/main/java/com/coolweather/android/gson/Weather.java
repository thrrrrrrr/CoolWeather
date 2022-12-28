package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {

    public String cityName;

    @SerializedName("code")
    public String statusCode;

    public String updateTime;

    public Now now;

    @Override
    public String toString() {
        return "Weather{" +
                "cityName='" + cityName + '\'' +
                ", statusCode='" + statusCode + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", now=" + now +
                '}';
    }
}
