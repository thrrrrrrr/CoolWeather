package com.coolweather.android.util;

import android.text.TextUtils;
import android.util.Log;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.gson.Air;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Suggestion;
import com.coolweather.android.gson.SuggestionItem;
import com.coolweather.android.gson.Warning;
import com.coolweather.android.gson.Weather;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Utility {
    /**
     * 解析处理服务器返回的省级数据
     */
    public static boolean handleProvinceResponse(String response) {
        if(!TextUtils.isEmpty(response)) {
            try{
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i < allProvinces.length(); i++) {
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save(); //数据存储到数据库中
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析处理服务器返回的市级数据
     */
    public static boolean handleCityResponse(String response, int provinceId) {
        if(!TextUtils.isEmpty(response)) {
            try{
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save(); //数据存储到数据库中
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析处理服务器返回的县级数据
     */
    public static boolean handleCountyResponse(String response, int cityId) {
        if(!TextUtils.isEmpty(response)) {
            try{
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save(); //数据存储到数据库中
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    /**
     * 处理json数据，返回weather对象
     */
    public static Weather handleWeatherResponse(String responseText) {
        try {
            Weather weather =  new Gson().fromJson(responseText, Weather.class);
            return weather;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public static Air handleAirResponse(String responseText) {
        try {
            String response = new JSONObject(responseText).getJSONObject("now").toString();
            return new Gson().fromJson(response, Air.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Warning handleWaringResponse(String responseText) {
        try {
            String response = new JSONObject(responseText).getJSONArray("warning").getJSONObject(0).toString();
            return new Gson().fromJson(response, Warning.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Suggestion handleSuggestionResponse(String responseText) {
        try {
            String response1 = new JSONObject(responseText).getJSONArray("daily").getJSONObject(0).toString();
            String response2 = new JSONObject(responseText).getJSONArray("daily").getJSONObject(1).toString();
            return new Suggestion(new Gson().fromJson(response1, SuggestionItem.class), new Gson().fromJson(response2, SuggestionItem.class));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Forecast> handleForecastResponse(String responseText) {
        try {
            JSONObject jsonObject = new JSONObject(responseText);
            String daily = jsonObject.getJSONArray("daily").toString();
            return new Gson().fromJson(daily, new TypeToken<List<Forecast>>(){}.getType());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
