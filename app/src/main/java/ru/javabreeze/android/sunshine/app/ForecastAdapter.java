package ru.javabreeze.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ru.javabreeze.android.sunshine.app.data.WeatherContract;

import static ru.javabreeze.android.sunshine.app.Constants.LOG_TAG;

/**
 * Created by Алексей on 18.12.2016.
 */

class ForecastAdapter extends CursorAdapter {

    private final static String LOG_TAG = ForecastAdapter.class.getName();

    private boolean mUseTodayLayout;


    ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    private static final String TAG = "ForecastAdapter";

    private static final int TODAY_VIEW_TYPE = 0;
    private static final int FUTURE_DAY_VIEW_TYPE = 1;

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position) {
        if (Constants.DEBUG) {
            //Log.v(LOG_TAG, "mUseTodayLayout: " + mUseTodayLayout);
        }
        return (position == 0 && mUseTodayLayout) ? TODAY_VIEW_TYPE : FUTURE_DAY_VIEW_TYPE;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId;
        switch (viewType) {
            case TODAY_VIEW_TYPE: layoutId = R.layout.list_item_forecast_today; break;
            case FUTURE_DAY_VIEW_TYPE: layoutId = R.layout.list_item_forecast; break;
            default: layoutId = -1;
        }
        View view = null;
        if (layoutId != -1) {
            view = LayoutInflater.from(context).inflate(layoutId, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder)view.getTag();

        String date = Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE), context);
        String forecast = cursor.getString(ForecastFragment.COL_WEATHER_DESC);

        boolean isMetric = Utility.isMetric(mContext);
        String hiTemp = Utility.formatTemperature(context, cursor.getDouble(ForecastFragment
                .COL_WEATHER_MAX_TEMP), isMetric);
        String lowTemp = Utility.formatTemperature(context, cursor.getDouble(ForecastFragment
                .COL_WEATHER_MIN_TEMP), isMetric);

        int viewType = getItemViewType(cursor.getPosition());
        int iconRes;
        if (viewType == TODAY_VIEW_TYPE) {
            iconRes = Utility.getResourceDetailConditionIcon(forecast);
        } else {
            iconRes = Utility.getResourceConditionIcon(forecast);
        }

        viewHolder.iconView.setImageResource(iconRes);
        viewHolder.dateView.setText(date);
        viewHolder.descriptionView.setText(forecast);
        viewHolder.highTempView.setText(hiTemp);
        viewHolder.lowTempView.setText(lowTemp);
    }

    /**
     * Cache of the children views for a forecast list item
     */
    private static class ViewHolder {
        final ImageView iconView;
        final TextView dateView;
        final TextView descriptionView;
        final TextView highTempView;
        final TextView lowTempView;

        ViewHolder(View view) {
            iconView = (ImageView)view.findViewById(R.id.list_item_icon);
            dateView = (TextView)view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView)view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView)view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView)view.findViewById(R.id.list_item_low_textview);
        }
    }
}
