package ru.javabreeze.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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

/**
 * Created by Алексей on 26.05.2016.
 */
public class ForecastFragment extends Fragment {

    private final String LOG_TAG = "Sunshine App";

    //private final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7";
    //private final String API_KEY = "&APPID=" + BuildConfig.OPEN_WEATHER_MAP_API_KEY;

    private ArrayAdapter<String> adapter;


    private Uri.Builder getUriBuilder() {
        return new Uri.Builder().scheme("http")
                .authority("api.openweathermap.org")
                .appendPath("data")
                .appendPath("2.5")
                .appendPath("forecast")
                .appendPath("daily")
                .appendQueryParameter("mode", "json")
                .appendQueryParameter("cnt", "7")
                .appendQueryParameter("units", "metric")
                .appendQueryParameter("APPID", BuildConfig.OPEN_WEATHER_MAP_API_KEY);
    }

    String[] stringList = {
            "Today - Sunny - 88/63",
            "Tomorrow - Foggy - 70/46",
            "Weds - Cloudy - 72/63",
            "Thurs - Rainy - 64/51",
            "Fri - Foggy - 70/46",
            "Sat - Sunny - 77/68"
    };

    String[] forecastResults;
    String[] newForecastResults;

    ArrayList<String> weekForecast = new ArrayList<>(Arrays.asList(stringList));

    private View view;

    public ForecastFragment() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            sendWeatherRequest();
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_show_location_on_map) {
            Uri location = Uri.parse("geo:0,0?q=" + getLocationFromPreferences());
            Intent showOnMap = new Intent(Intent.ACTION_VIEW, location);
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
        return super.onOptionsItemSelected(item);
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

        /*adapter = new ArrayAdapter<>(getActivity(),
                R.layout.list_item_forecast, R.id.list_item_forecast_textview, weekForecast);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);

        listView.setAdapter(adapter);*/

        sendWeatherRequest();

        return view;
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

    private void sendWeatherRequest(){
        new FetchWeatherTask().execute(getLocationFromPreferences());
    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Ogence the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class FetchWeatherTask extends AsyncTask<String, Void, String> {

        String forecastJsonStr = null;

        @Override
        protected String doInBackground(String... urls) {
            String url = getUriBuilder().appendQueryParameter("q", urls[0]).build().toString();

            //Log.v("Url to download: ", url);
            downloadUrl(url);
            return forecastJsonStr;
        }

        private void downloadUrl(String stringUrl) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                URL url = new URL(stringUrl);

                //Log.v(Constants.LOG_TAG, "Weather Url: " +stringUrl);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return;
                }
                forecastJsonStr = buffer.toString();
                //Log.v(LOG_TAG, forecastJsonStr);

            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                newForecastResults = new String[7];

                for (int i = 0; i < newForecastResults.length; i++) {
                    String weather = null;
                    double min = -200, max = -200;
                    try {
                        //time = WeatherDataParser.getTime(result, i);
                        weather = WeatherDataParser.getWeatherConditionForDay(result, i);
                        min = WeatherDataParser.getMinTemperatureForDay(result, i);
                        max = WeatherDataParser.getMaxTemperatureForDay(result, i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //create a Gregorian Calendar, which is in current date
                    GregorianCalendar gc = new GregorianCalendar();
                    //add i dates to current date of calendar
                    gc.add(GregorianCalendar.DATE, i);
                    //get that date, format it, and "save" it on variable day
                    Date time = gc.getTime();
                    SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE, MMM d");
                    String day = shortenedDateFormat.format(time);

                    if (weather == null) weather = "n/a";

                    switch (getTemperatureUnitsFromPreferences()) {
                        case "imperial":
                            newForecastResults[i] = day + " - " + weather + " - " +
                                    ((max > -200)?Math.round(max*1.8+32):"-") + "/" +
                                    ((min > -200)?Math.round(min*1.8+32):"-"); break;
                        case "metric":
                            newForecastResults[i] = day + " - " + weather + " - " +
                                    ((max > -200)?Math.round(max):"-") + "/" +
                                    ((min > -200)?Math.round(min):"-"); break;
                        default:
                            newForecastResults[i] = day + " - " + weather + " - " + "-/-"; break;
                    }

                }
            }
            updateWeather();
        }


    }

    private void updateWeather() {
        adapter = new ArrayAdapter<>(getActivity(),
                R.layout.list_item_forecast, R.id.list_item_forecast_textview, newForecastResults);

        ListView listView = (ListView) view.findViewById(R.id.listview_forecast);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v(Constants.LOG_TAG, "Clicked item: " + position + " - "
                        + newForecastResults[position]);
                Toast.makeText(getContext(), newForecastResults[position], Toast.LENGTH_SHORT)
                        .show();
                Intent intent = new Intent(getContext(), DetailActivity.class)
                        .putExtra(Constants.FORECAST, newForecastResults[position]);
                startActivity(intent);
            }
        });
    }
}
