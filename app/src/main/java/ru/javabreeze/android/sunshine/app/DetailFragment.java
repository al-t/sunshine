package ru.javabreeze.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ru.javabreeze.android.sunshine.app.data.WeatherContract;

/**
 * Created by Алексей on 18.02.2017.
 * The fragment to show the detailed weather forecast
 */

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private static final String URI_ARG = "URI_ARG";

    private ShareActionProvider mShareActionProvider;

    private String mForecast;

    private static final int DETAIL_LOADER = 0;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE
    };

    // these constants correspond to the projection defined above, and must change if the
    // projection changes
    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;
    private static final int COL_WEATHER_HUMIDITY = 5;
    private static final int COL_WEATHER_WIND_SPEED = 6;
    private static final int COL_WEATHER_WIND_DEGREES = 7;
    private static final int COL_WEATHER_PRESSURE = 8;
    private Uri mUri;


    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle args = getArguments();
        if (args != null) {
            mUri = (Uri)args.get(URI_ARG);
        }

        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_fragment, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_item_share);
        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
            /*if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            } else {
                Log.d(Constants.LOG_TAG, "Share Action Provider is null?");
            }*/
        if (mForecast != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private Intent createShareForecastIntent() {
        return new Intent(Intent.ACTION_SEND)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (Constants.DEBUG) Log.v(LOG_TAG, "In onCreateLoader");

        if (mUri == null) return null;

        return new CursorLoader(
                getActivity(),
                mUri,
                FORECAST_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (Constants.DEBUG) Log.v(LOG_TAG, "In OnLoad Finished");
        if (!data.moveToFirst()) {return; }

        Long date = data.getLong(COL_WEATHER_DATE);

        String dayOfWeek = Utility.getDayOfWeek(date, getContext());
        String formattedDate = Utility.formatDateForDetailActivity(date);

//            String dateString = Utility.formatDate(date, getContext());
        String weatherDescription = data.getString(COL_WEATHER_DESC);
        boolean isMetric = Utility.isMetric(getActivity());

        String high = Utility.formatTemperature(getContext(), data.getDouble
                (COL_WEATHER_MAX_TEMP), isMetric);
        String low = Utility.formatTemperature(getContext(), data.getDouble
                (COL_WEATHER_MIN_TEMP), isMetric);

        String humidity = getString(R.string.humidity_format, data.getString
                (COL_WEATHER_HUMIDITY));
//            String humidity = getString(R.string.humidity_format, data.getString
//                    (COL_WEATHER_HUMIDITY));
        Float wind = data.getFloat(COL_WEATHER_WIND_SPEED);
        String windDirection = Utility.formatWindDirection(data.getFloat
                (COL_WEATHER_WIND_DEGREES));
        String formattedWind = getString(R.string.wind_format, wind, windDirection);
        String pressure = getString(R.string.pressure_format, data.getFloat
                (COL_WEATHER_PRESSURE));


//            mForecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

//            TextView detailTextView = (TextView)getView().findViewById(R.id.detail_text);
//            detailTextView.setText(mForecast);

        TextView dayOfWeekTV = (TextView)getView().findViewById(R.id.detail_day_of_week_textview);
        TextView dateTV = (TextView)getView().findViewById(R.id.detail_date_textview);
        TextView highTV = (TextView)getView().findViewById(R.id.detail_high_textview);
        TextView lowTV = (TextView)getView().findViewById(R.id.detail_low_textview);
        TextView humidityTV = (TextView)getView().findViewById(R.id.detail_humidity);
        TextView windTV = (TextView)getView().findViewById(R.id.detail_wind);
        TextView pressureTV = (TextView)getView().findViewById(R.id.detail_pressure);
        ImageView icon = (ImageView)getView().findViewById(R.id.detail_icon);
        TextView detailTV = (TextView)getView().findViewById(R.id.detail_forecast_textview);

        dayOfWeekTV.setText(dayOfWeek);
        dateTV.setText(formattedDate);
        highTV.setText(high);
        lowTV.setText(low);
        humidityTV.setText(humidity);
        windTV.setText(formattedWind);
        pressureTV.setText(pressure);
        detailTV.setText(weatherDescription);
        icon.setImageResource(Utility.getResourceDetailConditionIcon(weatherDescription));

        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }

    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    public static DetailFragment newInstance(Uri dateUri) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(URI_ARG, dateUri);
        fragment.setArguments(args);
        return fragment;
    }

    public void onSettingsChanged(String newLocation) {
        if (mUri != null) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(mUri);
            Uri updateUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate
                    (newLocation, date);
            mUri = updateUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }
}
