package ru.javabreeze.android.sunshine.app;

/**
 * Created by Алексей on 18.12.2016.
 * Helper class with useful functions
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

class Utility {
    private final static String LOG_TAG = Utility.class.getSimpleName();
    static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_temperature_units_key),
                context.getString(R.string.pref_temperature_units_default))
                .equals(context.getResources().getStringArray(R.array.temperature_units_names)[0]);
    }

    static String formatTemperature(Context context, double temperature, boolean isMetric) {
        double temp;
        if (!isMetric) {
            temp = 9 * temperature / 5 + 32;
        } else {
            temp = temperature;
        }
        return context.getString(R.string.temperature_format, temp);
    }

    static String formatDate(long dateInMillis, Context context) {
        Date date = new Date(dateInMillis);
        Date today = new Date();
        long diff = Math.abs(today.getTime() - date.getTime());
        long dayDiff = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        if (Constants.DEBUG) Log.v(LOG_TAG, "dayDiff: " + dayDiff);
        String resultDate;
        if (DateUtils.isToday(date.getTime())) {
            resultDate = context.getString(R.string.today) + ", " + new SimpleDateFormat("MMMM " +
                    "d", Locale.getDefault()).format(date);
        } else if (dayDiff < 6) {
            resultDate = new SimpleDateFormat("EEEE", Locale.getDefault()).format(date);
        } else {
            resultDate = new SimpleDateFormat("MMM d", Locale.getDefault()).format(date);
        }
        return resultDate;
    }

    static String formatDateForDetailActivity(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return new SimpleDateFormat("MMMM d", Locale.getDefault()).format(date);
    }

    static String getDayOfWeek(long dateInMillis, Context context) {
        Date date = new Date(dateInMillis);
        String resultDate;
        if (DateUtils.isToday(date.getTime())) {
            resultDate = context.getString(R.string.today);
        } else {
            resultDate = new SimpleDateFormat("EEEE", Locale.getDefault()).format(date);
        }
        return resultDate;
    }

    static String formatWindDirection(Float degrees) {
        String directions[] = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        return directions[ (int)Math.round((  ((double)degrees % 360) / 45)) % 8 ];
    }
}
