package com.ambow.weatherdemo.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ambow.weatherdemo.R;
import com.ambow.weatherdemo.service.AutoUpdateService;
import com.ambow.weatherdemo.util.HttpUtil;
import com.ambow.weatherdemo.util.Utility;

import butterknife.BindView;
import butterknife.ButterKnife;


public class WeatherActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.city_name)
    TextView cityName;
    @BindView(R.id.publish_text)
    TextView publishText;
    @BindView(R.id.current_date)
    TextView currentDate;
    @BindView(R.id.weather_desp)
    TextView weatherDesp;
    @BindView(R.id.temp1)
    TextView temp1;
    @BindView(R.id.temp2)
    TextView temp2;
    @BindView(R.id.weather_info_layout)
    LinearLayout weatherInfoLayout;
    @BindView(R.id.switch_city)
    Button switchCity;
    @BindView(R.id.update_weather)
    Button updateWeather;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);
        //绑定控件
        ButterKnife.bind(this);
        switchCity.setOnClickListener(this);
        updateWeather.setOnClickListener(this);

        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)) {
            //有县级代号就查询天气
            publishText.setText("同步中。。。");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityName.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        } else {
            //没有就直接显示本地天气
            showWeather();

        }
    }

    //查询县级代号对应的天气代号
    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryFromServer(address, "countyCode");
    }

    //查询天气代号所对应的天气
    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryFromServer(address, "weatherCode");
    }

    //跟据传入的地址和类型向服务器查询天气代号和天气信息
    private void queryFromServer(final String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpUtil.HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        //服务器返回的数据解析出天气代号
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                } else if ("weatherCode".equals(type)) {
                    //处理服务器返回的天气信息
                    Utility.handleWeatherResponse(WeatherActivity.this, response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败。。。");
                    }
                });
            }
        });
    }

    //文件中读取存储到的天气信息，并显示到界面上
    private void showWeather() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        cityName.setText(preferences.getString("city_name", ""));
        temp1.setText(preferences.getString("temp1", ""));
        temp2.setText(preferences.getString("temp2", ""));
        weatherDesp.setText(preferences.getString("weather_desp", ""));
        publishText.setText("今天" + preferences.getString("publish_time", "") + "发布");
        currentDate.setText(preferences.getString("current_date", ""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityName.setVisibility(View.VISIBLE);
        //激活服务，保证后台运行
        Intent intent=new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.switch_city:
                Intent intent=new Intent(this,ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity",true);
                startActivity(intent);
                finish();
                break;
            case R.id.update_weather:
                publishText.setText("同步中。。。");
                SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode=preferences.getString("weather_code","");
                if (!TextUtils.isEmpty(weatherCode)){
                    queryWeatherInfo(weatherCode);
                }
                break;
            default:
                break;
        }
    }
}
