package com.coolweather.android.gson;

public class Forecast {

    public String fxDate;

    public String tempMax;

    public String tempMin;

    public String textDay;

    public String textNight;

    @Override
    public String toString() {
        return "Forecast{" +
                "fxDate='" + fxDate + '\'' +
                ", tempMax='" + tempMax + '\'' +
                ", tempMin='" + tempMin + '\'' +
                ", textDay='" + textDay + '\'' +
                ", textNight='" + textNight + '\'' +
                '}';
    }
}
