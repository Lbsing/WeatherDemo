package com.ambow.weatherdemo.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.ambow.weatherdemo.db.CoolWeatherDB;
import com.ambow.weatherdemo.model.City;
import com.ambow.weatherdemo.model.County;
import com.ambow.weatherdemo.model.Province;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Administrator on 2017/7/26.
 */

public class Utility {
    //解析处理服务器返回的省级数据
    public synchronized static boolean handleProvinceResponse(CoolWeatherDB coolWeatherDB,String response){
          if (!TextUtils.isEmpty((response))){
              String[] allProvince =response.split(",");
              if (allProvince!=null && allProvince.length>0){
                  for (String p:allProvince){
                      String[] array=p.split("\\|");
                      Province province=new Province();
                      province.setProvinceCode(array[0]);
                      province.setProvinceName(array[1]);
                      //解析出的数据存储到Province中
                      coolWeatherDB.saveProvince(province);
                  }
                  return true;
              }
          }
        return false;
    }
    //解析处理服务器返回的市级数据
    public synchronized static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB,String response,int provinceId){
        if (!TextUtils.isEmpty(response)){
            String[] allCities =response.split(",");
            if (allCities !=null && allCities.length>0){
                for (String c:allCities){
                    String [] array=c.split("\\|");
                    City city=new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    //解析出的市级数据存储到City表
                    coolWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }
    //解析处理服务器返回的县级数据
    public synchronized  static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB,String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            String[] allCounties=response.split(",");
            if (allCounties!=null && allCounties.length>0){
                for (String c: allCounties){
                    String[] array=c.split("\\|");
                    County county=new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    //解析后的数据添加到County表中
                    coolWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }
    //解析服务器返回的JSON数据，解析后的数据存储到本地
    public static void handleWeatherResponse(Context context,String response){
        try{
            JSONObject jsonObject=new JSONObject(response);
            JSONObject weatherInfo=jsonObject.getJSONObject("weatherinfo");
            String cityName=weatherInfo.getString("city");
            String weatherCode=weatherInfo.getString("cityid");
            String temp1=weatherInfo.getString("temp1");
            String temp2=weatherInfo.getString("temp2");
            String weatherDesp=weatherInfo.getString("weather");
            String publishTime=weatherInfo.getString("ptime");
            saveWeatherInfo(context,cityName,weatherCode,temp1,temp2,weatherDesp,publishTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //将服务器返回的数据存储到本地文件中
    public static void saveWeatherInfo(Context context, String cityName, String weatherCode, String temp1, String temp2, String weatherDesp, String publishTime) {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy年M月d日",Locale.CHINA);
        SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected",true);
        editor.putString("city_name",cityName);
        editor.putString("weather_code",weatherCode);
        editor.putString("temp1",temp1);
        editor.putString("temp2",temp2);
        editor.putString("weather_desp",weatherDesp);
        editor.putString("publish_time",publishTime);
        editor.putString("current_date", sdf.format(new Date()));
        editor.commit();
    }
}
