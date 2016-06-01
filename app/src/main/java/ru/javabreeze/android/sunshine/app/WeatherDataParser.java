package ru.javabreeze.android.sunshine.app;

/**
 * Created by Алексей on 31.05.2016.
 */
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherDataParser {

    public static int getTime(String weatherJsonStr, int dayIndex) throws JSONException {
        return new JSONObject(weatherJsonStr).getJSONArray("list").getJSONObject(dayIndex)
                .getInt("dt");
    }

    public static double getMaxTemperatureForDay(String weatherJsonStr, int dayIndex)
            throws JSONException {
        return new JSONObject(weatherJsonStr).getJSONArray("list").getJSONObject(dayIndex)
                .getJSONObject("temp").getDouble("max");
    }

    public static double getMinTemperatureForDay(String weatherJsonStr, int dayIndex)
            throws JSONException {
        return new JSONObject(weatherJsonStr).getJSONArray("list").getJSONObject(dayIndex)
                .getJSONObject("temp").getDouble("min");
    }

    public static String getWeatherConditionForDay(String weatherJsonStr, int dayIndex)
            throws JSONException {
        return new JSONObject(weatherJsonStr).getJSONArray("list").getJSONObject(dayIndex)
                .getJSONArray("weather").getJSONObject(0).getString("main");
    }

}
