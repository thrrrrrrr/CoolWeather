package com.coolweather.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Air;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Suggestion;
import com.coolweather.android.gson.Warning;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private static final String TAG = "WeatherActivity";

    private String weatherId;
    private String cityName;

    private ImageView bingPicImg;

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView warningText;
    private TextView suggestionText1;
    private TextView suggestionText2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >=21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        //初始化各个控件

        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        warningText = (TextView) findViewById(R.id.warning_text);
        suggestionText1 = (TextView) findViewById(R.id.suggestion_text1);
        suggestionText2 = (TextView) findViewById(R.id.suggestion_text2);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        String airString = prefs.getString("air", null);
        String WarningString = prefs.getString("warning", null);
        String SuggestionString = prefs.getString("suggestion", null);
        String forecastString = prefs.getString("forecasts", null);



        if (weatherString != null) {
            Log.d(TAG, "onCreate: 存储非空");
            loadBingPic();
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weather.cityName = prefs.getString("city_name", null);
            Air air = Utility.handleAirResponse(airString);
            Warning warning = Utility.handleWaringResponse(WarningString);
            Suggestion suggestion = Utility.handleSuggestionResponse(SuggestionString);
            List<Forecast> forecasts = Utility.handleForecastResponse(forecastString);

            showWeatherInfo(weather);
            showAirInfo(air);
            showWarningInfo(warning);
            showSuggestionInfo(suggestion);
            showForecastInfo(forecasts);

        } else {
            loadBingPic();
            weatherLayout.setVisibility(View.INVISIBLE);
            weatherId = getIntent().getStringExtra("weather_id").substring(2);
            cityName = getIntent().getStringExtra("city_name");
            requestWeather(weatherId, cityName);
            requestAir(weatherId);
            requestWarning(weatherId);
            requestSuggestion(weatherId);
            requestForecasts(weatherId);


            Log.d(TAG, "showWeatherInfo: 调用展示信息");
            weatherLayout.setVisibility(View.VISIBLE);


        }

    }

    private void loadBingPic() {
        String url = "https://www.bing.com/th?id=OHR.StorrRocks_ZH-CN4956679462_1920x1080.jpg";
        Glide.with(WeatherActivity.this).load(url).into(bingPicImg);
        Log.d(TAG, "loadBingPic: 背景图片加载");
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
                        Toast.makeText(WeatherActivity.this, "请检查网络", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(WeatherActivity.this, "请检查网络", Toast.LENGTH_SHORT).show();
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
        String cityName = weather.cityName;
        String updateTime = weather.updateTime;
        String obsTime = weather.now.obsTime;
        String temp = weather.now.temp;
        String feelsLike = weather.now.feelsLike;
        String text = weather.now.text;
        String windDir = weather.now.windDir;
        String windScale = weather.now.windScale;
        String humidity = weather.now.humidity;

        titleCity.setText(cityName);
        titleUpdateTime.setText((obsTime.substring(0, obsTime.indexOf("+")).replace("T", " ")));
        degreeText.setText(temp);
        weatherInfoText.setText(text);

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
                            Log.d(TAG, "run: 请求空气成功但是json解析中失败" + air);
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
                            Log.d(TAG, "run: 请求警告成功但是json解析中失败" + warning);
                        }
                    }
                });
            }
        });
    }

    private void showWarningInfo(Warning warning) {
        if (warning.text == null || warning.text == "") {
            warningText.setVisibility(View.GONE);
        }
        warningText.setText("灾害预警：" + warning.text);
    }

    private void requestSuggestion(String weatherId) {
        String weatherUrl = "https://devapi.qweather.com/v7/indices/1d?type=1,15&location="+weatherId+"&key=c630d1ed6b5d4c9499c67325b39a34ee";
        Log.d(TAG, "requestWeather: 请求建议信息" + weatherUrl);
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: 网络请求的时候失败");
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                Suggestion suggestion = Utility.handleSuggestionResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (suggestion != null) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("suggestion", responseText);
                            editor.apply();
                            showSuggestionInfo(suggestion);
                        } else {
                            Log.d(TAG, "run: 请求建议成功但是json解析中失败" + suggestion);
                        }
                    }
                });
            }
        });
    }

    private void showSuggestionInfo(Suggestion suggestion) {
        if (suggestion.suggestion1.text == null || suggestion.suggestion1.text == "") {
            suggestionText1.setVisibility(View.GONE);
        }
        if (suggestion.suggestion2.text == null || suggestion.suggestion2.text == "") {
            suggestionText1.setVisibility(View.GONE);
        }
        suggestionText1.setText("运动建议：" + suggestion.suggestion1.text);
        suggestionText2.setText("交通建议：" + suggestion.suggestion2.text);
    }

    private void requestForecasts(String weatherId) {
        String weatherUrl = "https://devapi.qweather.com/v7/weather/7d?location="+weatherId+"&key=c630d1ed6b5d4c9499c67325b39a34ee";
        Log.d(TAG, "requestWeather: 请求预报信息" + weatherUrl);
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: 网络请求的时候失败");
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                List<Forecast> forecasts = Utility.handleForecastResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (forecasts != null) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("forecasts", responseText);
                            editor.apply();
                            showForecastInfo(forecasts);
                        } else {
                            Log.d(TAG, "run: 请求预报成功但是json解析中失败" + forecasts);
                        }
                    }
                });
            }
        });
    }

    private void showForecastInfo(List<Forecast> forecasts) {
        forecastLayout.removeAllViews();
        //天气预报，循环遍历
        for (Forecast forecast : forecasts) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecase_item, forecastLayout, false);
            TextView dataText = (TextView) view.findViewById(R.id.data_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);

            dataText.setText(forecast.fxDate);
            if (forecast.textDay.equals(forecast.textNight)) {
                infoText.setText(forecast.textDay);
            } else {
                infoText.setText(forecast.textDay+"转"+forecast.textNight);
            }
            maxText.setText(forecast.tempMax);
            minText.setText(forecast.tempMin);
            forecastLayout.addView(view);
        }


    }
}

















