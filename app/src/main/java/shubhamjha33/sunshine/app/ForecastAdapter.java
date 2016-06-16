package shubhamjha33.sunshine.app;

/**
 * Created by Shubham on 06/10/2016.
 */
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import shubhamjha33.sunshine.app.data.WeatherContract;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
        return highLowStr;
    }

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    private static boolean mUseTodayLayout;

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    public void setmUseTodayLayout(boolean useTodayLayout){
        mUseTodayLayout=useTodayLayout;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
    public String convertCursorRowToUXFormat(Cursor cursor) {
        // get row indices for our cursor
        int idx_max_temp = WeatherContract.COL_WEATHER_MAX_TEMP;
        int idx_min_temp = WeatherContract.COL_WEATHER_MIN_TEMP;
        int idx_date = WeatherContract.COL_WEATHER_DATE;
        int idx_short_desc = WeatherContract.COL_WEATHER_DESC;

        String highAndLow = formatHighLows(
                cursor.getDouble(idx_max_temp),
                cursor.getDouble(idx_min_temp));

        return Utility.formatDate(cursor.getLong(idx_date)) +
                " - " + cursor.getString(idx_short_desc) +
                " - " + highAndLow;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        if(viewType==VIEW_TYPE_TODAY){
            layoutId=R.layout.list_item_forecast_today;
        }
        else if(viewType==VIEW_TYPE_FUTURE_DAY){
            layoutId=R.layout.list_item_forecast;
        }
        View view=LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder=new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        // Read weather icon ID from cursor
        ViewHolder viewHolder= (ViewHolder) view.getTag();
        int weatherId = cursor.getInt(WeatherContract.COL_WEATHER_CONDITION_ID);
        // Use placeholder image for now
        int viewType=getItemViewType(cursor.getPosition());
        if(viewType==VIEW_TYPE_FUTURE_DAY)
            viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));
        else if(viewType==VIEW_TYPE_TODAY)
            viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
        viewHolder.dateView.setText(Utility.getFriendlyDayString(mContext,cursor.getLong(WeatherContract.COL_WEATHER_DATE)));
        viewHolder.descriptionView.setText(cursor.getString(WeatherContract.COL_WEATHER_DESC));
        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        double high = cursor.getDouble(WeatherContract.COL_WEATHER_MAX_TEMP);
        viewHolder.highTempView.setText(Utility.formatTemperature(mContext,high, isMetric));
        //Read low temperature from cursor
        double low = cursor.getDouble(WeatherContract.COL_WEATHER_MIN_TEMP);
        viewHolder.lowTempView.setText(Utility.formatTemperature(mContext,low, isMetric));
    }

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }
}