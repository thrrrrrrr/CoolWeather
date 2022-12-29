package com.coolweather.android;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {

    private static final String TAG = "ChooseAreaFragment";

    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;
    public static final int LEVEL_SEARCH = 3;

    private Button searchButton;
    private LinearLayout searchTitle;
    private Button getSearchButton;

    private EditText province;
    private EditText city;
    private EditText county;

    private ProgressDialog progressDialog;

    private TextView titleText;

    private Button backButton;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();

    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;

    /**
     * 选中的城市
     */
    private City selectedCity;

    /**
     * 当前选中的级别
     */
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        searchButton = (Button) view.findViewById(R.id.search_button);
        searchTitle = (LinearLayout) view.findViewById(R.id.search_title);
        getSearchButton = (Button) view.findViewById(R.id.get_search);
        province = (EditText) view.findViewById(R.id.province_text);
        city = (EditText) view.findViewById(R.id.city_text);
        county = (EditText) view.findViewById(R.id.county_text);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        queryProvinces();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                    Log.d(TAG, "onItemClick: " + selectedProvince.getProvinceName());
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String countyName = countyList.get(position).getCountyName();
                    String provinceName = selectedProvince.getProvinceName();
                    String address = "https://geoapi.qweather.com/v2/city/lookup?location="+countyName+"&adm="+provinceName+"&key=c630d1ed6b5d4c9499c67325b39a34ee";
                    Log.d(TAG, "onItemClick: 查询WeatherId" + address);
                    HttpUtil.sendOkHttpRequest(address, new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (getActivity() instanceof MainActivity) {
                                        String weatherId = null;
                                        String countyName = null;
                                        try {
                                            JSONObject object = new JSONObject(response.body().string()).getJSONArray("location").getJSONObject(0);
                                            countyName = object.getString("name") + "•" + object.getString("adm2");
                                            weatherId = object.getString("id");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                                        intent.putExtra("weather_id", weatherId);
                                        intent.putExtra("city_name", countyName);
                                        startActivity(intent);
                                        getActivity().finish();
                                    }else if (getActivity() instanceof WeatherActivity) {
                                        String weatherId = null;
                                        String countyName = null;
                                        try {
                                            JSONObject object = new JSONObject(response.body().string()).getJSONArray("location").getJSONObject(0);
                                            countyName = object.getString("name") + "•" + object.getString("adm2");
                                            weatherId = object.getString("id");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        WeatherActivity activity = (WeatherActivity) getActivity();
                                        activity.drawerLayout.closeDrawers();
                                        activity.swipeRefresh.setRefreshing(true);
                                        activity.requestWeather(weatherId, countyName);
                                        activity.requestAir(weatherId);
                                        activity.requestWarning(weatherId);
                                        activity.requestSuggestion(weatherId);
                                        activity.requestForecasts(weatherId);
                                        activity.swipeRefresh.setRefreshing(false);
                                    }
                                }
                            });
                        }
                    });

                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                } else if (currentLevel == LEVEL_SEARCH) {
                    queryProvinces();
                    searchTitle.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    searchButton.setVisibility(View.VISIBLE);
                }
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentLevel = LEVEL_SEARCH;
                titleText.setText("城市搜索");
                backButton.setVisibility(View.VISIBLE);
                searchTitle.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
                searchButton.setVisibility(View.GONE);
            }
        });

        getSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String provinceName = province.getText().toString();
                String cityName = city.getText().toString();
                String countyName = county.getText().toString();
                if (provinceName.equals("") || cityName.equals("") || countyName.equals("")) {
                    Toast mToast = Toast.makeText(getContext(), "", Toast.LENGTH_LONG);
                    mToast.setText("请输入完整信息");
                    mToast.show();
                }
                String address = "https://geoapi.qweather.com/v2/city/lookup?location="+countyName+"&adm="+provinceName+"&key=c630d1ed6b5d4c9499c67325b39a34ee";
                HttpUtil.sendOkHttpRequest(address, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                    }
                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (getActivity() instanceof MainActivity) {
                                    String weatherId = null;
                                    String countyName = null;
                                    try {
                                        JSONObject object = new JSONObject(response.body().string()).getJSONArray("location").getJSONObject(0);
                                        countyName = object.getString("name") + "•" + object.getString("adm2");
                                        weatherId = object.getString("id");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    Intent intent = new Intent(getActivity(), WeatherActivity.class);
                                    intent.putExtra("weather_id", weatherId);
                                    intent.putExtra("city_name", countyName);
                                    startActivity(intent);
                                    getActivity().finish();
                                }else if (getActivity() instanceof WeatherActivity) {
                                    String weatherId = null;
                                    String countyName = null;
                                    try {
                                        JSONObject object = new JSONObject(response.body().string()).getJSONArray("location").getJSONObject(0);
                                        countyName = object.getString("name") + "•" + object.getString("adm2");
                                        weatherId = object.getString("id");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    WeatherActivity activity = (WeatherActivity) getActivity();
                                    activity.drawerLayout.closeDrawers();
                                    activity.swipeRefresh.setRefreshing(true);
                                    activity.requestWeather(weatherId, countyName);
                                    activity.requestAir(weatherId);
                                    activity.requestWarning(weatherId);
                                    activity.requestSuggestion(weatherId);
                                    activity.requestForecasts(weatherId);
                                    activity.swipeRefresh.setRefreshing(false);
                                }
                            }
                        });
                    }
                });
            }
        });

    }

    /**
     * 查询全国所有的省，优先从数据库查询，没有再去服务器上查
     */
    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
//                Log.d(TAG, "queryProvinces: "+province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "https://www.mxnzp.com/api/address/v3/list/province?app_id=ynkzaprwopl9hqri&app_secret=VHJSb2VBeEcvaEJ1T0FwSVpmSTlRZz09";
            queryFromServer(address, "province");
        }
    }

    /**
     * 查询省内所有的市，优先从数据库查询，没有再去服务器上查
     */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?",
                String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "https://www.mxnzp.com/api/address/v3/list/city?provinceCode=" + provinceCode+"&app_id=ynkzaprwopl9hqri&app_secret=VHJSb2VBeEcvaEJ1T0FwSVpmSTlRZz09";
            Log.d(TAG, "queryCities: " + address);
            queryFromServer(address, "city");
        }
    }

    /**
     * 查询全市所有的县，优先从数据库查询，没有再去服务器上查
     */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?",
                String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "https://www.mxnzp.com/api/address/v3/list/area?cityCode="+cityCode+"&provinceCode="+provinceCode+"&&app_id=ynkzaprwopl9hqri&app_secret=VHJSb2VBeEcvaEJ1T0FwSVpmSTlRZz09";
            Log.d(TAG, "queryCounties: " + address);
            queryFromServer(address, "county");
        }
    }

    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Log.d(TAG, "onResponse: " + responseText);
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                } else { //返回结果是空
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            Toast.makeText(getContext(), "网络故障，请使用搜索功能", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "网络故障，请使用搜索功能", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
