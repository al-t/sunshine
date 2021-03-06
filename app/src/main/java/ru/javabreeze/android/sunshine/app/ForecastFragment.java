package ru.javabreeze.android.sunshine.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ActionProvider;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import ru.javabreeze.android.sunshine.app.data.WeatherContract;
import ru.javabreeze.android.sunshine.app.service.SunshineService;
import ru.javabreeze.android.sunshine.app.sync.SunshineSyncAdapter;

/**
 * Created by Алексей on 26.05.2016.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = ForecastFragment.class.getSimpleName();

    private static final int FORECAST_LOADER = 0;

    private ForecastAdapter adapter;

    private static final String[] FORECAST_COLUMNS = new String[] {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    private View view;

    private int listItemSelectedPosition = ListView.INVALID_POSITION;
    private final String SELECTED_POSITION_KEY = "selected_position";
    ListView listView;
    private boolean mUseTodayLayout;

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (adapter != null) {
            adapter.setUseTodayLayout(useTodayLayout);
        }
    }

    public ForecastFragment() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if /*(id == R.id.action_refresh) {
            updateWeather();
            return true;
        } else if*/ (id == R.id.action_settings) {
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_show_location_on_map) {

            openPreferredLocationInMap();


        }
        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap() {
        if (adapter != null) {
            Cursor cursor = adapter.getCursor();
            if (cursor != null) {
                cursor.moveToPosition(0);
                String latitude = cursor.getString(COL_COORD_LAT);
                String longitude = cursor.getString(COL_COORD_LONG);

                Uri uri = Uri.parse("geo:" + latitude + "," + longitude);
                Intent showOnMap = new Intent(Intent.ACTION_VIEW, uri);
                PackageManager packageManager = getActivity().getPackageManager();
                List activities = packageManager.queryIntentActivities(showOnMap,
                        PackageManager.MATCH_DEFAULT_ONLY);
                boolean isIntentSafe = activities.size() > 0;
                if (isIntentSafe) {
                    startActivity(showOnMap);
                } else {
                    Toast.makeText(getContext(), getString(R.string.no_map_application),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main, container, false);

        adapter = new ForecastAdapter(getActivity(), null, 0);
        adapter.setUseTodayLayout(mUseTodayLayout);

        listView = (ListView)view.findViewById(R.id.listview_forecast);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long id) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate
                            (locationSetting, cursor.getLong(COL_WEATHER_DATE));
                    Callback callback = (Callback)getActivity();
                    callback.onItemSelected(uri);
                }
                listItemSelectedPosition = position;
            }
        });
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_POSITION_KEY)) {
            listItemSelectedPosition = savedInstanceState.getInt(SELECTED_POSITION_KEY);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle onState) {
        if (listItemSelectedPosition != ListView.INVALID_POSITION) {
            onState.putInt(SELECTED_POSITION_KEY, listItemSelectedPosition);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        updateWeather();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private String getLocationFromPreferences() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPref.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
    }

    private String getTemperatureUnitsFromPreferences() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPref.getString(getString(R.string.pref_temperature_units_key),
                getString(R.string.pref_temperature_units_default));
    }


    private void updateWeather() {

        /*Intent intent = new Intent(getActivity(), SunshineService.class);
        intent.putExtra(SunshineService.LOCATION_QUERY_EXTRA,
                Utility.getPreferredLocation(getActivity()));
        getActivity().startService(intent);*/

//        Intent alarmIntent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
//        alarmIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, Utility.getPreferredLocation
//                (getActivity()));
//
//        //Wrap in a pending intent which only fires once.
//        PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, alarmIntent,
//                PendingIntent.FLAG_ONE_SHOT);
//
//        AlarmManager am = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
//
//        //Set the AlarmManager to wake up the system.
//        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pi);
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order: Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting,
                System.currentTimeMillis());

        return new CursorLoader(
          getActivity(),
                uri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        if (listItemSelectedPosition != ListView.INVALID_POSITION) {
            listView.smoothScrollToPosition(listItemSelectedPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    public void onSettingsChanged() {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }
}
