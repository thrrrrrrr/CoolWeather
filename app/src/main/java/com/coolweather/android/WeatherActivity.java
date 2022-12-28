package com.coolweather.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.textservice.SuggestionsInfo;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.gson.Air;
import com.coolweather.android.gson.Suggestion;
import com.coolweather.android.gson.Warning;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private static final String TAG = "WeatherActivity";

    private String cityName = "未知城市";
    private String weatherId;

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView warningText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        //初始化各个控件

        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        warningText = (TextView) findViewById(R.id.warning_text);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather2", null);
        String airString = prefs.getString("air2", null);
        String WarningString = prefs.getString("warning2", null);
        String SuggestionString = prefs.getString("suggestion2", null);


        if (weatherString != null) {
            Log.d(TAG, "onCreate: 非空");
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weather.cityName = prefs.getString("city_name", "从存储中没找到城市名");
            Air air = Utility.handleAirResponse(airString);
            Warning warning = Utility.handleWaringResponse(WarningString);
            Suggestion suggestion = Utility.handleSuggestionResponse(SuggestionString);

            showWeatherInfo(weather);
            showAirInfo(air);
            showWarningInfo(warning);
            showSuggestion(suggestion);
//            showForecastInfo(forecast);

        } else {
            weatherLayout.setVisibility(View.INVISIBLE);
            weatherId = getIntent().getStringExtra("weather_id").substring(2);
            cityName = getIntent().getStringExtra("city_name");
            requestWeather(weatherId, cityName);
            requestAir(weatherId);
            requestWarning(weatherId);
            requestSuggestion(weatherId);
//            requestForecast(weatherId);


            Log.d(TAG, "showWeatherInfo: 调用展示信息");
            weatherLayout.setVisibility(View.VISIBLE);


        }

    }




    /**
     * 根据天气id请求城市天气信息
     */
    public void requestWeather(final String weatherId, final String cityName) {
        String weatherUrl = "https://devapi.qweather.com/v7/weather/now?location="+weatherId+"&key=c630d1ed6b5d4c9499c67325b39a34ee";
        Log.d(TAG, "requestWeather: 请求基本天气信息" + weatherUrl);
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "run: 网络请求的时候失败");
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "200".equals(weather.statusCode)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.putString("city_name", cityName);
                            editor.apply();
                            weather.cityName = cityName;
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "run: 请求成功但是json解析中失败" + weather);
                        }
                    }
                });
            }
        });
    }

    /**
     * 处理Weather实体类中的数据
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = this.cityName;
        String updateTime = weather.updateTime;
        String obsTime = weather.now.obsTime;
        String temp = weather.now.temp;
        String feelsLike = weather.now.feelsLike;
        String text = weather.now.text;
        String windDir = weather.now.windDir;
        String windScale = weather.now.windScale;
        String humidity = weather.now.humidity;

        titleCity.setText(cityName);
        titleUpdateTime.setText(obsTime);
        degreeText.setText(temp);
        weatherInfoText.setText(text);

        forecastLayout.removeAllViews();
        //天气预报，循环遍历
        View view = LayoutInflater.from(this).inflate(R.layout.forecase_item, forecastLayout, false);
        TextView dataText = (TextView) view.findViewById(R.id.data_text);
        TextView infoText = (TextView) view.findViewById(R.id.info_text);
        TextView maxText = (TextView) view.findViewById(R.id.max_text);
        TextView minText = (TextView) view.findViewById(R.id.min_text);
        dataText.setText("20200101");
        infoText.setText("晴");
        maxText.setText("10");
        minText.setText("1");
        forecastLayout.addView(view);

        //生活建议

    }

    /**
     * 请求空气信息
     * @param weatherId
     */
    public void requestAir(final String weatherId) {
        String weatherUrl = "https://devapi.qweather.com/v7/air/now?location="+weatherId+"&key=c630d1ed6b5d4c9499c67325b39a34ee";
        Log.d(TAG, "requestWeather: 请求基本空气信息" + weatherUrl);
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取空气信息失败", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "run: 网络请求的时候失败");
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                Air air = Utility.handleAirResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (air != null) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("air", responseText);
                            editor.apply();
                            showAirInfo(air);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取空气信息失败", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "run: 请求成功但是json解析中失败" + air);
                        }
                    }
                });
            }
        });
    }

    /**
     * 将空气信息上界面
     * @param air
     */
    private void showAirInfo(@NonNull Air air) {
        aqiText.setText(air.aqi);
        pm25Text.setText(air.pm2p5);
    }

    private void requestWarning(String weatherId) {
        String weatherUrl = "https://devapi.qweather.com/v7/warning/now?location="+weatherId+"&key=c630d1ed6b5d4c9499c67325b39a34ee";
        Log.d(TAG, "requestWeather: 请求警告信息" + weatherUrl);
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取警告信息失败", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "run: 网络请求的时候失败");
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                Warning warning = Utility.handleWaringResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (warning != null) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("warning", responseText);
                            editor.apply();
                            showWarningInfo(warning);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取警告信息失败", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "run: 请求成功但是json解析中失败" + warning);
                        }
                    }
                });
            }
        });
    }

    private void showWarningInfo(Warning warning) {
        warningText.setText(warning.text);
        if (warning.text == null || warning.text == "") {
            warningText.setVisibility(View.GONE);
        }
    }

    private void requestSuggestion(String weatherId) {
        String weatherUrl = "https://devapi.qweather.com/v7/indices/1d?type=0&location="+weatherId+"&key=c630d1ed6b5d4c9499c67325b39a34ee";
        Log.d(TAG, "requestWeather: 请求警告信息" + weatherUrl);
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取警告信息失败", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "run: 网络请求的时候失败");
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                Warning warning = Utility.handleWaringResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (warning != null) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("warning", responseText);
                            editor.apply();
                            showWarningInfo(warning);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取警告信息失败", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "run: 请求成功但是json解析中失败" + warning);
                        }
                    }
                });
            }
        });
    }
}

















