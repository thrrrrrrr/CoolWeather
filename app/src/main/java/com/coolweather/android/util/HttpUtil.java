package com.coolweather.android.util;

import android.util.Log;
import android.view.textclassifier.TextLinks;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpUtil {

    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }

    public static String queryWeatherId(String address) throws IOException, JSONException {
        Log.d("ChooseAreaFragment", "queryWeatherId: 进入查id1");
        OkHttpClient client = new OkHttpClient();
        Log.d("ChooseAreaFragment", "queryWeatherId: 进入查id2");
        Request request = new Request.Builder().url(address).get().build();
        Log.d("ChooseAreaFragment", "queryWeatherId: 进入查id3");
        Call call = client.newCall(request);
        Log.d("ChooseAreaFragment", "queryWeatherId: 进入查id4");
        Response response = call.execute();
        Log.d("ChooseAreaFragment", "queryWeatherId: 进入查id4");
        String responseData = response.body().string();
        Log.d("ChooseAreaFragment", "queryWeatherId: " + responseData);
        return new JSONObject(responseData).getJSONArray("location").getJSONObject(0).getString("id");
    }
}
