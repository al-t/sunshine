package ru.javabreeze.android.sunshine.app;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Calendar;

import ru.javabreeze.android.sunshine.app.data.WeatherContract;
import ru.javabreeze.android.sunshine.app.sync.SunshineSyncAdapter;


public class MainActivity extends AppCompatActivity implements Callback {

    private String mLocation;
    private boolean isMetric;
    private final String FORECASTFRAGMENT_TAG = "ForecastFragment tag";
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocation = Utility.getPreferredLocation(this);
        isMetric = Utility.isMetric(this);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.weather_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate
                        (mLocation, Calendar.getInstance().getTimeInMillis());
                DetailFragment detailFragment = DetailFragment.newInstance(uri);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, detailFragment, DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        ForecastFragment forecastFragment = (ForecastFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast);
        forecastFragment.setUseTodayLayout(!mTwoPane);
        setActionBarLogo();

        SunshineSyncAdapter.initializeSyncAdapter(this);
    }

    private void setActionBarLogo() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_logo);
        getSupportActionBar().setTitle(""); // we do not need the title here anymore
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        String newLocation = Utility.getPreferredLocation(this);
        Boolean newIsMetric = Utility.isMetric(this);
        if (Constants.DEBUG) {
            Log.v(LOG_TAG, "mTwoPane!: " + mTwoPane);
        }
        if ((newLocation != null && !newLocation.equals(mLocation)) ||
                !newIsMetric.equals(isMetric)) {
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById
                    (R.id.fragment_forecast);
            if (null != ff) {
                ff.onSettingsChanged();
            }
            DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag
                    (DETAILFRAGMENT_TAG);
            if (df != null) {
                df.onSettingsChanged(newLocation);
            }
            mLocation = newLocation;
            isMetric = newIsMetric;
        }
    }

    @Override
    public void onItemSelected(Uri dateUri) {
        if (dateUri != null) {
            if (!mTwoPane) {
                Intent intent = new Intent(this, DetailActivity.class);
                intent.setData(dateUri);
                startActivity(intent);
            } else {
                DetailFragment detailFragment = DetailFragment.newInstance(dateUri);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, detailFragment, DETAILFRAGMENT_TAG)
                        .commit();
            }
        }



    }



}
